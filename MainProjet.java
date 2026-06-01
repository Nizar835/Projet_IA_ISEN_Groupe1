import java.util.List;
import java.util.ArrayList;

public class MainProjet 
{
	public static void main(String[] args) 
	{
		System.out.println("--- DÉMARRAGE DE LA CHAÎNE DE TRAITEMENT ---");
		
		// --- 1. LECTURE, LABELLISATION ET MÉLANGE (TRAIN) ---
		System.out.println("Lecture et labellisation du dataset d'entraînement...");
		// Utilisation de la méthode de ton collègue pour tout charger !
		List<Image> imagesTrain = Image.chargeDataset("dataset_animaux/train/", true);
		
		if (imagesTrain == null || imagesTrain.isEmpty()) {
			System.out.println("Erreur : Aucune image d'entraînement trouvée.");
			return;
		}

		System.out.println("Mélange des données d'entraînement...");
		// Utilisation de la méthode de ton collègue pour le mélange aléatoire
		Image.melange(imagesTrain);
		
		// --- 2. EXTRACTION ET NORMALISATION SÉCURISÉE ---
		List<float[]> listeEntrees = new ArrayList<>();
		List<Float> listeResultats = new ArrayList<>();
		
		System.out.println("Normalisation et vérification des " + imagesTrain.size() + " images...");

		for (Image img : imagesTrain) 
		{
			// BOUCLIER ANTI-CRASH : On ignore l'image si elle n'a pas pu être lue
			if (img.donnees() == null) {
				continue; 
			}
			
			// Normalisation en une ligne grâce au code du collègue
			listeEntrees.add(img.donneesNormalisees());
			
			// Adaptation du label : le neurone attend 1 pour un chat, 0 pour le reste.
			// (Dans Image.java, LabelChat vaut 0).
			float labelPourNeurone = (img.label() == 0) ? 1.0f : 0.0f;
			listeResultats.add(labelPourNeurone);
		}
		
		// --- 3. ENTRAÎNEMENT DU NEURONE ---
		System.out.println("Conversion des données et instanciation du réseau...");
		
		float[][] entreesArray = new float[listeEntrees.size()][];
		for (int i = 0; i < listeEntrees.size(); i++) {
			entreesArray[i] = listeEntrees.get(i);
		}
		
		float[] resultatsArray = new float[listeResultats.size()];
		for (int i = 0; i < listeResultats.size(); i++) {
			resultatsArray[i] = listeResultats.get(i);
		}
		
		int nbEntreesNeurone = entreesArray[0].length;
		iNeurone neurone = new NeuroneSigmoide(nbEntreesNeurone);
		final float MSElimite = 0.05f; 
		
		System.out.println("Début de l'apprentissage supervisé sur " + entreesArray.length + " images saines...");
		neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
		System.out.println(">>> Apprentissage terminé avec succès ! <<<");
		
		// --- 4. PHASE DE TEST (ÉVALUATION) ---
		System.out.println("\n--- DÉBUT DE LA PHASE DE TEST ---");
		System.out.println("Lecture et labellisation du dataset de test...");
		
		// On réutilise la super méthode du collègue pour le dossier de test
		List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
		
		if (imagesTest != null && !imagesTest.isEmpty()) {
			int bonnesReponses = 0;
			int totalTestSains = 0;
			
			System.out.println("Évaluation sur " + imagesTest.size() + " nouvelles images...");

			for (Image imgTest : imagesTest) 
			{
				// Protection également activée pendant la phase de test
				if (imgTest.donnees() == null) {
					continue;
				}
				
				totalTestSains++;
				
				// On fait faire une prédiction au neurone avec les données normalisées
				neurone.metAJour(imgTest.donneesNormalisees());
				
				// Le neurone sort une probabilité. Si > 0.5, il pense "Chat" (1), sinon "Autre" (0)
				int labelPredit = (neurone.sortie() > 0.5f) ? 1 : 0;
				
				// On compare avec le vrai label attendu
				int labelAttendu = (imgTest.label() == 0) ? 1 : 0;
				
				if (labelPredit == labelAttendu) {
					bonnesReponses++;
				}
			}
			
			if (totalTestSains > 0) {
				float pourcentageReussite = ((float) bonnesReponses / totalTestSains) * 100;
				System.out.printf(">>> Résultat du test : %d bonnes réponses sur %d (soit %.2f %% de précision) <<<\n", 
									bonnesReponses, totalTestSains, pourcentageReussite);
			}
		} else {
			System.out.println("Dossier de test introuvable ou vide.");
		}
	}
}
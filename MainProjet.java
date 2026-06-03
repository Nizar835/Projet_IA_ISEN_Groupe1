import java.util.List;
import java.util.ArrayList;

public class MainProjet 
{
	public static void main(String[] args) 
	{
		System.out.println("--- DÉMARRAGE DE LA CHAÎNE DE TRAITEMENT ---");
		
		// --- 1. LECTURE, LABELLISATION ET MÉLANGE (TRAIN) ---
		System.out.println("Lecture et labellisation du dataset d'entraînement...");
		List<Image> imagesTrain = Image.chargeDataset("dataset_animaux/train/", true);
		
		if (imagesTrain == null || imagesTrain.isEmpty()) {
			System.out.println("Erreur : Aucune image d'entraînement trouvée.");
			return;
		}

		System.out.println("Mélange des données d'entraînement...");
		Image.melange(imagesTrain);
		
		// --- 2. EXTRACTION ET NORMALISATION ---
		List<float[]> listeEntrees = new ArrayList<>();
		List<Float> listeResultats = new ArrayList<>();
		
		System.out.println("Normalisation des images (Niveau 2)...");

		for (Image img : imagesTrain) 
		{
			// BOUCLIER ANTI-CRASH : On ignore l'image si elle est corrompue
			if (img.donnees() == null) {
				continue; 
			}
			
			// Le neurone attend 1 pour un chat, 0 pour le reste.
			float labelPourNeurone = (img.label() == 0) ? 1.0f : 0.0f;

			// On ajoute l'image normale
			listeEntrees.add(img.donneesNormalisees());
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
		
		// Instanciation du neurone Sigmoïde (robuste au bruit)
		iNeurone neurone = new NeuroneSigmoide(nbEntreesNeurone);
		final float MSElimite = 0.05f; 
		
		System.out.println("Début de l'apprentissage supervisé sur " + entreesArray.length + " images originales...");
		neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
		System.out.println(">>> Apprentissage terminé avec succès ! <<<");

		// --- AJOUT DE LA SAUVEGARDE ---
		neurone.sauvegarde("cerveau_binaire.txt");
		
		// --- 4. PHASE DE TEST (ÉVALUATION GLOBALE) ---
		System.out.println("\n--- DÉMONSTRATION : ÉVALUATION SUR LE JEU DE TEST ---");
		System.out.println("Chargement du dataset de test...");
		
		List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
		
		if (imagesTest != null && !imagesTest.isEmpty()) {
			
			int bonnesReponses = 0;
			int totalTest = 0;

			System.out.println("Passage de l'examen final sur toutes les images inédites...");

			for (Image imgTest : imagesTest) 
			{
				if (imgTest.donnees() == null) continue; // Sécurité
				
				// On donne l'image au réseau
				neurone.metAJour(imgTest.donneesNormalisees());
				float proba = neurone.sortie();
				
				// On vérifie le résultat
				float vraiLabel = (imgTest.label() == 0) ? 1.0f : 0.0f;
				float reponseArrondie = (proba >= 0.5f) ? 1.0f : 0.0f;
				
				if (reponseArrondie == vraiLabel) {
					bonnesReponses++;
				}
				totalTest++;
			}
			
			// Calcul et affichage du score
			float pourcentage = ((float) bonnesReponses / totalTest) * 100.0f;
			System.out.printf("\n>>> SCORE FINAL DE L'IA : %.2f %% de réussite (%d/%d) <<<\n", pourcentage, bonnesReponses, totalTest);
			
		} else {
			System.out.println("Dossier de test introuvable ou vide.");
		}
	}
}
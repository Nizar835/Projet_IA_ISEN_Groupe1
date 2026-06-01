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
		
		// --- 2. EXTRACTION ET NORMALISATION SÉCURISÉE ---
		List<float[]> listeEntrees = new ArrayList<>();
		List<Float> listeResultats = new ArrayList<>();
		
		System.out.println("Normalisation et vérification des " + imagesTrain.size() + " images...");

		for (Image img : imagesTrain) 
		{
			// BOUCLIER ANTI-CRASH : On ignore l'image si elle est corrompue
			if (img.donnees() == null) {
				continue; 
			}
			
			listeEntrees.add(img.donneesNormalisees());
			
			// Adaptation : le neurone attend 1 pour un chat, 0 pour le reste.
			// (Dans la classe Image, le label Chat vaut 0).
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
		
		// Instanciation de votre propre classe de Niveau 1
		iNeurone neurone = new NeuroneSigmoide(nbEntreesNeurone);
		final float MSElimite = 0.05f; 
		
		System.out.println("Début de l'apprentissage supervisé sur " + entreesArray.length + " images saines...");
		neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
		System.out.println(">>> Apprentissage terminé avec succès ! <<<");
		
		// --- 4. DÉMONSTRATION VISUELLE (LE CRASH TEST) ---
		System.out.println("\n--- DÉMONSTRATION : QUE VOIT L'IA ? ---");
		System.out.println("Chargement du dataset de test...");
		
		List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
		
		if (imagesTest != null && !imagesTest.isEmpty()) {
			// On mélange pour ne pas avoir que des chats ou que des chiens
			Image.melange(imagesTest);
			
			System.out.println("On pioche 5 images au hasard pour voir si l'IA s'est bien entraînée :");

			// On boucle uniquement sur les 5 premières images saines
			int testsRealises = 0;
			for (int i = 0; i < imagesTest.size() && testsRealises < 5; i++) 
			{
				Image imgTest = imagesTest.get(i);
				
				if (imgTest.donnees() == null) {
					continue; // On esquive les corrompues
				}
				
				testsRealises++;
				
				// On donne l'image au "cerveau"
				neurone.metAJour(imgTest.donneesNormalisees());
				float proba = neurone.sortie();
				
				// On récupère le vrai type d'animal pour vérifier (0 = Chat, 1 = Chien, 2 = Wild)
				String vraiAnimal = (imgTest.label() == 0) ? "CHAT" : (imgTest.label() == 1 ? "CHIEN" : "WILD (Sauvage)");
				
				System.out.printf("\n--- Test n°%d --- \n", testsRealises);
				System.out.printf("Vraie image : %s\n", vraiAnimal);
				System.out.printf("Certitude de l'IA d'être un CHAT : %.1f %%\n", (proba * 100));
				
				if (proba > 0.5f) {
					System.out.println("=> L'IA a tranché : C'EST UN CHAT ! 🐱");
				} else {
					System.out.println("=> L'IA a tranché : C'EST UN CHIEN (ou autre) ! 🐶");
				}
			}
		} else {
			System.out.println("Dossier de test introuvable ou vide.");
		}
	}
}
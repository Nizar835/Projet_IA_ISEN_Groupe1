import java.util.List;
import java.util.ArrayList;

public class TestMiroir 
{
	public static void main(String[] args) 
	{
		System.out.println("=== EXTENSION NIVEAU 3 : AUGMENTATION DE DONNÉES (MIROIR) ===");
		
		// 1. CHARGEMENT DU DATASET D'ORIGINE
		System.out.println("Lecture du dataset d'entraînement...");
		List<Image> imagesOriginales = Image.chargeDataset("dataset_animaux/train/", true);
		
		if (imagesOriginales == null || imagesOriginales.isEmpty()) {
			System.out.println("Erreur : Aucune image trouvée.");
			return;
		}

		List<float[]> listeEntrees = new ArrayList<>();
		List<Float> listeResultats = new ArrayList<>();
		
		System.out.println("Application du filtre miroir pour doubler les données...");

		// 2. BOUCLE DE DUPLICATION (On utilise ton FiltreImage !)
		for (Image img : imagesOriginales) 
		{
			if (img.donnees() == null) {
				continue; // Bouclier anti-crash toujours présent
			}
			
			// Pour le neurone, Chat = 1, Reste = 0
			float labelPourNeurone = (img.label() == 0) ? 1.0f : 0.0f;
			
			// A. On ajoute l'image originale normalisée
			float[] normales = img.donneesNormalisees();
			listeEntrees.add(normales);
			listeResultats.add(labelPourNeurone);
			
			// B. On crée la version miroir grâce à ton code et on l'ajoute !
			float[] pixelsMiroir = FiltreImage.miroirGris(normales, img.largeur(), img.hauteur());
			listeEntrees.add(pixelsMiroir);
			listeResultats.add(labelPourNeurone);
		}
		
		System.out.println("Taille du dataset après augmentation : " + listeEntrees.size() + " images.");

		// 3. MÉLANGE OBLIGATOIRE DES 20 000 IMAGES
		// Pour mélanger nos listes float[] et Float synchronisées, on utilise un tableau d'index
		System.out.println("Mélange de toutes les données...");
		List<Integer> indexList = new ArrayList<>();
		for (int i = 0; i < listeEntrees.size(); i++) {
			indexList.add(i);
		}
		java.util.Collections.shuffle(indexList);
		
		float[][] entreesArray = new float[listeEntrees.size()][];
		float[] resultatsArray = new float[listeResultats.size()];
		
		for (int i = 0; i < indexList.size(); i++) {
			int idxOrigine = indexList.get(i);
			entreesArray[i] = listeEntrees.get(idxOrigine);
			resultatsArray[i] = listeResultats.get(idxOrigine);
		}
		
		// 4. ENTRAÎNEMENT DU NEURONE
		int nbEntreesNeurone = entreesArray[0].length;
		iNeurone neurone = new NeuroneSigmoide(nbEntreesNeurone);
		final float MSElimite = 0.08f; 
		
		System.out.println("Début de l'apprentissage sur le dataset augmenté...");
		neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
		System.out.println(">>> Apprentissage terminé avec succès ! <<<");

		// --- AJOUT DE LA SAUVEGARDE ---
		neurone.sauvegarde("cerveau_miroir.txt");
		
		// 5. PHASE DE TEST RAPIDE (DÉMO VISUELLE SUR 5 IMAGES CASSE-COU)
		System.out.println("\n--- DÉMONSTRATION SUR LE JEU DE TEST ---");
		List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
		if (imagesTest != null && !imagesTest.isEmpty()) {
			Image.melange(imagesTest);
			int testsRealises = 0;
			for (int i = 0; i < imagesTest.size() && testsRealises < 5; i++) {
				Image imgTest = imagesTest.get(i);
				if (imgTest.donnees() == null) continue;
				
				testsRealises++;
				neurone.metAJour(imgTest.donneesNormalisees());
				float proba = neurone.sortie();
				String vraiAnimal = (imgTest.label() == 0) ? "CHAT" : (imgTest.label() == 1 ? "CHIEN" : "WILD");
				
				System.out.printf("\nTest n°%d - Vrai : %s | Proba Chat : %.1f %%\n", testsRealises, vraiAnimal, (proba * 100));
				System.out.println(proba > 0.5f ? "=> IA : CHAT ! 🐱" : "=> IA : CHIEN/AUTRE ! 🐶");
			}
		}
	}
}
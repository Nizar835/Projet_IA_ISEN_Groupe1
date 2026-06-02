import java.util.List;

public class DemoSauvegarde 
{
	public static void main(String[] args) 
	{
		System.out.println("=== TEST DU CHARGEMENT DE L'IA (SANS ENTRAÎNEMENT) ===");

		// 1. On charge le dataset de test pour piocher une image
		System.out.println("Lecture du dossier test...");
		List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
		
		if (imagesTest == null || imagesTest.isEmpty()) {
			System.out.println("Erreur : Dossier test introuvable.");
			return;
		}

		// On mélange et on prend la première image saine qui vient
		Image.melange(imagesTest);
		Image imageChoisie = imagesTest.get(0);
		while (imageChoisie.donnees() == null) { // Sécurité anti-crash
			Image.melange(imagesTest);
			imageChoisie = imagesTest.get(0);
		}

		float[] pixelsNormalises = imageChoisie.donneesNormalisees();

		// 2. CRÉATION D'UN NEURONE "VIDE" ET AMNÉSIQUE
		iNeurone monIA = new NeuroneSigmoide(pixelsNormalises.length);

		// 3. LE MIRACLE : On charge les connaissances au lieu d'apprendre !
		System.out.println("Chargement du cerveau depuis le fichier...");
		monIA.chargement("cerveau_binaire.txt"); // /!\ Assure-toi que ce fichier existe bien dans ton dossier !

		// 4. L'IA FAIT SA PRÉDICTION INSTANTANÉMENT
		monIA.metAJour(pixelsNormalises);
		float probabilite = monIA.sortie();

		// 5. AFFICHAGE DES RÉSULTATS
		String vraiLabel = (imageChoisie.label() == 0) ? "CHAT" : "CHIEN";
		System.out.println("\n------------------------------------------------");
		System.out.println("Vraie réponse attendue   : " + vraiLabel);
		System.out.printf("L'IA pense que c'est un Chat à : %.2f %%\n", (probabilite * 100));
		
		if (probabilite >= 0.5f) {
			System.out.println(">>> DÉCISION DE L'IA : C'est un CHAT ! 🐱 <<<");
		} else {
			System.out.println(">>> DÉCISION DE L'IA : C'est un CHIEN ! 🐶 <<<");
		}
		System.out.println("------------------------------------------------");
	}
}
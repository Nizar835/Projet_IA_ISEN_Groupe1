import java.util.List;
import java.util.Collections;

public class DemoSauvegarde 
{
	public static void main(String[] args) 
	{
		System.out.println("=== TEST DE L'IA (SANS ENTRAÎNEMENT) ===");

		// 1. On récupère la liste des noms de fichiers
		String dossierTest = "dataset_animaux/test/";
		List<String> fichiersTest = Image.listeFichiers(dossierTest);
		
		if (fichiersTest == null || fichiersTest.isEmpty()) {
			System.out.println("Erreur : Dossier test introuvable.");
			return;
		}

		// On mélange et on prend un fichier au hasard
		Collections.shuffle(fichiersTest);
		String cheminChoisi = fichiersTest.get(0);
		
		// On déduit le vrai label à partir du nom du fichier
		int vraiLabelAttendu = cheminChoisi.contains("cat") ? 0 : (cheminChoisi.contains("dog") ? 1 : 2);
		String vraiNomAnimal = (vraiLabelAttendu == 0) ? "CHAT" : (vraiLabelAttendu == 1 ? "CHIEN" : "WILD");

		// On crée notre objet Image
		Image imageChoisie = new Image(cheminChoisi, vraiLabelAttendu, true);
		
		if (imageChoisie.donnees() == null) {
			System.out.println("Image corrompue piochée, relancez le programme !");
			return;
		}

		float[] pixelsNormalises = imageChoisie.donneesNormalisees();

		// 2. CRÉATION D'UN NEURONE "VIDE" 
		iNeurone monIA = new NeuroneSigmoide(pixelsNormalises.length);

		// 3. CHARGEMENT DES POIDS SYNAPTIQUES
		System.out.println("Chargement du cerveau depuis 'cerveau_binaire.txt'...");
		monIA.chargement("cerveau_binaire.txt");

		// 4. L'IA FAIT SA PRÉDICTION
		monIA.metAJour(pixelsNormalises);
		float probabilite = monIA.sortie();

		String decisionIA = (probabilite >= 0.5f) ? "CHAT 🐱" : "CHIEN 🐶";

		// 5. AFFICHAGE DES RÉSULTATS DANS LE TERMINAL
		System.out.println("\n================================================");
		System.out.println("Vraie réponse attendue   : " + vraiNomAnimal);
		System.out.printf("Confiance pour un Chat   : %.2f %%\n", (probabilite * 100));
		System.out.println(">>> DÉCISION DE L'IA     : " + decisionIA + " <<<");
		System.out.println("================================================");
		
		// LE LIEN POUR VOIR L'IMAGE (Ctrl + Clic dans VS Code)
		System.out.println("\nPour vérifier, ouvrez ce fichier (Ctrl + Clic) :");
		System.out.println(cheminChoisi);
		System.out.println("================================================\n");
	}
}
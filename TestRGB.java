import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class TestRGB 
{
	public static void main(String[] args) 
	{
		System.out.println("=== EXTENSION NIVEAU 3 : APPRENTISSAGE EN COULEURS (RGB) ===");
		
		// --- 1. LECTURE ET LABELLISATION (TRAIN) ---
		String dossierTrain = "dataset_animaux/train/";
		List<String> cheminsFichiers = Image.listeFichiers(dossierTrain);
		
		if (cheminsFichiers == null || cheminsFichiers.isEmpty()) {
			System.out.println("Erreur : Aucune image d'entraînement trouvée.");
			return;
		}

		List<float[]> listeEntrees = new ArrayList<>();
		List<Float> listeResultats = new ArrayList<>();
		
		System.out.println("Lecture et normalisation RGB de " + cheminsFichiers.size() + " images...");

		for (String chemin : cheminsFichiers) 
		{
			int labelAttendu = chemin.contains("cat") ? 1 : 0;
			
			// false = mode RGB (Couleurs)
			Image img = new Image(chemin, labelAttendu, false); 
			
			// BOUCLIER ANTI-CRASH : Sécurité pour le fichier corrompu
			if (img.donnees() == null) {
				System.out.println("⚠️ Image RGB ignorée car corrompue : " + chemin);
				continue;
			}
			
			// --- 2. NORMALISATION (Appel propre de la méthode) ---
			float[] pixelsNormalises = img.donneesNormalisees();
			listeEntrees.add(pixelsNormalises);
			listeResultats.add((float) labelAttendu);
		}
		
		// --- 3. MÉLANGE DES DONNÉES ---
		System.out.println("Mélange aléatoire des données...");
		List<Integer> indexList = new ArrayList<>();
		for (int i = 0; i < listeEntrees.size(); i++) {
			indexList.add(i);
		}
		Collections.shuffle(indexList);
		
		List<float[]> entreesMelangees = new ArrayList<>();
		List<Float> resultatsMelanges = new ArrayList<>();
		
		for (int index : indexList) {
			entreesMelangees.add(listeEntrees.get(index));
			resultatsMelanges.add(listeResultats.get(index));
		}
		listeEntrees = entreesMelangees;
		listeResultats = resultatsMelanges;
		
		// --- 4. ENTRAÎNEMENT ---
		float[][] entreesArray = new float[listeEntrees.size()][];
		for (int i = 0; i < listeEntrees.size(); i++) {
			entreesArray[i] = listeEntrees.get(i);
		}
		
		float[] resultatsArray = new float[listeResultats.size()];
		for (int i = 0; i < listeResultats.size(); i++) {
			resultatsArray[i] = listeResultats.get(i);
		}
		
		int nbEntreesNeurone = entreesArray[0].length;
		System.out.println("Le réseau aura " + nbEntreesNeurone + " synapses (3 par pixel).");
		
		iNeurone neurone = new NeuroneSigmoide(nbEntreesNeurone);
		final float MSElimite = 0.05f; 
		
		System.out.println("Début de l'apprentissage (Attention, cela sera plus long en RGB)...");
		neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
		
		// --- 5. PHASE DE TEST DIRECTE ---
		System.out.println("\n--- ÉVALUATION DU MODÈLE RGB SUR LE JEU DE TEST ---");
		String dossierTest = "dataset_animaux/test/";
		List<String> fichiersTest = Image.listeFichiers(dossierTest);
		
		int bonnesReponses = 0;
		int totalTestSains = 0;

		for (String chemin : fichiersTest) 
		{
			int labelAttendu = chemin.contains("cat") ? 1 : 0;
			
			Image imgTest = new Image(chemin, labelAttendu, false);
			
			// Protection pendant la phase de test
			if (imgTest.donnees() == null) {
				continue;
			}
			
			totalTestSains++;
			
			// --- NORMALISATION TEST (Appel propre de la méthode) ---
			float[] pixelsNormalisesTest = imgTest.donneesNormalisees();
			
			neurone.metAJour(pixelsNormalisesTest);
			int labelPredit = (neurone.sortie() > 0.5f) ? 1 : 0;
			
			if (labelPredit == labelAttendu) {
				bonnesReponses++;
			}
		}
		
		if (totalTestSains > 0) {
			float pourcentageReussite = ((float) bonnesReponses / totalTestSains) * 100;
			System.out.printf(">>> Précision du modèle RGB : %.2f %% (%d/%d) <<<\n", 
								pourcentageReussite, bonnesReponses, totalTestSains);
		}
	}
}
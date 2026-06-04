import java.util.List;
import java.util.ArrayList;

public class TestParametres 
{
	public static void main(String[] args) 
	{
		System.out.println("=== TEST AVEC DIFFÉRENTS PARAMÈTRES ===");
		
		List<Image> imagesTrain = Image.chargeDataset("dataset_animaux/train/", true);
		if (imagesTrain == null || imagesTrain.isEmpty()) {
			System.out.println("Erreur : Aucune image trouvée.");
			return;
		}
		Image.melange(imagesTrain);
		
		List<float[]> listeEntrees = new ArrayList<>();
		List<Float> listeResultats = new ArrayList<>();

		for (Image img : imagesTrain) {
			if (img.donnees() == null) continue;
			float labelPourNeurone = (img.label() == 0) ? 1.0f : 0.0f;
			listeEntrees.add(img.donneesNormalisees());
			listeResultats.add(labelPourNeurone);
		}
		
		float[][] entreesArray = new float[listeEntrees.size()][];
		for (int i = 0; i < listeEntrees.size(); i++)
			entreesArray[i] = listeEntrees.get(i);
		
		float[] resultatsArray = new float[listeResultats.size()];
		for (int i = 0; i < listeResultats.size(); i++)
			resultatsArray[i] = listeResultats.get(i);
		
		int nbEntreesNeurone = entreesArray[0].length;

		// --- TEST 1 : eta=0.001, MSElimite=0.15 ---
		System.out.println("\nParamètres : eta=0.001, MSElimite=0.15");
		Neurone.fixeCoefApprentissage(0.001f);
		iNeurone neurone = new NeuroneSigmoide(nbEntreesNeurone);
		neurone.apprentissage(entreesArray, resultatsArray, 0.15f);
		
		List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
		int bonnesReponses = 0;
		int totalTest = 0;
		for (Image imgTest : imagesTest) {
			if (imgTest.donnees() == null) continue;
			neurone.metAJour(imgTest.donneesNormalisees());
			float vraiLabel = (imgTest.label() == 0) ? 1.0f : 0.0f;
			float reponseArrondie = (neurone.sortie() >= 0.5f) ? 1.0f : 0.0f;
			if (reponseArrondie == vraiLabel) bonnesReponses++;
			totalTest++;
		}
		float score1 = ((float) bonnesReponses / totalTest) * 100.0f;
		System.out.printf(">>> SCORE (eta=0.001, MSE=0.15) : %.2f %%\n", score1);

		// --- TEST 2 : eta=0.00001, MSElimite=0.20 ---
		System.out.println("\nParamètres : eta=0.00001, MSElimite=0.20");
		Neurone.fixeCoefApprentissage(0.00001f);
		iNeurone neurone2 = new NeuroneSigmoide(nbEntreesNeurone);
		neurone2.apprentissage(entreesArray, resultatsArray, 0.20f);
		
		bonnesReponses = 0;
		totalTest = 0;
		for (Image imgTest : imagesTest) {
			if (imgTest.donnees() == null) continue;
			neurone2.metAJour(imgTest.donneesNormalisees());
			float vraiLabel = (imgTest.label() == 0) ? 1.0f : 0.0f;
			float reponseArrondie = (neurone2.sortie() >= 0.5f) ? 1.0f : 0.0f;
			if (reponseArrondie == vraiLabel) bonnesReponses++;
			totalTest++;
		}
		float score2 = ((float) bonnesReponses / totalTest) * 100.0f;
		System.out.printf(">>> SCORE (eta=0.00001, MSE=0.20) : %.2f %%\n", score2);

		// Comparaison finale
		System.out.println("\n=== COMPARAISON FINALE ===");
		System.out.printf("eta=0.001,   MSE=0.15 : %.2f %%\n", score1);
		System.out.printf("eta=0.00001, MSE=0.20 : %.2f %%\n", score2);
	}
}
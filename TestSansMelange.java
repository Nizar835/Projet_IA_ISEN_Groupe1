import java.util.List;
import java.util.ArrayList;

public class TestSansMelange 
{
	public static void main(String[] args) 
	{
		System.out.println("--- TEST SANS MÉLANGE DES DONNÉES ---");
		
		List<Image> imagesTrain = Image.chargeDataset("dataset_animaux/train/", true);
		
		if (imagesTrain == null || imagesTrain.isEmpty()) {
			System.out.println("Erreur : Aucune image d'entraînement trouvée.");
			return;
		}

		// PAS DE MÉLANGE ICI — c'est le test !
		
		List<float[]> listeEntrees = new ArrayList<>();
		List<Float> listeResultats = new ArrayList<>();

		for (Image img : imagesTrain) 
		{
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
		iNeurone neurone = new NeuroneSigmoide(nbEntreesNeurone);
		final float MSElimite = 0.05f; 
		
		System.out.println("Début de l'apprentissage SANS mélange...");
		neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
		System.out.println(">>> Apprentissage terminé ! <<<");
		
		System.out.println("\n--- ÉVALUATION SUR LE JEU DE TEST ---");
		List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
		
		if (imagesTest != null && !imagesTest.isEmpty()) {
			int bonnesReponses = 0;
			int totalTest = 0;

			for (Image imgTest : imagesTest) 
			{
				if (imgTest.donnees() == null) continue;
				neurone.metAJour(imgTest.donneesNormalisees());
				float proba = neurone.sortie();
				float vraiLabel = (imgTest.label() == 0) ? 1.0f : 0.0f;
				float reponseArrondie = (proba >= 0.5f) ? 1.0f : 0.0f;
				if (reponseArrondie == vraiLabel) bonnesReponses++;
				totalTest++;
			}
			
			float pourcentage = ((float) bonnesReponses / totalTest) * 100.0f;
			System.out.printf("\n>>> SCORE SANS MÉLANGE : %.2f %% de réussite (%d/%d) <<<\n", 
				pourcentage, bonnesReponses, totalTest);
		}
	}
}
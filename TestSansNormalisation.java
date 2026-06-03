import java.util.List;
import java.util.ArrayList;

public class TestSansNormalisation 
{
	public static void main(String[] args) 
	{
		System.out.println("--- TEST SANS NORMALISATION ---");
		
		List<Image> imagesTrain = Image.chargeDataset("dataset_animaux/train/", true);
		
		if (imagesTrain == null || imagesTrain.isEmpty()) {
			System.out.println("Erreur : Aucune image d'entraînement trouvée.");
			return;
		}

		Image.melange(imagesTrain);
		
		List<float[]> listeEntrees = new ArrayList<>();
		List<Float> listeResultats = new ArrayList<>();

		for (Image img : imagesTrain) 
		{
			if (img.donnees() == null) continue;
			float labelPourNeurone = (img.label() == 0) ? 1.0f : 0.0f;

			// SANS normalisation : pixels entre 0-255
			float[] pixelsBruts = new float[img.donnees().length];
			for (int i = 0; i < img.donnees().length; i++)
				pixelsBruts[i] = (float) img.donnees()[i];
			listeEntrees.add(pixelsBruts);
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
		final float MSElimite = 0.15f; 
		
		System.out.println("Début de l'apprentissage SANS normalisation...");
		neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
		
		// --- LA SAUVEGARDE EST AJOUTÉE ICI ---
		neurone.sauvegarde("cerveau_sans_norm.txt");
		System.out.println(">>> Apprentissage terminé et cerveau sauvegardé ! <<<");
		
		System.out.println("\n--- ÉVALUATION SUR LE JEU DE TEST ---");
		List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
		
		if (imagesTest != null && !imagesTest.isEmpty()) {
			int bonnesReponses = 0;
			int totalTest = 0;

			for (Image imgTest : imagesTest) 
			{
				if (imgTest.donnees() == null) continue;
				
				float[] pixelsBrutsTest = new float[imgTest.donnees().length];
				for (int i = 0; i < imgTest.donnees().length; i++)
					pixelsBrutsTest[i] = (float) imgTest.donnees()[i];
				neurone.metAJour(pixelsBrutsTest);
				float proba = neurone.sortie();
				float vraiLabel = (imgTest.label() == 0) ? 1.0f : 0.0f;
				float reponseArrondie = (proba >= 0.5f) ? 1.0f : 0.0f;
				if (reponseArrondie == vraiLabel) bonnesReponses++;
				totalTest++;
			}
			
			float pourcentage = ((float) bonnesReponses / totalTest) * 100.0f;
			System.out.printf("\n>>> SCORE SANS NORMALISATION : %.2f %% de réussite (%d/%d) <<<\n", 
				pourcentage, bonnesReponses, totalTest);
		}
	}
}
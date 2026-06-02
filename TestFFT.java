import java.util.List;
import java.util.ArrayList;

public class TestFFT 
{
    public static void main(String[] args) 
    {
        System.out.println("--- TEST AVEC FFT 2D ---");
        List<Image> imagesTrain = Image.chargeDataset("dataset_animaux/train/", true);
        if (imagesTrain == null || imagesTrain.isEmpty()) {
            System.out.println("Erreur : Aucune image trouvée.");
            return;
        }
        Image.melange(imagesTrain);
        List<float[]> listeEntrees = new ArrayList<>();
        List<Float> listeResultats = new ArrayList<>();
        System.out.println("Application de la FFT sur les images...");
        for (Image img : imagesTrain) {
            if (img.donnees() == null) continue;
            float labelPourNeurone = (img.label() == 0) ? 1.0f : 0.0f;
            int taille = img.donnees().length;
            Complexe[] signal = new Complexe[taille];
            for (int i = 0; i < taille; i++)
                signal[i] = new ComplexeCartesien(img.donnees()[i] / 255.0, 0);
            Complexe[] resultatFFT = FFTCplx.appliqueSur(signal);
            float[] features = new float[taille];
            double maxMod = 0;
            for (int i = 0; i < taille; i++)
                if (resultatFFT[i].mod() > maxMod)
                    maxMod = resultatFFT[i].mod();
            for (int i = 0; i < taille; i++)
                features[i] = (maxMod > 0) ? (float)(resultatFFT[i].mod() / maxMod) : 0;
            listeEntrees.add(features);
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
        final float MSElimite = 0.20f;
        System.out.println("Début de l'apprentissage avec FFT...");
        neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
        System.out.println(">>> Apprentissage terminé ! <<<");
        List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
        if (imagesTest != null && !imagesTest.isEmpty()) {
            int bonnesReponses = 0;
            int totalTest = 0;
            for (Image imgTest : imagesTest) {
                if (imgTest.donnees() == null) continue;
                int taille = imgTest.donnees().length;
                Complexe[] signal = new Complexe[taille];
                for (int i = 0; i < taille; i++)
                    signal[i] = new ComplexeCartesien(imgTest.donnees()[i] / 255.0, 0);
                Complexe[] resultatFFT = FFTCplx.appliqueSur(signal);
                float[] features = new float[taille];
                double maxMod = 0;
                for (int i = 0; i < taille; i++)
                    if (resultatFFT[i].mod() > maxMod)
                        maxMod = resultatFFT[i].mod();
                for (int i = 0; i < taille; i++)
                    features[i] = (maxMod > 0) ? (float)(resultatFFT[i].mod() / maxMod) : 0;
                neurone.metAJour(features);
                float proba = neurone.sortie();
                float vraiLabel = (imgTest.label() == 0) ? 1.0f : 0.0f;
                float reponseArrondie = (proba >= 0.5f) ? 1.0f : 0.0f;
                if (reponseArrondie == vraiLabel) bonnesReponses++;
                totalTest++;
            }
            float pourcentage = ((float) bonnesReponses / totalTest) * 100.0f;
            System.out.printf("\n>>> SCORE AVEC FFT : %.2f %% de réussite (%d/%d) <<<\n",
                pourcentage, bonnesReponses, totalTest);
        }
    }
}
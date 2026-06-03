import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class TestFFT 
{
    public static void main(String[] args) 
    {
        System.out.println("=== EXTENSION NIVEAU 3 : ANALYSE FRÉQUENTIELLE (FFT 2D) ===");
        
        List<Image> imagesTrain = Image.chargeDataset("dataset_animaux/train/", true);
        if (imagesTrain == null || imagesTrain.isEmpty()) {
            System.out.println("Erreur : Aucune image trouvée.");
            return;
        }
        
        Image.melange(imagesTrain);
        
        // Listes sécurisées pour le multi-threading
        List<float[]> listeEntrees = Collections.synchronizedList(new ArrayList<>());
        List<Float> listeResultats = Collections.synchronizedList(new ArrayList<>());
        
        System.out.println("Extraction des caractéristiques FFT en parallèle (Exploitation de tous les cœurs du processeur)...");
        System.out.println("Veuillez patienter, calculs mathématiques lourds en cours...");

        // Traitement parallèle : chaque cœur du processeur gère ses propres images
        imagesTrain.parallelStream().forEach(img -> {
            if (img.donnees() == null) return;
            
            float labelPourNeurone = (img.label() == 0) ? 1.0f : 0.0f;
            int taille = img.donnees().length;
            Complexe[] signal = new Complexe[taille];
            
            for (int i = 0; i < taille; i++) {
                signal[i] = new ComplexeCartesien(img.donnees()[i] / 255.0, 0);
            }
            
            // Calcul de la FFT
            Complexe[] resultatFFT = FFTCplx.appliqueSur(signal);
            
            float[] features = new float[taille];
            double maxMod = 0;
            
            // Recherche du module maximum
            for (int i = 0; i < taille; i++) {
                double currentMod = resultatFFT[i].mod();
                if (currentMod > maxMod) {
                    maxMod = currentMod;
                }
            }
            
            // Normalisation des features
            for (int i = 0; i < taille; i++) {
                features[i] = (maxMod > 0) ? (float)(resultatFFT[i].mod() / maxMod) : 0;
            }
            
            // Ajout sécurisé aux listes globales
            listeEntrees.add(features);
            listeResultats.add(labelPourNeurone);
        });

        System.out.println("Extraction FFT terminée ! Préparation du réseau de neurones...");

        float[][] entreesArray = new float[listeEntrees.size()][];
        for (int i = 0; i < listeEntrees.size(); i++) {
            entreesArray[i] = listeEntrees.get(i);
        }
        
        float[] resultatsArray = new float[listeResultats.size()];
        for (int i = 0; i < listeResultats.size(); i++) {
            resultatsArray[i] = listeResultats.get(i);
        }
        
        int nbEntreesNeurone = entreesArray[0].length;
        iNeurone neurone = new NeuroneSigmoide(nbEntreesNeurone);
        final float MSElimite = 0.20f; 
        
        System.out.println("Début de l'apprentissage sur les spectres fréquentiels...");
        neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
        
        // --- AJOUT DE LA SAUVEGARDE ---
        neurone.sauvegarde("cerveau_fft.txt");

        // --- PHASE DE TEST ---
        System.out.println("\n--- ÉVALUATION SUR LE JEU DE TEST (FFT) ---");
        List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
        
        if (imagesTest != null && !imagesTest.isEmpty()) {
            int bonnesReponses = 0;
            int totalTest = 0;
            
            for (Image imgTest : imagesTest) {
                if (imgTest.donnees() == null) continue;
                
                int taille = imgTest.donnees().length;
                Complexe[] signal = new Complexe[taille];
                for (int i = 0; i < taille; i++) {
                    signal[i] = new ComplexeCartesien(imgTest.donnees()[i] / 255.0, 0);
                }
                
                Complexe[] resultatFFT = FFTCplx.appliqueSur(signal);
                float[] features = new float[taille];
                double maxMod = 0;
                
                for (int i = 0; i < taille; i++) {
                    if (resultatFFT[i].mod() > maxMod) maxMod = resultatFFT[i].mod();
                }
                for (int i = 0; i < taille; i++) {
                    features[i] = (maxMod > 0) ? (float)(resultatFFT[i].mod() / maxMod) : 0;
                }
                
                neurone.metAJour(features);
                float proba = neurone.sortie();
                float vraiLabel = (imgTest.label() == 0) ? 1.0f : 0.0f;
                float reponseArrondie = (proba >= 0.5f) ? 1.0f : 0.0f;
                
                if (reponseArrondie == vraiLabel) bonnesReponses++;
                totalTest++;
            }
            float pourcentageReussite = ((float) bonnesReponses / totalTest) * 100;
            System.out.printf(">>> Précision du modèle FFT : %.2f %% (%d/%d) <<<\n", 
                              pourcentageReussite, bonnesReponses, totalTest);
        }
    }
}
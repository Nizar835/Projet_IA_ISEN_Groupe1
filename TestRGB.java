import java.util.List;
import java.util.ArrayList;

public class TestRGB 
{
    public static void main(String[] args) 
    {
        System.out.println("=== EXTENSION NIVEAU 3 : APPRENTISSAGE EN COULEURS (ESPACE RGB) ===");
        
        // --- 1. LECTURE DU DATASET EN MODE COULEURS ---
        System.out.println("Lecture du dataset d'entraînement en mode RGB...");
        // Le paramètre 'false' indique à la classe Image de ne PAS forcer le niveau de gris
        // et d'extraire les 3 canaux de couleurs (Rouge, Vert, Bleu).
        List<Image> imagesTrain = Image.chargeDataset("dataset_animaux/train/", false);
        
        if (imagesTrain == null || imagesTrain.isEmpty()) {
            System.out.println("Erreur : Aucune image d'entraînement trouvée.");
            return;
        }

        System.out.println("Mélange aléatoire des données (Shuffle) pour éviter l'overfitting...");
        Image.melange(imagesTrain);

        // --- 2. PRÉPARATION DES TENSEURS POUR LE NEURONE ---
        List<float[]> listeEntrees = new ArrayList<>();
        List<Float> listeResultats = new ArrayList<>();

        for (Image img : imagesTrain) 
        {
            if (img.donnees() == null) continue; // Bouclier anti-crash pour les images corrompues

            // La cible : 1.0f pour les chats, 0.0f pour le reste (chiens et wilds)
            float labelPourNeurone = (img.label() == 0) ? 1.0f : 0.0f;

            listeEntrees.add(img.donneesNormalisees());
            listeResultats.add(labelPourNeurone);
        }

        // Conversion des listes dynamiques en tableaux primitifs (requis par le Neurone)
        float[][] entreesArray = new float[listeEntrees.size()][];
        for (int i = 0; i < listeEntrees.size(); i++) {
            entreesArray[i] = listeEntrees.get(i);
        }
        
        float[] resultatsArray = new float[listeResultats.size()];
        for (int i = 0; i < listeResultats.size(); i++) {
            resultatsArray[i] = listeResultats.get(i);
        }
        
        // --- 3. ENTRAÎNEMENT DU RÉSEAU DE NEURONES ---
        int nbEntreesNeurone = entreesArray[0].length;
        System.out.println("Le réseau aura " + nbEntreesNeurone + " synapses (3 par pixel : R, G, B).");
        
        iNeurone neurone = new NeuroneSigmoide(nbEntreesNeurone);
        final float MSElimite = 0.08f; 
        
        System.out.println("Début de l'apprentissage (Attention, calculs lourds dus à la taille des matrices 3D)...");
        neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
        System.out.println(">>> Apprentissage RGB terminé avec succès ! <<<");


        neurone.sauvegarde("cerveau_rgb.txt");
        System.out.println("Cerveau sauvegardé sous le nom 'cerveau_rgb.txt'.");

        // --- 4. ÉVALUATION GLOBALE SUR LE JEU DE TEST ---
        System.out.println("\n--- ÉVALUATION DU MODÈLE RGB SUR LE JEU DE TEST ---");
        // On charge les images de test également en mode RGB (false)
        List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", false); 
        
        if (imagesTest != null && !imagesTest.isEmpty()) {
            int bonnesReponses = 0;
            int totalTest = 0;

            for (Image imgTest : imagesTest) 
            {
                if (imgTest.donnees() == null) continue;

                // On soumet les pixels de test (normalisés) au réseau
                neurone.metAJour(imgTest.donneesNormalisees());
                float proba = neurone.sortie();

                // On compare la prédiction avec la vraie nature de l'image
                float vraiLabel = (imgTest.label() == 0) ? 1.0f : 0.0f;
                float reponseArrondie = (proba >= 0.5f) ? 1.0f : 0.0f;

                if (reponseArrondie == vraiLabel) {
                    bonnesReponses++;
                }
                totalTest++;
            }
            
            if (totalTest > 0) {
                float pourcentageReussite = ((float) bonnesReponses / totalTest) * 100.0f;
                System.out.printf(">>> Précision du modèle RGB : %.2f %% (%d/%d) <<<\n", 
                                  pourcentageReussite, bonnesReponses, totalTest);
            }
        }
    }
}
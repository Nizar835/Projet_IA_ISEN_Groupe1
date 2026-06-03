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

        // 2. BOUCLE DE DUPLICATION
        for (Image img : imagesOriginales) 
        {
            if (img.donnees() == null) {
                continue; // Bouclier anti-crash pour les images corrompues
            }
            
            // Pour le neurone : Chat = 1.0f, Reste (Chien/Wild) = 0.0f
            float labelPourNeurone = (img.label() == 0) ? 1.0f : 0.0f;
            
            // A. On ajoute l'image originale normalisée
            float[] normales = img.donneesNormalisees();
            listeEntrees.add(normales);
            listeResultats.add(labelPourNeurone);
            
            // B. On crée la version miroir et on l'ajoute
            float[] pixelsMiroir = FiltreImage.miroirGris(normales, img.largeur(), img.hauteur());
            listeEntrees.add(pixelsMiroir);
            listeResultats.add(labelPourNeurone);
        }
        
        System.out.println("Taille du dataset après augmentation : " + listeEntrees.size() + " images.");

        // 3. MÉLANGE OBLIGATOIRE DES DONNÉES AUGMENTÉES (Train)
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
        
        // MSE ajustée à 0.08f pour équilibrer la lourdeur des 20 000 images
        final float MSElimite = 0.08f; 
        
        System.out.println("Début de l'apprentissage sur le dataset augmenté...");
        neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
        System.out.println(">>> Apprentissage terminé avec succès ! <<<");

        // --- SAUVEGARDE DU CERVEAU ---
        neurone.sauvegarde("cerveau_miroir.txt");
        System.out.println("Cerveau sauvegardé sous le nom 'cerveau_miroir.txt'.");
        
        // ==========================================================
        // 5. ÉVALUATION GLOBALE SUR LE JEU DE TEST COMPLET
        // ==========================================================
        System.out.println("\n--- ÉVALUATION SUR LE JEU DE TEST ---");
        List<Image> imagesTest = Image.chargeDataset("dataset_animaux/test/", true);
        
        if (imagesTest != null && !imagesTest.isEmpty()) {
            int bonnesReponses = 0;
            int totalTest = 0;

            for (Image imgTest : imagesTest) 
            {
                if (imgTest.donnees() == null) continue;
                
                // Le neurone lit l'image
                neurone.metAJour(imgTest.donneesNormalisees());
                float proba = neurone.sortie();
                
                // Le corrigé (Chat = 1.0f, Reste = 0.0f)
                float vraiLabel = (imgTest.label() == 0) ? 1.0f : 0.0f;
                
                // La prédiction de l'IA (Arrondie à 1.0f ou 0.0f)
                float reponseArrondie = (proba >= 0.5f) ? 1.0f : 0.0f;
                
                if (reponseArrondie == vraiLabel) {
                    bonnesReponses++;
                }
                totalTest++;
            }
            
            // Calcul du pourcentage
            float pourcentage = ((float) bonnesReponses / totalTest) * 100.0f;
            System.out.printf("\n>>> SCORE MODÈLE MIROIR : %.2f %% de réussite (%d/%d) <<<\n", 
                pourcentage, bonnesReponses, totalTest);
        } else {
            System.out.println("Erreur : Aucun dataset de test trouvé pour l'évaluation.");
        }
    }
}
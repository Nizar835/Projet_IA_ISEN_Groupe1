import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class TestTSL 
{
    public static void main(String[] args) 
    {
        System.out.println("=== EXTENSION NIVEAU 3 : COULEUR TSL (Teinte, Saturation, Luminosité) ===");
        
        // --- 1. LECTURE ET ENTRAÎNEMENT ---
        System.out.println("Lecture du dataset d'entraînement...");
        String dossierTrain = "dataset_animaux/train/";
        List<String> fichiersTrain = Image.listeFichiers(dossierTrain); 
        
        if (fichiersTrain == null || fichiersTrain.isEmpty()) {
            System.out.println("Erreur : Dossier d'entraînement introuvable.");
            return;
        }

        System.out.println("Mélange des fichiers...");
        Collections.shuffle(fichiersTrain);
        
        List<float[]> listeEntrees = new ArrayList<>();
        List<Float> listeResultats = new ArrayList<>();
        
        System.out.println("Conversion matricielle des images en espace TSL...");

        int index = 0;
        for (String chemin : fichiersTrain) 
        {
            try {
                BufferedImage bimg = ImageIO.read(new File(chemin));
                if (bimg == null) continue;

                int largeur = bimg.getWidth();
                int hauteur = bimg.getHeight();
                
                float[] pixelsTSL = new float[largeur * hauteur * 3];
                int pos = 0;
                
                for (int y = 0; y < hauteur; y++) {
                    for (int x = 0; x < largeur; x++) {
                        int rgb = bimg.getRGB(x, y);
                        Color couleur = new Color(rgb, true);
                        
                        float[] tsl = new float[3];
                        Color.RGBtoHSB(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), tsl);
                        
                        pixelsTSL[pos] = tsl[0];
                        pixelsTSL[pos+1] = tsl[1];
                        pixelsTSL[pos+2] = tsl[2];
                        pos += 3;
                    }
                }
                
                float labelPourNeurone = chemin.contains("cat") ? 1.0f : 0.0f;
                listeEntrees.add(pixelsTSL);
                listeResultats.add(labelPourNeurone);
                
                index++;
                if (index % 5000 == 0) {
                    System.out.println(index + " images converties...");
                }
                
            } catch (Exception e) {
                // Ignore les images corrompues
            }
        }
        
        System.out.println("Toutes les images sont converties ! Taille du dataset : " + listeEntrees.size());

        float[][] entreesArray = new float[listeEntrees.size()][];
        for (int i = 0; i < listeEntrees.size(); i++) entreesArray[i] = listeEntrees.get(i);
        
        float[] resultatsArray = new float[listeResultats.size()];
        for (int i = 0; i < listeResultats.size(); i++) resultatsArray[i] = listeResultats.get(i);
        
        int nbEntreesNeurone = entreesArray[0].length;
        iNeurone neurone = new NeuroneSigmoide(nbEntreesNeurone);
        final float MSElimite = 0.15f; 
        
        System.out.println("Début de l'apprentissage (Attention, c'est très lourd : il y a 3 canaux de couleur !)...");
        neurone.apprentissage(entreesArray, resultatsArray, MSElimite);
        System.out.println(">>> Apprentissage TSL terminé avec succès ! <<<");

        neurone.sauvegarde("cerveau_tsl.txt");
        System.out.println("Cerveau sauvegardé sous le nom 'cerveau_tsl.txt'.");

        // ==========================================================
        // --- 2. NOUVELLE PHASE : ÉVALUATION SUR LE JEU DE TEST  ---
        // ==========================================================
        System.out.println("\n--- ÉVALUATION DU MODÈLE TSL SUR LE JEU DE TEST ---");
        String dossierTest = "dataset_animaux/test/";
        List<String> fichiersTest = Image.listeFichiers(dossierTest);
        
        if (fichiersTest == null || fichiersTest.isEmpty()) {
            System.out.println("Erreur : Dossier de test introuvable.");
            return;
        }

        int bonnesReponses = 0;
        int totalTest = 0;

        for (String cheminTest : fichiersTest) 
        {
            try {
                BufferedImage bimgTest = ImageIO.read(new File(cheminTest));
                if (bimgTest == null) continue;

                int largeur = bimgTest.getWidth();
                int hauteur = bimgTest.getHeight();
                float[] pixelsTSL = new float[largeur * hauteur * 3];
                int pos = 0;
                
                // On reconvertit l'image de test en TSL pour le neurone
                for (int y = 0; y < hauteur; y++) {
                    for (int x = 0; x < largeur; x++) {
                        int rgb = bimgTest.getRGB(x, y);
                        Color couleur = new Color(rgb, true);
                        float[] tsl = new float[3];
                        Color.RGBtoHSB(couleur.getRed(), couleur.getGreen(), couleur.getBlue(), tsl);
                        pixelsTSL[pos] = tsl[0];
                        pixelsTSL[pos+1] = tsl[1];
                        pixelsTSL[pos+2] = tsl[2];
                        pos += 3;
                    }
                }
                
                int labelAttendu = cheminTest.contains("cat") ? 1 : 0;
                
                // Prédiction
                neurone.metAJour(pixelsTSL);
                int labelPredit = (neurone.sortie() >= 0.5f) ? 1 : 0;
                
                if (labelPredit == labelAttendu) {
                    bonnesReponses++;
                }
                totalTest++;
                
            } catch (Exception e) {
                // Ignore les images corrompues
            }
        }
        
        if (totalTest > 0) {
            float pourcentageReussite = ((float) bonnesReponses / totalTest) * 100;
            System.out.printf(">>> Précision du modèle TSL : %.2f %% (%d/%d) <<<\n", 
                                pourcentageReussite, bonnesReponses, totalTest);
        }
    }
}
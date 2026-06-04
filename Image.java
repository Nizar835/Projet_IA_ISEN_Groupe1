import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

/**
 * Classe utilitaire pour le Traitement du Signal visuel (THS).
 * Permet de charger, convertir et préparer les images pour le réseau de neurones.
 */
public class Image {
    
    // --- CONSTANTES : ÉTIQUETTES DES CLASSES (LABELS) ---
    public static int LabelChat = 0;
    public static int LabelChien = 1;
    public static int LabelWild = 2;
    public static int LabelInconnu = 3;
    
    // --- ATTRIBUTS DE L'IMAGE ---
    private int label = -1;
    private int largeur = 0;
    private int hauteur = 0;
    
    // Le tenseur de l'image : Les pixels sont "aplatis" dans un tableau 1D 
    // car le perceptron simple ne prend qu'un vecteur d'entrée (1D), pas une matrice 2D.
    private int[] donnees = null;

    // =====================================================================
    // ACCESSEURS (GETTERS)
    // =====================================================================
    public int label() { return this.label; }
    public int largeur() { return this.largeur; }
    public int hauteur() { return this.hauteur; }
    public int taille() { return this.donnees.length; }
    public int[] donnees() { return this.donnees; }

    /**
     * Détermine si l'image est en niveaux de gris ou en couleurs (RGB)
     * en comparant la taille du tableau de données à la surface de l'image.
     */
    public boolean estEnNiveauxDeGris() {
        return this.taille() == (this.largeur() * this.hauteur());
    }

    /**
     * NORMALISATION (ESSENTIEL POUR L'IA) :
     * Convertit les pixels bruts (0 à 255) en valeurs flottantes entre 0.0 et 1.0.
     * Mathématiquement indispensable pour éviter la saturation de la fonction Sigmoïde 
     * et stabiliser la descente de gradient lors de l'apprentissage.
     */
    public float[] donneesNormalisees() {
        if (this.donnees == null) {
            return new float[0];
        } else {
            float[] pixelsNormalises = new float[this.donnees.length];
            for(int i = 0; i < this.donnees.length; ++i) {
                pixelsNormalises[i] = (float)this.donnees[i] / 255.0F;
            }
            return pixelsNormalises;
        }
    }

    public void afficheMetadonnees() {
        String typeImage = this.estEnNiveauxDeGris() ? "grayscale" : " couleurs";
        System.out.printf("Image (%s): label=%d, largeur=%d, hauteur=%d, taille=%d\n", 
                          typeImage, this.label(), this.largeur(), this.hauteur(), this.taille());
    }

    // =====================================================================
    // CONSTRUCTEUR : LECTURE ET CONVERSION DU SIGNAL VISUEL
    // =====================================================================
    public Image(String cheminImage, int labelImage, boolean forcerGris) {
        try {
            BufferedImage bimg = ImageIO.read(new File(cheminImage));
            this.label = labelImage;
            this.largeur = bimg.getWidth(null);
            this.hauteur = bimg.getHeight(null);
            
            // Si on force le gris, 1 pixel = 1 case. Si couleur, 1 pixel = 3 cases (R, G, B)
            int tailleTableau = forcerGris ? (this.hauteur * this.largeur) : (3 * this.hauteur * this.largeur);
            this.donnees = new int[tailleTableau];

            // Extraction matricielle (Pixel par pixel)
            for(int y = 0; y < this.hauteur; ++y) {
                for(int x = 0; x < this.largeur; ++x) {
                    
                    // Récupération de l'octet brut du pixel
                    long rgb = (long)bimg.getRGB(x, y);
                    
                    // Décalage binaire (Bit Shifting) pour isoler les canaux R, G et B
                    int r = (int)(rgb >> 16 & 255L);
                    int g = (int)(rgb >> 8 & 255L);
                    int b = (int)(rgb & 255L);
                    
                    int index = y * this.largeur + x; // Calcul de la position dans le tableau aplati (1D)
                    
                    if (forcerGris) {
                        // Formule officielle de la luminance standardisée (Rec. 709)
                        // L'œil humain est plus sensible au vert, d'où les coefficients différents.
                        float luminance = 0.2125F * (float)r + 0.7154F * (float)g + 0.0721F * (float)b;
                        this.donnees[index] = (int)Math.max(0.0F, Math.min(255.0F, luminance));
                    } else {
                        // Traitement en couleurs : on sépare les canaux pour le neurone
                        this.donnees[3 * index + 0] = r;
                        this.donnees[3 * index + 1] = g;
                        this.donnees[3 * index + 2] = b;
                    }
                }
            }
        } catch (Exception e) {
            System.err.printf("Image non trouvée ou non lisible: %s\n", cheminImage);
        }
    }

    // =====================================================================
    // OUTILS DE DATA SCIENCE (GESTION DE DATASETS)
    // =====================================================================
    
    /**
     * Charge un dossier entier d'images et attribue automatiquement le bon 
     * label (la cible) en analysant le nom du fichier.
     */
    public static List<Image> chargeDataset(String dossierPath, boolean niveauxDeGris) {
        ArrayList<Image> listeImages = new ArrayList<>();
        List<String> cheminsFichiers = listeFichiers(dossierPath);
        
        if (cheminsFichiers != null) {
            for(String chemin : cheminsFichiers) {
                // Auto-Labellisation basée sur la nomenclature des fichiers
                int labelDevine = chemin.contains("cat") ? LabelChat : 
                                 (chemin.contains("dog") ? LabelChien : 
                                 (chemin.contains("wild") ? LabelWild : LabelInconnu));
                                 
                listeImages.add(new Image(chemin, labelDevine, niveauxDeGris));
            }
        }
        return listeImages;
    }

    /**
     * Mélange le dataset (Shuffle). 
     * Étape cruciale en Machine Learning pour éviter que le réseau de neurones 
     * n'apprenne les images "par cœur" dans l'ordre (Overfitting).
     */
    public static void melange(List<Image> liste) {
        Collections.shuffle(liste);
    }

    /**
     * Scanne un répertoire et liste les chemins absolus de tous les fichiers réguliers.
     */
    public static List<String> listeFichiers(String repertoire) {
        List<String> chemins = null;
        try {
            chemins = Files.walk(Paths.get(repertoire))
                           .filter(chemin -> Files.isRegularFile(chemin, new LinkOption[0]))
                           .map(Path::toAbsolutePath)
                           .map(Path::toString)
                           .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Dossier introuvable ou vide : " + repertoire);
        }
        return chemins;
    }

    // =====================================================================
    // MÉTHODE PRINCIPALE DE TEST DE LA CLASSE
    // =====================================================================
    public static void main(String[] args) {
        List<Image> datasetTrain = chargeDataset("dataset_animaux/train/", true);
        if (datasetTrain != null && !datasetTrain.isEmpty()) {
            melange(datasetTrain);
            System.out.println("Nombre d'images chargées : " + datasetTrain.size());
            datasetTrain.get(0).afficheMetadonnees();
        } else {
            System.out.println("Aucune image trouvée pour le test de la classe.");
        }
    }
}
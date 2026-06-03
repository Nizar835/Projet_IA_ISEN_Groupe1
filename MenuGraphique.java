import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;

public class MenuGraphique extends JFrame {

    private JTextArea consoleArea;
    private JLabel imageLabel;
    private JLabel resultLabel;
    private JButton[] actionButtons; 
    private JTextField etaField;
    
    private JComboBox<String> comboModele1; 
    private JComboBox<String> comboModele2;
    private JCheckBox cbFusion;

    // --- PALETTE DE COULEURS ULTRA-MODERNE ---
    private final Color BG_COLOR = new Color(30, 30, 30);
    private final Color PANEL_BG = new Color(37, 37, 38);
    private final Color BUTTON_BG = new Color(63, 63, 70);
    private final Color TEXT_COLOR = new Color(240, 240, 240);
    private final Color ACCENT_BLUE = new Color(0, 122, 204);
    private final Color SUCCESS_GREEN = new Color(76, 175, 80);
    private final Color WARNING_ORANGE = new Color(255, 152, 0);

    public MenuGraphique() {
        setTitle("🧠 Dashboard IA Pro - L'Intégrale (Groupe 1)");
        setSize(1350, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(15, 15));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // =========================================================
        // HAUT : HYPERPARAMÈTRES
        // =========================================================
        JPanel panelConfig = creerPanneauStyle("⚙️ Configuration Globale");
        panelConfig.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 10));
        
        JLabel lblEta = new JLabel("Taux d'apprentissage (ETA) :");
        lblEta.setForeground(TEXT_COLOR);
        lblEta.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelConfig.add(lblEta);
        
        etaField = new JTextField("0.0001", 8);
        etaField.setBackground(new Color(50, 50, 50));
        etaField.setForeground(Color.WHITE);
        etaField.setCaretColor(Color.WHITE);
        etaField.setFont(new Font("Consolas", Font.BOLD, 14));
        etaField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80)),
            new EmptyBorder(5, 5, 5, 5)
        ));
        panelConfig.add(etaField);
        
        JButton btnAppliquerEta = creerBoutonStyle("Appliquer", ACCENT_BLUE);
        btnAppliquerEta.addActionListener(e -> {
            try {
                float nouvelEta = Float.parseFloat(etaField.getText());
                Neurone.fixeCoefApprentissage(nouvelEta);
                System.out.println("✅ ETA appliqué : " + nouvelEta);
            } catch (Exception ex) {
                System.out.println("❌ L'ETA doit être un nombre décimal !");
            }
        });
        panelConfig.add(btnAppliquerEta);
        add(panelConfig, BorderLayout.NORTH);

        // =========================================================
        // GAUCHE : LANCEURS D'ENTRAÎNEMENT (Maintenant 9 options !)
        // =========================================================
        JPanel panelMenu = creerPanneauStyle("1. Entraînements (Création des cerveaux)");
        panelMenu.setLayout(new GridLayout(9, 1, 10, 8)); // Modifié pour 9 boutons
        panelMenu.setPreferredSize(new Dimension(320, 0));

        actionButtons = new JButton[]{
            creerBoutonStyle("1. Base (Binaire)", BUTTON_BG),
            creerBoutonStyle("2. Multi-Classes (3 Neurones)", BUTTON_BG),
            creerBoutonStyle("3. Data Augmentation (Miroir)", BUTTON_BG),
            creerBoutonStyle("4. Apprentissage RGB", BUTTON_BG),
            creerBoutonStyle("5. Apprentissage TSL", BUTTON_BG),
            creerBoutonStyle("6. Apprentissage par FFT", BUTTON_BG),
            creerBoutonStyle("7. Crash-Test : Sans Mélange", BUTTON_BG),
            creerBoutonStyle("8. Crash-Test : Sans Normalisation", BUTTON_BG),
            creerBoutonStyle("9. Évaluation : Matrice de Confusion", BUTTON_BG)
        };

        for (JButton btn : actionButtons) panelMenu.add(btn);
        add(panelMenu, BorderLayout.WEST);

        // =========================================================
        // DROITE : MOTEUR D'INFÉRENCE & ENSEMBLE LEARNING
        // =========================================================
        JPanel panelTest = creerPanneauStyle("2. Laboratoire d'Inférence (Tests)");
        panelTest.setLayout(new BorderLayout(5, 15));
        panelTest.setPreferredSize(new Dimension(420, 0));

        JPanel panelOptionsTest = new JPanel(new GridLayout(6, 1, 5, 5));
        panelOptionsTest.setOpaque(false);
        
        JLabel lblM1 = new JLabel("Modèle Principal :");
        lblM1.setForeground(TEXT_COLOR);
        lblM1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelOptionsTest.add(lblM1);
        
        // --- LA LISTE DES MODÈLES AVEC LE TSL ---
        String[] modelesListe = {
            "Base (cerveau_binaire.txt)", "Multi-Classes (chat, chien, wild)", 
            "Couleur RGB (cerveau_rgb.txt)", "Couleur TSL (cerveau_tsl.txt)", 
            "Miroir (cerveau_miroir.txt)", "FFT (cerveau_fft.txt)", 
            "Sans Mélange (cerveau_sans_melange.txt)", "Sans Normalisation (cerveau_sans_norm.txt)"
        };
        
        comboModele1 = new JComboBox<>(modelesListe);
        panelOptionsTest.add(comboModele1);

        cbFusion = new JCheckBox("🤝 Activer la FUSION (Ensemble Learning)");
        cbFusion.setForeground(SUCCESS_GREEN);
        cbFusion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cbFusion.setOpaque(false);
        cbFusion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelOptionsTest.add(cbFusion);

        JLabel lblM2 = new JLabel("Modèle Secondaire (Pour la fusion) :");
        lblM2.setForeground(Color.GRAY);
        panelOptionsTest.add(lblM2);

        comboModele2 = new JComboBox<>(modelesListe);
        comboModele2.setEnabled(false);
        panelOptionsTest.add(comboModele2);

        cbFusion.addActionListener(e -> {
            boolean isFusion = cbFusion.isSelected();
            comboModele2.setEnabled(isFusion);
            lblM2.setForeground(isFusion ? TEXT_COLOR : Color.GRAY);
            if(isFusion && comboModele1.getSelectedItem().toString().contains("Multi-Classes")) {
                cbFusion.setSelected(false);
                comboModele2.setEnabled(false);
                System.out.println("⚠️ La fusion n'est pas disponible pour le Multi-Classes !");
            }
        });
        panelTest.add(panelOptionsTest, BorderLayout.NORTH);

        JPanel panelBoutonsTest = new JPanel(new GridLayout(2, 1, 10, 10));
        panelBoutonsTest.setOpaque(false);

        JButton btnTesterImage = creerBoutonStyle("🔍 Tester une Image Ciblée", ACCENT_BLUE);
        btnTesterImage.setPreferredSize(new Dimension(0, 60));
        btnTesterImage.addActionListener(e -> testerImageManuel());
        
        JButton btnTesterAleatoire = creerBoutonStyle("🎲 Lancer Animation (10 Images Aléatoires)", WARNING_ORANGE);
        btnTesterAleatoire.setPreferredSize(new Dimension(0, 60));
        btnTesterAleatoire.addActionListener(e -> lancerTestAleatoireAnime());

        panelBoutonsTest.add(btnTesterImage);
        panelBoutonsTest.add(btnTesterAleatoire);
        panelTest.add(panelBoutonsTest, BorderLayout.SOUTH);
        
        add(panelTest, BorderLayout.EAST);

        // =========================================================
        // CENTRE : VISUALISEUR
        // =========================================================
        JPanel panelCentre = creerPanneauStyle("Écran d'Analyse");
        panelCentre.setLayout(new BorderLayout());
        
        imageLabel = new JLabel("En attente d'une image...", SwingConstants.CENTER);
        imageLabel.setForeground(Color.GRAY);
        imageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        panelCentre.add(imageLabel, BorderLayout.CENTER);
        
        resultLabel = new JLabel("PRÊT", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        resultLabel.setForeground(TEXT_COLOR);
        resultLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        panelCentre.add(resultLabel, BorderLayout.SOUTH);
        
        add(panelCentre, BorderLayout.CENTER);

        // =========================================================
        // BAS : CONSOLE DE LOGS
        // =========================================================
        consoleArea = new JTextArea(8, 50);
        consoleArea.setEditable(false);
        consoleArea.setBackground(new Color(15, 15, 15));
        consoleArea.setForeground(new Color(10, 200, 50));
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        
        JScrollPane scrollPane = new JScrollPane(consoleArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        
        JPanel panelBas = creerPanneauStyle("Terminal d'exécution");
        panelBas.setLayout(new BorderLayout());
        panelBas.add(scrollPane, BorderLayout.CENTER);
        add(panelBas, BorderLayout.SOUTH);

        redirigerConsole();

        // Assignation multi-thread
        actionButtons[0].addActionListener(e -> lancerTacheSecurisee(() -> MainProjet.main(new String[]{})));
        actionButtons[1].addActionListener(e -> lancerTacheSecurisee(() -> TestMultiClasses.main(new String[]{})));
        actionButtons[2].addActionListener(e -> lancerTacheSecurisee(() -> TestMiroir.main(new String[]{})));
        actionButtons[3].addActionListener(e -> lancerTacheSecurisee(() -> TestRGB.main(new String[]{})));
        actionButtons[4].addActionListener(e -> lancerTacheSecurisee(() -> TestTSL.main(new String[]{}))); // Nouveau TSL
        actionButtons[5].addActionListener(e -> lancerTacheSecurisee(() -> TestFFT.main(new String[]{})));
        actionButtons[6].addActionListener(e -> lancerTacheSecurisee(() -> TestSansMelange.main(new String[]{})));
        actionButtons[7].addActionListener(e -> lancerTacheSecurisee(() -> TestSansNormalisation.main(new String[]{})));
        actionButtons[8].addActionListener(e -> lancerTacheSecurisee(() -> TestMatriceConfusion.main(new String[]{})));
    }

    // =====================================================================
    // UTILITAIRES DE DESIGN
    // =====================================================================
    private JPanel creerPanneauStyle(String titre) {
        JPanel p = new JPanel();
        p.setBackground(PANEL_BG);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)), titre);
        border.setTitleColor(TEXT_COLOR);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        p.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(10, 10, 10, 10)));
        return p;
    }

    private JButton creerBoutonStyle(String texte, Color bgColor) {
        JButton btn = new JButton(texte);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (btn.isEnabled()) btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        return btn;
    }

    private void redirigerConsole() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) { } 
            @Override
            public void write(byte[] b, int off, int len) {
                String texte = new String(b, off, len, StandardCharsets.UTF_8);
                SwingUtilities.invokeLater(() -> {
                    consoleArea.append(texte);
                    consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
                });
            }
        };
        PrintStream ps = new PrintStream(out, true, StandardCharsets.UTF_8);
        System.setOut(ps); System.setErr(ps);
    }

    private void lancerTacheSecurisee(Runnable tâche) {
        for (JButton btn : actionButtons) btn.setEnabled(false);
        resultLabel.setText("CALCULS EN COURS...");
        resultLabel.setForeground(WARNING_ORANGE);
        new Thread(() -> {
            try { tâche.run(); } catch (Exception ex) { System.out.println("Erreur : " + ex.getMessage()); } 
            finally {
                SwingUtilities.invokeLater(() -> {
                    for (JButton btn : actionButtons) btn.setEnabled(true);
                    resultLabel.setText("PRÊT");
                    resultLabel.setForeground(SUCCESS_GREEN);
                });
            }
        }).start();
    }

    // =====================================================================
    // ANIMATION FLUIDE
    // =====================================================================
    private void lancerTestAleatoireAnime() {
        // CORRECTION DU BUG : On utilise listeFichiers(String) directement
        List<String> fichiersTest = Image.listeFichiers("dataset_animaux/test/");
        if (fichiersTest == null || fichiersTest.isEmpty()) {
            System.out.println("❌ Erreur : Dossier de test introuvable.");
            return;
        }

        Collections.shuffle(fichiersTest);
        int nbImages = Math.min(10, fichiersTest.size());

        new Thread(() -> {
            try {
                System.out.println("\n🚀 DÉMARRAGE DU TEST EN RAFALE (10 IMAGES) ...");
                int scoreTotal = 0;

                for (int i = 0; i < nbImages; i++) {
                    String chemin = fichiersTest.get(i);
                    File f = new File(chemin);
                    
                    SwingUtilities.invokeLater(() -> {
                        try {
                            BufferedImage bimg = ImageIO.read(f);
                            java.awt.Image scaled = bimg.getScaledInstance(350, 350, java.awt.Image.SCALE_SMOOTH);
                            imageLabel.setIcon(new ImageIcon(scaled));
                            imageLabel.setText("");
                        } catch (Exception e) {}
                    });

                    int prediction = executerInference(f, cbFusion.isSelected());
                    int vraiLabel = chemin.contains("cat") ? 0 : (chemin.contains("dog") ? 1 : 2);

                    if (prediction == vraiLabel) scoreTotal++;

                    Thread.sleep(1200); 
                }

                System.out.println("\n🏆 FIN DU TEST ALÉATOIRE ! Score : " + scoreTotal + " / " + nbImages);
                
                final int finalScore = scoreTotal;
                SwingUtilities.invokeLater(() -> {
                    imageLabel.setIcon(null); 
                    
                    String couleurScore = (finalScore >= 6) ? "#4CAF50" : "#FF9800";
                    String htmlTrophy = "<html><div style='text-align: center; font-family: Segoe UI;'>"
                            + "<h1 style='color: white; font-size: 36px; margin-bottom: 5px;'>🏆 TEST TERMINÉ</h1>"
                            + "<h2 style='color: " + couleurScore + "; font-size: 50px; margin-top: 0px;'>" + (finalScore * 10) + " %</h2>"
                            + "<p style='color: gray; font-size: 16px;'>" + finalScore + " bonnes réponses sur " + nbImages + " images</p>"
                            + "</div></html>";
                    
                    imageLabel.setText(htmlTrophy);
                    resultLabel.setText("BILAN : " + finalScore + " / " + nbImages);
                    resultLabel.setForeground(finalScore >= 6 ? SUCCESS_GREEN : WARNING_ORANGE);
                });

            } catch (Exception ex) {
                System.out.println("Erreur durant l'animation : " + ex.getMessage());
            }
        }).start();
    }

    private void testerImageManuel() {
        JFileChooser fc = new JFileChooser("dataset_animaux/test/");
        fc.setDialogTitle("Sélectionnez l'image à tester");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        
        File fichierImage = fc.getSelectedFile();
        try {
            BufferedImage bimg = ImageIO.read(fichierImage);
            java.awt.Image scaledImg = bimg.getScaledInstance(350, 350, java.awt.Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImg));
            imageLabel.setText("");
            
            executerInference(fichierImage, cbFusion.isSelected());
        } catch (Exception ex) {
            System.out.println("Erreur de lecture de l'image.");
        }
    }

    // =====================================================================
    // MOTEUR D'INFÉRENCE GLOBAL
    // =====================================================================
    private int executerInference(File fichierImage, boolean avecFusion) {
        try {
            System.out.println("\n--- ANALYSE DE : " + fichierImage.getName() + " ---");
            String mod1 = (String) comboModele1.getSelectedItem();
            
            if (mod1.contains("Multi-Classes")) {
                float[] pixels = new Image(fichierImage.getAbsolutePath(), Image.LabelInconnu, true).donneesNormalisees();
                iNeurone nChat = new NeuroneSigmoide(pixels.length); nChat.chargement("cerveau_chat.txt");
                iNeurone nChien = new NeuroneSigmoide(pixels.length); nChien.chargement("cerveau_chien.txt");
                iNeurone nWild = new NeuroneSigmoide(pixels.length); nWild.chargement("cerveau_wild.txt");
                nChat.metAJour(pixels); nChien.metAJour(pixels); nWild.metAJour(pixels);
                float pChat = nChat.sortie(), pChien = nChien.sortie(), pWild = nWild.sortie();
                
                if (pChat > pChien && pChat > pWild) {
                    majUIResultat(String.format("CHAT 🐱 (%.1f%%)", pChat*100), SUCCESS_GREEN); return 0;
                } else if (pChien > pChat && pChien > pWild) {
                    majUIResultat(String.format("CHIEN 🐶 (%.1f%%)", pChien*100), ACCENT_BLUE); return 1;
                } else {
                    majUIResultat(String.format("WILD 🦁 (%.1f%%)", pWild*100), WARNING_ORANGE); return 2;
                }
            }

            float proba1 = obtenirProbaModele(mod1, fichierImage);
            System.out.printf("Avis Modèle 1 : %.2f%%\n", proba1 * 100);
            float probaFinale = proba1;

            if (avecFusion) {
                String mod2 = (String) comboModele2.getSelectedItem();
                float proba2 = obtenirProbaModele(mod2, fichierImage);
                System.out.printf("Avis Modèle 2 : %.2f%%\n", proba2 * 100);
                
                probaFinale = (proba1 + proba2) / 2.0f;
                System.out.printf("🧠 FUSION DES CERVEAUX : %.2f%%\n", probaFinale * 100);
            }

            if (probaFinale >= 0.5f) {
                majUIResultat(String.format(avecFusion ? "CHAT 🐱 (Fusion: %.1f%%)" : "CHAT 🐱 (%.1f%%)", probaFinale * 100), SUCCESS_GREEN);
                return 0;
            } else {
                majUIResultat(String.format(avecFusion ? "CHIEN/WILD 🐶 (Fusion: %.1f%%)" : "CHIEN/WILD 🐶 (%.1f%%)", probaFinale * 100), WARNING_ORANGE);
                return 1;
            }

        } catch (Exception ex) {
            System.out.println("❌ Erreur : " + ex.getMessage());
            majUIResultat("ERREUR D'ANALYSE", Color.RED);
            return -1;
        }
    }

    private void majUIResultat(String texte, Color couleur) {
        SwingUtilities.invokeLater(() -> {
            resultLabel.setText(texte);
            resultLabel.setForeground(couleur);
        });
    }

    // =====================================================================
    // L'USINE À DONNÉES (Gère tous les formats, y compris le TSL)
    // =====================================================================
    private float obtenirProbaModele(String modeleStr, File imgFile) throws Exception {
        String fichierTxt = extraireNomFichier(modeleStr);
        if (!new File(fichierTxt).exists()) throw new Exception("Fichier manquant : " + fichierTxt);

        boolean isRGB = modeleStr.contains("Couleur RGB");
        Image img = new Image(imgFile.getAbsolutePath(), Image.LabelInconnu, !isRGB);
        float[] pixels;

        if (modeleStr.contains("FFT")) {
            int taille = img.donnees().length;
            Complexe[] signal = new Complexe[taille];
            for (int i = 0; i < taille; i++) signal[i] = new ComplexeCartesien(img.donnees()[i] / 255.0, 0);
            Complexe[] resFFT = FFTCplx.appliqueSur(signal);
            pixels = new float[taille];
            double max = 0;
            for (int i = 0; i < taille; i++) if (resFFT[i].mod() > max) max = resFFT[i].mod();
            for (int i = 0; i < taille; i++) pixels[i] = (max > 0) ? (float)(resFFT[i].mod() / max) : 0;
        } 
        else if (modeleStr.contains("TSL")) { // --- INTÉGRATION DE LA CONVERSION TSL ---
            BufferedImage bimg = ImageIO.read(imgFile);
            int w = bimg.getWidth(), h = bimg.getHeight();
            pixels = new float[w * h * 3];
            int pos = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    Color c = new Color(bimg.getRGB(x, y), true);
                    float[] tsl = new float[3];
                    Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), tsl);
                    pixels[pos++] = tsl[0]; 
                    pixels[pos++] = tsl[1]; 
                    pixels[pos++] = tsl[2];
                }
            }
        } 
        else if (modeleStr.contains("Sans Norm")) {
            pixels = new float[img.donnees().length];
            for(int i = 0; i < img.donnees().length; i++) pixels[i] = img.donnees()[i];
        } else {
            pixels = img.donneesNormalisees();
        }

        iNeurone neurone = new NeuroneSigmoide(pixels.length);
        neurone.chargement(fichierTxt);
        neurone.metAJour(pixels);
        return neurone.sortie();
    }

    private String extraireNomFichier(String str) {
        int start = str.indexOf("("); int end = str.indexOf(")");
        if (start != -1 && end != -1) return str.substring(start + 1, end);
        return "";
    }

    public static void main(String[] args) {
        // LE SECRET POUR ÉCRASER LE THÈME WINDOWS (Méthode forte) :
        try { 
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); 
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> new MenuGraphique().setVisible(true));
    }
}
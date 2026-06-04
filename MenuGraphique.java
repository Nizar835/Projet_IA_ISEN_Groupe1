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
    
    private JButton btnTesterImage;
    private JButton btnTesterAleatoire;
    private JButton btnEntrainementSequentiel;
    private JButton btnStop; // LE KILL SWITCH ULTIME

    private JTextField etaField;
    private JComboBox<String> comboModele1; 
    private JComboBox<String> comboModele2;
    private JCheckBox cbFusion;

    // --- SÉCURITÉ DE HAUT NIVEAU (POISON PILL) ---
    private volatile boolean stopDemande = false;
    private volatile boolean poisonPillActive = false;
    private volatile Thread threadEnCours = null;

    // --- PALETTE DE COULEURS ---
    private final Color BG_COLOR = new Color(30, 30, 30);
    private final Color PANEL_BG = new Color(37, 37, 38);
    private final Color BUTTON_BG = new Color(63, 63, 70);
    private final Color TEXT_COLOR = new Color(240, 240, 240);
    private final Color ACCENT_BLUE = new Color(0, 122, 204);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113); 
    private final Color ERROR_RED = new Color(231, 76, 60);     
    private final Color WARNING_ORANGE = new Color(255, 152, 0);
    private final Color BATCH_PURPLE = new Color(142, 68, 173); 

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
        // GAUCHE : LANCEURS D'ENTRAÎNEMENT
        // =========================================================
        JPanel panelMenu = creerPanneauStyle("1. Entraînements (Création des cerveaux)");
        panelMenu.setLayout(new GridLayout(11, 1, 10, 8)); 
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

        btnEntrainementSequentiel = creerBoutonStyle("🚀 ENTRAÎNEMENT SÉQUENTIEL COMPLET", BATCH_PURPLE);
        btnEntrainementSequentiel.addActionListener(e -> lancerEntrainementSequentiel());
        panelMenu.add(btnEntrainementSequentiel);

        // --- LE BOUTON STOP (KILL SWITCH INFAILLIBLE) ---
        btnStop = creerBoutonStyle("🛑 STOP (ARRÊT IMMÉDIAT)", ERROR_RED);
        btnStop.setEnabled(false); 
        btnStop.addActionListener(e -> {
            if (threadEnCours != null && !stopDemande) {
                stopDemande = true;
                poisonPillActive = true; // Armement du piège dans la console
                threadEnCours.interrupt(); // Coupe les pauses (Sleep) instantanément
                resultLabel.setText("ARRÊT BRUTAL...");
                resultLabel.setForeground(ERROR_RED);
            }
        });
        panelMenu.add(btnStop);

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

        btnTesterImage = creerBoutonStyle("🔍 Tester une Image Ciblée", ACCENT_BLUE);
        btnTesterImage.setPreferredSize(new Dimension(0, 60));
        btnTesterImage.addActionListener(e -> testerImageManuel());
        
        btnTesterAleatoire = creerBoutonStyle("🎲 Lancer Animation (10 Images Aléatoires)", WARNING_ORANGE);
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

        actionButtons[0].addActionListener(e -> lancerTacheSecurisee(() -> MainProjet.main(new String[]{})));
        actionButtons[1].addActionListener(e -> lancerTacheSecurisee(() -> TestMultiClasses.main(new String[]{})));
        actionButtons[2].addActionListener(e -> lancerTacheSecurisee(() -> TestMiroir.main(new String[]{})));
        actionButtons[3].addActionListener(e -> lancerTacheSecurisee(() -> TestRGB.main(new String[]{})));
        actionButtons[4].addActionListener(e -> lancerTacheSecurisee(() -> TestTSL.main(new String[]{}))); 
        actionButtons[5].addActionListener(e -> lancerTacheSecurisee(() -> TestFFT.main(new String[]{})));
        actionButtons[6].addActionListener(e -> lancerTacheSecurisee(() -> TestSansMelange.main(new String[]{})));
        actionButtons[7].addActionListener(e -> lancerTacheSecurisee(() -> TestSansNormalisation.main(new String[]{})));
        actionButtons[8].addActionListener(e -> lancerTacheSecurisee(() -> TestMatriceConfusion.main(new String[]{})));
    }

    // =====================================================================
    // NOYAU DU KILL SWITCH (L'INJECTION D'ERREUR DANS LE SYSTEM.OUT)
    // =====================================================================
    private void redirigerConsole() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) { verifierPoisonPill(); } 
            
            @Override
            public void write(byte[] b, int off, int len) {
                verifierPoisonPill();
                String texte = new String(b, off, len, StandardCharsets.UTF_8);
                SwingUtilities.invokeLater(() -> {
                    consoleArea.append(texte);
                    consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
                });
            }
            
            private void verifierPoisonPill() {
                if (poisonPillActive && Thread.currentThread() == threadEnCours) {
                    throw new RuntimeException("POISON_PILL");
                }
            }
        };
        PrintStream ps = new PrintStream(out, true, StandardCharsets.UTF_8);
        System.setOut(ps); System.setErr(ps);
    }

    // =====================================================================
    // MÉTHODES DE GESTION DU CYCLE DE VIE DES PROCESSUS
    // =====================================================================
    private void setBoutonsActifs(boolean actif) {
        for (JButton btn : actionButtons) btn.setEnabled(actif);
        btnEntrainementSequentiel.setEnabled(actif);
        btnTesterImage.setEnabled(actif);
        btnTesterAleatoire.setEnabled(actif);
        btnStop.setEnabled(!actif); // STOP n'est cliquable QUE quand ça calcule
    }

    private void preparerAvantTache() {
        setBoutonsActifs(false); 
        stopDemande = false; 
        poisonPillActive = false;
        resultLabel.setText("CALCULS EN COURS...");
        resultLabel.setForeground(WARNING_ORANGE);
    }
    
    private void gererArretBrutal() {
        poisonPillActive = false; // On désarme le piège pour pouvoir afficher le texte rouge
        System.out.println("\n=======================================================");
        System.out.println("🛑 PROCESSUS FOUDROYÉ SUR COMMANDE !");
        System.out.println("=======================================================\n");
    }

    private void nettoyerApresTache() {
        SwingUtilities.invokeLater(() -> {
            setBoutonsActifs(true);
            if (stopDemande) {
                resultLabel.setText("STOPPÉ");
                resultLabel.setForeground(ERROR_RED);
            } else {
                resultLabel.setText("PRÊT");
                resultLabel.setForeground(SUCCESS_GREEN);
            }
            stopDemande = false;
            threadEnCours = null;
        });
    }

    // =====================================================================
    // LANCEURS GLOBAUX SÉCURISÉS
    // =====================================================================
    private void lancerTacheSecurisee(Runnable tâche) {
        preparerAvantTache();
        threadEnCours = new Thread(() -> {
            try { 
                tâche.run(); 
            } catch (RuntimeException re) {
                if ("POISON_PILL".equals(re.getMessage())) gererArretBrutal();
                else System.out.println("Erreur : " + re.getMessage());
            } catch (Exception ex) { 
                System.out.println("Erreur : " + ex.getMessage()); 
            } finally {
                nettoyerApresTache();
            }
        });
        threadEnCours.start();
    }

    private void lancerEntrainementSequentiel() {
        preparerAvantTache();
        resultLabel.setText("BATCH EN COURS...");
        resultLabel.setForeground(BATCH_PURPLE);
        
        threadEnCours = new Thread(() -> {
            try {
                System.out.println("\n=======================================================");
                System.out.println("🚀 DÉMARRAGE DE L'ENTRAÎNEMENT SÉQUENTIEL (Batch Mode)");
                System.out.println("=======================================================\n");

                MainProjet.main(new String[]{}); System.gc(); Thread.sleep(1000);
                TestMultiClasses.main(new String[]{}); System.gc(); Thread.sleep(1000);
                TestMiroir.main(new String[]{}); System.gc(); Thread.sleep(1000);
                TestRGB.main(new String[]{}); System.gc(); Thread.sleep(1000);
                TestTSL.main(new String[]{}); System.gc(); Thread.sleep(1000);
                TestFFT.main(new String[]{}); System.gc(); Thread.sleep(1000);
                TestSansMelange.main(new String[]{}); System.gc(); Thread.sleep(1000);
                TestSansNormalisation.main(new String[]{});

                System.out.println("\n=======================================================");
                System.out.println("✅ ENTRAÎNEMENT COMPLET TERMINÉ !");
                System.out.println("=======================================================\n");

            } catch (RuntimeException re) {
                if ("POISON_PILL".equals(re.getMessage())) gererArretBrutal();
                else System.out.println("Erreur : " + re.getMessage());
            } catch (InterruptedException ie) {
                gererArretBrutal();
            } catch (Exception ex) {
                System.out.println("❌ Erreur : " + ex.getMessage());
            } finally {
                nettoyerApresTache();
            }
        });
        threadEnCours.start();
    }

    private void lancerTestAleatoireAnime() {
        List<String> fichiersTest = Image.listeFichiers("dataset_animaux/test/");
        if (fichiersTest == null || fichiersTest.isEmpty()) {
            System.out.println("❌ Erreur : Dossier de test introuvable.");
            return;
        }
        Collections.shuffle(fichiersTest);
        int nbImages = Math.min(10, fichiersTest.size());

        preparerAvantTache();

        threadEnCours = new Thread(() -> {
            int scoreTotal = 0;
            int imagesTestees = 0;
            try {
                System.out.println("\n🚀 DÉMARRAGE DU TEST EN RAFALE (10 IMAGES) ...");

                for (int i = 0; i < nbImages; i++) {
                    String cheminAbsolu = fichiersTest.get(i).toLowerCase();
                    File f = new File(fichiersTest.get(i));
                    
                    try {
                        final BufferedImage bimg = ImageIO.read(f);
                        if (bimg == null) continue;
                        
                        SwingUtilities.invokeLater(() -> {
                            java.awt.Image scaled = bimg.getScaledInstance(350, 350, java.awt.Image.SCALE_SMOOTH);
                            imageLabel.setIcon(new ImageIcon(scaled));
                            imageLabel.setText("");
                        });

                        int prediction = executerInference(f, cbFusion.isSelected());
                        boolean isMulti = comboModele1.getSelectedItem().toString().contains("Multi-Classes");
                        int vraiLabel = isMulti ? 
                            (cheminAbsolu.contains("cat") ? 0 : (cheminAbsolu.contains("dog") ? 1 : 2)) : 
                            (cheminAbsolu.contains("cat") ? 0 : 1);

                        if (prediction == vraiLabel) scoreTotal++;
                        imagesTestees++;

                    } catch (Exception errImage) {}

                    Thread.sleep(1200); 
                }
                System.out.println("\n🏆 FIN DU TEST ALÉATOIRE ! Score : " + scoreTotal + " / " + nbImages);
                
            } catch (RuntimeException re) {
                if ("POISON_PILL".equals(re.getMessage())) gererArretBrutal();
                else System.out.println("Erreur : " + re.getMessage());
            } catch (InterruptedException ie) {
                gererArretBrutal();
            } catch (Exception ex) {
                System.out.println("Erreur durant l'animation : " + ex.getMessage());
            } finally {
                final int fScore = scoreTotal;
                final int fTotal = imagesTestees;
                SwingUtilities.invokeLater(() -> {
                    imageLabel.setIcon(null); 
                    if (stopDemande || fTotal == 0) {
                        imageLabel.setText("<html><div style='text-align: center;'><h1 style='color:#E74C3C;'>🛑 ANIMATION FOUDROYÉE</h1></div></html>");
                    } else {
                        String couleurScore = (fScore >= 6) ? "#4CAF50" : (fScore < 5 ? "#E74C3C" : "#FF9800"); 
                        String htmlTrophy = "<html><div style='text-align: center; font-family: Segoe UI;'>"
                                + "<h1 style='color: white; font-size: 36px; margin-bottom: 5px;'>🏆 TEST TERMINÉ</h1>"
                                + "<h2 style='color: " + couleurScore + "; font-size: 50px; margin-top: 0px;'>" + (int)(((float)fScore/fTotal)*100) + " %</h2>"
                                + "<p style='color: gray; font-size: 16px;'>" + fScore + " bonnes réponses sur " + fTotal + " images</p>"
                                + "</div></html>";
                        imageLabel.setText(htmlTrophy);
                    }
                });
                nettoyerApresTache();
            }
        });
        threadEnCours.start();
    }

    private void testerImageManuel() {
        JFileChooser fc = new JFileChooser("dataset_animaux/test/");
        fc.setDialogTitle("Sélectionnez l'image à tester");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        
        File fichierImage = fc.getSelectedFile();
        preparerAvantTache();

        threadEnCours = new Thread(() -> {
            try {
                BufferedImage bimg = ImageIO.read(fichierImage);
                SwingUtilities.invokeLater(() -> {
                    java.awt.Image scaledImg = bimg.getScaledInstance(350, 350, java.awt.Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImg));
                    imageLabel.setText("");
                });
                executerInference(fichierImage, cbFusion.isSelected());
            } catch (RuntimeException re) {
                if ("POISON_PILL".equals(re.getMessage())) gererArretBrutal();
            } catch (Exception ex) {
                System.out.println("Erreur de lecture de l'image.");
            } finally {
                nettoyerApresTache();
            }
        });
        threadEnCours.start();
    }

    // =====================================================================
    // MOTEUR D'INFÉRENCE INCHANGÉ
    // =====================================================================
    private int executerInference(File fichierImage, boolean avecFusion) {
        try {
            System.out.println("\n--- ANALYSE DE : " + fichierImage.getName() + " ---");
            String mod1 = (String) comboModele1.getSelectedItem();
            boolean isMulti = mod1.contains("Multi-Classes");
            
            if (isMulti && avecFusion) {
                System.out.println("ℹ️ Fusion ignorée mathématiquement pour le modèle Multi-Classes.");
                avecFusion = false; 
            }

            String cheminAbsolu = fichierImage.getAbsolutePath().toLowerCase();
            int vraiLabel = -1; 
            if (cheminAbsolu.contains("cat")) vraiLabel = 0;
            else if (cheminAbsolu.contains("dog")) vraiLabel = 1;
            else if (cheminAbsolu.contains("wild")) vraiLabel = 2;

            int prediction = -1;
            String texteAffiche = "";

            if (isMulti) {
                float[] pixels = new Image(fichierImage.getAbsolutePath(), Image.LabelInconnu, true).donneesNormalisees();
                iNeurone nChat = new NeuroneSigmoide(pixels.length); nChat.chargement("cerveau_chat.txt");
                iNeurone nChien = new NeuroneSigmoide(pixels.length); nChien.chargement("cerveau_chien.txt");
                iNeurone nWild = new NeuroneSigmoide(pixels.length); nWild.chargement("cerveau_wild.txt");
                nChat.metAJour(pixels); nChien.metAJour(pixels); nWild.metAJour(pixels);
                float pChat = nChat.sortie(), pChien = nChien.sortie(), pWild = nWild.sortie();
                
                if (pChat > pChien && pChat > pWild) {
                    texteAffiche = String.format("CHAT 🐱 (%.1f%%)", pChat*100); prediction = 0;
                } else if (pChien > pChat && pChien > pWild) {
                    texteAffiche = String.format("CHIEN 🐶 (%.1f%%)", pChien*100); prediction = 1;
                } else {
                    texteAffiche = String.format("WILD 🦁 (%.1f%%)", pWild*100); prediction = 2;
                }
            } else {
                float proba1 = obtenirProbaModele(mod1, fichierImage);
                float probaFinale = proba1;

                if (avecFusion) {
                    String mod2 = (String) comboModele2.getSelectedItem();
                    float proba2 = obtenirProbaModele(mod2, fichierImage);
                    probaFinale = (proba1 + proba2) / 2.0f;
                    System.out.printf("🧠 FUSION DES CERVEAUX : %.2f%%\n", probaFinale * 100);
                }

                if (probaFinale >= 0.5f) {
                    texteAffiche = String.format(avecFusion ? "CHAT 🐱 (Fusion: %.1f%%)" : "CHAT 🐱 (%.1f%%)", probaFinale * 100);
                    prediction = 0;
                } else {
                    texteAffiche = String.format(avecFusion ? "CHIEN/WILD 🐶 (Fusion: %.1f%%)" : "CHIEN/WILD 🐶 (%.1f%%)", probaFinale * 100);
                    prediction = 1; 
                }
            }

            Color couleurResultat = ACCENT_BLUE; 

            if (vraiLabel != -1) {
                int vraiLabelAComparer = vraiLabel;
                if (!isMulti && vraiLabel == 2) vraiLabelAComparer = 1; 

                if (prediction == vraiLabelAComparer) {
                    couleurResultat = SUCCESS_GREEN; 
                } else {
                    couleurResultat = ERROR_RED;     
                }
            }

            majUIResultat(texteAffiche, couleurResultat);
            return prediction;

        } catch (Exception ex) {
            System.out.println("❌ Erreur : " + ex.getMessage());
            majUIResultat("ERREUR D'ANALYSE", ERROR_RED);
            return -1;
        }
    }

    private void majUIResultat(String texte, Color couleur) {
        SwingUtilities.invokeLater(() -> {
            resultLabel.setText(texte);
            resultLabel.setForeground(couleur);
        });
    }

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
        else if (modeleStr.contains("TSL")) { 
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

    public static void main(String[] args) {
        try { 
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); 
        } catch (Exception e) {}
        
        SwingUtilities.invokeLater(() -> new MenuGraphique().setVisible(true));
    }
}
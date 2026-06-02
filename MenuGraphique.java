import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class MenuGraphique extends JFrame {

    private JTextArea consoleArea;
    private JLabel imageLabel;
    private JLabel resultLabel;
    private JButton[] menuButtons;

    public MenuGraphique() {
        setTitle("🧠 Dashboard IA - Détection Animaux (Avec FFT)");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- PANNEAU DE GAUCHE : LE MENU ---
        JPanel panelMenu = new JPanel();
        panelMenu.setLayout(new GridLayout(8, 1, 10, 10)); // Modifié pour 8 emplacements
        panelMenu.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelMenu.setPreferredSize(new Dimension(280, 0));

        JButton btnBinaire = new JButton("1. Apprentissage Binaire");
        JButton btnMulti = new JButton("2. Multi-Classes (Chat/Chien/Wild)");
        JButton btnMiroir = new JButton("3. Data Augmentation (Miroir)");
        JButton btnCouleur = new JButton("4. Apprentissage RGB");
        JButton btnMatrice = new JButton("5. Matrice de Confusion");
        JButton btnFFT = new JButton("6. Apprentissage par FFT"); // NOUVEAU BOUTON
        JButton btnTesterImage = new JButton("🔍 Tester une image au hasard");
        JButton btnTesterImageManuelle = new JButton("📁 Choisir une image à tester");

        btnTesterImageManuelle.setBackground(new Color(0, 102, 204));
        btnTesterImageManuelle.setForeground(Color.WHITE);
        btnTesterImageManuelle.setFont(new Font("Arial", Font.BOLD, 13));

        // Ajout du bouton FFT dans le tableau de verrouillage
        menuButtons = new JButton[]{btnBinaire, btnMulti, btnMiroir, btnCouleur, btnMatrice, btnFFT, btnTesterImage, btnTesterImageManuelle};

        for (JButton btn : menuButtons) {
            panelMenu.add(btn);
        }
        add(panelMenu, BorderLayout.WEST);

        // --- PANNEAU CENTRAL : LE VISUALISEUR ---
        JPanel panelCentre = new JPanel(new BorderLayout());
        panelCentre.setBorder(BorderFactory.createTitledBorder("Visualiseur d'Analyse"));
        panelCentre.setBackground(Color.DARK_GRAY);

        imageLabel = new JLabel("Aucune image sélectionnée", SwingConstants.CENTER);
        imageLabel.setForeground(Color.LIGHT_GRAY);
        panelCentre.add(imageLabel, BorderLayout.CENTER);

        resultLabel = new JLabel("EN ATTENTE", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 24));
        resultLabel.setForeground(Color.WHITE);
        resultLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panelCentre.add(resultLabel, BorderLayout.SOUTH);

        add(panelCentre, BorderLayout.CENTER);

        // --- PANNEAU DU BAS : LA CONSOLE ---
        consoleArea = new JTextArea(10, 50);
        consoleArea.setEditable(false);
        consoleArea.setBackground(Color.BLACK);
        consoleArea.setForeground(new Color(0, 255, 0));
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(consoleArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Logs d'exécution"));
        add(scrollPane, BorderLayout.SOUTH);

        redirigerConsole();

        // --- ACTIONS DES BOUTONS ---
        btnBinaire.addActionListener(e -> lancerTacheSecurisee(() -> MainProjet.main(new String[]{})));
        btnMulti.addActionListener(e -> lancerTacheSecurisee(() -> TestMultiClasses.main(new String[]{})));
        btnMiroir.addActionListener(e -> lancerTacheSecurisee(() -> TestMiroir.main(new String[]{})));
        btnCouleur.addActionListener(e -> lancerTacheSecurisee(() -> TestRGB.main(new String[]{})));
        btnMatrice.addActionListener(e -> lancerTacheSecurisee(() -> TestMatriceConfusion.main(new String[]{})));
        btnFFT.addActionListener(e -> lancerTacheSecurisee(() -> TestFFT.main(new String[]{}))); // NOUVELLE ACTION
        
        btnTesterImageManuelle.addActionListener(e -> testerImageManuelle());
    }

    private void lancerTacheSecurisee(Runnable tâche) {
        verrouillerMenu(false);
        consoleArea.setText(""); 
        resultLabel.setText("APPRENTISSAGE EN COURS...");
        resultLabel.setForeground(Color.ORANGE);
        imageLabel.setIcon(null);
        imageLabel.setText("Calculs mathématiques en cours...");

        new Thread(() -> {
            try {
                tâche.run();
            } catch (Exception ex) {
                System.out.println("Erreur durant l'exécution : " + ex.getMessage());
            } finally {
                SwingUtilities.invokeLater(() -> {
                    verrouillerMenu(true);
                    resultLabel.setText("TERMINÉ");
                    resultLabel.setForeground(Color.GREEN);
                    imageLabel.setText("Prêt pour une nouvelle action.");
                });
            }
        }).start();
    }

    private void verrouillerMenu(boolean actif) {
        for (JButton btn : menuButtons) {
            btn.setEnabled(actif);
        }
    }

    private void testerImageManuelle() {
        JFileChooser fileChooser = new JFileChooser("dataset_animaux/test/");
        fileChooser.setDialogTitle("Sélectionnez une image à analyser");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fichierChoisi = fileChooser.getSelectedFile();
            String chemin = fichierChoisi.getAbsolutePath();
            
            try {
                BufferedImage bimg = ImageIO.read(fichierChoisi);
                java.awt.Image scaledImg = bimg.getScaledInstance(300, 300, java.awt.Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImg));
                imageLabel.setText(""); 
            } catch (IOException ex) {
                System.out.println("Erreur de lecture de l'image.");
            }

            System.out.println("\n--- DÉMARRAGE DE L'ANALYSE ---");
            System.out.println("Image : " + fichierChoisi.getName());
            
            Image imgTest = new Image(chemin, Image.LabelInconnu, true);
            if (imgTest.donnees() == null) {
                resultLabel.setText("ERREUR : IMAGE CORROMPUE");
                resultLabel.setForeground(Color.RED);
                return;
            }

            float[] pixelsNormalises = imgTest.donneesNormalisees();
            iNeurone monIA = new NeuroneSigmoide(pixelsNormalises.length);
            
            File fichierCerveau = new File("cerveau_binaire.txt");
            if (!fichierCerveau.exists()) {
                resultLabel.setText("ERREUR : AUCUN CERVEAU SAUVEGARDÉ");
                resultLabel.setForeground(Color.RED);
                System.out.println("Veuillez d'abord lancer l'Apprentissage Binaire !");
                return;
            }

            monIA.chargement("cerveau_binaire.txt");
            monIA.metAJour(pixelsNormalises);
            float proba = monIA.sortie();

            if (proba >= 0.5f) {
                resultLabel.setText(String.format("C'EST UN CHAT 🐱 (Confiance : %.1f %%)", proba * 100));
                resultLabel.setForeground(new Color(50, 205, 50)); 
            } else {
                resultLabel.setText(String.format("C'EST UN CHIEN/WILD 🐶 (Score : %.1f %%)", proba * 100));
                resultLabel.setForeground(new Color(255, 69, 0)); 
            }
        }
    }

    private void redirigerConsole() {
        PrintStream printStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                consoleArea.append(String.valueOf((char) b));
                consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
            }
        });
        System.setOut(printStream);
        System.setErr(printStream);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> {
            new MenuGraphique().setVisible(true);
            System.out.println("✅ Interface chargée avec succès. Prête pour l'évaluation.");
        });
    }
}
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Random;

public class NonogramGame extends JFrame {
    private Nonogram nonogram;
    private JButton[][] gridButtons;
    private JLabel statusLabel;
    private JLabel titleLabel;
    private Random random = new Random();
    private int currentSize = 5;
    
    // Variables pour la visualisation des stratégies
    private JButton strategyButton;
    private JComboBox<String> strategyComboBox;
    private Timer visualizationTimer;
    private SolverStrategy currentStrategy;
    private Nonogram visualizationPuzzle;
    private boolean isVisualizing = false;
    private int visualizationSpeed = 800;
    
    private final int[] AVAILABLE_SIZES = {3, 4, 5};

    // 🎨 Palette de couleurs moderne
    private final Color BACKGROUND_COLOR = new Color(18, 18, 18); // Noir profond
    private final Color PANEL_COLOR = new Color(30, 30, 30); // Gris foncé
    private final Color ACCENT_COLOR = new Color(0, 150, 255); // Bleu moderne
    private final Color ACCENT_HOVER = new Color(0, 180, 255); // Bleu clair au survol
    private final Color GRID_COLOR = new Color(45, 45, 45);
    private final Color FILLED_COLOR = new Color(0, 150, 255); // Bleu pour les cases remplies
    private final Color CROSSED_COLOR = new Color(255, 80, 80); // Rouge pour les croix
    private final Color SUCCESS_COLOR = new Color(46, 204, 113); // Vert succès

    public NonogramGame() {
        generateNewPuzzle();
        initializeUI();
    }

    /**
     * 🎲 Génère un puzzle avec taille aléatoire et solution unique GARANTIE
     */
    private void generateNewPuzzle() {
        // 🎲 Choisir une taille aléatoire
        currentSize = AVAILABLE_SIZES[random.nextInt(AVAILABLE_SIZES.length)];
        
        if (statusLabel != null) {
            statusLabel.setText("⏳ Génération d'un puzzle " + currentSize + "×" + currentSize + "...");
        }
        
        System.out.println("🎲 Nouvelle taille aléatoire : " + currentSize + "×" + currentSize);
        
        int maxAttempts = 100;
        int attempt = 0;
        
        while (attempt < maxAttempts) {
            attempt++;
            
            CellState[][] solution = generateRandomSolution(currentSize);
            int[][] rowClues = calculateRowClues(solution);
            int[][] colClues = calculateColClues(solution);
            
            LineClues clues = new LineClues(rowClues, colClues);
            
            if (PuzzleValidator.hasUniqueSolution(clues, currentSize, currentSize)) {
                this.nonogram = new Nonogram(currentSize, currentSize, clues, solution);
                System.out.println("✅ Puzzle " + currentSize + "×" + currentSize + " valide généré en " + attempt + " tentative(s)");
                return;
            }
            
            System.out.println("⚠️ Tentative " + attempt + " : Puzzle rejeté (solutions multiples)");
        }
        
        System.out.println("⚠️ Échec après " + maxAttempts + " tentatives, utilisation d'un puzzle prédéfini");
        this.nonogram = NonogramFactory.createSimple5x5();
        this.currentSize = 5;
    }

    /**
     * Génère une solution aléatoire avec densité contrôlée
     */
    private CellState[][] generateRandomSolution(int size) {
        CellState[][] solution = new CellState[size][size];
        
        // Densité variable (30-50% de cases remplies pour des puzzles intéressants)
        double density = 0.3 + random.nextDouble() * 0.2;
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                solution[i][j] = random.nextDouble() < density ? 
                    CellState.FILLED : CellState.EMPTY;
            }
        }
        
        // S'assurer qu'il y a au moins une case remplie par ligne/colonne
        ensureValidPuzzle(solution);
        
        return solution;
    }

    private void ensureValidPuzzle(CellState[][] solution) {
        int size = solution.length;
        
        // Vérifier chaque ligne
        for (int i = 0; i < size; i++) {
            boolean hasFilled = false;
            for (int j = 0; j < size; j++) {
                if (solution[i][j] == CellState.FILLED) {
                    hasFilled = true;
                    break;
                }
            }
            if (!hasFilled) {
                solution[i][random.nextInt(size)] = CellState.FILLED;
            }
        }
        
        // Vérifier chaque colonne
        for (int j = 0; j < size; j++) {
            boolean hasFilled = false;
            for (int i = 0; i < size; i++) {
                if (solution[i][j] == CellState.FILLED) {
                    hasFilled = true;
                    break;
                }
            }
            if (!hasFilled && random.nextBoolean()) {
                solution[random.nextInt(size)][j] = CellState.FILLED;
            }
        }
    }

    private int[][] calculateRowClues(CellState[][] solution) {
        int size = solution.length;
        int[][] rowClues = new int[size][];

        for (int i = 0; i < size; i++) {
            java.util.List<Integer> clues = new java.util.ArrayList<>();
            int count = 0;

            for (int j = 0; j < size; j++) {
                if (solution[i][j] == CellState.FILLED) {
                    count++;
                } else {
                    if (count > 0) {
                        clues.add(count);
                        count = 0;
                    }
                }
            }

            if (count > 0) {
                clues.add(count);
            }

            // Ne pas ajouter 0 : utiliser un tableau vide si aucun indice
            if (clues.isEmpty()) {
                rowClues[i] = new int[0];
            } else {
                rowClues[i] = clues.stream().mapToInt(Integer::intValue).toArray();
            }
        }

        return rowClues;
    }

    private int[][] calculateColClues(CellState[][] solution) {
        int size = solution.length;
        int[][] colClues = new int[size][];

        for (int j = 0; j < size; j++) {
            java.util.List<Integer> clues = new java.util.ArrayList<>();
            int count = 0;

            for (int i = 0; i < size; i++) {
                if (solution[i][j] == CellState.FILLED) {
                    count++;
                } else {
                    if (count > 0) {
                        clues.add(count);
                        count = 0;
                    }
                }
            }

            if (count > 0) {
                clues.add(count);
            }

            // Ne pas ajouter 0 : utiliser un tableau vide si aucun indice
            if (clues.isEmpty()) {
                colClues[j] = new int[0];
            } else {
                colClues[j] = clues.stream().mapToInt(Integer::intValue).toArray();
            }
        }

        return colClues;
    }

    private void initializeUI() {
        setTitle("🎯 Nonogram - Solution Unique Garantie");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Panel principal avec padding
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBackground(BACKGROUND_COLOR);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header avec titre élégant
        mainContainer.add(createHeaderPanel(), BorderLayout.NORTH);

        // Panel central avec la grille
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.add(createColumnCluesPanel(), BorderLayout.NORTH);

        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.setBackground(BACKGROUND_COLOR);
        gridContainer.add(createRowCluesPanel(), BorderLayout.WEST);
        gridContainer.add(createGridPanel(), BorderLayout.CENTER);
        centerPanel.add(gridContainer, BorderLayout.CENTER);

        mainContainer.add(centerPanel, BorderLayout.CENTER);
        mainContainer.add(createControlPanel(), BorderLayout.SOUTH);

        add(mainContainer);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * 🎨 Header moderne avec titre et infos
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        titleLabel = new JLabel("🎯 NONOGRAM PUZZLE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(ACCENT_COLOR);

        JLabel subtitleLabel = new JLabel(currentSize + "×" + currentSize + " Grid • Solution Unique", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(150, 150, 150));

        JPanel titleContainer = new JPanel(new GridLayout(2, 1, 0, 5));
        titleContainer.setBackground(PANEL_COLOR);
        titleContainer.add(titleLabel);
        titleContainer.add(subtitleLabel);

        panel.add(titleContainer, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createColumnCluesPanel() {
        int[][] colClues = nonogram.getClues().getColClues();
        int maxClueHeight = getMaxClueLength(colClues);
        
        JPanel panel = new JPanel(new GridLayout(maxClueHeight, nonogram.getWidth(), 2, 2));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 10));
        panel.setBackground(BACKGROUND_COLOR);

        for (int clueRow = 0; clueRow < maxClueHeight; clueRow++) {
            for (int col = 0; col < nonogram.getWidth(); col++) {
                JLabel label = new JLabel("", SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 14));
                label.setForeground(ACCENT_COLOR);
                label.setOpaque(true);
                label.setBackground(PANEL_COLOR);
                label.setBorder(BorderFactory.createLineBorder(GRID_COLOR, 1));
                
                int clueIndex = colClues[col].length - maxClueHeight + clueRow;
                if (clueIndex >= 0) {
                    label.setText(String.valueOf(colClues[col][clueIndex]));
                }
                
                panel.add(label);
            }
        }

        return panel;
    }

    private JPanel createRowCluesPanel() {
        int[][] rowClues = nonogram.getClues().getRowClues();
        
        JPanel panel = new JPanel(new GridLayout(nonogram.getHeight(), 1, 2, 2));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        panel.setBackground(BACKGROUND_COLOR);

        for (int row = 0; row < nonogram.getHeight(); row++) {
            JPanel cluePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
            cluePanel.setBackground(PANEL_COLOR);
            cluePanel.setBorder(BorderFactory.createLineBorder(GRID_COLOR, 1));

            for (int clue : rowClues[row]) {
                JLabel label = new JLabel(String.valueOf(clue));
                label.setFont(new Font("Arial", Font.BOLD, 14));
                label.setForeground(CROSSED_COLOR);
                label.setPreferredSize(new Dimension(20, 25));
                cluePanel.add(label);
            }

            panel.add(cluePanel);
        }

        return panel;
    }

    private JPanel createGridPanel() {
        JPanel panel = new JPanel(new GridLayout(nonogram.getHeight(), nonogram.getWidth(), 2, 2));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 3),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panel.setBackground(GRID_COLOR);

        gridButtons = new JButton[nonogram.getHeight()][nonogram.getWidth()];

        for (int row = 0; row < nonogram.getHeight(); row++) {
            for (int col = 0; col < nonogram.getWidth(); col++) {
                JButton button = createGridButton(row, col);
                gridButtons[row][col] = button;
                panel.add(button);
            }
        }

        return panel;
    }

    private JButton createGridButton(int row, int col) {
        JButton button = new JButton();
        
        int buttonSize = currentSize <= 5 ? 45 : 
                        currentSize <= 7 ? 40 : 35;
        
        button.setPreferredSize(new Dimension(buttonSize, buttonSize));
        button.setBackground(new Color(40, 40, 40));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRID_COLOR, 1),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        final int r = row, c = col;

        // Effet hover moderne
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (nonogram.getCell(r, c) == CellState.EMPTY) {
                    button.setBackground(new Color(55, 55, 55));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateButtonDisplay(button, r, c);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    handleCellClick(r, c, true);
                }
            }
        });

        button.addActionListener(e -> handleCellClick(r, c, false));

        return button;
    }

    /**
     * Met à jour l'apparence d'un bouton selon son état
     */
    private void updateButtonDisplay(JButton button, int row, int col) {
        switch (nonogram.getCell(row, col)) {
            case EMPTY:
                button.setBackground(new Color(40, 40, 40));
                button.setText("");
                break;
            case FILLED:
                button.setBackground(FILLED_COLOR);
                button.setText("");
                break;
            case CROSSED:
                button.setBackground(new Color(40, 40, 40));
                button.setText("×");
                button.setForeground(CROSSED_COLOR);
                break;
        }
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Status en haut
        statusLabel = new JLabel("🎲 Taille : " + currentSize + "×" + currentSize + " | Clic gauche: ■ | Clic droit: ×", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(new Color(200, 200, 200));

        // Panel pour les contrôles de stratégie (NOUVEAU)
        JPanel strategyPanel = createStrategyPanel();

        // Boutons en bas (existants)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(PANEL_COLOR);

        JButton resetButton = createModernButton("🎲 Nouveau Puzzle", ACCENT_COLOR);
        resetButton.addActionListener(e -> resetGame());

        JButton solveButton = createModernButton("💡 Aide", new Color(255, 193, 7));
        solveButton.addActionListener(e -> showHint());

        JButton quitButton = createModernButton("🚪 Quitter", CROSSED_COLOR);
        quitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(resetButton);
        buttonPanel.add(solveButton);
        buttonPanel.add(quitButton);

        // Assemblage
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PANEL_COLOR);
        topPanel.add(statusLabel, BorderLayout.NORTH);
        topPanel.add(strategyPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
    private JPanel createStrategyPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(PANEL_COLOR);
        
        strategyComboBox = new JComboBox<>(new String[]{
        	    "Simple Line Solver", 
        	    "Backtracking Solver",
        	    "LogicStrategy",
        	    "RandomStrategy"
        	});

        strategyComboBox.setBackground(PANEL_COLOR);
        strategyComboBox.setForeground(Color.WHITE);
        strategyComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        
        strategyButton = createModernButton("👁️ Voir la Stratégie", new Color(46, 204, 113));
        strategyButton.addActionListener(e -> toggleStrategyVisualization());
        
        JButton speedButton = createModernButton("⚡ Vitesse", new Color(255, 193, 7));
        speedButton.addActionListener(e -> changeVisualizationSpeed());
        
        panel.add(new JLabel("Stratégie:"));
        panel.add(strategyComboBox);
        panel.add(strategyButton);
        panel.add(speedButton);
        
        return panel;
    }
        //Méthodes de visualisation 
    private void toggleStrategyVisualization() {
        if (isVisualizing) {
            stopStrategyVisualization();
        } else {
            startStrategyVisualization();
        }
    }

    private void startStrategyVisualization() {
        String selectedStrategy = (String) strategyComboBox.getSelectedItem();
        
        if ("Simple Line Solver".equals(selectedStrategy)) {
            currentStrategy = new SimpleLineSolver();
        } 
        else if ("Backtracking Solver".equals(selectedStrategy)) {
            currentStrategy = new BacktrackingSolver();
        }
        else if ("LogicStrategy".equals(selectedStrategy)) {
            currentStrategy = new LogicStrategy();   // <-- AJOUT ICI
        }
        else if ("RandomStrategy".equals(selectedStrategy)) { // <-- CORRIGÉ
            currentStrategy = new RandomStrategy(2000);
        }

        currentStrategy.setStepByStepMode(true);
        currentStrategy.resetStatistics();
        
        visualizationPuzzle = copyPuzzle(nonogram);
        
        isVisualizing = true;
        strategyButton.setText("⏹️ Arrêter");
        strategyButton.setBackground(CROSSED_COLOR);
        
        statusLabel.setText("👁️ Visualisation : " + selectedStrategy + " - Étape 0");
        statusLabel.setForeground(new Color(255, 193, 7));
        
        startVisualizationTimer();
    }


    private void startVisualizationTimer() {
        visualizationTimer = new Timer(visualizationSpeed, e -> {
            if (!isVisualizing || !currentStrategy.hasNextStep()) {
                stopStrategyVisualization();
                if (visualizationPuzzle.isSolved()) {
                    statusLabel.setText("✅ Stratégie terminée - Puzzle résolu !");
                    statusLabel.setForeground(SUCCESS_COLOR);
                }
                return;
            }
            
            currentStrategy.executeNextStep(visualizationPuzzle);
            updateGridFromVisualizationPuzzle();
            
            int step = currentStrategy.getCurrentStep();
            statusLabel.setText("👁️ " + currentStrategy.getName() + " - Étape " + step);
        });
        
        visualizationTimer.start();
    }

    private void stopStrategyVisualization() {
        if (visualizationTimer != null) {
            visualizationTimer.stop();
        }
        isVisualizing = false;
        strategyButton.setText("👁️ Voir la Stratégie");
        strategyButton.setBackground(new Color(46, 204, 113));
        updateGridDisplay();
    }

    private void updateGridFromVisualizationPuzzle() {
        for (int row = 0; row < nonogram.getHeight(); row++) {
            for (int col = 0; col < nonogram.getWidth(); col++) {
                CellState state = visualizationPuzzle.getCell(row, col);
                updateButtonDisplay(gridButtons[row][col], state);
            }
        }
    }

    private void updateButtonDisplay(JButton button, CellState state) {
        switch (state) {
            case EMPTY:
                button.setBackground(new Color(40, 40, 40));
                button.setText("");
                break;
            case FILLED:
                button.setBackground(FILLED_COLOR);
                button.setText("");
                break;
            case CROSSED:
                button.setBackground(new Color(40, 40, 40));
                button.setText("×");
                button.setForeground(CROSSED_COLOR);
                break;
        }
    }

        private int countFilledCells(Nonogram puzzle) {
            int count = 0;
            for (int row = 0; row < puzzle.getHeight(); row++) {
                for (int col = 0; col < puzzle.getWidth(); col++) {
                    if (puzzle.getCell(row, col) == CellState.FILLED) {
                        count++;
                    }
                }
            }
            return count;
        }

        private Nonogram copyPuzzle(Nonogram original) {
            return new Nonogram(
                original.getWidth(),
                original.getHeight(), 
                original.getClues(),
                original.getSolution()
            );
        }

        private void changeVisualizationSpeed() {
            String[] speeds = {"Lent", "Normal", "Rapide", "Très Rapide"};
            int[] delays = {1500, 800, 300, 100};
            
            String currentSpeed = "Normal";
            if (visualizationSpeed == 1500) currentSpeed = "Lent";
            else if (visualizationSpeed == 800) currentSpeed = "Normal"; 
            else if (visualizationSpeed == 300) currentSpeed = "Rapide";
            else if (visualizationSpeed == 100) currentSpeed = "Très Rapide";
            
            String newSpeed = (String) JOptionPane.showInputDialog(this,
                "Choisissez la vitesse de visualisation:",
                "Vitesse de Visualisation",
                JOptionPane.QUESTION_MESSAGE,
                null,
                speeds,
                currentSpeed);
            
            if (newSpeed != null) {
                for (int i = 0; i < speeds.length; i++) {
                    if (speeds[i].equals(newSpeed)) {
                        visualizationSpeed = delays[i];
                        if (visualizationTimer != null) {
                            visualizationTimer.setDelay(visualizationSpeed);
                        }
                        break;
                    }
                }
            }
        }

    /**
     * 🎨 Crée un bouton moderne avec effets
     */
    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(160, 40));
        
        // Effet hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void handleCellClick(int row, int col, boolean isRightClick) {
        if (isRightClick) {
            if (nonogram.getCell(row, col) == CellState.CROSSED) {
                nonogram.setCell(row, col, CellState.EMPTY);
            } else {
                nonogram.setCell(row, col, CellState.CROSSED);
            }
        } else {
            if (nonogram.getCell(row, col) == CellState.FILLED) {
                nonogram.setCell(row, col, CellState.EMPTY);
            } else {
                nonogram.setCell(row, col, CellState.FILLED);
            }
        }

        updateGridDisplay();

        if (nonogram.isSolved()) {
            showVictory();
        }
    }

    private void updateGridDisplay() {
        for (int row = 0; row < nonogram.getHeight(); row++) {
            for (int col = 0; col < nonogram.getWidth(); col++) {
                updateButtonDisplay(gridButtons[row][col], row, col);
            }
        }
    }

    private void resetGame() {
        generateNewPuzzle(); // 🎲 Génère un nouveau puzzle avec taille aléatoire
        getContentPane().removeAll();
        initializeUI();
        revalidate();
        repaint();
        statusLabel.setText("🎲 Nouveau puzzle " + currentSize + "×" + currentSize + " généré !");
    }

    private void showHint() {
        for (int row = 0; row < nonogram.getHeight(); row++) {
            for (int col = 0; col < nonogram.getWidth(); col++) {
                final int finalRow = row;
                final int finalCol = col;
                
                if (nonogram.getCell(row, col) == CellState.EMPTY && 
                    nonogram.getSolution()[row][col] == CellState.FILLED) {
                    gridButtons[row][col].setBackground(new Color(255, 193, 7));
                    gridButtons[row][col].setText("?");
                    gridButtons[row][col].setForeground(Color.BLACK);
                    statusLabel.setText("💡 Indice : Case (" + row + "," + col + ") doit être remplie");
                    statusLabel.setForeground(new Color(255, 193, 7));
                    
                    // Retour à la normale après 2 secondes
                    Timer timer = new Timer(2000, e -> {
                        updateButtonDisplay(gridButtons[finalRow][finalCol], finalRow, finalCol);
                        statusLabel.setText("🎲 Taille : " + currentSize + "×" + currentSize + " | Continuez à jouer !");
                        statusLabel.setForeground(new Color(200, 200, 200));
                    });
                    timer.setRepeats(false);
                    timer.start();
                    return;
                }
            }
        }
        statusLabel.setText("✅ Aucun indice nécessaire - vous êtes sur la bonne voie !");
        statusLabel.setForeground(SUCCESS_COLOR);
    }

    private void showVictory() {
        // Animation de victoire
        Timer timer = new Timer(150, new ActionListener() {
            int flashCount = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (flashCount < 8) {
                    for (int row = 0; row < nonogram.getHeight(); row++) {
                        for (int col = 0; col < nonogram.getWidth(); col++) {
                            if (nonogram.getCell(row, col) == CellState.FILLED) {
                                gridButtons[row][col].setBackground(
                                    flashCount % 2 == 0 ? SUCCESS_COLOR : FILLED_COLOR
                                );
                            }
                        }
                    }
                    flashCount++;
                } else {
                    ((Timer)e.getSource()).stop();
                    // Remettre les couleurs normales
                    updateGridDisplay();
                }
            }
        });
        timer.start();

        // Message de victoire 
        UIManager.put("OptionPane.background", PANEL_COLOR);
        UIManager.put("Panel.background", PANEL_COLOR);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        
        JOptionPane.showMessageDialog(this,
            "🎉 FÉLICITATIONS ! 🎉\n\n" +
            "Vous avez résolu le puzzle " + currentSize + "×" + currentSize + " !\n" +
            "Solution unique validée ✓",
            "VICTOIRE !",
            JOptionPane.INFORMATION_MESSAGE);

        statusLabel.setText("🎉 PUZZLE RÉSOLU ! Cliquez sur 'Nouveau Puzzle' pour continuer");
        statusLabel.setForeground(SUCCESS_COLOR);
    }

    private int getMaxClueLength(int[][] clues) {
        int max = 0;
        for (int[] clueArray : clues) {
            max = Math.max(max, clueArray.length);
        }
        return max;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new NonogramGame();
        });
    }
}
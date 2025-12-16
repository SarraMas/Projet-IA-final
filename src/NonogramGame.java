import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
    private int visualizationSpeed = 300;
   
    private final int[] AVAILABLE_SIZES = {3, 4, 5};

    // Couleurs
    private final Color BACKGROUND_COLOR = new Color(18, 18, 18);
    private final Color PANEL_COLOR = new Color(30, 30, 30);
    private final Color ACCENT_COLOR = new Color(0, 150, 255);
    private final Color GRID_COLOR = new Color(45, 45, 45);
    private final Color FILLED_COLOR = new Color(0, 150, 255);
    private final Color CROSSED_COLOR = new Color(255, 80, 80);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);

    public NonogramGame() {
        generateNewPuzzle();
        initializeUI();
    }

    private void generateNewPuzzle() {
        currentSize = AVAILABLE_SIZES[random.nextInt(AVAILABLE_SIZES.length)];
       
        if (statusLabel != null) {
            statusLabel.setText("⏳ Génération puzzle " + currentSize + "×" + currentSize + "...");
        }
       
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
                System.out.println("✅ Puzzle " + currentSize + "×" + currentSize + " généré");
                return;
            }
        }
       
        this.nonogram = createFallbackPuzzle();
        this.currentSize = 5;
    }

    private CellState[][] generateRandomSolution(int size) {
        CellState[][] solution = new CellState[size][size];
        double density = 0.3 + random.nextDouble() * 0.2;
       
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                solution[i][j] = random.nextDouble() < density ? CellState.FILLED : CellState.EMPTY;
            }
        }
        ensureValidPuzzle(solution);
        return solution;
    }

    private void ensureValidPuzzle(CellState[][] solution) {
        int size = solution.length;
        for (int i = 0; i < size; i++) {
            boolean hasFilled = false;
            for (int j = 0; j < size; j++) {
                if (solution[i][j] == CellState.FILLED) {
                    hasFilled = true;
                    break;
                }
            }
            if (!hasFilled && random.nextDouble() > 0.3) {
                solution[i][random.nextInt(size)] = CellState.FILLED;
            }
        }
        for (int j = 0; j < size; j++) {
            boolean hasFilled = false;
            for (int i = 0; i < size; i++) {
                if (solution[i][j] == CellState.FILLED) {
                    hasFilled = true;
                    break;
                }
            }
            if (!hasFilled && random.nextDouble() > 0.3) {
                solution[random.nextInt(size)][j] = CellState.FILLED;
            }
        }
    }

    private Nonogram createFallbackPuzzle() {
        int[][] rowClues = {{1}, {1}, {5}, {1}, {1}};
        int[][] colClues = {{1}, {1}, {5}, {1}, {1}};
        CellState[][] solution = {
            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY},
            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY},
            {CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED},
            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY},
            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY}
        };
        return new Nonogram(5, 5, new LineClues(rowClues, colClues), solution);
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
            if (count > 0) clues.add(count);
            rowClues[i] = clues.isEmpty() ? new int[0] : clues.stream().mapToInt(Integer::intValue).toArray();
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
            if (count > 0) clues.add(count);
            colClues[j] = clues.isEmpty() ? new int[0] : clues.stream().mapToInt(Integer::intValue).toArray();
        }
        return colClues;
    }

    private void initializeUI() {
        setTitle("🎯 Nonogram - Solution Unique");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BACKGROUND_COLOR);

        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBackground(BACKGROUND_COLOR);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainContainer.add(createHeaderPanel(), BorderLayout.NORTH);

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
        int buttonSize = currentSize <= 5 ? 45 : currentSize <= 7 ? 40 : 35;
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
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (nonogram.getCell(r, c) == CellState.EMPTY) {
                    button.setBackground(new Color(55, 55, 55));
                }
            }
            public void mouseExited(MouseEvent e) {
                updateButtonDisplay(button, r, c);
            }
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    handleCellClick(r, c, true);
                }
            }
        });
        button.addActionListener(e -> handleCellClick(r, c, false));
        return button;
    }

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

        statusLabel = new JLabel("🎲 " + currentSize + "×" + currentSize + " | Clic gauche: ■ | Clic droit: ×", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(new Color(200, 200, 200));

        JPanel strategyPanel = createStrategyPanel();
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(PANEL_COLOR);

        JButton resetButton = createModernButton("🎲 Nouveau", ACCENT_COLOR);
        resetButton.addActionListener(e -> resetGame());

        JButton solveButton = createModernButton("💡 Aide", new Color(255, 193, 7));
        solveButton.addActionListener(e -> showHint());

        JButton quitButton = createModernButton("🚪 Quitter", CROSSED_COLOR);
        quitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(resetButton);
        buttonPanel.add(solveButton);
        buttonPanel.add(quitButton);

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
            "Logic Strategy",
            "Backtracking Solver",
            "🤖 AI Heuristic Strategy",
            "🎯 Advanced AI Strategy (Solution)"
        });
        strategyComboBox.setBackground(PANEL_COLOR);
        strategyComboBox.setForeground(Color.WHITE);
        strategyComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
       
        strategyButton = createModernButton("👁️ Voir Stratégie", new Color(46, 204, 113));
        strategyButton.addActionListener(e -> toggleStrategyVisualization());
       
        JButton speedButton = createModernButton("⚡ Vitesse", new Color(255, 193, 7));
        speedButton.addActionListener(e -> changeSpeed());
       
        JLabel label = new JLabel("Stratégie:");
        label.setForeground(Color.WHITE);
        
        panel.add(label);
        panel.add(strategyComboBox);
        panel.add(strategyButton);
        panel.add(speedButton);
        return panel;
    }
    
    private void toggleStrategyVisualization() {
        if (isVisualizing) {
            stopVisualization();
        } else {
            startVisualization();
        }
    }

    private void startVisualization() {
        String selected = (String) strategyComboBox.getSelectedItem();
        System.out.println("🎯 Démarrage: " + selected);

        if (selected.contains("Simple Line")) {
            currentStrategy = new SimpleLineSolver();
        } else if (selected.contains("Logic")) {
            currentStrategy = new LogicStrategy();
        } else if (selected.contains("Backtracking")) {
            currentStrategy = new BacktrackingSolver();
        } else if (selected.contains("AI Heuristic")) {
            currentStrategy = new AIHeuristicStrategy();
        } else if (selected.contains("AdvancedAIStrategy")) {
        	currentStrategy=new AdvancedAIStrategy();
        }

        if (currentStrategy == null) {
            JOptionPane.showMessageDialog(this, "Stratégie non reconnue", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentStrategy.setStepByStepMode(true);
        currentStrategy.resetStatistics();

        visualizationPuzzle = new Nonogram(
            nonogram.getWidth(),
            nonogram.getHeight(),
            nonogram.getClues(),
            nonogram.getSolution()
        );

        System.out.println("📋 Puzzle copié: " + visualizationPuzzle.getWidth() + "×" + visualizationPuzzle.getHeight());

        isVisualizing = true;
        strategyButton.setText("⏹️ Arrêter");
        strategyButton.setBackground(CROSSED_COLOR);
        statusLabel.setText("👁️ Visualisation: " + currentStrategy.getName());
        statusLabel.setForeground(new Color(255, 193, 7));

        startTimer();
    }

    private void startTimer() {
        visualizationTimer = new Timer(visualizationSpeed, e -> {
            if (!isVisualizing) {
                stopVisualization();
                return;
            }
           
            if (!currentStrategy.hasNextStep()) {
                System.out.println("❌ Plus d'étapes");
                stopVisualization();
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(visualizationPuzzle.isSolved() ? 
                        "✅ Résolu !" : "⚠️ Bloqué");
                    statusLabel.setForeground(visualizationPuzzle.isSolved() ? 
                        SUCCESS_COLOR : CROSSED_COLOR);
                });
                return;
            }
           
            // Exécuter l'étape
            boolean cont = currentStrategy.executeNextStep(visualizationPuzzle);
            
            // ✅ FORCER mise à jour sur le thread Swing
            SwingUtilities.invokeLater(() -> {
                updateGridFromViz();
                statusLabel.setText("👁️ Étape " + currentStrategy.getCurrentStep());
            });
           
            if (!cont || visualizationPuzzle.isSolved()) {
                System.out.println("✅ Terminé");
                stopVisualization();
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("✅ Puzzle résolu !");
                    statusLabel.setForeground(SUCCESS_COLOR);
                });
            }
        });
        visualizationTimer.start();
        System.out.println("▶️ Timer démarré (" + visualizationSpeed + "ms)");
    }

    private void stopVisualization() {
        if (visualizationTimer != null) {
            visualizationTimer.stop();
        }
        isVisualizing = false;
        strategyButton.setText("👁️ Voir Stratégie");
        strategyButton.setBackground(new Color(46, 204, 113));
        // NE PAS réinitialiser l'affichage - garder le résultat de la stratégie
        System.out.println("⏹️ Arrêté");
    }

    private void updateGridFromViz() {
        // Copier ET afficher l'état actuel
        for (int r = 0; r < nonogram.getHeight(); r++) {
            for (int c = 0; c < nonogram.getWidth(); c++) {
                CellState state = visualizationPuzzle.getCell(r, c);
                nonogram.setCell(r, c, state);
                
                // Mise à jour visuelle immédiate
                JButton btn = gridButtons[r][c];
                switch (state) {
                    case EMPTY:
                        btn.setBackground(new Color(40, 40, 40));
                        btn.setText("");
                        break;
                    case FILLED:
                        btn.setBackground(FILLED_COLOR);
                        btn.setText("");
                        break;
                    case CROSSED:
                        btn.setBackground(new Color(40, 40, 40));
                        btn.setText("×");
                        btn.setForeground(CROSSED_COLOR);
                        break;
                }
            }
        }
    }

    private void updateButtonState(JButton button, CellState state) {
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

    private void changeSpeed() {
        String[] speeds = {"Lent", "Normal", "Rapide", "Ultra"};
        int[] delays = {800, 300, 100, 30};
        String current = "Normal";
        if (visualizationSpeed == 800) current = "Lent";
        else if (visualizationSpeed == 300) current = "Normal";
        else if (visualizationSpeed == 100) current = "Rapide";
        else if (visualizationSpeed == 30) current = "Ultra";
       
        String newSpeed = (String) JOptionPane.showInputDialog(this,
            "Vitesse:", "Vitesse", JOptionPane.QUESTION_MESSAGE,
            null, speeds, current);
       
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

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 40));
       
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        return button;
    }

    private void handleCellClick(int row, int col, boolean isRight) {
        if (isRight) {
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
        for (int r = 0; r < nonogram.getHeight(); r++) {
            for (int c = 0; c < nonogram.getWidth(); c++) {
                updateButtonDisplay(gridButtons[r][c], r, c);
            }
        }
    }

    private void resetGame() {
        generateNewPuzzle();
        getContentPane().removeAll();
        initializeUI();
        revalidate();
        repaint();
    }

    private void showHint() {
        for (int r = 0; r < nonogram.getHeight(); r++) {
            for (int c = 0; c < nonogram.getWidth(); c++) {
                if (nonogram.getCell(r, c) == CellState.EMPTY &&
                    nonogram.getSolution()[r][c] == CellState.FILLED) {
                    final int fr = r, fc = c;
                    gridButtons[r][c].setBackground(new Color(255, 193, 7));
                    gridButtons[r][c].setText("?");
                    statusLabel.setText("💡 Case (" + r + "," + c + ") à remplir");
                    Timer t = new Timer(2000, evt -> {
                        updateButtonDisplay(gridButtons[fr][fc], fr, fc);
                        statusLabel.setText("🎲 " + currentSize + "×" + currentSize);
                    });
                    t.setRepeats(false);
                    t.start();
                    return;
                }
            }
        }
        statusLabel.setText("✅ Aucun indice nécessaire");
    }

    private void showVictory() {
        JOptionPane.showMessageDialog(this,
            "🎉 BRAVO ! Puzzle résolu !",
            "VICTOIRE",
            JOptionPane.INFORMATION_MESSAGE);
        statusLabel.setText("🎉 RÉSOLU !");
        statusLabel.setForeground(SUCCESS_COLOR);
    }

    private int getMaxClueLength(int[][] clues) {
        int max = 0;
        for (int[] c : clues) {
            max = Math.max(max, c.length);
        }
        return max;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NonogramGame());
    }
}
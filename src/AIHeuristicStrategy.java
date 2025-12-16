import java.util.*;

/**
 * 🤖 STRATÉGIE IA HEURISTIQUE - VERSION OPTIMISÉE POUR 150/150
 * 
 * AMÉLIORATIONS MAJEURES :
 * 1. Limites augmentées (200k backtracks, 180s timeout)
 * 2. Heuristique améliorée avec plusieurs critères
 * 3. Déduction plus agressive avant backtracking
 * 4. Détection précoce optimisée
 * 5. Priorisation intelligente des cases
 * 
 * OBJECTIF : Résoudre 150/150 puzzles du benchmark
 */
public class AIHeuristicStrategy implements SolverStrategy {
    
    private SolverStatistics stats;
    private Nonogram nonogram;
    private int width;
    private int height;
    
    // Solveur de lignes intégré
    private SimpleLineSolver lineSolver;
    
    // 🚀 LIMITES AUGMENTÉES POUR RÉSOUDRE TOUS LES PUZZLES
    private static final int MAX_BACKTRACKS = 300000; // Augmenté de 50k à 200k
    private static final long MAX_TIME_MS = 300000; // Augmenté de 60s à 180s
    
    // Mode pas-à-pas
    private boolean stepByStepMode = false;
    private Queue<CellChange> changeQueue;
    
    private static class CellChange {
        int row, col;
        CellState state;
        CellChange(int r, int c, CellState s) {
            row = r; col = c; state = s;
        }
    }
    
    public AIHeuristicStrategy() {
        this.stats = new SolverStatistics();
        this.lineSolver = new SimpleLineSolver();
        this.changeQueue = new LinkedList<>();
    }
    
    @Override
    public String getName() {
        return "🤖 AI Heuristic Strategy (Optimisée 150/150)";
    }
    
    @Override
    public SolverStatistics getStatistics() {
        return stats;
    }
    
    @Override
    public void resetStatistics() {
        this.stats = new SolverStatistics();
        this.changeQueue.clear();
    }
    
    @Override
    public void setStepByStepMode(boolean enabled) {
        this.stepByStepMode = enabled;
        resetStatistics();
    }
    
    @Override
    public boolean solve(Nonogram nonogram) {
        resetStatistics();
        this.nonogram = nonogram;
        this.width = nonogram.getWidth();
        this.height = nonogram.getHeight();
        
        long startTime = System.currentTimeMillis();
        
        if (stepByStepMode) {
            prepareStepByStepSolution();
            return false;
        }
        
        // PHASE 1 : Déduction pure maximale (plusieurs passes)
        System.out.println("🧠 Phase 1: Déduction logique avancée...");
        applyAdvancedDeduction();
        stats.setCellsSolvedByDeduction(countFilledCells());
        System.out.println("  ✅ " + stats.getCellsSolvedByDeduction() + " cases déduites");
        
        boolean solved;
        if (nonogram.isSolved()) {
            System.out.println("✅ Résolu par déduction pure !");
            solved = true;
        } else {
            // PHASE 2 : Backtracking guidé par heuristiques améliorées
            System.out.println("🎯 Phase 2: Backtracking intelligent optimisé...");
            solved = smartBacktrack(startTime, 0);
        }
        
        // Statistiques finales
        long endTime = System.currentTimeMillis();
        stats.setExecutionTimeMs(endTime - startTime);
        stats.setSolved(solved);
        
        int totalCells = width * height;
        int filledCells = countFilledCells();
        stats.setCompletionPercentage((filledCells * 100.0) / totalCells);
        stats.setCellsSolvedByGuessing(filledCells - stats.getCellsSolvedByDeduction());
        
        if (!solved) {
            if (stats.getBacktrackCount() >= MAX_BACKTRACKS) {
                stats.setErrorMessage("Limite de backtracks atteinte");
            } else {
                stats.setErrorMessage("Timeout");
            }
        }
        
        System.out.println("Résultat: " + (solved ? "✅ RÉSOLU" : "❌ ÉCHEC"));
        System.out.println("  Temps: " + stats.getExecutionTimeMs() + "ms");
        System.out.println("  Backtracks: " + stats.getBacktrackCount());
        
        return solved;
    }
    
    /**
     * 🧠 DÉDUCTION AVANCÉE : Plus de passes et plus agressive
     */
    private void applyAdvancedDeduction() {
        boolean progress = true;
        int iterations = 0;
        int maxIterations = 100; // Augmenté de 50 à 100
        
        while (progress && iterations < maxIterations && !nonogram.isSolved()) {
            progress = false;
            iterations++;
            stats.incrementSteps();
            
            // Technique 1 : Simple line solving (plusieurs passes)
            for (int pass = 0; pass < 3; pass++) {
                if (lineSolver.solve(nonogram)) {
                    progress = true;
                }
            }
            
            // Technique 2 : Analyse de probabilités (cases certaines à 100%)
            if (applyCertainCells()) {
                progress = true;
            }
            
            // Technique 3 : Détection de cases impossibles
            if (markImpossibleCells()) {
                progress = true;
            }
            
            // Technique 4 : Cases avec probabilité très élevée/faible
            if (applyHighConfidenceCells()) {
                progress = true;
            }
        }
    }
    
    /**
     * 📊 ANALYSE DE PROBABILITÉS : Identifie les cases certaines
     */
    private boolean applyCertainCells() {
        boolean changed = false;
        
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (nonogram.getCell(row, col) != CellState.EMPTY) {
                    continue;
                }
                
                double prob = calculateProbability(row, col);
                
                // Si probabilité = 1.0 → certainement FILLED
                if (prob >= 0.9999) {
                    nonogram.setCell(row, col, CellState.FILLED);
                    stats.incrementDeductionCells();
                    changed = true;
                }
                // Si probabilité = 0.0 → certainement CROSSED
                else if (prob <= 0.0001) {
                    nonogram.setCell(row, col, CellState.CROSSED);
                    stats.incrementDeductionCells();
                    changed = true;
                }
            }
        }
        
        return changed;
    }
    
    /**
     * 🎯 NOUVELLE TECHNIQUE : Cases avec haute confiance (>95% ou <5%)
     */
    private boolean applyHighConfidenceCells() {
        boolean changed = false;
        
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (nonogram.getCell(row, col) != CellState.EMPTY) {
                    continue;
                }
                
                double prob = calculateProbability(row, col);
                
                // Haute confiance FILLED (>98%)
                if (prob >= 0.98) {
                    nonogram.setCell(row, col, CellState.FILLED);
                    stats.incrementDeductionCells();
                    changed = true;
                }
                // Haute confiance CROSSED (<2%)
                else if (prob <= 0.02) {
                    nonogram.setCell(row, col, CellState.CROSSED);
                    stats.incrementDeductionCells();
                    changed = true;
                }
            }
        }
        
        return changed;
    }
    
    /**
     * 🎯 CALCUL DE PROBABILITÉ pour une case - OPTIMISÉ
     */
    private double calculateProbability(int row, int col) {
        // Analyser la ligne
        List<CellState[]> rowSolutions = generatePossibleLines(
            getRow(row), 
            nonogram.getClues().getRowClues()[row],
            width
        );
        
        if (rowSolutions.isEmpty()) return 0.5; // Défaut si aucune solution
        
        int rowFilled = 0;
        for (CellState[] sol : rowSolutions) {
            if (sol[col] == CellState.FILLED) rowFilled++;
        }
        double rowProb = (double) rowFilled / rowSolutions.size();
        
        // Analyser la colonne
        List<CellState[]> colSolutions = generatePossibleLines(
            getColumn(col),
            nonogram.getClues().getColClues()[col],
            height
        );
        
        if (colSolutions.isEmpty()) return 0.5; // Défaut si aucune solution
        
        int colFilled = 0;
        for (CellState[] sol : colSolutions) {
            if (sol[row] == CellState.FILLED) colFilled++;
        }
        double colProb = (double) colFilled / colSolutions.size();
        
        // Combiner les probabilités (moyenne pondérée)
        // Donner plus de poids au plus restrictif
        return Math.min(rowProb, colProb);
    }
    
    /**
     * 🚫 MARQUER LES CASES IMPOSSIBLES
     */
    private boolean markImpossibleCells() {
        boolean changed = false;
        
        // Vérifier les lignes complètes
        for (int row = 0; row < height; row++) {
            if (isLineComplete(getRow(row), nonogram.getClues().getRowClues()[row])) {
                for (int col = 0; col < width; col++) {
                    if (nonogram.getCell(row, col) == CellState.EMPTY) {
                        nonogram.setCell(row, col, CellState.CROSSED);
                        stats.incrementDeductionCells();
                        changed = true;
                    }
                }
            }
        }
        
        // Vérifier les colonnes complètes
        for (int col = 0; col < width; col++) {
            if (isLineComplete(getColumn(col), nonogram.getClues().getColClues()[col])) {
                for (int row = 0; row < height; row++) {
                    if (nonogram.getCell(row, col) == CellState.EMPTY) {
                        nonogram.setCell(row, col, CellState.CROSSED);
                        stats.incrementDeductionCells();
                        changed = true;
                    }
                }
            }
        }
        
        return changed;
    }
    
    /**
     * 🎲 BACKTRACKING INTELLIGENT - VERSION OPTIMISÉE
     */
    private boolean smartBacktrack(long startTime, int depth) {
        stats.incrementSteps();
        
        // Sécurités
        if (System.currentTimeMillis() - startTime > MAX_TIME_MS) return false;
        if (stats.getBacktrackCount() > MAX_BACKTRACKS) return false;
        if (depth > width * height) return false;
        
        // Vérifications
        if (nonogram.isSolved()) return true;
        if (hasContradiction()) {
            stats.incrementBacktracks();
            return false;
        }
        
        // 🚀 Appliquer la déduction AGRESSIVE après chaque choix
        applyAdvancedDeduction();
        
        if (nonogram.isSolved()) return true;
        if (hasContradiction()) {
            stats.incrementBacktracks();
            return false;
        }
        
        // 🎯 HEURISTIQUE AMÉLIORÉE : Choisir la meilleure case
        CellChoice choice = findBestCellToGuess();
        
        if (choice == null) {
            stats.incrementBacktracks();
            return false;
        }
        
        // Sauvegarder l'état
        CellState[][] backup = copyGrid();
        
        // 🎯 Essayer d'abord la valeur la plus probable
        CellState firstTry = choice.probability > 0.5 ? CellState.FILLED : CellState.CROSSED;
        CellState secondTry = firstTry == CellState.FILLED ? CellState.CROSSED : CellState.FILLED;
        
        // Essai 1
        nonogram.setCell(choice.row, choice.col, firstTry);
        if (smartBacktrack(startTime, depth + 1)) {
            return true;
        }
        
        // Échec - restaurer
        stats.incrementBacktracks();
        restoreGrid(backup);
        
        // Essai 2
        nonogram.setCell(choice.row, choice.col, secondTry);
        if (smartBacktrack(startTime, depth + 1)) {
            return true;
        }
        
        // Les deux ont échoué
        stats.incrementBacktracks();
        restoreGrid(backup);
        return false;
    }
    
    /**
     * 🎯 HEURISTIQUE AMÉLIORÉE : Trouve la meilleure case à deviner
     * Critères multiples avec pondération intelligente
     */
    private CellChoice findBestCellToGuess() {
        CellChoice best = null;
        double bestScore = -1;
        
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (nonogram.getCell(row, col) != CellState.EMPTY) {
                    continue;
                }
                
                double prob = calculateProbability(row, col);
                
                // 📊 SCORE MULTI-CRITÈRES
                // 1. Certitude (distance à 0.5) - poids 50%
                double certainty = Math.abs(prob - 0.5) * 2;
                
                // 2. Nombre de voisins déterminés - poids 30%
                int neighbors = countDeterminedNeighbors(row, col);
                double neighborScore = Math.min(neighbors / (double)(width + height), 1.0);
                
                // 3. Position centrale (favoriser le centre) - poids 10%
                double centerDist = Math.sqrt(
                    Math.pow((row - height/2.0), 2) + 
                    Math.pow((col - width/2.0), 2)
                );
                double maxDist = Math.sqrt(Math.pow(height/2.0, 2) + Math.pow(width/2.0, 2));
                double centerScore = 1.0 - (centerDist / maxDist);
                
                // 4. Nombre de solutions possibles (moins = mieux) - poids 10%
                int rowSolutions = generatePossibleLines(getRow(row), 
                    nonogram.getClues().getRowClues()[row], width).size();
                int colSolutions = generatePossibleLines(getColumn(col), 
                    nonogram.getClues().getColClues()[col], height).size();
                double solutionScore = 1.0 / (1.0 + Math.log(rowSolutions + colSolutions + 1));
                
                // Score final pondéré
                double score = certainty * 50 +
                              neighborScore * 30 +
                              centerScore * 10 +
                              solutionScore * 10;
                
                if (score > bestScore) {
                    bestScore = score;
                    best = new CellChoice(row, col, prob, score);
                }
            }
        }
        
        return best;
    }
    
    private static class CellChoice {
        int row, col;
        double probability;
        double score;
        
        CellChoice(int r, int c, double p, double s) {
            row = r; col = c; probability = p; score = s;
        }
    }
    
    /**
     * Compte le nombre de voisins (ligne + colonne) déjà déterminés
     */
    private int countDeterminedNeighbors(int row, int col) {
        int count = 0;
        
        for (int c = 0; c < width; c++) {
            if (nonogram.getCell(row, c) != CellState.EMPTY) count++;
        }
        
        for (int r = 0; r < height; r++) {
            if (nonogram.getCell(r, col) != CellState.EMPTY) count++;
        }
        
        return count;
    }
    
    /**
     * Vérifie si une ligne est complète (tous les blocs placés)
     */
    private boolean isLineComplete(CellState[] line, int[] clue) {
        List<Integer> groups = new ArrayList<>();
        int count = 0;
        
        for (CellState cell : line) {
            if (cell == CellState.FILLED) {
                count++;
            } else if (count > 0) {
                groups.add(count);
                count = 0;
            }
        }
        if (count > 0) groups.add(count);
        
        if (groups.size() != clue.length) return false;
        
        for (int i = 0; i < groups.size(); i++) {
            if (!groups.get(i).equals(clue[i])) return false;
        }
        
        return true;
    }
    
    /**
     * 🚨 DÉTECTION DE CONTRADICTION optimisée
     */
    private boolean hasContradiction() {
        // Vérifier toutes les lignes
        for (int row = 0; row < height; row++) {
            if (isLineContradiction(getRow(row), nonogram.getClues().getRowClues()[row])) {
                return true;
            }
        }
        
        // Vérifier toutes les colonnes
        for (int col = 0; col < width; col++) {
            if (isLineContradiction(getColumn(col), nonogram.getClues().getColClues()[col])) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isLineContradiction(CellState[] line, int[] clue) {
        List<Integer> completeGroups = new ArrayList<>();
        int currentCount = 0;
        boolean hasEmpty = false;
        
        for (CellState cell : line) {
            if (cell == CellState.FILLED) {
                currentCount++;
            } else {
                if (currentCount > 0) {
                    completeGroups.add(currentCount);
                    currentCount = 0;
                }
                if (cell == CellState.EMPTY) {
                    hasEmpty = true;
                }
            }
        }
        if (currentCount > 0) completeGroups.add(currentCount);
        
        // Ligne complète
        if (!hasEmpty) {
            if (completeGroups.size() != clue.length) return true;
            for (int i = 0; i < completeGroups.size(); i++) {
                if (!completeGroups.get(i).equals(clue[i])) return true;
            }
            return false;
        }
        
        // Ligne incomplète
        if (completeGroups.size() > clue.length) return true;
        
        for (int i = 0; i < completeGroups.size(); i++) {
            if (completeGroups.get(i) > clue[i]) return true;
        }
        
        return false;
    }
    
    /**
     * Génère toutes les lignes possibles compatibles avec l'état actuel
     */
    private List<CellState[]> generatePossibleLines(CellState[] current, int[] clue, int length) {
        List<CellState[]> results = new ArrayList<>();
        
        if (clue == null || clue.length == 0) {
            CellState[] line = new CellState[length];
            Arrays.fill(line, CellState.CROSSED);
            if (isCompatible(line, current)) {
                results.add(line);
            }
            return results;
        }
        
        generateLinesRecursive(clue, length, 0, 0, new CellState[length], current, results);
        return results;
    }
    
    private void generateLinesRecursive(int[] clue, int length, int pos, int clueIndex,
                                       CellState[] line, CellState[] current,
                                       List<CellState[]> results) {
        if (clueIndex >= clue.length) {
            for (int i = pos; i < length; i++) {
                line[i] = CellState.CROSSED;
            }
            if (isCompatible(line, current)) {
                results.add(line.clone());
            }
            return;
        }
        
        int groupSize = clue[clueIndex];
        int minSpaceNeeded = groupSize;
        for (int i = clueIndex + 1; i < clue.length; i++) {
            minSpaceNeeded += 1 + clue[i];
        }
        
        for (int start = pos; start <= length - minSpaceNeeded; start++) {
            for (int i = pos; i < start; i++) {
                line[i] = CellState.CROSSED;
            }
            
            for (int i = start; i < start + groupSize; i++) {
                line[i] = CellState.FILLED;
            }
            
            int nextPos = start + groupSize;
            if (clueIndex < clue.length - 1) {
                if (nextPos < length) {
                    line[nextPos] = CellState.CROSSED;
                    nextPos++;
                }
            }
            
            generateLinesRecursive(clue, length, nextPos, clueIndex + 1, line, current, results);
        }
    }
    
    private boolean isCompatible(CellState[] generated, CellState[] current) {
        for (int i = 0; i < generated.length; i++) {
            if (current[i] == CellState.FILLED && generated[i] != CellState.FILLED) {
                return false;
            }
            if (current[i] == CellState.CROSSED && generated[i] != CellState.CROSSED) {
                return false;
            }
        }
        return true;
    }
    
    // ========== MODE PAS-À-PAS ==========
    
    @Override
    public boolean executeNextStep(Nonogram nonogram) {
        this.nonogram = nonogram;
        this.width = nonogram.getWidth();
        this.height = nonogram.getHeight();
        
        if (changeQueue.isEmpty()) {
            return false;
        }
        
        CellChange change = changeQueue.poll();
        nonogram.setCell(change.row, change.col, change.state);
        stats.incrementSteps();
        stats.incrementDeductionCells();
        
        return true;
    }
    
    private void prepareStepByStepSolution() {
        changeQueue.clear();
        
        // Résoudre dans une copie
        Nonogram copy = new Nonogram(width, height, nonogram.getClues(), nonogram.getSolution());
        solve(copy);
        
        // Enregistrer les changements
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                CellState target = copy.getCell(r, c);
                if (nonogram.getCell(r, c) != target) {
                    changeQueue.add(new CellChange(r, c, target));
                }
            }
        }
    }
    
    @Override
    public boolean hasNextStep() {
        return !changeQueue.isEmpty();
    }
    
    @Override
    public int getCurrentStep() {
        return stats.getTotalSteps();
    }
    
    // ========== UTILITAIRES ==========
    
    private CellState[] getRow(int row) {
        CellState[] line = new CellState[width];
        for (int col = 0; col < width; col++) {
            line[col] = nonogram.getCell(row, col);
        }
        return line;
    }
    
    private CellState[] getColumn(int col) {
        CellState[] column = new CellState[height];
        for (int row = 0; row < height; row++) {
            column[row] = nonogram.getCell(row, col);
        }
        return column;
    }
    
    private int countFilledCells() {
        int count = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (nonogram.getCell(row, col) == CellState.FILLED) {
                    count++;
                }
            }
        }
        return count;
    }
    
    private CellState[][] copyGrid() {
        CellState[][] copy = new CellState[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                copy[row][col] = nonogram.getCell(row, col);
            }
        }
        return copy;
    }
    
    private void restoreGrid(CellState[][] backup) {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                nonogram.setCell(row, col, backup[row][col]);
            }
        }
    }
    
}

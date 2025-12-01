import java.util.*;

/**
 * SIMPLE LINE SOLVER - VERSION FINALE CORRIGÉE
 * 
 * CORRECTION MAJEURE : Marque maintenant les cases CROSSED (certainement vides)
 */
public class SimpleLineSolver implements SolverStrategy {

    private SolverStatistics stats;
    private Nonogram nonogram;
    private int width;
    private int height;
    private boolean stepByStepMode = false;
    private int currentRow = 0;
    private int currentCol = 0;
    private boolean processingRows = true;

    public SimpleLineSolver() {
        this.stats = new SolverStatistics();
    }

    @Override
    public String getName() {
        return "Simple Line Solver (Déduction pure)";
    }

    @Override
    public SolverStatistics getStatistics() {
        return stats;
    }

    @Override
    public void resetStatistics() {
        this.stats = new SolverStatistics();
        this.currentRow = 0;
        this.currentCol = 0;
        this.processingRows = true;
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
            return false;
        }

        boolean progress = true;
        int iterationCount = 0;
        int maxIterations = 100;

        while (progress && iterationCount < maxIterations && !nonogram.isSolved()) {
            progress = false;
            iterationCount++;
            stats.incrementSteps();

            for (int row = 0; row < height; row++) {
                if (solveRow(row)) {
                    progress = true;
                }
            }

            for (int col = 0; col < width; col++) {
                if (solveColumn(col)) {
                    progress = true;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        stats.setExecutionTimeMs(endTime - startTime);

        boolean solved = nonogram.isSolved();
        stats.setSolved(solved);

        int totalCells = width * height;
        int filledCells = countFilledCells();
        stats.setCompletionPercentage((filledCells * 100.0) / totalCells);
        stats.setCellsSolvedByDeduction(filledCells);

        if (!solved) {
            stats.setErrorMessage("Bloqué - nécessite du backtracking");
        }

        return solved;
    }

    @Override
    public boolean executeNextStep(Nonogram nonogram) {
        this.nonogram = nonogram;
        this.width = nonogram.getWidth();
        this.height = nonogram.getHeight();

        stats.incrementSteps();

        if (processingRows) {
            if (solveRow(currentRow)) {
                return true;
            }
            currentRow++;
            if (currentRow >= height) {
                currentRow = 0;
                processingRows = false;
            }
        } else {
            if (solveColumn(currentCol)) {
                return true;
            }
            currentCol++;
            if (currentCol >= width) {
                currentCol = 0;
                processingRows = true;
            }
        }

        return true;
    }

    @Override
    public boolean hasNextStep() {
        return nonogram == null || !nonogram.isSolved();
    }

    @Override
    public int getCurrentStep() {
        return stats.getTotalSteps();
    }

    /**
     * RÉSOLUTION DE LIGNE - CORRIGÉE POUR MARQUER FILLED **ET** CROSSED
     */
    private boolean solveRow(int row) {
        CellState[] currentLine = getRow(row);
        int[] clue = nonogram.getClues().getRowClues()[row];

        List<CellState[]> possibleSolutions = generatePossibleLines(clue, width, currentLine);

        if (possibleSolutions.isEmpty()) {
            return false;
        }

        boolean modified = false;

        // Pour chaque case EMPTY, vérifier si toutes les solutions ont la même valeur
        for (int col = 0; col < width; col++) {
            if (currentLine[col] != CellState.EMPTY) {
                continue; // Case déjà déterminée
            }

            CellState first = possibleSolutions.get(0)[col];
            boolean allSame = true;

            for (int i = 1; i < possibleSolutions.size(); i++) {
                if (possibleSolutions.get(i)[col] != first) {
                    allSame = false;
                    break;
                }
            }

            // ✅ CORRECTION CRITIQUE : Appliquer FILLED **OU** CROSSED
            if (allSame) {
                nonogram.setCell(row, col, first);
                modified = true;
                stats.incrementDeductionCells();
            }
        }

        return modified;
    }

    /**
     * RÉSOLUTION DE COLONNE - CORRIGÉE POUR MARQUER FILLED **ET** CROSSED
     */
    private boolean solveColumn(int col) {
        CellState[] currentColumn = getColumn(col);
        int[] clue = nonogram.getClues().getColClues()[col];

        List<CellState[]> possibleSolutions = generatePossibleLines(clue, height, currentColumn);

        if (possibleSolutions.isEmpty()) {
            return false;
        }

        boolean modified = false;

        for (int row = 0; row < height; row++) {
            if (currentColumn[row] != CellState.EMPTY) {
                continue;
            }

            CellState first = possibleSolutions.get(0)[row];
            boolean allSame = true;

            for (int i = 1; i < possibleSolutions.size(); i++) {
                if (possibleSolutions.get(i)[row] != first) {
                    allSame = false;
                    break;
                }
            }

            // ✅ CORRECTION CRITIQUE : Appliquer FILLED **OU** CROSSED
            if (allSame) {
                nonogram.setCell(row, col, first);
                modified = true;
                stats.incrementDeductionCells();
            }
        }

        return modified;
    }

    /**
     * GÉNÉRATION DE LIGNES POSSIBLES - CORRIGÉE
     * Génère des lignes avec FILLED **ET** CROSSED
     */
    private List<CellState[]> generatePossibleLines(int[] clue, int length, CellState[] current) {
        List<CellState[]> results = new ArrayList<>();

        // Cas spécial : aucun bloc → toute la ligne est CROSSED
        if (clue == null || clue.length == 0) {
            CellState[] line = new CellState[length];
            Arrays.fill(line, CellState.CROSSED);
            if (isCompatible(line, current)) {
                results.add(line);
            }
            return results;
        }

        // Générer toutes les combinaisons possibles
        generateLinesRecursive(clue, length, 0, 0, new CellState[length], current, results);
        return results;
    }

    /**
     * GÉNÉRATION RÉCURSIVE - VERSION CORRIGÉE
     * Place les blocs FILLED et remplit le reste avec CROSSED
     */
    private void generateLinesRecursive(int[] clue, int length, int pos, int clueIndex,
                                       CellState[] line, CellState[] current,
                                       List<CellState[]> results) {
        // Cas de base : tous les blocs placés
        if (clueIndex >= clue.length) {
            // ✅ CORRECTION : Remplir le reste avec CROSSED (pas EMPTY)
            for (int i = pos; i < length; i++) {
                line[i] = CellState.CROSSED;
            }

            if (isCompatible(line, current)) {
                results.add(line.clone());
            }
            return;
        }

        int groupSize = clue[clueIndex];

        // Calculer l'espace minimum nécessaire
        int minSpaceNeeded = groupSize;
        for (int i = clueIndex + 1; i < clue.length; i++) {
            minSpaceNeeded += 1 + clue[i];
        }

        // Essayer toutes les positions possibles pour ce bloc
        for (int start = pos; start <= length - minSpaceNeeded; start++) {
            // ✅ CORRECTION : Placer CROSSED avant le bloc
            for (int i = pos; i < start; i++) {
                line[i] = CellState.CROSSED;
            }

            // Placer le bloc FILLED
            for (int i = start; i < start + groupSize; i++) {
                line[i] = CellState.FILLED;
            }

            int nextPos = start + groupSize;

            // ✅ CORRECTION : CROSSED obligatoire après le bloc
            if (clueIndex < clue.length - 1) {
                if (nextPos < length) {
                    line[nextPos] = CellState.CROSSED;
                    nextPos++;
                }
            }

            // Récursion
            generateLinesRecursive(clue, length, nextPos, clueIndex + 1, line, current, results);
        }
    }

    /**
     * VÉRIFICATION DE COMPATIBILITÉ
     */
    private boolean isCompatible(CellState[] generated, CellState[] current) {
        for (int i = 0; i < generated.length; i++) {
            CellState curr = current[i];
            CellState gen = generated[i];

            if (curr == CellState.FILLED && gen != CellState.FILLED) {
                return false;
            }
            if (curr == CellState.CROSSED && gen != CellState.CROSSED) {
                return false;
            }
        }
        return true;
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
}
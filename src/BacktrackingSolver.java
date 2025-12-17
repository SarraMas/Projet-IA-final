

class BacktrackingSolver implements SolverStrategy {
    
    private SolverStatistics stats;
    private Nonogram nonogram;
    private int width;
    private int height;
    private SimpleLineSolver lineSolver;
    
    private static final int MAX_BACKTRACKS = 100000;
    private static final long MAX_TIME_MS = 120000;
    
    private boolean stepByStepMode = false;
    private java.util.Stack<BacktrackState> backtrackStack;
    
    private static class BacktrackState {
        CellState[][] grid;
        int row;
        int col;
        int attempt;
        
        BacktrackState(CellState[][] grid, int row, int col, int attempt) {
            this.grid = grid;
            this.row = row;
            this.col = col;
            this.attempt = attempt;
        }
    }
    
    public BacktrackingSolver() {
        this.stats = new SolverStatistics();
        this.lineSolver = new SimpleLineSolver();
        this.backtrackStack = new java.util.Stack<>();
    }
    
    @Override
    public String getName() {
        return "Backtracking Solver (Essai-erreur)";
    }
    
    @Override
    public SolverStatistics getStatistics() {
        return stats;
    }
    
    @Override
    public void resetStatistics() {
        this.stats = new SolverStatistics();
        this.backtrackStack.clear();
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
        
        // PHASE 1 : Déduction pure
        System.out.println("Phase 1: Déduction pure...");
        lineSolver.solve(nonogram);
        stats.setCellsSolvedByDeduction(countFilledCells());
        System.out.println("  → " + stats.getCellsSolvedByDeduction() + " cases déduites");
        
        // PHASE 2 : Backtracking si nécessaire
        boolean solved;
        if (nonogram.isSolved()) {
            System.out.println("✅ Résolu par déduction pure !");
            solved = true;
        } else {
            System.out.println("Phase 2: Backtracking...");
            solved = backtrackRecursive(startTime, 0);
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
   
    private boolean backtrackRecursive(long startTime, int depth) {
        stats.incrementSteps();
        
  
        if (System.currentTimeMillis() - startTime > MAX_TIME_MS) {
            return false;
        }
        
        if (stats.getBacktrackCount() > MAX_BACKTRACKS) {
            return false;
        }
        
        if (depth > width * height) {
            return false;
        }
        
      
        if (nonogram.isSolved()) {
            return true;
        }
        
        if (hasContradiction()) {
            stats.incrementBacktracks();
            return false;
        }
        
      
        lineSolver.solve(nonogram);
        
        if (nonogram.isSolved()) {
            return true;
        }
        
        if (hasContradiction()) {
            stats.incrementBacktracks();
            return false;
        }
        
   
        int[] bestCell = findBestCellToGuess();
        
        if (bestCell == null) {
            stats.incrementBacktracks();
            return false;
        }
        
        int row = bestCell[0];
        int col = bestCell[1];
        
       
        CellState[][] backup = copyGrid();
        
       
        nonogram.setCell(row, col, CellState.FILLED);
        
        if (backtrackRecursive(startTime, depth + 1)) {
            return true;
        }
        
     
        stats.incrementBacktracks();
        restoreGrid(backup);
        
      
        nonogram.setCell(row, col, CellState.CROSSED);
        
        if (backtrackRecursive(startTime, depth + 1)) {
            return true;
        }
        
     
        stats.incrementBacktracks();
        restoreGrid(backup);
        return false;
    }
    
    @Override
    public boolean executeNextStep(Nonogram nonogram) {
        this.nonogram = nonogram;
        this.width = nonogram.getWidth();
        this.height = nonogram.getHeight();
        
        stats.incrementSteps();
        
        if (stats.getTotalSteps() == 1) {
            lineSolver.solve(nonogram);
            stats.setCellsSolvedByDeduction(countFilledCells());
            return !nonogram.isSolved();
        }
        
        int[] cell = findBestCellToGuess();
        if (cell != null) {
            nonogram.setCell(cell[0], cell[1], CellState.FILLED);
            lineSolver.solve(nonogram);
        }
        
        return !nonogram.isSolved();
    }
    
    @Override
    public boolean hasNextStep() {
        return nonogram == null || !nonogram.isSolved();
    }
    
    @Override
    public int getCurrentStep() {
        return stats.getTotalSteps();
    }
    
   
    private int[] findBestCellToGuess() {
        int bestRow = -1;
        int bestCol = -1;
        int bestScore = -1;
        
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (nonogram.getCell(row, col) != CellState.EMPTY) {
                    continue;
                }
                
                int score = countDeterminedNeighbors(row, col);
                
                if (score > bestScore) {
                    bestScore = score;
                    bestRow = row;
                    bestCol = col;
                }
            }
        }
        
        if (bestRow == -1) {
            return null;
        }
        
        return new int[]{bestRow, bestCol};
    }
    
    private int countDeterminedNeighbors(int row, int col) {
        int count = 0;
        
        for (int c = 0; c < width; c++) {
            if (nonogram.getCell(row, c) != CellState.EMPTY) {
                count++;
            }
        }
        
        for (int r = 0; r < height; r++) {
            if (nonogram.getCell(r, col) != CellState.EMPTY) {
                count++;
            }
        }
        
        return count;
    }
    
  
    private boolean hasContradiction() {
        for (int row = 0; row < height; row++) {
            if (isLineContradiction(getRow(row), nonogram.getClues().getRowClues()[row])) {
                return true;
            }
        }
        
        for (int col = 0; col < width; col++) {
            if (isLineContradiction(getColumn(col), nonogram.getClues().getColClues()[col])) {
                return true;
            }
        }
        
        return false;
    }
    

     
    private boolean isLineContradiction(CellState[] line, int[] clue) {
        java.util.List<Integer> completeGroups = new java.util.ArrayList<>();
        int currentCount = 0;
        boolean hasEmpty = false;
        
        for (int i = 0; i < line.length; i++) {
            if (line[i] == CellState.FILLED) {
                currentCount++;
            } else {
                if (currentCount > 0) {
                    completeGroups.add(currentCount);
                    currentCount = 0;
                }
                if (line[i] == CellState.EMPTY) {
                    hasEmpty = true;
                }
            }
        }
        
        if (currentCount > 0) {
            completeGroups.add(currentCount);
        }
        
     
        if (!hasEmpty) {
            if (completeGroups.size() != clue.length) {
                return true;
            }
            for (int i = 0; i < completeGroups.size(); i++) {
                if (!completeGroups.get(i).equals(clue[i])) {
                    return true;
                }
            }
            return false;
        }
        
       
        if (completeGroups.size() > clue.length) {
            return true;
        }
        
        for (int i = 0; i < completeGroups.size(); i++) {
            if (completeGroups.get(i) > clue[i]) {
                return true;
            }
        }
        
        return false;
    }
    
   
    
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
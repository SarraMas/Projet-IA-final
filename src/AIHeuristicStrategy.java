import java.util.*;


public class AIHeuristicStrategy implements SolverStrategy {
    
    private SolverStatistics stats;
    private Nonogram nonogram;
    private int width;
    private int height;
    
    private SimpleLineSolver lineSolver;
    
   
    private static final int MAX_BACKTRACKS = 250000;
    private static final long MAX_TIME_MS = 120000;
    

    private Map<String, List<CellState[]>> cacheLinesPossibles;
    
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
        this.cacheLinesPossibles = new HashMap<>();
    }
    
    @Override
    public String getName() {
        return "🤖 AI Heuristic Strategy (Ultra-Optimisée v2)";
    }
    
    @Override
    public SolverStatistics getStatistics() {
        return stats;
    }
    
    @Override
    public void resetStatistics() {
        this.stats = new SolverStatistics();
        this.changeQueue.clear();
        this.cacheLinesPossibles.clear();
    }
    
    @Override
    public void setStepByStepMode(boolean enabled) {
        this.stepByStepMode = enabled;
        resetStatistics();
    }
    
    @Override
    public boolean solve(Nonogram nonogram) {
        this.nonogram = nonogram;
        this.width = nonogram.getWidth();
        this.height = nonogram.getHeight();
        
      
        if (!stepByStepMode) {
            resetStatistics();
            this.cacheLinesPossibles.clear();
        }
        
        long startTime = System.currentTimeMillis();
        
        // PHASE 1 : Déduction ULTRA-AGRESSIVE
        applyUltraAggressiveDeduction();
        stats.setCellsSolvedByDeduction(countDeterminedCells());
        
        boolean solved;
        if (nonogram.isSolved()) {
            solved = true;
        } else {
            // PHASE 2 : Backtracking avec MRV
            solved = mrvBacktrack(startTime, 0);
        }
        
        // Statistiques finales
        long endTime = System.currentTimeMillis();
        stats.setExecutionTimeMs(endTime - startTime);
        stats.setSolved(solved);
        
        int totalCells = width * height;
        int determinedCells = countDeterminedCells();
        stats.setCompletionPercentage((determinedCells * 100.0) / totalCells);
        stats.setCellsSolvedByGuessing(determinedCells - stats.getCellsSolvedByDeduction());
        
        if (!solved) {
            if (stats.getBacktrackCount() >= MAX_BACKTRACKS) {
                stats.setErrorMessage("Limite de backtracks atteinte");
            } else {
                stats.setErrorMessage("Timeout");
            }
        }
        
        return solved;
    }
    
   
    private void applyUltraAggressiveDeduction() {
        boolean progress = true;
        int iterations = 0;
        int maxIterations = 100;
        
        while (progress && iterations < maxIterations && !nonogram.isSolved()) {
            progress = false;
            iterations++;
            stats.incrementSteps();
            
            // Technique 1 : Line solving multiple passes
            for (int pass = 0; pass < 3; pass++) {
                if (lineSolver.solve(nonogram)) {
                    progress = true;
                }
            }
            
            // Technique 2 : Cases certaines avec cache
            if (applyCertainCellsWithCache()) {
                progress = true;
            }
            
            // Technique 3 : Propagation de contraintes
            if (propagateConstraints()) {
                progress = true;
            }
            
            // Technique 4 : Marquer impossible
            if (markImpossibleCells()) {
                progress = true;
            }
            
            // 🔥 NOUVEAU : Forced cells (cases forcées)
            if (applyForcedCells()) {
                progress = true;
            }
        }
    }
    
   
    private boolean applyCertainCellsWithCache() {
        boolean changed = false;
        
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (nonogram.getCell(row, col) != CellState.EMPTY) {
                    continue;
                }
                
                double prob = calculateProbabilityWithCache(row, col);
                
                if (prob >= 0.999) {
                    nonogram.setCell(row, col, CellState.FILLED);
                    stats.incrementDeductionCells();
                    changed = true;
                } else if (prob <= 0.001) {
                    nonogram.setCell(row, col, CellState.CROSSED);
                    stats.incrementDeductionCells();
                    changed = true;
                }
            }
        }
        
        return changed;
    }
    
    
    private double calculateProbabilityWithCache(int row, int col) {
        List<CellState[]> rowSolutions = getPossibleLinesWithCache(
            getRow(row), 
            nonogram.getClues().getRowClues()[row],
            width,
            true,
            row
        );
        
        if (rowSolutions.isEmpty()) return 0.5;
        
        int rowFilled = 0;
        for (CellState[] sol : rowSolutions) {
            if (sol[col] == CellState.FILLED) rowFilled++;
        }
        double rowProb = (double) rowFilled / rowSolutions.size();
        
        List<CellState[]> colSolutions = getPossibleLinesWithCache(
            getColumn(col),
            nonogram.getClues().getColClues()[col],
            height,
            false,
            col
        );
        
        if (colSolutions.isEmpty()) return 0.5;
        
        int colFilled = 0;
        for (CellState[] sol : colSolutions) {
            if (sol[row] == CellState.FILLED) colFilled++;
        }
        double colProb = (double) colFilled / colSolutions.size();
        
        return Math.min(rowProb, colProb);
    }
    
   
    private List<CellState[]> getPossibleLinesWithCache(CellState[] current, int[] clue, 
                                                         int length, boolean isRow, int index) {
        String key = generateCacheKey(current, clue, isRow, index);
        
        if (cacheLinesPossibles.containsKey(key)) {
            return cacheLinesPossibles.get(key);
        }
        
        List<CellState[]> result = generatePossibleLines(current, clue, length);
        cacheLinesPossibles.put(key, result);
        
        return result;
    }
    
    private String generateCacheKey(CellState[] line, int[] clue, boolean isRow, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append(isRow ? "R" : "C").append(index).append(":");
        for (CellState c : line) {
            sb.append(c == CellState.FILLED ? "F" : c == CellState.CROSSED ? "X" : "E");
        }
        sb.append(":");
        for (int i : clue) {
            sb.append(i).append(",");
        }
        return sb.toString();
    }
    
   
    private boolean applyForcedCells() {
        boolean changed = false;
        
        // Pour chaque ligne
        for (int row = 0; row < height; row++) {
            CellState[] line = getRow(row);
            int[] clue = nonogram.getClues().getRowClues()[row];
            
            List<CellState[]> solutions = getPossibleLinesWithCache(line, clue, width, true, row);
            
            if (solutions.isEmpty()) continue;
            if (solutions.size() == 1) {
                // Une seule solution possible !
                for (int col = 0; col < width; col++) {
                    if (line[col] == CellState.EMPTY) {
                        nonogram.setCell(row, col, solutions.get(0)[col]);
                        stats.incrementDeductionCells();
                        changed = true;
                    }
                }
            }
        }
        
        
        for (int col = 0; col < width; col++) {
            CellState[] column = getColumn(col);
            int[] clue = nonogram.getClues().getColClues()[col];
            
            List<CellState[]> solutions = getPossibleLinesWithCache(column, clue, height, false, col);
            
            if (solutions.isEmpty()) continue;
            if (solutions.size() == 1) {
                for (int row = 0; row < height; row++) {
                    if (column[row] == CellState.EMPTY) {
                        nonogram.setCell(row, col, solutions.get(0)[row]);
                        stats.incrementDeductionCells();
                        changed = true;
                    }
                }
            }
        }
        
        return changed;
    }
    
    
    private boolean propagateConstraints() {
        boolean changed = false;
        
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (nonogram.getCell(row, col) != CellState.EMPTY) {
                    continue;
                }
                
                // Si cette case ne peut être que FILLED dans toutes les solutions
                if (mustBeFilled(row, col)) {
                    nonogram.setCell(row, col, CellState.FILLED);
                    stats.incrementDeductionCells();
                    changed = true;
                }
                // Si cette case ne peut être que CROSSED dans toutes les solutions
                else if (mustBeCrossed(row, col)) {
                    nonogram.setCell(row, col, CellState.CROSSED);
                    stats.incrementDeductionCells();
                    changed = true;
                }
            }
        }
        
        return changed;
    }
    
    private boolean mustBeFilled(int row, int col) {
        List<CellState[]> rowSolutions = getPossibleLinesWithCache(
            getRow(row), nonogram.getClues().getRowClues()[row], width, true, row);
        
        for (CellState[] sol : rowSolutions) {
            if (sol[col] != CellState.FILLED) return false;
        }
        
        return !rowSolutions.isEmpty();
    }
    
    private boolean mustBeCrossed(int row, int col) {
        List<CellState[]> rowSolutions = getPossibleLinesWithCache(
            getRow(row), nonogram.getClues().getRowClues()[row], width, true, row);
        
        for (CellState[] sol : rowSolutions) {
            if (sol[col] != CellState.CROSSED) return false;
        }
        
        return !rowSolutions.isEmpty();
    }
    
   
    private boolean markImpossibleCells() {
        boolean changed = false;
        
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
    
  //  BACKTRACKING avec MRV (Minimum Remaining Values)
    
    private boolean mrvBacktrack(long startTime, int depth) {
        stats.incrementSteps();
        
        if (System.currentTimeMillis() - startTime > MAX_TIME_MS) return false;
        if (stats.getBacktrackCount() > MAX_BACKTRACKS) return false;
        if (depth > width * height) return false;
        
        if (nonogram.isSolved()) return true;
        if (hasContradictionFast()) {
            stats.incrementBacktracks();
            return false;
        }
        
        // Déduction rapide
        for (int i = 0; i < 2; i++) {
            lineSolver.solve(nonogram);
            applyCertainCellsWithCache();
        }
        
        if (nonogram.isSolved()) return true;
        if (hasContradictionFast()) {
            stats.incrementBacktracks();
            return false;
        }
        
        //  MRV : Choisir la case avec le moins de solutions possibles
        CellChoice choice = findBestCellMRV();
        
        if (choice == null) {
            stats.incrementBacktracks();
            return false;
        }
        
        CellState[][] backup = copyGrid();
        
        CellState firstTry = choice.probability > 0.5 ? CellState.FILLED : CellState.CROSSED;
        CellState secondTry = firstTry == CellState.FILLED ? CellState.CROSSED : CellState.FILLED;
        
        nonogram.setCell(choice.row, choice.col, firstTry);
        cacheLinesPossibles.clear(); // Invalider cache
        
        if (mrvBacktrack(startTime, depth + 1)) {
            return true;
        }
        
        stats.incrementBacktracks();
        restoreGrid(backup);
        cacheLinesPossibles.clear();
        
        nonogram.setCell(choice.row, choice.col, secondTry);
        
        if (mrvBacktrack(startTime, depth + 1)) {
            return true;
        }
        
        stats.incrementBacktracks();
        restoreGrid(backup);
        cacheLinesPossibles.clear();
        return false;
    }
    
    // HEURISTIQUE MRV : Minimum Remaining Values
    
    private CellChoice findBestCellMRV() {
        CellChoice best = null;
        double bestScore = Double.MAX_VALUE;
        
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (nonogram.getCell(row, col) != CellState.EMPTY) {
                    continue;
                }
                
                // Compter les solutions possibles
                List<CellState[]> rowSolutions = getPossibleLinesWithCache(
                    getRow(row), nonogram.getClues().getRowClues()[row], width, true, row);
                List<CellState[]> colSolutions = getPossibleLinesWithCache(
                    getColumn(col), nonogram.getClues().getColClues()[col], height, false, col);
                
                if (rowSolutions.isEmpty() || colSolutions.isEmpty()) continue;
                
                // Score = nombre moyen de solutions (moins = mieux)
                double score = (rowSolutions.size() + colSolutions.size()) / 2.0;
                
                // Bonus pour certitude
                double prob = calculateProbabilityWithCache(row, col);
                double certainty = Math.abs(prob - 0.5);
                score = score * (1 - certainty); // Favoriser cases certaines
                
                if (score < bestScore) {
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
    
    // DÉTECTION RAPIDE DE CONTRADICTION
     
    private boolean hasContradictionFast() {
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
        
        if (!hasEmpty) {
            if (completeGroups.size() != clue.length) return true;
            for (int i = 0; i < completeGroups.size(); i++) {
                if (!completeGroups.get(i).equals(clue[i])) return true;
            }
            return false;
        }
        
        if (completeGroups.size() > clue.length) return true;
        
        for (int i = 0; i < completeGroups.size(); i++) {
            if (completeGroups.get(i) > clue[i]) return true;
        }
        
        return false;
    }
    
   //générattion de lignes possibles 
    private List<CellState[]> generatePossibleLines(CellState[] current, int[] clue, int length) {
        List<CellState[]> results = new ArrayList<>();
        
        if (clue == null || clue.length == 0 || (clue.length == 1 && clue[0] == 0)) {
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
    
  
    
    @Override
    public boolean executeNextStep(Nonogram nonogram) {
        // Initialiser si nécessaire
        if (this.nonogram == null || this.nonogram != nonogram) {
            this.nonogram = nonogram;
            this.width = nonogram.getWidth();
            this.height = nonogram.getHeight();
        }
        
       
        if (changeQueue.isEmpty() && stats.getTotalSteps() == 0) {
            System.out.println("🔍 Préparation solution step-by-step...");
            
            
            Nonogram copy = new Nonogram(width, height, nonogram.getClues(), nonogram.getSolution());
            AIHeuristicStrategy solver = new AIHeuristicStrategy();
            
            System.out.println("🎯 Résolution de la copie...");
            boolean solved = solver.solve(copy);
            System.out.println(solved ? "✅ Copie résolue" : "❌ Échec");
            
           
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < width; c++) {
                    CellState current = nonogram.getCell(r, c);
                    CellState target = copy.getCell(r, c);
                    
                    if (current != target) {
                        changeQueue.add(new CellChange(r, c, target));
                    }
                }
            }
            
            System.out.println("📋 " + changeQueue.size() + " changements enregistrés");
            
            if (changeQueue.isEmpty()) {
                System.out.println("❌ Aucun changement");
                return false;
            }
        }
        
     
        if (!changeQueue.isEmpty()) {
            CellChange change = changeQueue.poll();
            nonogram.setCell(change.row, change.col, change.state);
            stats.incrementSteps();
            
            System.out.println("📝 Étape " + stats.getTotalSteps() + 
                             " : Case (" + change.row + "," + change.col + ") → " + change.state);
            
            return true; 
        }
        
        return false; 
    }
   
    private void prepareStepByStepSolutionFixed() {
        changeQueue.clear();
        
        // Créer une copie indépendante du puzzle
        Nonogram copy = new Nonogram(width, height, nonogram.getClues(), nonogram.getSolution());
        
        // Copier l'état actuel (grille vide normalement)
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                copy.setCell(r, c, nonogram.getCell(r, c));
            }
        }
        
        System.out.println("🎯 Résolution de la copie...");
        
        // Créer une nouvelle instance de la stratégie pour résoudre
        AIHeuristicStrategy tempSolver = new AIHeuristicStrategy();
        boolean solved = tempSolver.solve(copy);
        
        System.out.println(solved ? "✅ Copie résolue" : "❌ Échec résolution");
        
        // Comparer et enregistrer tous les changements
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                CellState current = nonogram.getCell(r, c);
                CellState target = copy.getCell(r, c);
                
                if (current != target) {
                    changeQueue.add(new CellChange(r, c, target));
                }
            }
        }
        
        System.out.println("📋 " + changeQueue.size() + " changements enregistrés");
    }
    
    
    public boolean hasNextStep() {
        
        if (stepByStepMode) {
          
            return nonogram != null && !nonogram.isSolved() && stats.getTotalSteps() < 1000;
        }
        return false;
    }
    
    @Override
    public int getCurrentStep() {
        return stats.getTotalSteps();
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
    
    private int countDeterminedCells() {
        int count = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (nonogram.getCell(row, col) != CellState.EMPTY) {
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

public interface SolverStrategy {
    
    
    boolean solve(Nonogram nonogram);
    
  
    String getName();
    
  
    SolverStatistics getStatistics();
   
    void resetStatistics();
    
    void setStepByStepMode(boolean enabled);
    boolean executeNextStep(Nonogram nonogram);
    boolean hasNextStep();
    int getCurrentStep();
}


class SolverStatistics {
   
    private long executionTimeMs;
    
   
    private int totalSteps;
    
    
    private int backtrackCount;
    
    
    private int cellsSolvedByDeduction;
    
   
    private int cellsSolvedByGuessing;
    
   
    private double completionPercentage;
    
   
    private boolean solved;
    
  
    private String errorMessage;
    
    
    public SolverStatistics() {
        this.executionTimeMs = 0;
        this.totalSteps = 0;
        this.backtrackCount = 0;
        this.cellsSolvedByDeduction = 0;
        this.cellsSolvedByGuessing = 0;
        this.completionPercentage = 0.0;
        this.solved = false;
        this.errorMessage = "";
    }
    
   
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public int getTotalSteps() {
        return totalSteps;
    }
    
    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }
    
    public void incrementSteps() {
        this.totalSteps++;
    }
    
    public int getBacktrackCount() {
        return backtrackCount;
    }
    
    public void setBacktrackCount(int backtrackCount) {
        this.backtrackCount = backtrackCount;
    }
    
    public void incrementBacktracks() {
        this.backtrackCount++;
    }
    
    public int getCellsSolvedByDeduction() {
        return cellsSolvedByDeduction;
    }
    
    public void setCellsSolvedByDeduction(int cellsSolvedByDeduction) {
        this.cellsSolvedByDeduction = cellsSolvedByDeduction;
    }
    
    public void incrementDeductionCells() {
        this.cellsSolvedByDeduction++;
    }
    
    public int getCellsSolvedByGuessing() {
        return cellsSolvedByGuessing;
    }
    
    public void setCellsSolvedByGuessing(int cellsSolvedByGuessing) {
        this.cellsSolvedByGuessing = cellsSolvedByGuessing;
    }
    
    public void incrementGuessingCells() {
        this.cellsSolvedByGuessing++;
    }
    
    public double getCompletionPercentage() {
        return completionPercentage;
    }
    
    public void setCompletionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
    
    public boolean isSolved() {
        return solved;
    }
    
    public void setSolved(boolean solved) {
        this.solved = solved;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
   
    public void printSummary() {
        System.out.println("=== STATISTIQUES DE RÉSOLUTION ===");
        System.out.println("Résolu : " + (solved ? "✅ OUI" : "❌ NON"));
        System.out.println("Temps : " + executionTimeMs + " ms");
        System.out.println("Étapes totales : " + totalSteps);
        System.out.println("Backtracks : " + backtrackCount);
        System.out.println("Cases par déduction : " + cellsSolvedByDeduction);
        System.out.println("Cases par essai : " + cellsSolvedByGuessing);
        System.out.println("Complétion : " + String.format("%.1f", completionPercentage) + "%");
        
        if (!solved && !errorMessage.isEmpty()) {
            System.out.println("Erreur : " + errorMessage);
        }
    }
    
   
    public String toCSV() {
        return String.format("%b,%d,%d,%d,%d,%d,%.2f",
            solved, executionTimeMs, totalSteps, backtrackCount,
            cellsSolvedByDeduction, cellsSolvedByGuessing, completionPercentage);
    }
    
    public static String getCSVHeader() {
        return "Solved,Time(ms),Steps,Backtracks,Deduction,Guessing,Completion(%)";
    }
}
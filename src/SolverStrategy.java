/**
 * INTERFACE POUR TOUTES LES STRATÉGIES DE RÉSOLUTION
 * 
 * Chaque stratégie doit implémenter cette interface pour :
 * - Uniformiser l'API (toutes les stratégies ont les mêmes méthodes)
 * - Permettre la comparaison équitable
 * - Faciliter l'ajout de nouvelles stratégies
 */
public interface SolverStrategy {
    
    /**
     * RÉSOUDRE UN PUZZLE
     * 
     * @param nonogram Le puzzle à résoudre
     * @return true si résolu avec succès, false sinon
     */
    boolean solve(Nonogram nonogram);
    
    /**
     * OBTENIR LE NOM DE LA STRATÉGIE
     * 
     * @return Nom descriptif (ex: "Backtracking Simple")
     */
    String getName();
    
    /**
     * OBTENIR LES STATISTIQUES DE LA DERNIÈRE RÉSOLUTION
     * 
     * @return Objet contenant toutes les métriques
     */
    SolverStatistics getStatistics();
    
    /**
     * RÉINITIALISER LES STATISTIQUES
     * Appelé avant chaque nouvelle résolution
     */
    void resetStatistics();
    
    void setStepByStepMode(boolean enabled);
    boolean executeNextStep(Nonogram nonogram);
    boolean hasNextStep();
    int getCurrentStep();
}

/**
 * CLASSE POUR STOCKER LES STATISTIQUES D'UNE RÉSOLUTION
 * 
 * Contient toutes les métriques pour comparer les stratégies :
 * - Temps d'exécution
 * - Nombre d'étapes
 * - Nombre de backtracks
 * - Mémoire utilisée
 * - etc.
 */
class SolverStatistics {
    // Temps d'exécution en millisecondes
    private long executionTimeMs;
    
    // Nombre total d'étapes/itérations
    private int totalSteps;
    
    // Nombre de retours en arrière (backtracks)
    private int backtrackCount;
    
    // Nombre de cases résolues par déduction
    private int cellsSolvedByDeduction;
    
    // Nombre de cases résolues par essai-erreur
    private int cellsSolvedByGuessing;
    
    // Pourcentage du puzzle résolu (0-100)
    private double completionPercentage;
    
    // Puzzle résolu avec succès ?
    private boolean solved;
    
    // Message d'erreur si échec
    private String errorMessage;
    
    // ========== CONSTRUCTEUR ==========
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
    
    // ========== GETTERS ET SETTERS ==========
    
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
    
    // ========== MÉTHODES UTILITAIRES ==========
    
    /**
     * Affiche un résumé des statistiques
     */
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
    
    /**
     * Retourne une ligne CSV avec toutes les stats
     */
    public String toCSV() {
        return String.format("%b,%d,%d,%d,%d,%d,%.2f",
            solved, executionTimeMs, totalSteps, backtrackCount,
            cellsSolvedByDeduction, cellsSolvedByGuessing, completionPercentage);
    }
    
    /**
     * Header CSV pour les exports
     */
    public static String getCSVHeader() {
        return "Solved,Time(ms),Steps,Backtracks,Deduction,Guessing,Completion(%)";
    }
}
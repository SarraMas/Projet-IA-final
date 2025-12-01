/**
 * PROGRAMME DE TEST RAPIDE POUR LES STRATÉGIES
 */
public class TestStrategies {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("TEST DES STRATÉGIES DE RÉSOLUTION");
        System.out.println("=".repeat(60));
        System.out.println();
        
        // Créer un puzzle de test simple (5x5)
        int[][] rowClues = {{1}, {3}, {5}, {3}, {1}};
        int[][] colClues = {{1}, {3}, {5}, {3}, {1}};
        
        CellState[][] solution = {
            {CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY, CellState.EMPTY},
            {CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.EMPTY, CellState.EMPTY},
            {CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED},
            {CellState.EMPTY, CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.EMPTY},
            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY}
        };
        
        LineClues clues = new LineClues(rowClues, colClues);
        
        // Test 1 : SimpleLineSolver
        System.out.println("📊 TEST 1 : SimpleLineSolver");
        System.out.println("-".repeat(60));
        
        Nonogram puzzle1 = new Nonogram(5, 5, clues, solution);
        SimpleLineSolver solver1 = new SimpleLineSolver();
        
        boolean solved1 = solver1.solve(puzzle1);
        SolverStatistics stats1 = solver1.getStatistics();
        
        System.out.println("Résultat : " + (solved1 ? "✅ RÉSOLU" : "❌ ÉCHEC"));
        stats1.printSummary();
        System.out.println();
        
        // Afficher la grille résolue
        System.out.println("Grille résolue par SimpleLineSolver :");
        printGrid(puzzle1);
        System.out.println();
        
        // Test 2 : BacktrackingSolver
        System.out.println("📊 TEST 2 : BacktrackingSolver");
        System.out.println("-".repeat(60));
        
        Nonogram puzzle2 = new Nonogram(5, 5, clues, solution);
        BacktrackingSolver solver2 = new BacktrackingSolver();
        
        boolean solved2 = solver2.solve(puzzle2);
        SolverStatistics stats2 = solver2.getStatistics();
        
        System.out.println("Résultat : " + (solved2 ? "✅ RÉSOLU" : "❌ ÉCHEC"));
        stats2.printSummary();
        System.out.println();
        
        // Afficher la grille résolue
        System.out.println("Grille résolue par BacktrackingSolver :");
        printGrid(puzzle2);
        System.out.println();
        
        // Test 3 : Puzzle plus difficile (nécessite backtracking)
        System.out.println("📊 TEST 3 : Puzzle difficile");
        System.out.println("-".repeat(60));
        
        int[][] rowClues3 = {{1, 1}, {1, 1}, {5}, {1, 1}, {2, 2}};
        int[][] colClues3 = {{1, 1}, {1, 2}, {5}, {1, 1}, {2, 1}};
        
        CellState[][] solution3 = {
            {CellState.FILLED, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.FILLED},
            {CellState.FILLED, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.FILLED},
            {CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED},
            {CellState.FILLED, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.FILLED},
            {CellState.FILLED, CellState.FILLED, CellState.EMPTY, CellState.FILLED, CellState.FILLED}
        };
        
        LineClues clues3 = new LineClues(rowClues3, colClues3);
        
        // Test avec SimpleLineSolver
        Nonogram puzzle3a = new Nonogram(5, 5, clues3, solution3);
        SimpleLineSolver solver3a = new SimpleLineSolver();
        boolean solved3a = solver3a.solve(puzzle3a);
        
        System.out.println("SimpleLineSolver : " + (solved3a ? "✅ RÉSOLU" : "⚠️ BLOQUÉ"));
        System.out.println("  Complétion : " + 
            String.format("%.1f", solver3a.getStatistics().getCompletionPercentage()) + "%");
        System.out.println();
        
        // Test avec BacktrackingSolver
        Nonogram puzzle3b = new Nonogram(5, 5, clues3, solution3);
        BacktrackingSolver solver3b = new BacktrackingSolver();
        boolean solved3b = solver3b.solve(puzzle3b);
        
        System.out.println("BacktrackingSolver : " + (solved3b ? "✅ RÉSOLU" : "❌ ÉCHEC"));
        solver3b.getStatistics().printSummary();
        System.out.println();
        
        // Résumé final
        System.out.println("=".repeat(60));
        System.out.println("📊 RÉSUMÉ DES TESTS");
        System.out.println("=".repeat(60));
        System.out.println("SimpleLineSolver :");
        System.out.println("  ✅ Rapide et sans backtracking");
        System.out.println("  ⚠️ Ne résout pas tous les puzzles");
        System.out.println();
        System.out.println("BacktrackingSolver :");
        System.out.println("  ✅ Résout tous les puzzles avec solution unique");
        System.out.println("  ⚠️ Plus lent (utilise essai-erreur)");
        System.out.println("=".repeat(60));
    }
    
    /**
     * Affiche une grille de façon lisible
     */
    private static void printGrid(Nonogram puzzle) {
        for (int row = 0; row < puzzle.getHeight(); row++) {
            for (int col = 0; col < puzzle.getWidth(); col++) {
                switch (puzzle.getCell(row, col)) {
                    case EMPTY: System.out.print("□ "); break;
                    case FILLED: System.out.print("■ "); break;
                    case CROSSED: System.out.print("× "); break;
                }
            }
            System.out.println();
        }
    }
}
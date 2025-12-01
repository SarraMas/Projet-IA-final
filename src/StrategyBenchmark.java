import java.util.*;
import java.io.*;

/**
 * SYSTÈME DE BENCHMARK POUR COMPARER LES STRATÉGIES
 * 
 * Permet de :
 * - Tester plusieurs stratégies sur le même ensemble de puzzles
 * - Comparer les performances (temps, étapes, backtracks, etc.)
 * - Générer des rapports et graphiques
 * - Exporter les résultats en CSV
 */
public class StrategyBenchmark {
    
    private List<SolverStrategy> strategies;
    private List<Nonogram> testPuzzles;
    private Map<String, List<SolverStatistics>> results;
    
    public StrategyBenchmark() {
        this.strategies = new ArrayList<>();
        this.testPuzzles = new ArrayList<>();
        this.results = new HashMap<>();
    }
    
    /**
     * AJOUTER UNE STRATÉGIE À TESTER
     */
    public void addStrategy(SolverStrategy strategy) {
        strategies.add(strategy);
        results.put(strategy.getName(), new ArrayList<>());
    }
    
    /**
     * AJOUTER UN PUZZLE DE TEST
     */
    public void addTestPuzzle(Nonogram puzzle) {
        testPuzzles.add(puzzle);
    }
    
    /**
     * GÉNÉRER UN ENSEMBLE DE PUZZLES DE TEST
     * 
     * @param count Nombre de puzzles à générer
     * @param sizes Tailles possibles (ex: [5, 6, 7, 8])
     */
    public void generateTestPuzzles(int count, int[] sizes) {
        System.out.println("🎲 Génération de " + count + " puzzles de test...");
        Random random = new Random();
        
        for (int i = 0; i < count; i++) {
            // Choisir une taille aléatoire
            int size = sizes[random.nextInt(sizes.length)];
            
            // Générer un puzzle valide
            Nonogram puzzle = generateValidPuzzle(size);
            
            if (puzzle != null) {
                testPuzzles.add(puzzle);
                System.out.println("  ✅ Puzzle " + (i + 1) + "/" + count + " : " + size + "×" + size);
            } else {
                System.out.println("  ⚠️ Échec génération puzzle " + (i + 1));
                i--; // Réessayer
            }
        }
        
        System.out.println("✅ " + testPuzzles.size() + " puzzles générés\n");
    }
    
    /**
     * GÉNÉRER UN PUZZLE VALIDE avec solution unique
     */
    private Nonogram generateValidPuzzle(int size) {
        Random random = new Random();
        int maxAttempts = 50;
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Générer solution aléatoire
            CellState[][] solution = new CellState[size][size];
            double density = 0.3 + random.nextDouble() * 0.2;
            
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    solution[i][j] = random.nextDouble() < density ? 
                        CellState.FILLED : CellState.EMPTY;
                }
            }
            
            // Garantir au moins une case par ligne/colonne
            for (int i = 0; i < size; i++) {
                boolean hasFilledRow = false;
                boolean hasFilledCol = false;
                
                for (int j = 0; j < size; j++) {
                    if (solution[i][j] == CellState.FILLED) hasFilledRow = true;
                    if (solution[j][i] == CellState.FILLED) hasFilledCol = true;
                }
                
                if (!hasFilledRow) solution[i][random.nextInt(size)] = CellState.FILLED;
                if (!hasFilledCol) solution[random.nextInt(size)][i] = CellState.FILLED;
            }
            
            // Calculer les indices
            int[][] rowClues = calculateRowClues(solution, size);
            int[][] colClues = calculateColClues(solution, size);
            LineClues clues = new LineClues(rowClues, colClues);
            
            // Vérifier solution unique
            if (PuzzleValidator.hasUniqueSolution(clues, size, size)) {
                return new Nonogram(size, size, clues, solution);
            }
        }
        
        return null; // Échec
    }
    
    /**
     * LANCER LE BENCHMARK
     * Teste toutes les stratégies sur tous les puzzles
     */
    public void runBenchmark() {
        System.out.println("🚀 DÉBUT DU BENCHMARK");
        System.out.println("Stratégies : " + strategies.size());
        System.out.println("Puzzles : " + testPuzzles.size());
        System.out.println("=" .repeat(60) + "\n");
        
        int totalTests = strategies.size() * testPuzzles.size();
        int currentTest = 0;
        
        // Pour chaque stratégie
        for (SolverStrategy strategy : strategies) {
            System.out.println("📊 Test de : " + strategy.getName());
            System.out.println("-".repeat(60));
            
            List<SolverStatistics> strategyResults = results.get(strategy.getName());
            
            // Tester sur chaque puzzle
            for (int i = 0; i < testPuzzles.size(); i++) {
                currentTest++;
                Nonogram puzzle = testPuzzles.get(i);
                
                // Créer une copie du puzzle (pour ne pas modifier l'original)
                Nonogram puzzleCopy = copyPuzzle(puzzle);
                
                // Résoudre avec cette stratégie
                System.out.print("  Puzzle " + (i + 1) + "/" + testPuzzles.size() + 
                               " (" + puzzle.getWidth() + "×" + puzzle.getHeight() + ") ... ");
                
                strategy.resetStatistics();
                boolean solved = strategy.solve(puzzleCopy);
                
                SolverStatistics stats = strategy.getStatistics();
                strategyResults.add(stats);
                
                // Afficher le résultat
                String status = solved ? "✅ OK" : "❌ FAIL";
                System.out.println(status + " (" + stats.getExecutionTimeMs() + "ms, " + 
                                 stats.getTotalSteps() + " étapes)");
            }
            
            System.out.println();
        }
        
        System.out.println("✅ BENCHMARK TERMINÉ\n");
    }
    
    /**
     * AFFICHER LE RAPPORT COMPARATIF
     */
    public void printReport() {
        System.out.println("=" .repeat(80));
        System.out.println("📊 RAPPORT COMPARATIF DES STRATÉGIES");
        System.out.println("=" .repeat(80));
        System.out.println();
        
        for (SolverStrategy strategy : strategies) {
            List<SolverStatistics> stats = results.get(strategy.getName());
            
            System.out.println("🔹 " + strategy.getName());
            System.out.println("-".repeat(80));
            
            // Calculer les moyennes
            int totalSolved = 0;
            long totalTime = 0;
            int totalSteps = 0;
            int totalBacktracks = 0;
            double totalCompletion = 0;
            
            for (SolverStatistics stat : stats) {
                if (stat.isSolved()) totalSolved++;
                totalTime += stat.getExecutionTimeMs();
                totalSteps += stat.getTotalSteps();
                totalBacktracks += stat.getBacktrackCount();
                totalCompletion += stat.getCompletionPercentage();
            }
            
            int count = stats.size();
            
            System.out.println("  Taux de réussite    : " + totalSolved + "/" + count + 
                             " (" + (totalSolved * 100.0 / count) + "%)");
            System.out.println("  Temps moyen         : " + (totalTime / count) + " ms");
            System.out.println("  Étapes moyennes     : " + (totalSteps / count));
            System.out.println("  Backtracks moyens   : " + (totalBacktracks / count));
            System.out.println("  Complétion moyenne  : " + 
                             String.format("%.1f", totalCompletion / count) + "%");
            System.out.println();
        }
        
        System.out.println("=" .repeat(80));
    }
    
    /**
     * EXPORTER LES RÉSULTATS EN CSV
     */
    public void exportToCSV(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Header
            writer.println("Strategy,Puzzle,Size," + SolverStatistics.getCSVHeader());
            
            // Données
            for (SolverStrategy strategy : strategies) {
                List<SolverStatistics> stats = results.get(strategy.getName());
                
                for (int i = 0; i < stats.size(); i++) {
                    Nonogram puzzle = testPuzzles.get(i);
                    writer.println(strategy.getName() + "," + 
                                 (i + 1) + "," + 
                                 puzzle.getWidth() + "," + 
                                 stats.get(i).toCSV());
                }
            }
            
            System.out.println("✅ Résultats exportés dans : " + filename);
            
        } catch (IOException e) {
            System.err.println("❌ Erreur export CSV : " + e.getMessage());
        }
    }
    
    // ========== MÉTHODES UTILITAIRES ==========
    
    private int[][] calculateRowClues(CellState[][] solution, int size) {
        int[][] rowClues = new int[size][];
        
        for (int i = 0; i < size; i++) {
            List<Integer> clues = new ArrayList<>();
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
            if (clues.isEmpty()) clues.add(0);
            
            rowClues[i] = clues.stream().mapToInt(Integer::intValue).toArray();
        }
        
        return rowClues;
    }
    
    private int[][] calculateColClues(CellState[][] solution, int size) {
        int[][] colClues = new int[size][];
        
        for (int j = 0; j < size; j++) {
            List<Integer> clues = new ArrayList<>();
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
            if (clues.isEmpty()) clues.add(0);
            
            colClues[j] = clues.stream().mapToInt(Integer::intValue).toArray();
        }
        
        return colClues;
    }
    
    private Nonogram copyPuzzle(Nonogram original) {
        return new Nonogram(
            original.getWidth(),
            original.getHeight(),
            original.getClues(),
            original.getSolution()
        );
    }
    
    /**
     * MAIN DE TEST
     */
    public static void main(String[] args) {
        StrategyBenchmark benchmark = new StrategyBenchmark();
        
        // Ajouter les stratégies à tester
        benchmark.addStrategy(new SimpleLineSolver());
        benchmark.addStrategy(new BacktrackingSolver());
        
        // Générer des puzzles de test
        int[] sizes = {5, 6, 7, 8};
        benchmark.generateTestPuzzles(10, sizes); // 10 puzzles
        
        // Lancer le benchmark
        benchmark.runBenchmark();
        
        // Afficher le rapport
        benchmark.printReport();
        
        // Exporter en CSV
        benchmark.exportToCSV("benchmark_results.csv");
    }
}
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 📊 BENCHMARK COMPLET - VERSION OPTIMISÉE
 * Génère 150 puzzles VALIDES et teste toutes les stratégies
 */
public class BenchmarkComplet {
    
    private List<SolverStrategy> strategies;
    private List<Nonogram> puzzles150;
    private Map<String, List<SolverStatistics>> resultats;
    
    public BenchmarkComplet() {
        this.strategies = new ArrayList<>();
        this.puzzles150 = new ArrayList<>();
        this.resultats = new HashMap<>();
    }
    
    public void ajouterStrategies() {
        System.out.println("📋 Ajout des stratégies...");
        
        strategies.add(new SimpleLineSolver());
        strategies.add(new LogicStrategy());
        strategies.add(new RandomStrategy(3000));
        strategies.add(new BacktrackingSolver());
        strategies.add(new AIHeuristicStrategy());
        
        for (SolverStrategy s : strategies) {
            resultats.put(s.getName(), new ArrayList<>());
            System.out.println("  ✅ " + s.getName());
        }
        
        System.out.println();
    }
    
    /**
     * 🎲 GÉNÉRATION OPTIMISÉE - Distribution progressive
     */
    public void generer150Puzzles() {
        System.out.println("🎲 GÉNÉRATION DE 150 PUZZLES");
        System.out.println("=".repeat(60));
        
        Random random = new Random(42);
        int generes = 0;
        int tentatives = 0;
        int maxTentatives = 3000;
        
        // Distribution : beaucoup de petits, quelques moyens
        int[] distribution = {
            3,3,3,3,3,3,3,3,3,3, // 10× 3×3
            4,4,4,4,4,4,4,4,4,4,4,4,4,4,4, // 15× 4×4
            4,4,4,4,4,4,4,4,4,4, // 10× 4×4
            5,5,5,5,5,5,5,5,5,5,5,5,5,5,5, // 15× 5×5
            5,5,5,5,5,5,5,5,5,5,5,5,5,5,5, // 15× 5×5
            5,5,5,5,5, // 5× 5×5
            6,6,6,6,6,6,6,6,6,6, // 10× 6×6
            6,6,6,6,6,6,6,6,6,6, // 10× 6×6
            7,7,7,7,7,7,7,7,7,7 // 10× 7×7
        };
        
        // Compléter jusqu'à 150
        List<Integer> tailles = new ArrayList<>();
        for (int t : distribution) {
            tailles.add(t);
        }
        while (tailles.size() < 150) {
            tailles.add(5); // Remplir avec du 5×5
        }
        
        Collections.shuffle(tailles, random);
        
        while (generes < 150 && tentatives < maxTentatives) {
            tentatives++;
            int taille = tailles.get(generes);
            
            Nonogram puzzle = genererPuzzleValide(taille, random);
            
            if (puzzle != null) {
                puzzles150.add(puzzle);
                generes++;
                
                if (generes % 10 == 0) {
                    System.out.println("  ✅ " + generes + "/150 puzzles générés");
                }
            }
            
            if (tentatives % 100 == 0) {
                System.out.println("  ⏳ Tentatives : " + tentatives + " | Générés : " + generes);
            }
        }
        
        System.out.println("✅ GÉNÉRATION TERMINÉE : " + puzzles150.size() + " puzzles\n");
    }
    
    /**
     * 🏃 BENCHMARK avec timeout par puzzle
     */
    public void executerBenchmark() {
        System.out.println("🚀 EXÉCUTION DU BENCHMARK");
        System.out.println("=".repeat(60));
        System.out.println("Puzzles : " + puzzles150.size());
        System.out.println("Stratégies : " + strategies.size());
        System.out.println();
        
        for (int s = 0; s < strategies.size(); s++) {
            SolverStrategy strategy = strategies.get(s);
            
            System.out.println("📊 TEST : " + strategy.getName());
            System.out.println("-".repeat(60));
            
            List<SolverStatistics> statsStrategie = resultats.get(strategy.getName());
            
            for (int p = 0; p < puzzles150.size(); p++) {
                Nonogram puzzle = puzzles150.get(p);
                Nonogram copie = copierPuzzle(puzzle);
                
                if (p % 30 == 0) {
                    System.out.println("  📌 Puzzle " + (p+1) + "/150");
                }
                
                strategy.resetStatistics();
                
                // Timeout adaptatif selon la taille
                int taille = puzzle.getWidth();
                long timeout = taille <= 5 ? 30000 : taille <= 7 ? 60000 : 120000;
                
                boolean resolu = resoudreAvecTimeout(strategy, copie, timeout);
                
                SolverStatistics stats = strategy.getStatistics();
                statsStrategie.add(stats);
            }
            
            System.out.println("  ✅ Stratégie terminée\n");
        }
        
        System.out.println("✅ BENCHMARK TERMINÉ\n");
    }
    
    /**
     * 📈 RÉSULTATS COMPLETS
     */
    public void afficherResultatsComplets() {
        System.out.println("=".repeat(80));
        System.out.println("📊 RÉSULTATS COMPLETS - 150 PUZZLES");
        System.out.println("=".repeat(80));
        System.out.println();
        
        for (SolverStrategy strategy : strategies) {
            String nom = strategy.getName();
            List<SolverStatistics> stats = resultats.get(nom);
            
            System.out.println("🔹 " + nom);
            System.out.println("-".repeat(80));
            
            int resolus = 0;
            long tempsTotal = 0;
            long noeudsTotal = 0;
            int backtracksTotal = 0;
            double completionTotal = 0;
            
            long tempsMin = Long.MAX_VALUE;
            long tempsMax = 0;
            
            for (SolverStatistics s : stats) {
                if (s.isSolved()) resolus++;
                
                long temps = s.getExecutionTimeMs();
                tempsTotal += temps;
                tempsMin = Math.min(tempsMin, temps);
                tempsMax = Math.max(tempsMax, temps);
                
                noeudsTotal += s.getTotalSteps();
                backtracksTotal += s.getBacktrackCount();
                completionTotal += s.getCompletionPercentage();
            }
            
            int total = stats.size();
            double tauxReussite = (resolus * 100.0) / total;
            
            System.out.println("  ✅ Taux de réussite     : " + resolus + "/" + total + 
                             " (" + String.format("%.1f", tauxReussite) + "%)");
            System.out.println("  ⏱️  Temps TOTAL         : " + tempsTotal + " ms");
            System.out.println("  📊 Temps MOYEN         : " + (tempsTotal / total) + " ms");
            System.out.println("  ⚡ Temps MIN           : " + tempsMin + " ms");
            System.out.println("  🌟 Temps MAX           : " + tempsMax + " ms");
            System.out.println("  🔢 Nœuds TOTAL         : " + noeudsTotal);
            System.out.println("  📈 Nœuds MOYEN         : " + (noeudsTotal / total));
            System.out.println("  🔄 Backtracks TOTAL    : " + backtracksTotal);
            System.out.println("  📊 Complétion MOYENNE  : " + 
                             String.format("%.1f", completionTotal / total) + "%");
            System.out.println();
        }
        
        afficherClassement();
        
        System.out.println("=".repeat(80));
    }
    
    /**
     * 🏆 CLASSEMENT
     */
    private void afficherClassement() {
        System.out.println("🏆 CLASSEMENT DES STRATÉGIES");
        System.out.println("-".repeat(80));
        
        List<ClassementEntry> classement = new ArrayList<>();
        
        for (SolverStrategy strategy : strategies) {
            String nom = strategy.getName();
            List<SolverStatistics> stats = resultats.get(nom);
            
            int resolus = 0;
            long tempsTotal = 0;
            
            for (SolverStatistics s : stats) {
                if (s.isSolved()) resolus++;
                tempsTotal += s.getExecutionTimeMs();
            }
            
            double tauxReussite = (resolus * 100.0) / stats.size();
            long tempsMoyen = tempsTotal / stats.size();
            double score = (tauxReussite * 1000) - tempsMoyen;
            
            classement.add(new ClassementEntry(nom, resolus, tempsTotal, score, tauxReussite));
        }
        
        classement.sort((a, b) -> Double.compare(b.score, a.score));
        
        for (int i = 0; i < classement.size(); i++) {
            ClassementEntry e = classement.get(i);
            String medaille = i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : "  ";
            
            System.out.println(medaille + " " + (i+1) + ". " + e.nom);
            System.out.println("     Résolus: " + e.resolus + "/150 (" + 
                             String.format("%.1f", e.tauxReussite) + "%) | " +
                             "Temps: " + e.tempsTotal + "ms | " +
                             "Score: " + String.format("%.0f", e.score));
        }
        
        System.out.println();
    }
    
    /**
     * 💾 EXPORT CSV
     */
    public void exporterCSV(String fichier) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fichier, StandardCharsets.UTF_8))) {
            writer.println("Strategie;Puzzle;Taille;Resolu;Temps_ms;Noeuds;Backtracks;Completion_%");
            
            for (SolverStrategy strategy : strategies) {
                String nom = cleanCSV(strategy.getName());
                List<SolverStatistics> stats = resultats.get(strategy.getName());
                
                for (int i = 0; i < stats.size(); i++) {
                    Nonogram puzzle = puzzles150.get(i);
                    SolverStatistics s = stats.get(i);
                    
                    writer.println(
                        nom + ";" +
                        (i+1) + ";" +
                        puzzle.getWidth() + ";" +
                        (s.isSolved() ? "OUI" : "NON") + ";" +
                        s.getExecutionTimeMs() + ";" +
                        s.getTotalSteps() + ";" +
                        s.getBacktrackCount() + ";" +
                        String.format("%.2f", s.getCompletionPercentage()).replace(",", ".")
                    );
                }
            }
            
            System.out.println("✅ Résultats exportés dans : " + fichier);
            
        } catch (IOException e) {
            System.err.println("❌ Erreur export : " + e.getMessage());
        }
    }
    
    public void exporterResume(String fichier) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fichier, StandardCharsets.UTF_8))) {
            writer.println("Strategie;Taux_Reussite_%;Temps_Total_ms;Temps_Moyen_ms;Temps_Min_ms;Temps_Max_ms;" +
                         "Noeuds_Total;Noeuds_Moyen;Backtracks_Total;Completion_Moyenne_%;Score");
            
            for (SolverStrategy strategy : strategies) {
                String nom = cleanCSV(strategy.getName());
                List<SolverStatistics> stats = resultats.get(strategy.getName());
                
                int resolus = 0;
                long tempsTotal = 0;
                long noeudsTotal = 0;
                int backtracksTotal = 0;
                double completionTotal = 0;
                
                long tempsMin = Long.MAX_VALUE;
                long tempsMax = 0;
                
                for (SolverStatistics s : stats) {
                    if (s.isSolved()) resolus++;
                    
                    long temps = s.getExecutionTimeMs();
                    tempsTotal += temps;
                    tempsMin = Math.min(tempsMin, temps);
                    tempsMax = Math.max(tempsMax, temps);
                    
                    noeudsTotal += s.getTotalSteps();
                    backtracksTotal += s.getBacktrackCount();
                    completionTotal += s.getCompletionPercentage();
                }
                
                int total = stats.size();
                double tauxReussite = (resolus * 100.0) / total;
                long tempsMoyen = tempsTotal / total;
                double score = (tauxReussite * 1000) - tempsMoyen;
                
                writer.println(
                    nom + ";" +
                    String.format("%.2f", tauxReussite).replace(",", ".") + ";" +
                    tempsTotal + ";" +
                    tempsMoyen + ";" +
                    tempsMin + ";" +
                    tempsMax + ";" +
                    noeudsTotal + ";" +
                    (noeudsTotal / total) + ";" +
                    backtracksTotal + ";" +
                    String.format("%.2f", completionTotal / total).replace(",", ".") + ";" +
                    String.format("%.2f", score).replace(",", ".")
                );
            }
            
            System.out.println("✅ Résumé exporté dans : " + fichier);
            
        } catch (IOException e) {
            System.err.println("❌ Erreur export : " + e.getMessage());
        }
    }

    private String cleanCSV(String text) {
        return text.replace(",", " -").replace(";", " -");
    }
    
    // ========== GÉNÉRATION DE PUZZLES VALIDES ==========
    
    /**
     * 🔥 GÉNÉRATION OPTIMISÉE avec validation stricte
     */
    private Nonogram genererPuzzleValide(int taille, Random random) {
        int maxTentatives = 200;
        
        for (int t = 0; t < maxTentatives; t++) {
            CellState[][] solution = new CellState[taille][taille];
            
            // Densité adaptative
            double densite = 0.25 + random.nextDouble() * 0.3;
            
            for (int i = 0; i < taille; i++) {
                for (int j = 0; j < taille; j++) {
                    solution[i][j] = random.nextDouble() < densite ? 
                        CellState.FILLED : CellState.EMPTY;
                }
            }
            
            // S'assurer qu'il y a des cases remplies (mais pas forcément partout)
            for (int i = 0; i < taille; i++) {
                boolean ligneOk = false, colOk = false;
                for (int j = 0; j < taille; j++) {
                    if (solution[i][j] == CellState.FILLED) ligneOk = true;
                    if (solution[j][i] == CellState.FILLED) colOk = true;
                }
                // Ne forcer que si vraiment vide ET aléatoirement
                if (!ligneOk && random.nextDouble() > 0.4) {
                    solution[i][random.nextInt(taille)] = CellState.FILLED;
                }
                if (!colOk && random.nextDouble() > 0.4) {
                    solution[random.nextInt(taille)][i] = CellState.FILLED;
                }
            }
            
            int[][] rowClues = calculerIndicesLignes(solution, taille);
            int[][] colClues = calculerIndicesColonnes(solution, taille);
            LineClues clues = new LineClues(rowClues, colClues);
            
            if (PuzzleValidator.hasUniqueSolution(clues, taille, taille)) {
                return new Nonogram(taille, taille, clues, solution);
            }
        }
        
        return null;
    }
    
    private boolean resoudreAvecTimeout(SolverStrategy strategy, Nonogram puzzle, long timeoutMs) {
        final boolean[] resultat = {false};
        
        Thread thread = new Thread(() -> {
            resultat[0] = strategy.solve(puzzle);
        });
        
        thread.start();
        
        try {
            thread.join(timeoutMs);
            if (thread.isAlive()) {
                thread.interrupt();
                return false;
            }
        } catch (InterruptedException e) {
            return false;
        }
        
        return resultat[0];
    }
    
    private int[][] calculerIndicesLignes(CellState[][] solution, int taille) {
        int[][] indices = new int[taille][];
        for (int i = 0; i < taille; i++) {
            List<Integer> clues = new ArrayList<>();
            int count = 0;
            for (int j = 0; j < taille; j++) {
                if (solution[i][j] == CellState.FILLED) {
                    count++;
                } else if (count > 0) {
                    clues.add(count);
                    count = 0;
                }
            }
            if (count > 0) clues.add(count);
            indices[i] = clues.isEmpty() ? new int[0] : 
                         clues.stream().mapToInt(Integer::intValue).toArray();
        }
        return indices;
    }
    
    private int[][] calculerIndicesColonnes(CellState[][] solution, int taille) {
        int[][] indices = new int[taille][];
        for (int j = 0; j < taille; j++) {
            List<Integer> clues = new ArrayList<>();
            int count = 0;
            for (int i = 0; i < taille; i++) {
                if (solution[i][j] == CellState.FILLED) {
                    count++;
                } else if (count > 0) {
                    clues.add(count);
                    count = 0;
                }
            }
            if (count > 0) clues.add(count);
            indices[j] = clues.isEmpty() ? new int[0] : 
                         clues.stream().mapToInt(Integer::intValue).toArray();
        }
        return indices;
    }
    
    private Nonogram copierPuzzle(Nonogram original) {
        return new Nonogram(
            original.getWidth(),
            original.getHeight(),
            original.getClues(),
            original.getSolution()
        );
    }
    
    private static class ClassementEntry {
        String nom;
        int resolus;
        long tempsTotal;
        double score;
        double tauxReussite;
        
        ClassementEntry(String nom, int resolus, long tempsTotal, double score, double tauxReussite) {
            this.nom = nom;
            this.resolus = resolus;
            this.tempsTotal = tempsTotal;
            this.score = score;
            this.tauxReussite = tauxReussite;
        }
    }
    
    public static void main(String[] args) {
        BenchmarkComplet benchmark = new BenchmarkComplet();
        
        System.out.println("🎯 BENCHMARK COMPLET - 150 PUZZLES");
        System.out.println("=".repeat(80));
        System.out.println();
        
        benchmark.ajouterStrategies();
        benchmark.generer150Puzzles();
        benchmark.executerBenchmark();
        benchmark.afficherResultatsComplets();
        
        benchmark.exporterCSV("resultats_detailles.csv");
        benchmark.exporterResume("resultats_resume.csv");
        
        System.out.println("\n✅ BENCHMARK TERMINÉ !");
    }
}
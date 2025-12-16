import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 📊 SYSTÈME DE BENCHMARK COMPLET
 * 
 * Génère 150 puzzles et teste toutes les stratégies
 * Compare : temps, nombre de nœuds explorés, taux de réussite
 */
public class BenchmarkComplet {
    
    private List<SolverStrategy> strategies;
    private List<Nonogram> puzzles150;
    
    // Résultats : Map<Nom Stratégie, Liste des stats pour chaque puzzle>
    private Map<String, List<SolverStatistics>> resultats;
    
    public BenchmarkComplet() {
        this.strategies = new ArrayList<>();
        this.puzzles150 = new ArrayList<>();
        this.resultats = new HashMap<>();
    }
    
    /**
     * 🎯 ÉTAPE 1 : Ajouter toutes les stratégies à tester
     */
    public void ajouterStrategies() {
        System.out.println("📋 Ajout des stratégies...");
        
        strategies.add(new SimpleLineSolver());
        strategies.add(new LogicStrategy());
        strategies.add(new RandomStrategy(5000)); // Limité pour le benchmark
        strategies.add(new BacktrackingSolver());
        strategies.add(new AIHeuristicStrategy()); // 🤖 NOTRE IA - Doit être la meilleure !
        
        // Initialiser les résultats
        for (SolverStrategy s : strategies) {
            resultats.put(s.getName(), new ArrayList<>());
            System.out.println("  ✅ " + s.getName());
        }
        
        System.out.println();
    }
    
    /**
     * 🎲 ÉTAPE 2 : Générer 150 puzzles valides
     */
    public void generer150Puzzles() {
        System.out.println("🎲 GÉNÉRATION DE 150 PUZZLES");
        System.out.println("=".repeat(60));
        
        // Distribution des tailles optimisée
        int[] tailles = {
                4, 4, 4, 4, 4, 4, 4, 4, 4, 4,  // 10x 4x4 (très facile)
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5,  // 10x 5x5 (facile)
                5, 5, 5, 5, 5, 5, 5, 5, 5, 5,  // 10x 5x5 supplémentaires
                6, 6, 6, 6, 6, 6, 6, 6, 6, 6,  // 10x 6x6 (moyen)
                6, 6, 6, 6, 6                   // 5x 6x6 supplémentaires
            };
        
        // Répéter le pattern pour atteindre 150
        List<Integer> taillesList = new ArrayList<>();
        while (taillesList.size() < 150) {
            for (int t : tailles) {
                if (taillesList.size() < 150) {
                    taillesList.add(5);
                }
            }
        }
        
        Random random = new Random(42); // Seed fixe pour reproductibilité
        int generes = 0;
        int tentatives = 0;
        
        while (generes < 150 && tentatives < 2000) {
            tentatives++;
            int taille = taillesList.get(generes);
            
            Nonogram puzzle = genererPuzzleValide(taille, random);
            
            if (puzzle != null) {
                puzzles150.add(puzzle);
                generes++;
                
                if (generes % 10 == 0) {
                    System.out.println("  ✅ " + generes + "/150 puzzles générés");
                }
            }
        }
        
        System.out.println("✅ GÉNÉRATION TERMINÉE : " + puzzles150.size() + " puzzles\n");
    }
    
    /**
     * 🏃 ÉTAPE 3 : Exécuter tous les tests
     */
    public void executerBenchmark() {
        System.out.println("🚀 EXÉCUTION DU BENCHMARK");
        System.out.println("=".repeat(60));
        System.out.println("Puzzles : " + puzzles150.size());
        System.out.println("Stratégies : " + strategies.size());
        System.out.println("Tests totaux : " + (puzzles150.size() * strategies.size()));
        System.out.println();
        
        int testTotal = 0;
        int testMax = puzzles150.size() * strategies.size();
        
        // Pour chaque stratégie
        for (int s = 0; s < strategies.size(); s++) {
            SolverStrategy strategy = strategies.get(s);
            
            System.out.println("📊 TEST : " + strategy.getName());
            System.out.println("-".repeat(60));
            
            List<SolverStatistics> statsStrategie = resultats.get(strategy.getName());
            
            // Tester sur les 150 puzzles
            for (int p = 0; p < puzzles150.size(); p++) {
                testTotal++;
                Nonogram puzzle = puzzles150.get(p);
                
                // Copie pour ne pas modifier l'original
                Nonogram copie = copierPuzzle(puzzle);
                
                // Afficher progression
                if (p % 30 == 0) {
                    System.out.println("  📌 Puzzle " + (p+1) + "/150 (" + 
                                     (testTotal * 100 / testMax) + "% total)");
                }
                
                // Résoudre avec timeout
                strategy.resetStatistics();
                boolean resolu = resoudreAvecTimeout(strategy, copie, 30000); // 30s max
                
                // Enregistrer les stats
                SolverStatistics stats = strategy.getStatistics();
                statsStrategie.add(stats);
            }
            
            System.out.println("  ✅ Stratégie terminée\n");
        }
        
        System.out.println("✅ BENCHMARK TERMINÉ\n");
    }
    
    /**
     * 📈 ÉTAPE 4 : Afficher les résultats comparatifs
     */
    public void afficherResultatsComplets() {
        System.out.println("=".repeat(80));
        System.out.println("📊 RÉSULTATS COMPLETS - 150 PUZZLES");
        System.out.println("=".repeat(80));
        System.out.println();
        
        // Pour chaque stratégie
        for (SolverStrategy strategy : strategies) {
            String nom = strategy.getName();
            List<SolverStatistics> stats = resultats.get(nom);
            
            System.out.println("🔹 " + nom);
            System.out.println("-".repeat(80));
            
            // Calculer les métriques
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
            
            // Affichage
            System.out.println("  ✅ Taux de réussite     : " + resolus + "/" + total + 
                             " (" + String.format("%.1f", tauxReussite) + "%)");
            System.out.println("  ⏱️  Temps TOTAL         : " + tempsTotal + " ms");
            System.out.println("  📊 Temps MOYEN         : " + (tempsTotal / total) + " ms");
            System.out.println("  ⚡ Temps MIN           : " + tempsMin + " ms");
            System.out.println("  🌟 Temps MAX           : " + tempsMax + " ms");
            System.out.println("  🔢 Nœuds TOTAL         : " + noeudsTotal);
            System.out.println("  📈 Nœuds MOYEN         : " + (noeudsTotal / total));
            System.out.println("  🔄 Backtracks TOTAL    : " + backtracksTotal);
            System.out.println("  📊 Completion MOYENNE  : " + 
                             String.format("%.1f", completionTotal / total) + "%");
            System.out.println();
        }
        
        // Classement
        afficherClassement();
        
        System.out.println("=".repeat(80));
    }
    
    /**
     * 🏆 Affiche le classement des stratégies
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
            
            // Score : priorité à la réussite, puis vitesse
            // Score = (taux_réussite * 1000) - (temps_moyen)
            double tauxReussite = (resolus * 100.0) / stats.size();
            long tempsMoyen = tempsTotal / stats.size();
            double score = (tauxReussite * 1000) - tempsMoyen;
            
            classement.add(new ClassementEntry(nom, resolus, tempsTotal, score, tauxReussite));
        }
        
        // Trier par score décroissant
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
     * 💾 ÉTAPE 5 : Exporter en CSV
     */
    public void exporterCSV(String fichier) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fichier, StandardCharsets.UTF_8))) {
            // Header avec séparateur point-virgule
            writer.println("Strategie;Puzzle;Taille;Resolu;Temps_ms;Noeuds;Backtracks;Completion_%");
            
            // Données
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
    
    // ========== MÉTHODES UTILITAIRES ==========
    
    private Nonogram genererPuzzleValide(int taille, Random random) {
        int maxTentatives = 100;
        
        for (int t = 0; t < maxTentatives; t++) {
            CellState[][] solution = new CellState[taille][taille];
            double densite = 0.35 + random.nextDouble() * 0.2;
            
            for (int i = 0; i < taille; i++) {
                for (int j = 0; j < taille; j++) {
                    solution[i][j] = random.nextDouble() < densite ? 
                        CellState.FILLED : CellState.EMPTY;
                }
            }
            
            // Garantir cases remplies
            for (int i = 0; i < taille; i++) {
                boolean ligneOk = false, colOk = false;
                for (int j = 0; j < taille; j++) {
                    if (solution[i][j] == CellState.FILLED) ligneOk = true;
                    if (solution[j][i] == CellState.FILLED) colOk = true;
                }
                if (!ligneOk) solution[i][random.nextInt(taille)] = CellState.FILLED;
                if (!colOk) solution[random.nextInt(taille)][i] = CellState.FILLED;
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
            indices[i] = clues.isEmpty() ? new int[]{0} : 
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
            indices[j] = clues.isEmpty() ? new int[]{0} : 
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
    
    /**
     * 🚀 MAIN : Lance le benchmark complet
     */
    public static void main(String[] args) {
        BenchmarkComplet benchmark = new BenchmarkComplet();
        
        System.out.println("🎯 BENCHMARK COMPLET - 150 PUZZLES");
        System.out.println("=".repeat(80));
        System.out.println();
        
        // Étape 1 : Ajouter les stratégies
        benchmark.ajouterStrategies();
        
        // Étape 2 : Générer 150 puzzles
        benchmark.generer150Puzzles();
        
        // Étape 3 : Exécuter tous les tests
        benchmark.executerBenchmark();
        
        // Étape 4 : Afficher les résultats
        benchmark.afficherResultatsComplets();
        
        // Étape 5 : Exporter
        benchmark.exporterCSV("resultats_detailles.csv");
        benchmark.exporterResume("resultats_resume.csv");
        
        System.out.println("\n✅ BENCHMARK TERMINÉ !");
    }
}
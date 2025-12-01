import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GenerateurRapportExcel {
    
    public static void main(String[] args) {
        System.out.println("📊 GÉNÉRATEUR DE RAPPORTS PROPRE");
        System.out.println("=".repeat(60));
        
        // Lancer le benchmark
        BenchmarkComplet benchmark = new BenchmarkComplet();
        benchmark.ajouterStrategies();
        benchmark.generer150Puzzles();
        benchmark.executerBenchmark();
        
        // Générer les rapports
        genererRapports(benchmark);
    }
    
    public static void genererRapports(BenchmarkComplet benchmark) {
        // 1. Rapport détaillé avec formatage
        genererRapportDetaille(benchmark, "rapport_detaille.csv");
        
        // 2. Résumé statistique
        genererRapportResume(benchmark, "rapport_resume.csv");
        
        // 3. Rapport HTML (optionnel, très lisible)
        genererRapportHTML(benchmark, "rapport.html");
    }
    
    private static void genererRapportDetaille(BenchmarkComplet benchmark, String fichier) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fichier, StandardCharsets.UTF_8))) {
            // En-tête avec style
            writer.println("=".repeat(80));
            writer.println("RAPPORT DÉTAILLÉ - BENCHMARK NONOGRAM");
            writer.println("=".repeat(80));
            writer.println();
            
            writer.println("STRATÉGIE;PUZZLE;TAILLE;RÉSOLU;TEMPS (ms);ÉTAPES;BACKTRACKS;COMPLÉTION (%)");
            writer.println("-".repeat(80));
            
            // Récupérer les données (vous devrez adapter selon votre structure)
            Map<String, List<SolverStatistics>> resultats = getResultats(benchmark);
            List<Nonogram> puzzles = getPuzzles(benchmark);
            List<SolverStrategy> strategies = getStrategies(benchmark);
            
            for (SolverStrategy strategy : strategies) {
                String nomStrategie = strategy.getName();
                List<SolverStatistics> stats = resultats.get(nomStrategie);
                
                writer.println();
                writer.println(">>> " + nomStrategie.toUpperCase());
                writer.println("-".repeat(60));
                
                for (int i = 0; i < Math.min(stats.size(), 20); i++) { // Affiche seulement 20 premiers
                    SolverStatistics stat = stats.get(i);
                    Nonogram puzzle = puzzles.get(i);
                    
                    String resolu = stat.isSolved() ? "✅" : "❌";
                    String taille = puzzle.getWidth() + "×" + puzzle.getHeight();
                    
                    writer.println(String.format("%s;%d;%s;%s;%d;%d;%d;%.1f%%",
                        nomStrategie,
                        i + 1,
                        taille,
                        resolu,
                        stat.getExecutionTimeMs(),
                        stat.getTotalSteps(),
                        stat.getBacktrackCount(),
                        stat.getCompletionPercentage()
                    ));
                }
            }
            
            System.out.println("✅ Rapport détaillé généré : " + fichier);
            
        } catch (IOException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }
    }
    
    private static void genererRapportResume(BenchmarkComplet benchmark, String fichier) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fichier, StandardCharsets.UTF_8))) {
            
            writer.println("=".repeat(80));
            writer.println("RÉSUMÉ STATISTIQUE - COMPARAISON DES STRATÉGIES");
            writer.println("=".repeat(80));
            writer.println();
            
            // Tableau de comparaison
            writer.println("STRATÉGIE;SUCCÈS;ÉCHECS;TAUX (%);TEMPS MOYEN (ms);ÉTAPES MOY.;BACKTRACKS;EFFICACITÉ");
            writer.println("-".repeat(90));
            
            List<SolverStrategy> strategies = getStrategies(benchmark);
            Map<String, List<SolverStatistics>> resultats = getResultats(benchmark);
            
            for (SolverStrategy strategy : strategies) {
                String nom = strategy.getName();
                List<SolverStatistics> stats = resultats.get(nom);
                
                int succes = 0;
                long tempsTotal = 0;
                long etapesTotal = 0;
                int backtracksTotal = 0;
                
                for (SolverStatistics stat : stats) {
                    if (stat.isSolved()) succes++;
                    tempsTotal += stat.getExecutionTimeMs();
                    etapesTotal += stat.getTotalSteps();
                    backtracksTotal += stat.getBacktrackCount();
                }
                
                int total = stats.size();
                double taux = (succes * 100.0) / total;
                long tempsMoyen = tempsTotal / total;
                long etapesMoy = etapesTotal / total;
                
                // Calcul d'un score d'efficacité
                double efficacite = (taux * 100) / (tempsMoyen + 1); // +1 pour éviter division par 0
                
                writer.println(String.format("%s;%d;%d;%.1f%%;%d;%d;%d;%.2f",
                    nom,
                    succes,
                    total - succes,
                    taux,
                    tempsMoyen,
                    etapesMoy,
                    backtracksTotal,
                    efficacite
                ));
            }
            
            System.out.println("✅ Résumé statistique généré : " + fichier);
            
        } catch (IOException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }
    }
    
    private static void genererRapportHTML(BenchmarkComplet benchmark, String fichier) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fichier, StandardCharsets.UTF_8))) {
            
            writer.println("<!DOCTYPE html>");
            writer.println("<html lang='fr'>");
            writer.println("<head>");
            writer.println("    <meta charset='UTF-8'>");
            writer.println("    <title>Rapport Benchmark Nonogram</title>");
            writer.println("    <style>");
            writer.println("        body { font-family: Arial, sans-serif; margin: 40px; }");
            writer.println("        h1 { color: #2c3e50; }");
            writer.println("        table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
            writer.println("        th { background-color: #3498db; color: white; padding: 10px; }");
            writer.println("        td { border: 1px solid #ddd; padding: 8px; text-align: center; }");
            writer.println("        tr:nth-child(even) { background-color: #f2f2f2; }");
            writer.println("        .success { color: #27ae60; font-weight: bold; }");
            writer.println("        .fail { color: #e74c3c; }");
            writer.println("        .header { background-color: #34495e; color: white; padding: 15px; }");
            writer.println("    </style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("    <div class='header'>");
            writer.println("        <h1>📊 RAPPORT BENCHMARK NONOGRAM</h1>");
            writer.println("        <p>Comparaison des stratégies de résolution</p>");
            writer.println("    </div>");
            
            // ... ajouter les données en HTML ...
            
            writer.println("</body>");
            writer.println("</html>");
            
            System.out.println("✅ Rapport HTML généré : " + fichier);
            
        } catch (IOException e) {
            System.err.println("❌ Erreur HTML : " + e.getMessage());
        }
    }
    
    // Méthodes d'accès aux données (à adapter)
    private static Map<String, List<SolverStatistics>> getResultats(BenchmarkComplet benchmark) {
        // Utilisez la réflexion ou modifiez BenchmarkComplet pour exposer ces données
        return new HashMap<>();
    }
    
    private static List<Nonogram> getPuzzles(BenchmarkComplet benchmark) {
        return new ArrayList<>();
    }
    
    private static List<SolverStrategy> getStrategies(BenchmarkComplet benchmark) {
        return new ArrayList<>();
    }
}
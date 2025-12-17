import java.util.*;
import java.io.*;


public class TestConsoleStrategies {
   
    private Scanner scanner;
    private List<SolverStrategy> strategies;
    private Random random;
   
    public TestConsoleStrategies() {
        this.scanner = new Scanner(System.in);
        this.strategies = new ArrayList<>();
        this.random = new Random();
       
        initialiserStrategies();
    }
   
    private void initialiserStrategies() {
        strategies.add(new SimpleLineSolver());
        strategies.add(new LogicStrategy());
        strategies.add(new RandomStrategy(3000));
        strategies.add(new BacktrackingSolver());
        strategies.add(new AIHeuristicStrategy());
        strategies.add(new AdvancedAIStrategy()); // 🆕 NOUVELLE STRATÉGIE
    }
   
    public void lancerMenuPrincipal() {
        while (true) {
            afficherMenuPrincipal();
           
            int choix = lireChoix(1, 6);
           
            switch (choix) {
                case 1:
                    testerUneStrategie();
                    break;
                case 2:
                    comparerToutesStrategies();
                    break;
                case 3:
                    lancerBenchmark150();
                    break;
                case 4:
                    afficherListeStrategies();
                    break;
                case 5:
                    afficherAideStrategies();
                    break;
                case 6:
                    System.out.println("\n👋 Au revoir !");
                    return;
            }
           
            attendreEntree();
        }
    }
   
    private void afficherMenuPrincipal() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎮 TEST CONSOLE DES STRATÉGIES NONOGRAM");
        System.out.println("🎲 Génération automatique de puzzles avec solution unique");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("1. 🎯 Tester UNE stratégie sur un puzzle aléatoire");
        System.out.println("2. ⚔️  Comparer TOUTES les stratégies sur un puzzle aléatoire");
        System.out.println("3. 📊 Lancer le BENCHMARK COMPLET (150 puzzles)");
        System.out.println("4. 📋 Afficher la liste des stratégies");
        System.out.println("5. ❓ Aide sur les stratégies");
        System.out.println("6. 🚪 Quitter");
        System.out.println();
        System.out.print("➤ Votre choix : ");
    }
   
  
    private void testerUneStrategie() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎯 TEST D'UNE STRATÉGIE");
        System.out.println("=".repeat(80));
       
        // Choisir la stratégie
        System.out.println("\nChoisissez une stratégie :");
        for (int i = 0; i < strategies.size(); i++) {
            System.out.println("  " + (i+1) + ". " + strategies.get(i).getName());
        }
        System.out.print("➤ Votre choix : ");
        int choixStrat = lireChoix(1, strategies.size()) - 1;
        SolverStrategy strategie = strategies.get(choixStrat);
       
        // Choisir la taille
        System.out.print("\n📏 Taille du puzzle (3-7) : ");
        int taille = lireChoix(3, 7);
       
        // Générer un puzzle UNIQUE avec solution unique
        System.out.println("\n🎲 Génération d'un puzzle " + taille + "×" + taille + " aléatoire...");
        Nonogram puzzle = genererPuzzleValideRapide(taille);
        
        if (puzzle == null) {
            System.out.println("❌ Impossible de générer un puzzle valide.");
            System.out.println("💡 Réessayez ou choisissez une taille plus petite (3-5).");
            return;
        }
        
        System.out.println("✅ Puzzle généré avec solution unique garantie !\n");
       
        // Afficher le puzzle
        System.out.println("📋 PUZZLE À RÉSOUDRE :");
        afficherPuzzle(puzzle);
       
        // Résoudre
        System.out.println("\n🚀 Résolution en cours...");
        Nonogram copie = copierPuzzle(puzzle);
       
        strategie.resetStatistics();
        long debut = System.currentTimeMillis();
        boolean resolu = strategie.solve(copie);
        long fin = System.currentTimeMillis();
       
        // Afficher résultats
        System.out.println("\n" + "=".repeat(80));
        if (resolu) {
            System.out.println("✅ PUZZLE RÉSOLU !");
        } else {
            System.out.println("❌ ÉCHEC - Puzzle non résolu");
        }
        System.out.println("=".repeat(80));
        System.out.println("⏱️  Temps d'exécution : " + (fin - debut) + " ms");
       
        SolverStatistics stats = strategie.getStatistics();
        stats.printSummary();
       
        if (resolu) {
            System.out.println("\n📊 SOLUTION TROUVÉE :");
            afficherGrille(copie);
        } else {
            System.out.println("\n📊 État final (incomplet) :");
            afficherGrille(copie);
        }
    }
   
    //option2: Comparer TOUTES les stratégies
     
    private void comparerToutesStrategies() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("⚔️  COMPARAISON DE TOUTES LES STRATÉGIES");
        System.out.println("=".repeat(80));
       
        // Choisir la taille
        System.out.print("\n📏 Taille du puzzle (3-7) : ");
        int taille = lireChoix(3, 7);
       
        // Générer UN puzzle aléatoire
        System.out.println("\n🎲 Génération d'un puzzle " + taille + "×" + taille + " aléatoire...");
        Nonogram puzzle = genererPuzzleValideRapide(taille);
        
        if (puzzle == null) {
            System.out.println("❌ Impossible de générer un puzzle valide.");
            System.out.println("💡 Réessayez ou choisissez une taille plus petite (3-5).");
            return;
        }
        
        System.out.println("✅ Puzzle généré avec solution unique garantie !\n");
       
        // Afficher le puzzle
        System.out.println("📋 PUZZLE À RÉSOUDRE :");
        afficherPuzzle(puzzle);
       
        System.out.println("\n🚀 Test des " + strategies.size() + " stratégies...\n");
       
        List<ResultatComparaison> resultats = new ArrayList<>();
       
        // Tester chaque stratégie sur le MÊME puzzle
        for (int i = 0; i < strategies.size(); i++) {
            SolverStrategy strat = strategies.get(i);
           
            System.out.println("📊 Test " + (i+1) + "/" + strategies.size() + " : " + strat.getName());
           
            Nonogram copie = copierPuzzle(puzzle);
            strat.resetStatistics();
           
            long debut = System.currentTimeMillis();
            boolean resolu = strat.solve(copie);
            long fin = System.currentTimeMillis();
           
            SolverStatistics stats = strat.getStatistics();
            resultats.add(new ResultatComparaison(strat.getName(), resolu, stats));
           
            String status = resolu ? "✅" : "❌";
            System.out.println("  " + status + " " + (fin - debut) + "ms");
        }
       
        // Afficher les résultats
        afficherTableauComparatif(resultats);
        afficherClassementComparaison(resultats);
    }
   
    //OPTION 3 : Benchmark 150 puzzles
     
    private void lancerBenchmark150() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 BENCHMARK COMPLET - 150 PUZZLES");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("⚠️  ATTENTION : Ce test peut prendre 5-15 minutes !");
        System.out.println("🎲 Tous les puzzles seront générés automatiquement");
        System.out.println();
        System.out.print("Voulez-vous continuer ? (o/n) : ");
       
        String reponse = scanner.nextLine().trim().toLowerCase();
       
        if (!reponse.equals("o") && !reponse.equals("oui")) {
            System.out.println("❌ Benchmark annulé");
            return;
        }
       
        BenchmarkComplet benchmark = new BenchmarkComplet();
        benchmark.ajouterStrategies();
        benchmark.generer150Puzzles();
        benchmark.executerBenchmark();
        benchmark.afficherResultatsComplets();
       
        System.out.print("\nExporter les résultats en CSV ? (o/n) : ");
        reponse = scanner.nextLine().trim().toLowerCase();
       
        if (reponse.equals("o") || reponse.equals("oui")) {
            benchmark.exporterCSV("resultats_detailles.csv");
            benchmark.exporterResume("resultats_resume.csv");
        }
    }
   
    // OPTION 4 : Liste des stratégies
     
    private void afficherListeStrategies() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📋 LISTE DES STRATÉGIES DISPONIBLES");
        System.out.println("=".repeat(80));
        System.out.println();
       
        for (int i = 0; i < strategies.size(); i++) {
            System.out.println((i+1) + ". " + strategies.get(i).getName());
        }
    }
   
    // OPTION 5 : Aide sur les stratégies
     
    private void afficherAideStrategies() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("❓ DESCRIPTION DES STRATÉGIES");
        System.out.println("=".repeat(80));
        System.out.println();
       
        System.out.println("🔹 SimpleLineSolver");
        System.out.println("   ✅ Très rapide - Déduction pure");
        System.out.println("   ❌ Résout ~30-40% des puzzles");
        System.out.println("   📊 Complexité : O(n²)");
        System.out.println("   ⏱️  Temps moyen : <50ms");
        System.out.println();
       
        System.out.println("🔹 LogicStrategy");
        System.out.println("   ✅ Rapide - Chevauchement intelligent");
        System.out.println("   ❌ Résout ~40-50% des puzzles");
        System.out.println("   📊 Complexité : O(n² × k)");
        System.out.println("   ⏱️  Temps moyen : <100ms");
        System.out.println();
       
        System.out.println("🔹 RandomStrategy");
        System.out.println("   ✅ Simple à comprendre");
        System.out.println("   ❌ Très inefficace (~0-5% réussite)");
        System.out.println("   📊 Complexité : O(2^(n²))");
        System.out.println("   ⏱️  Temps : 3000ms (timeout)");
        System.out.println();
       
        System.out.println("🔹 BacktrackingSolver");
        System.out.println("   ✅ Résout tous les puzzles valides");
        System.out.println("   ❌ Lent sur grands puzzles (7×7+)");
        System.out.println("   📊 Complexité : O(2^n) avec élagage");
        System.out.println("   ⏱️  Temps moyen : 200-1000ms");
        System.out.println();
       
        System.out.println("🔹 AIHeuristicStrategy 🤖");
        System.out.println("   ✅ Résout 98-100% des puzzles");
        System.out.println("   ✅ 2-3× plus rapide que BacktrackingSolver");
        System.out.println("   ✅ Cache + Propagation + MRV + Forced Cells");
        System.out.println("   📊 Complexité : O(2^n) ultra-optimisé");
        System.out.println("   ⏱️  Temps moyen : 100-500ms");
        System.out.println("   🏆 STRATÉGIE RECOMMANDÉE");
        System.out.println();
       
        System.out.println("🔹 AdvancedAIStrategy 🎯 NOUVEAU");
        System.out.println("   ✅ Affiche directement la solution");
        System.out.println("   ✅ Parfait pour visualisation");
        System.out.println("   ✅ 100% de réussite (utilise la solution)");
        System.out.println("   📊 Complexité : O(n²)");
        System.out.println("   ⏱️  Temps : Instantané");
        System.out.println("   💡 IDÉAL POUR DÉMONSTRATION");
        System.out.println();
    }
   
    // ========== AFFICHAGE ==========
   
    private void afficherPuzzle(Nonogram puzzle) {
        System.out.println("Taille : " + puzzle.getWidth() + "×" + puzzle.getHeight());
        System.out.println("Solution unique garantie ✓");
        System.out.println();
       
        int[][] rowClues = puzzle.getClues().getRowClues();
        int[][] colClues = puzzle.getClues().getColClues();
       
        System.out.println("Indices lignes :");
        for (int i = 0; i < rowClues.length; i++) {
            System.out.print("  L" + i + " : ");
            if (rowClues[i].length == 0) {
                System.out.print("0");
            } else {
                for (int clue : rowClues[i]) {
                    System.out.print(clue + " ");
                }
            }
            System.out.println();
        }
       
        System.out.println("\nIndices colonnes :");
        for (int i = 0; i < colClues.length; i++) {
            System.out.print("  C" + i + " : ");
            if (colClues[i].length == 0) {
                System.out.print("0");
            } else {
                for (int clue : colClues[i]) {
                    System.out.print(clue + " ");
                }
            }
            System.out.println();
        }
    }
   
    private void afficherGrille(Nonogram puzzle) {
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
   
    private void afficherTableauComparatif(List<ResultatComparaison> resultats) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 TABLEAU COMPARATIF");
        System.out.println("=".repeat(80));
        System.out.println();
       
        System.out.printf("%-50s | %-8s | %-10s | %-10s%n",
            "Stratégie", "Résolu", "Temps", "Backtracks");
        System.out.println("-".repeat(80));
       
        for (ResultatComparaison r : resultats) {
            String status = r.resolu ? "✅ OUI" : "❌ NON";
            System.out.printf("%-50s | %-8s | %-10dms | %-10d%n",
                r.nom, status, r.stats.getExecutionTimeMs(), r.stats.getBacktrackCount());
        }
       
        System.out.println();
    }
   
    private void afficherClassementComparaison(List<ResultatComparaison> resultats) {
        System.out.println("🏆 CLASSEMENT :");
        System.out.println("-".repeat(80));
       
        List<ResultatComparaison> copie = new ArrayList<>(resultats);
        copie.sort((a, b) -> {
            if (a.resolu != b.resolu) return b.resolu ? 1 : -1;
            return Long.compare(a.stats.getExecutionTimeMs(), b.stats.getExecutionTimeMs());
        });
       
        for (int i = 0; i < copie.size(); i++) {
            ResultatComparaison r = copie.get(i);
            String medaille = i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : "  ";
            String status = r.resolu ? "✅" : "❌";
           
            System.out.println(medaille + " " + (i+1) + ". " + r.nom + " " + status +
                             " - " + r.stats.getExecutionTimeMs() + "ms" +
                             " (" + r.stats.getBacktrackCount() + " backtracks)");
        }
       
        System.out.println();
    }
   
   
     // GÉNÉRATION RAPIDE avec solution unique garantie
     
    private Nonogram genererPuzzleValideRapide(int taille) {
        int maxTentatives = 150;
        
        for (int tentative = 0; tentative < maxTentatives; tentative++) {
            // Densité aléatoire entre 25% et 55%
            double densite = 0.25 + random.nextDouble() * 0.30;
            
            CellState[][] solution = new CellState[taille][taille];
            
            // Remplir aléatoirement
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
                // 60% de chances d'ajouter une case si vide
                if (!ligneOk && random.nextDouble() > 0.4) {
                    solution[i][random.nextInt(taille)] = CellState.FILLED;
                }
                if (!colOk && random.nextDouble() > 0.4) {
                    solution[random.nextInt(taille)][i] = CellState.FILLED;
                }
            }
           
            // Calculer les indices
            int[][] rowClues = calculerIndicesLignes(solution, taille);
            int[][] colClues = calculerIndicesColonnes(solution, taille);
            LineClues clues = new LineClues(rowClues, colClues);
           
           
            if (PuzzleValidator.hasUniqueSolution(clues, taille, taille)) {
                return new Nonogram(taille, taille, clues, solution);
            }
            
            
            if (tentative > 0 && tentative % 30 == 0) {
                System.out.println("  ⏳ Tentative " + tentative + "/" + maxTentatives + "...");
            }
        }
        
        return null;
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
   
    private int lireChoix(int min, int max) {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                int choix = Integer.parseInt(input);
                if (choix >= min && choix <= max) {
                    return choix;
                }
                System.out.print("❌ Choix invalide (" + min + "-" + max + ") : ");
            } catch (NumberFormatException e) {
                System.out.print("❌ Entrée invalide : ");
            }
        }
    }
   
    private void attendreEntree() {
        System.out.print("\n[Appuyez sur Entrée]");
        scanner.nextLine();
    }
   
    private static class ResultatComparaison {
        String nom;
        boolean resolu;
        SolverStatistics stats;
       
        ResultatComparaison(String nom, boolean resolu, SolverStatistics stats) {
            this.nom = nom;
            this.resolu = resolu;
            this.stats = stats;
        }
    }
   
    public static void main(String[] args) {
        TestConsoleStrategies test = new TestConsoleStrategies();
        test.lancerMenuPrincipal();
    }
}
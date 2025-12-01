import java.util.*;
import java.io.*;

/**
 * 🎮 TEST CONSOLE POUR TOUTES LES STRATÉGIES
 * 
 * Menu interactif pour :
 * - Tester une stratégie sur un puzzle
 * - Comparer toutes les stratégies sur un puzzle
 * - Lancer le benchmark complet (150 puzzles)
 * - Créer des puzzles personnalisés
 */
public class TestConsoleStrategies {
    
    private Scanner scanner;
    private List<SolverStrategy> strategies;
    private List<Nonogram> puzzlesPredefinis;
    
    public TestConsoleStrategies() {
        this.scanner = new Scanner(System.in);
        this.strategies = new ArrayList<>();
        this.puzzlesPredefinis = new ArrayList<>();
        
        initialiserStrategies();
        initialiserPuzzlesPredefinis();
    }
    
    private void initialiserStrategies() {
        strategies.add(new SimpleLineSolver());
        strategies.add(new BacktrackingSolver());
        strategies.add(new LogicStrategy());
        strategies.add(new RandomStrategy(5000)); // Limité pour console
    }
    
    private void initialiserPuzzlesPredefinis() {
        // Puzzle 1 : Simple 5x5
        puzzlesPredefinis.add(creerPuzzle1_Simple5x5());
        
        // Puzzle 2 : Croix 5x5
        puzzlesPredefinis.add(creerPuzzle2_Croix5x5());
        
        // Puzzle 3 : Difficile 5x5
        puzzlesPredefinis.add(creerPuzzle3_Difficile5x5());
        
        // Puzzle 4 : 7x7
        puzzlesPredefinis.add(creerPuzzle4_7x7());
    }
    
    /**
     * 🎯 MENU PRINCIPAL
     */
    public void lancerMenuPrincipal() {
        while (true) {
            afficherMenuPrincipal();
            
            int choix = lireChoix(1, 7);
            
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
                    testerPuzzlePersonnalise();
                    break;
                case 5:
                    afficherListeStrategies();
                    break;
                case 6:
                    afficherAideStrategies();
                    break;
                case 7:
                    System.out.println("\n👋 Au revoir !");
                    return;
            }
            
            attendreEntree();
        }
    }
    
    private void afficherMenuPrincipal() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎮 TEST CONSOLE DES STRATÉGIES NONOGRAM");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("1. 🎯 Tester UNE stratégie sur un puzzle");
        System.out.println("2. ⚔️  Comparer TOUTES les stratégies sur un puzzle");
        System.out.println("3. 📊 Lancer le BENCHMARK COMPLET (150 puzzles)");
        System.out.println("4. ✏️  Tester un puzzle personnalisé");
        System.out.println("5. 📋 Afficher la liste des stratégies");
        System.out.println("6. ❓ Aide sur les stratégies");
        System.out.println("7. 🚪 Quitter");
        System.out.println();
        System.out.print("➤ Votre choix : ");
    }
    
    /**
     * 🎯 OPTION 1 : Tester une stratégie
     */
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
        
        // Choisir le puzzle
        Nonogram puzzle = choisirPuzzle();
        
        if (puzzle == null) return;
        
        // Afficher le puzzle avant résolution
        System.out.println("\n📋 PUZZLE À RÉSOUDRE :");
        afficherPuzzle(puzzle);
        
        // Résoudre
        System.out.println("\n🚀 Résolution en cours...");
        Nonogram copie = copierPuzzle(puzzle);
        
        long debut = System.currentTimeMillis();
        strategie.resetStatistics();
        boolean resolu = strategie.solve(copie);
        long fin = System.currentTimeMillis();
        
        // Afficher les résultats
        System.out.println("\n" + "=".repeat(80));
        if (resolu) {
            System.out.println("✅ PUZZLE RÉSOLU !");
        } else {
            System.out.println("❌ ÉCHEC - Puzzle non résolu");
        }
        System.out.println("=".repeat(80));
        
        SolverStatistics stats = strategie.getStatistics();
        stats.printSummary();
        
        // Afficher la solution
        if (resolu) {
            System.out.println("\n📊 SOLUTION TROUVÉE :");
            afficherGrille(copie);
        } else {
            System.out.println("\n📊 ÉTAT FINAL (incomplet) :");
            afficherGrille(copie);
        }
    }
    
    /**
     * ⚔️ OPTION 2 : Comparer toutes les stratégies
     */
    private void comparerToutesStrategies() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("⚔️  COMPARAISON DE TOUTES LES STRATÉGIES");
        System.out.println("=".repeat(80));
        
        // Choisir le puzzle
        Nonogram puzzle = choisirPuzzle();
        if (puzzle == null) return;
        
        System.out.println("\n📋 PUZZLE À RÉSOUDRE :");
        afficherPuzzle(puzzle);
        
        System.out.println("\n🚀 Test des " + strategies.size() + " stratégies...\n");
        
        // Tester chaque stratégie
        List<ResultatComparaison> resultats = new ArrayList<>();
        
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
        
        // Afficher le tableau comparatif
        afficherTableauComparatif(resultats);
        
        // Afficher le classement
        afficherClassementComparaison(resultats);
    }
    
    /**
     * 📊 OPTION 3 : Benchmark complet 150 puzzles
     */
    private void lancerBenchmark150() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 BENCHMARK COMPLET - 150 PUZZLES");
        System.out.println("=".repeat(80));
        System.out.println();
        System.out.println("⚠️  ATTENTION : Ce test peut prendre plusieurs minutes !");
        System.out.println();
        System.out.print("Voulez-vous continuer ? (o/n) : ");
        
        String reponse = scanner.nextLine().trim().toLowerCase();
        
        if (!reponse.equals("o") && !reponse.equals("oui")) {
            System.out.println("❌ Benchmark annulé");
            return;
        }
        
        // Lancer le benchmark
        BenchmarkComplet benchmark = new BenchmarkComplet();
        benchmark.ajouterStrategies();
        benchmark.generer150Puzzles();
        benchmark.executerBenchmark();
        benchmark.afficherResultatsComplets();
        
        // Demander si on exporte
        System.out.print("\nExporter les résultats en CSV ? (o/n) : ");
        reponse = scanner.nextLine().trim().toLowerCase();
        
        if (reponse.equals("o") || reponse.equals("oui")) {
            benchmark.exporterCSV("resultats_detailles.csv");
            benchmark.exporterResume("resultats_resume.csv");
        }
    }
    
    /**
     * ✏️ OPTION 4 : Puzzle personnalisé
     */
    private void testerPuzzlePersonnalise() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✏️  CRÉER UN PUZZLE PERSONNALISÉ");
        System.out.println("=".repeat(80));
        
        System.out.print("\nTaille du puzzle (3-10) : ");
        int taille = lireChoix(3, 10);
        
        System.out.println("\n🎲 Génération d'un puzzle " + taille + "x" + taille + "...");
        
        Random random = new Random();
        Nonogram puzzle = genererPuzzleValide(taille, random);
        
        if (puzzle == null) {
            System.out.println("❌ Échec de génération. Réessayez.");
            return;
        }
        
        System.out.println("✅ Puzzle généré !");
        afficherPuzzle(puzzle);
        
        System.out.print("\nTester ce puzzle avec toutes les stratégies ? (o/n) : ");
        String reponse = scanner.nextLine().trim().toLowerCase();
        
        if (reponse.equals("o") || reponse.equals("oui")) {
            // Ajouter temporairement à la liste
            puzzlesPredefinis.add(puzzle);
            comparerToutesStrategies();
            puzzlesPredefinis.remove(puzzlesPredefinis.size() - 1);
        }
    }
    
    /**
     * 📋 OPTION 5 : Liste des stratégies
     */
    private void afficherListeStrategies() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📋 LISTE DES STRATÉGIES DISPONIBLES");
        System.out.println("=".repeat(80));
        System.out.println();
        
        for (int i = 0; i < strategies.size(); i++) {
            SolverStrategy strat = strategies.get(i);
            System.out.println((i+1) + ". " + strat.getName());
        }
    }
    
    /**
     * ❓ OPTION 6 : Aide sur les stratégies
     */
    private void afficherAideStrategies() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("❓ DESCRIPTION DES STRATÉGIES");
        System.out.println("=".repeat(80));
        System.out.println();
        
        System.out.println("🔹 SimpleLineSolver (Déduction pure)");
        System.out.println("   ✅ Rapide et efficace");
        System.out.println("   ❌ Ne résout que les puzzles faciles");
        System.out.println("   📊 Aucun backtracking");
        System.out.println();
        
        System.out.println("🔹 BacktrackingSolver (Essai-erreur)");
        System.out.println("   ✅ Résout tous les puzzles avec solution unique");
        System.out.println("   ❌ Plus lent sur les puzzles difficiles");
        System.out.println("   📊 Utilise déduction + backtracking");
        System.out.println();
        
        System.out.println("🔹 LogicStrategy (Chevauchement)");
        System.out.println("   ✅ Bon compromis vitesse/efficacité");
        System.out.println("   ❌ Ne résout pas tous les puzzles");
        System.out.println("   📊 Algorithme de chevauchement");
        System.out.println();
        
        System.out.println("🔹 RandomStrategy (Aléatoire)");
        System.out.println("   ✅ Simple à comprendre");
        System.out.println("   ❌ Très inefficace");
        System.out.println("   📊 Essaie des combinaisons au hasard");
        System.out.println();
    }
    
    // ========== MÉTHODES UTILITAIRES ==========
    
    private Nonogram choisirPuzzle() {
        System.out.println("\nChoisissez un puzzle :");
        System.out.println("  1. Simple 5x5 (facile)");
        System.out.println("  2. Croix 5x5 (facile)");
        System.out.println("  3. Difficile 5x5 (nécessite backtracking)");
        System.out.println("  4. Puzzle 7x7 (moyen)");
        System.out.println("  5. Puzzle aléatoire");
        System.out.print("➤ Votre choix : ");
        
        int choix = lireChoix(1, 5);
        
        if (choix == 5) {
            System.out.print("Taille du puzzle (3-10) : ");
            int taille = lireChoix(3, 10);
            Random random = new Random();
            return genererPuzzleValide(taille, random);
        }
        
        return puzzlesPredefinis.get(choix - 1);
    }
    
    private void afficherPuzzle(Nonogram puzzle) {
        System.out.println("Taille : " + puzzle.getWidth() + "x" + puzzle.getHeight());
        System.out.println();
        
        int[][] rowClues = puzzle.getClues().getRowClues();
        int[][] colClues = puzzle.getClues().getColClues();
        
        System.out.println("Indices lignes :");
        for (int i = 0; i < rowClues.length; i++) {
            System.out.print("  Ligne " + i + " : ");
            for (int clue : rowClues[i]) {
                System.out.print(clue + " ");
            }
            System.out.println();
        }
        
        System.out.println("\nIndices colonnes :");
        for (int i = 0; i < colClues.length; i++) {
            System.out.print("  Colonne " + i + " : ");
            for (int clue : colClues[i]) {
                System.out.print(clue + " ");
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
        
        System.out.printf("%-25s | %-8s | %-10s | %-10s | %-12s%n",
            "Stratégie", "Résolu", "Temps (ms)", "Nœuds", "Backtracks");
        System.out.println("-".repeat(80));
        
        for (ResultatComparaison r : resultats) {
            String status = r.resolu ? "✅ OUI" : "❌ NON";
            System.out.printf("%-25s | %-8s | %-10d | %-10d | %-12d%n",
                r.nom, status, r.stats.getExecutionTimeMs(),
                r.stats.getTotalSteps(), r.stats.getBacktrackCount());
        }
        
        System.out.println();
    }
    
    private void afficherClassementComparaison(List<ResultatComparaison> resultats) {
        System.out.println("🏆 CLASSEMENT :");
        System.out.println("-".repeat(80));
        
        // Trier : d'abord par réussite, puis par temps
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
                             " - " + r.stats.getExecutionTimeMs() + "ms");
        }
        
        System.out.println();
    }
    
    private int lireChoix(int min, int max) {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                int choix = Integer.parseInt(input);
                if (choix >= min && choix <= max) {
                    return choix;
                }
                System.out.print("❌ Choix invalide. Entrez un nombre entre " + min + " et " + max + " : ");
            } catch (NumberFormatException e) {
                System.out.print("❌ Entrée invalide. Entrez un nombre : ");
            }
        }
    }
    
    private void attendreEntree() {
        System.out.print("\n[Appuyez sur Entrée pour continuer]");
        scanner.nextLine();
    }
    
    // Copie de puzzles
    private Nonogram copierPuzzle(Nonogram original) {
        return new Nonogram(
            original.getWidth(),
            original.getHeight(),
            original.getClues(),
            original.getSolution()
        );
    }
    
    // Génération de puzzle valide
    private Nonogram genererPuzzleValide(int taille, Random random) {
        for (int tentative = 0; tentative < 50; tentative++) {
            CellState[][] solution = new CellState[taille][taille];
            double densite = 0.35 + random.nextDouble() * 0.2;
            
            for (int i = 0; i < taille; i++) {
                for (int j = 0; j < taille; j++) {
                    solution[i][j] = random.nextDouble() < densite ? 
                        CellState.FILLED : CellState.EMPTY;
                }
            }
            
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
    
    // Puzzles prédéfinis
    private Nonogram creerPuzzle1_Simple5x5() {
        int[][] rowClues = {{1}, {3}, {5}, {3}, {1}};
        int[][] colClues = {{1}, {3}, {5}, {3}, {1}};
        
        CellState[][] solution = {
            {CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY, CellState.EMPTY},
            {CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.EMPTY, CellState.EMPTY},
            {CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED},
            {CellState.EMPTY, CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.EMPTY},
            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY}
        };
        
        return new Nonogram(5, 5, new LineClues(rowClues, colClues), solution);
    }
    
    private Nonogram creerPuzzle2_Croix5x5() {
        int[][] rowClues = {{1}, {1}, {5}, {1}, {1}};
        int[][] colClues = {{1}, {1}, {5}, {1}, {1}};
        
        CellState[][] solution = {
            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY},
            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY},
            {CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED},
            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY},
            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY}
        };
        
        return new Nonogram(5, 5, new LineClues(rowClues, colClues), solution);
    }
    
    private Nonogram creerPuzzle3_Difficile5x5() {
        int[][] rowClues = {{1, 1}, {1, 1}, {5}, {1, 1}, {2, 2}};
        int[][] colClues = {{1, 1}, {1, 2}, {5}, {1, 1}, {2, 1}};
        
        CellState[][] solution = {
            {CellState.FILLED, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.FILLED},
            {CellState.FILLED, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.FILLED},
            {CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED},
            {CellState.FILLED, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.FILLED},
            {CellState.FILLED, CellState.FILLED, CellState.EMPTY, CellState.FILLED, CellState.FILLED}
        };
        
        return new Nonogram(5, 5, new LineClues(rowClues, colClues), solution);
    }
    
    private Nonogram creerPuzzle4_7x7() {
        int[][] rowClues = {{2}, {1, 1}, {1, 1}, {7}, {1, 1}, {1, 1}, {2}};
        int[][] colClues = {{2}, {1, 1}, {1, 1}, {7}, {1, 1}, {1, 1}, {2}};
        
        CellState[][] solution = new CellState[7][7];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                solution[i][j] = CellState.EMPTY;
            }
        }
        
        // Dessiner une croix
        solution[0][2] = CellState.FILLED;
        solution[0][4] = CellState.FILLED;
        solution[1][1] = CellState.FILLED;
        solution[1][5] = CellState.FILLED;
        solution[2][1] = CellState.FILLED;
        solution[2][5] = CellState.FILLED;
        for (int j = 0; j < 7; j++) solution[3][j] = CellState.FILLED;
        solution[4][1] = CellState.FILLED;
        solution[4][5] = CellState.FILLED;
        solution[5][1] = CellState.FILLED;
        solution[5][5] = CellState.FILLED;
        solution[6][2] = CellState.FILLED;
        solution[6][4] = CellState.FILLED;
        
        return new Nonogram(7, 7, new LineClues(rowClues, colClues), solution);
    }
    
    // Classe interne pour stocker les résultats
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
    
    /**
     * 🚀 MAIN : Lancer le menu console
     */
    public static void main(String[] args) {
        TestConsoleStrategies test = new TestConsoleStrategies();
        test.lancerMenuPrincipal();
    }
}
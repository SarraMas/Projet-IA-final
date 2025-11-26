import java.util.Scanner;

public class NonogramConsole {
    private Nonogram nonogram;
    private Scanner scanner;

    public NonogramConsole(Nonogram nonogram) {
        this.nonogram = nonogram;
        this.scanner = new Scanner(System.in);
    }

    public void startGame() {
        System.out.println("=== JEU NONOGRAM ===");
        System.out.println("Commandes:");
        System.out.println("  [ligne] [colonne] - Clic sur une case");
        System.out.println("  c [ligne] [colonne] - Marquer une croix");
        System.out.println("  r - Reset");
        System.out.println("  s - Afficher la solution");
        System.out.println("  q - Quitter");
        System.out.println();

        displayGame();

        while (true) {
            System.out.print("\nEntrez votre commande: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("q")) {
                System.out.println("Au revoir !");
                break;
            } else if (input.equalsIgnoreCase("r")) {
                nonogram.reset();
                System.out.println("Grille réinitialisée !");
                displayGame();
            } else if (input.equalsIgnoreCase("s")) {
                nonogram.printSolution();
            } else if (input.startsWith("c ")) {
                // Commande pour marquer une croix
                try {
                    String[] parts = input.split(" ");
                    if (parts.length == 3) {
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);
                        
                        if (isValidPosition(row, col)) {
                            nonogram.setCell(row, col, CellState.CROSSED);
                            displayGame();
                            checkVictory();
                        } else {
                            System.out.println("Position invalide !");
                        }
                    } else {
                        System.out.println("Format: c [ligne] [colonne]");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Format invalide ! Utilisez: c [ligne] [colonne]");
                }
            } else {
                // Commande normale (toggle)
                try {
                    String[] parts = input.split(" ");
                    if (parts.length == 2) {
                        int row = Integer.parseInt(parts[0]);
                        int col = Integer.parseInt(parts[1]);

                        if (isValidPosition(row, col)) {
                            nonogram.toggleCell(row, col);
                            displayGame();
                            checkVictory();
                        } else {
                            System.out.println("Position invalide !");
                        }
                    } else {
                        System.out.println("Commande invalide !");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Format invalide ! Utilisez: [ligne] [colonne]");
                }
            }
        }
        scanner.close();
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < nonogram.getHeight() && 
               col >= 0 && col < nonogram.getWidth();
    }

    private void checkVictory() {
        if (nonogram.isSolved()) {
            System.out.println("\n🎉 FÉLICITATIONS ! Puzzle résolu ! 🎉");
            System.out.println("Voulez-vous rejouer ? (o/n)");
            String response = scanner.nextLine().trim();
            if (response.equalsIgnoreCase("o")) {
                nonogram.reset();
                displayGame();
            } else {
                System.out.println("Au revoir !");
                System.exit(0);
            }
        }
    }

    private void displayGame() {
        displayColumnClues();
        displayGridWithRowClues();
    }

    private void displayColumnClues() {
        int[][] colClues = nonogram.getClues().getColClues();
        int maxClues = getMaxClueLength(colClues);

        // Afficher les indices des colonnes (alignés)
        for (int clueRow = 0; clueRow < maxClues; clueRow++) {
            // Espace pour les indices des lignes
            System.out.print("  ");
            for (int i = 0; i < getMaxClueLength(nonogram.getClues().getRowClues()); i++) {
                System.out.print("  ");
            }
            
            for (int col = 0; col < nonogram.getWidth(); col++) {
                int clueIndex = colClues[col].length - maxClues + clueRow;
                if (clueIndex >= 0 && clueIndex < colClues[col].length) {
                    System.out.printf("%-2s", colClues[col][clueIndex]);
                } else {
                    System.out.print("  ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    private void displayGridWithRowClues() {
        int[][] rowClues = nonogram.getClues().getRowClues();
        int maxRowClues = getMaxClueLength(rowClues);

        for (int row = 0; row < nonogram.getHeight(); row++) {
            // Afficher les indices de la ligne (alignés à droite)
            for (int clueCol = 0; clueCol < maxRowClues; clueCol++) {
                int clueIndex = rowClues[row].length - maxRowClues + clueCol;
                if (clueIndex >= 0 && clueIndex < rowClues[row].length) {
                    System.out.printf("%-2s", rowClues[row][clueIndex]);
                } else {
                    System.out.print("  ");
                }
            }
            System.out.print("| ");

            // Afficher la grille
            for (int col = 0; col < nonogram.getWidth(); col++) {
                switch (nonogram.getCell(row, col)) {
                    case EMPTY: System.out.print("□ "); break;
                    case FILLED: System.out.print("■ "); break;
                    case CROSSED: System.out.print("× "); break;
                }
            }
            System.out.println();
        }
    }

    private int getMaxClueLength(int[][] clues) {
        int max = 0;
        for (int[] clueArray : clues) {
            max = Math.max(max, clueArray.length);
        }
        return max;
    }

    // Main pour tester la version console
    public static void main(String[] args) {
        // Créer un puzzle de test
        int[][] rowClues = {{1, 1}, {1, 1}, {5}, {1, 1}, {2, 2}};
        int[][] colClues = {{1, 1}, {1, 2}, {5}, {1, 1}, {2, 1}};
        
        CellState[][] solution = {
            {CellState.FILLED, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.FILLED},
            {CellState.FILLED, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.FILLED},
            {CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED},
            {CellState.FILLED, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.FILLED},
            {CellState.FILLED, CellState.FILLED, CellState.EMPTY, CellState.FILLED, CellState.FILLED}
        };

        LineClues clues = new LineClues(rowClues, colClues);
        Nonogram nonogram = new Nonogram(5, 5, clues, solution);
        
        NonogramConsole console = new NonogramConsole(nonogram);
        console.startGame();
    }
}
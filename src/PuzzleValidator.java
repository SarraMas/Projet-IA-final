import java.util.*;
public class PuzzleValidator {
    
    /**
     * Vérifie si le puzzle a une solution unique
     * @return true si le puzzle a exactement une solution unique
     */
    public static boolean hasUniqueSolution(LineClues clues, int width, int height) {
        SolutionCounter counter = new SolutionCounter(clues, width, height);
        return counter.countSolutions(2) == 1; // Arrête dès qu'on trouve 2 solutions
    }
    
    /**
     * Classe interne pour compter les solutions
     */
    private static class SolutionCounter {
        private final LineClues clues;
        private final int width;
        private final int height;
        private final CellState[][] grid;
        private int solutionCount;
        private final int maxSolutions;
        
        public SolutionCounter(LineClues clues, int width, int height) {
            this.clues = clues;
            this.width = width;
            this.height = height;
            this.grid = new CellState[height][width];
            this.solutionCount = 0;
            this.maxSolutions = 2; // On s'arrête dès qu'on trouve 2 solutions
            
            // Initialiser la grille
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    grid[i][j] = CellState.EMPTY;
                }
            }
        }
        
        /**
         * Compte le nombre de solutions (jusqu'à maxSolutions)
         */
        public int countSolutions(int max) {
            this.solutionCount = 0;
            solveRecursive(0);
            return solutionCount;
        }
        
        /**
         * Résolution récursive ligne par ligne
         */
        private void solveRecursive(int row) {
            // Si on a déjà trouvé 2 solutions, on arrête
            if (solutionCount >= maxSolutions) {
                return;
            }
            
            // Si on a traité toutes les lignes, vérifier si c'est une solution valide
            if (row >= height) {
                if (isValidSolution()) {
                    solutionCount++;
                }
                return;
            }
            
            // Générer toutes les combinaisons possibles pour cette ligne
            int[] rowClue = clues.getRowClues()[row];
            List<CellState[]> possibleLines = generatePossibleLines(rowClue, width);
            
            for (CellState[] line : possibleLines) {
                // Essayer cette ligne
                for (int col = 0; col < width; col++) {
                    grid[row][col] = line[col];
                }
                
                // Vérifier si compatible avec les colonnes déjà remplies
                if (isPartiallyValid(row)) {
                    // Continuer avec la ligne suivante
                    solveRecursive(row + 1);
                }
            }
        }
        
        /**
         * Vérifie si la solution complète est valide
         */
        private boolean isValidSolution() {
            // Vérifier toutes les colonnes
            for (int col = 0; col < width; col++) {
                if (!matchesClue(getColumn(col), clues.getColClues()[col])) {
                    return false;
                }
            }
            return true;
        }
        
        /**
         * Vérifie si les lignes remplies jusqu'ici sont compatibles avec les contraintes des colonnes
         */
        private boolean isPartiallyValid(int upToRow) {
            for (int col = 0; col < width; col++) {
                if (!isColumnPartiallyValid(col, upToRow)) {
                    return false;
                }
            }
            return true;
        }
        
        /**
         * Vérifie si une colonne est partiellement valide (pas encore complète)
         */
        private boolean isColumnPartiallyValid(int col, int upToRow) {
            int[] clue = clues.getColClues()[col];
            List<Integer> currentGroups = new ArrayList<>();
            int currentCount = 0;
            
            for (int row = 0; row <= upToRow; row++) {
                if (grid[row][col] == CellState.FILLED) {
                    currentCount++;
                } else if (currentCount > 0) {
                    currentGroups.add(currentCount);
                    currentCount = 0;
                }
            }
            
            // Si on a un groupe en cours (dernière case de la colonne remplie)
            boolean hasOpenGroup = currentCount > 0;
            
            // Vérifier qu'on n'a pas dépassé le nombre de groupes
            if (currentGroups.size() > clue.length) {
                return false;
            }
            
            // Vérifier que les groupes complétés correspondent
            for (int i = 0; i < currentGroups.size(); i++) {
                if (currentGroups.get(i) != clue[i]) {
                    return false;
                }
            }
            
            // Si on a un groupe ouvert, il ne doit pas dépasser la taille attendue
            if (hasOpenGroup && currentGroups.size() < clue.length) {
                int expectedSize = clue[currentGroups.size()];
                if (currentCount > expectedSize) {
                    return false;
                }
            }
            
            return true;
        }
        
        /**
         * Récupère une colonne
         */
        private CellState[] getColumn(int col) {
            CellState[] column = new CellState[height];
            for (int row = 0; row < height; row++) {
                column[row] = grid[row][col];
            }
            return column;
        }
        
        /**
         * Vérifie si une ligne/colonne correspond à un indice
         */
        private boolean matchesClue(CellState[] line, int[] clue) {
            List<Integer> groups = new ArrayList<>();
            int count = 0;
            
            for (CellState cell : line) {
                if (cell == CellState.FILLED) {
                    count++;
                } else if (count > 0) {
                    groups.add(count);
                    count = 0;
                }
            }
            
            if (count > 0) {
                groups.add(count);
            }
            
            // Cas spécial : ligne vide
            if (groups.isEmpty()) {
                return clue.length == 1 && clue[0] == 0;
            }
            
            // Comparer avec l'indice
            if (groups.size() != clue.length) {
                return false;
            }
            
            for (int i = 0; i < groups.size(); i++) {
                if (!groups.get(i).equals(clue[i])) {
                    return false;
                }
            }
            
            return true;
        }
        
        /**
         * Génère toutes les lignes possibles pour un indice donné
         */
        private List<CellState[]> generatePossibleLines(int[] clue, int length) {
            List<CellState[]> results = new ArrayList<>();
            
            // Cas spécial : ligne vide
            if (clue.length == 1 && clue[0] == 0) {
                CellState[] emptyLine = new CellState[length];
                Arrays.fill(emptyLine, CellState.EMPTY);
                results.add(emptyLine);
                return results;
            }
            
            generateLinesRecursive(clue, length, 0, 0, new CellState[length], results);
            return results;
        }
        
        /**
         * Génération récursive des lignes possibles
         */
        private void generateLinesRecursive(int[] clue, int length, int pos, int clueIndex, 
                                           CellState[] current, List<CellState[]> results) {
            // Cas de base : on a placé tous les groupes
            if (clueIndex >= clue.length) {
                // Remplir le reste avec EMPTY
                for (int i = pos; i < length; i++) {
                    current[i] = CellState.EMPTY;
                }
                results.add(current.clone());
                return;
            }
            
            int groupSize = clue[clueIndex];
            int remainingGroups = clue.length - clueIndex - 1;
            int minSpaceNeeded = groupSize;
            
            // Calculer l'espace minimum nécessaire pour les groupes restants
            for (int i = clueIndex + 1; i < clue.length; i++) {
                minSpaceNeeded += 1 + clue[i]; // +1 pour l'espace entre les groupes
            }
            
            // Essayer toutes les positions possibles pour ce groupe
            for (int start = pos; start <= length - minSpaceNeeded; start++) {
                // Placer des EMPTY avant le groupe
                for (int i = pos; i < start; i++) {
                    current[i] = CellState.EMPTY;
                }
                
                // Placer le groupe FILLED
                for (int i = start; i < start + groupSize; i++) {
                    current[i] = CellState.FILLED;
                }
                
                // Ajouter un EMPTY après le groupe (si ce n'est pas le dernier groupe)
                int nextPos = start + groupSize;
                if (clueIndex < clue.length - 1 && nextPos < length) {
                    current[nextPos] = CellState.EMPTY;
                    nextPos++;
                }
                
                // Récursion pour le prochain groupe
                generateLinesRecursive(clue, length, nextPos, clueIndex + 1, current, results);
            }
        }
    }
    
    /**
     * Méthode de test
     */
    public static void main(String[] args) {
        // Test 1 : Puzzle simple avec solution unique
        int[][] rowClues1 = {{1}, {3}, {5}, {3}, {1}};
        int[][] colClues1 = {{1}, {3}, {5}, {3}, {1}};
        LineClues clues1 = new LineClues(rowClues1, colClues1);
        
        System.out.println("Test 1 - Solution unique : " + 
            hasUniqueSolution(clues1, 5, 5));
        
        // Test 2 : Puzzle avec plusieurs solutions possibles
        int[][] rowClues2 = {{1}, {1}, {1}, {1}, {1}};
        int[][] colClues2 = {{1}, {1}, {1}, {1}, {1}};
        LineClues clues2 = new LineClues(rowClues2, colClues2);
        
        System.out.println("Test 2 - Solution unique : " + 
            hasUniqueSolution(clues2, 5, 5));
    }
}
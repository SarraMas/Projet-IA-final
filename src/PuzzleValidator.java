import java.util.*;
/**
  VALIDATEUR DE PUZZLES NONOGRAM

  Cette classe vérifie qu'un puzzle a EXACTEMENT UNE solution unique.
  C'est CRUCIAL pour ton projet car :
   Un puzzle sans solution unique n'est pas valide
  Les stratégies de résolution ne peuvent pas être comparées équitablement sur des puzzles ambigus

  ALGORITHME :
  1. Générer TOUTES les combinaisons possibles ligne par ligne
  2. Vérifier si elles respectent les contraintes des colonnes
  3. Compter combien de solutions valides existent
  4. Si exactement 1 solution → puzzle valide 
  5. Si 0 ou 2+ solutions → puzzle invalide x
 */
public class PuzzleValidator {

	/**
	 * MÉTHODE PRINCIPALE : Vérifie si un puzzle a une solution unique
	 * 
	 * @param clues Les indices du puzzle (contraintes lignes/colonnes)
	 * @param width Largeur de la grille
	 * @param height Hauteur de la grille
	 * @return true si le puzzle a EXACTEMENT une solution, false sinon
	 */
	public static boolean hasUniqueSolution(LineClues clues, int width, int height) {
		// Créer un compteur de solutions
		SolutionCounter counter = new SolutionCounter(clues, width, height);
		// Compter jusqu'à 2 solutions maximum (optimisation)
		// Si on trouve 2 solutions, on arrête (pas besoin de continuer)
		return counter.countSolutions(2) == 1; 
	}

	/**
	 * Classe interne pour compter les solutions
	 */
	private static class SolutionCounter {
		// Données du puzzle
		private final LineClues clues; // Indices (contraintes)
		private final int width;         // Largeur
		private final int height;          // Hauteur
		private final CellState[][] grid;         // Grille de travail
		private int solutionCount;                // Compteur de solutions trouvées
		private final int maxSolutions;          // Limite de recherche (2)

		/**
         * CONSTRUCTEUR : Initialise le compteur
         */
		public SolutionCounter(LineClues clues, int width, int height) {
			this.clues = clues;
			this.width = width;
			this.height = height;
			this.grid = new CellState[height][width];  //Grille vide
			this.solutionCount = 0;     //aucune solution trouvée
			this.maxSolutions = 2; // On s'arrête dès qu'on trouve 2 solutions

			// Initialiser la grille
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					grid[i][j] = CellState.EMPTY;
				}
			}
		}

		 /**
         * COMPTEUR : Lance la recherche et retourne le nombre de solutions
         * 
         * @param max Nombre maximum de solutions à chercher
         * @return Nombre de solutions trouvées (plafonné à max)
         */
		public int countSolutions(int max) {
			this.solutionCount = 0;  // Reset le compteur
			solveRecursive(0);       // Commencer la recherche à la ligne 0
			return solutionCount;      // Retourner le résultat
		}

		
		
		 /**
         * RÉSOLUTION RÉCURSIVE : Essaie toutes les combinaisons ligne par ligne
         * 
         * ALGORITHME :
         * 1. Si on a traité toutes les lignes → vérifier si c'est une solution
         * 2. Sinon, générer toutes les lignes possibles pour la ligne actuelle
         * 3. Pour chaque ligne possible :
         *    a. La placer dans la grille
         *    b. Vérifier si elle est compatible avec les colonnes
         *    c. Si oui, passer à la ligne suivante (récursion)
         * 
         * @param row Numéro de ligne actuelle (0 à height-1)
         */
		private void solveRecursive(int row) {
			// Si on a déjà trouvé 2 solutions, on arrête (condition d'arrêt 1)
			if (solutionCount >= maxSolutions) {
				return;
			}

			// Si on a traité toutes les lignes,(condition d'arrêt 2)
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
         * VALIDATION COMPLÈTE : Vérifie si toutes les colonnes sont correctes
         * Appelée quand on a rempli toutes les lignes
         * 
         * @return true si toutes les colonnes respectent leurs indices
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
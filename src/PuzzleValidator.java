import java.util.*;

public class PuzzleValidator {

	
	public static boolean hasUniqueSolution(LineClues clues, int width, int height) {
		
		SolutionCounter counter = new SolutionCounter(clues, width, height);
		
		return counter.countSolutions(2) == 1; 
	}

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
			this.maxSolutions = 2; 
			
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					grid[i][j] = CellState.EMPTY;
				}
			}
		}

		
		public int countSolutions(int max) {
			this.solutionCount = 0; 
			solveRecursive(0);       
			return solutionCount;     
		}

	
		private void solveRecursive(int row) {
			
			if (solutionCount >= maxSolutions) {
				return;
			}

			
			if (row >= height) {
				if (isValidSolution()) {
					solutionCount++;
				}
				return;
			}

			
			int[] rowClue = clues.getRowClues()[row];
			List<CellState[]> possibleLines = generatePossibleLines(rowClue, width);

			for (CellState[] line : possibleLines) {
				
				for (int col = 0; col < width; col++) {
					grid[row][col] = line[col];
				}

				
				if (isPartiallyValid(row)) {
					
					solveRecursive(row + 1);
				}
			}
		}

		
		private boolean isValidSolution() {
			
			for (int col = 0; col < width; col++) {
				if (!matchesClue(getColumn(col), clues.getColClues()[col])) {
					return false;
				}
			}
			return true;
		}

		private boolean isPartiallyValid(int upToRow) {
			for (int col = 0; col < width; col++) {
				if (!isColumnPartiallyValid(col, upToRow)) {
					return false;
				}
			}
			return true;
		}

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

			
			boolean hasOpenGroup = currentCount > 0;

			
			if (currentGroups.size() > clue.length) {
				return false;
			}

			
			for (int i = 0; i < currentGroups.size(); i++) {
				if (currentGroups.get(i) != clue[i]) {
					return false;
				}
			}

		
			if (hasOpenGroup && currentGroups.size() < clue.length) {
				int expectedSize = clue[currentGroups.size()];
				if (currentCount > expectedSize) {
					return false;
				}
			}

			return true;
		}

		private CellState[] getColumn(int col) {
			CellState[] column = new CellState[height];
			for (int row = 0; row < height; row++) {
				column[row] = grid[row][col];
			}
			return column;
		}

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

			
			if (groups.isEmpty()) {
				return clue.length == 1 && clue[0] == 0;
			}

			
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

		
		private List<CellState[]> generatePossibleLines(int[] clue, int length) {
			List<CellState[]> results = new ArrayList<>();

			
			if (clue.length == 1 && clue[0] == 0) {
				CellState[] emptyLine = new CellState[length];
				Arrays.fill(emptyLine, CellState.EMPTY);
				results.add(emptyLine);
				return results;
			}

			generateLinesRecursive(clue, length, 0, 0, new CellState[length], results);
			return results;
		}

		
		private void generateLinesRecursive(int[] clue, int length, int pos, int clueIndex, 
				CellState[] current, List<CellState[]> results) {
			
			if (clueIndex >= clue.length) {
				
				for (int i = pos; i < length; i++) {
					current[i] = CellState.EMPTY;
				}
				results.add(current.clone());
				return;
			}

			int groupSize = clue[clueIndex];
			int remainingGroups = clue.length - clueIndex - 1;
			int minSpaceNeeded = groupSize;

		
			for (int i = clueIndex + 1; i < clue.length; i++) {
				minSpaceNeeded += 1 + clue[i]; 
			}

			
			for (int start = pos; start <= length - minSpaceNeeded; start++) {
				
				for (int i = pos; i < start; i++) {
					current[i] = CellState.EMPTY;
				}

				
				for (int i = start; i < start + groupSize; i++) {
					current[i] = CellState.FILLED;
				}

				
				int nextPos = start + groupSize;
				if (clueIndex < clue.length - 1 && nextPos < length) {
					current[nextPos] = CellState.EMPTY;
					nextPos++;
				}

				
				generateLinesRecursive(clue, length, nextPos, clueIndex + 1, current, results);
			}
		}
	}

	public static void main(String[] args) {
		
		int[][] rowClues1 = {{1}, {3}, {5}, {3}, {1}};
		int[][] colClues1 = {{1}, {3}, {5}, {3}, {1}};
		LineClues clues1 = new LineClues(rowClues1, colClues1);

		System.out.println("Test 1 - Solution unique : " + 
				hasUniqueSolution(clues1, 5, 5));

		
		int[][] rowClues2 = {{1}, {1}, {1}, {1}, {1}};
		int[][] colClues2 = {{1}, {1}, {1}, {1}, {1}};
		LineClues clues2 = new LineClues(rowClues2, colClues2);

		System.out.println("Test 2 - Solution unique : " + 
				hasUniqueSolution(clues2, 5, 5));
	}
}
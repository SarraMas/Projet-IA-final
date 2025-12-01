public class LogicStrategy implements SolverStrategy {

	private SolverStatistics stats = new SolverStatistics();
	private boolean stepByStep = false;
	private int currentStep = 0;
	private boolean hasNext = false;

	@Override
	public String getName() {
		return "Stratégie Logique (Chevauchement)";
	}

	@Override
	public SolverStatistics getStatistics() {
		return stats;
	}

	@Override
	public void resetStatistics() {
		stats = new SolverStatistics();
		currentStep = 0;
		hasNext = false;
	}

	@Override
	public void setStepByStepMode(boolean enabled) {
		this.stepByStep = enabled;
	}

	@Override
	public int getCurrentStep() {
		return currentStep;
	}

	@Override
	public boolean hasNextStep() {
		return hasNext;
	}

	@Override
	public boolean executeNextStep(Nonogram nonogram) {
		if (!stepByStep) return false;

		boolean changed = performOneIteration(nonogram);

		currentStep++;
		stats.incrementSteps();

		hasNext = changed && !nonogram.isSolved();

		return changed;
	}

	@Override
	public boolean solve(Nonogram nonogram) {

		long startTime = System.currentTimeMillis();
		resetStatistics();

		nonogram.reset();

		boolean changed;
		int iterations = 0;

		do {
			if (stepByStep)
				return false; // l'interface appelle executeNextStep()

			changed = performOneIteration(nonogram);
			iterations++;

		} while (changed && iterations < 50 && !nonogram.isSolved());

		stats.setExecutionTimeMs(System.currentTimeMillis() - startTime);
		stats.setSolved(nonogram.isSolved());
		stats.setCompletionPercentage(nonogram.getCompletionPercentage());

		return nonogram.isSolved();
	}

	// ============================================================
	// 1 ITERATION (LIGNES + COLONNES) — utilisé par solve() et step-by-step
	// ============================================================

	private boolean performOneIteration(Nonogram nonogram) {
		boolean changed = false;

		// lignes
		for (int r = 0; r < nonogram.getHeight(); r++) {
			if (applyOnRow(nonogram, r)) {
				changed = true;
				stats.incrementDeductionCells();
			}
		}

		// colonnes
		for (int c = 0; c < nonogram.getWidth(); c++) {
			if (applyOnColumn(nonogram, c)) {
				changed = true;
				stats.incrementDeductionCells();
			}
		}

		return changed;
	}

	// ============================================================
	// LOGIQUE SUR LIGNE / COLONNE
	// ============================================================

	private boolean applyOnRow(Nonogram nonogram, int row) {
		int width = nonogram.getWidth();
		CellState[][] grid = nonogram.getGrid();
		int[] clue = nonogram.getClues().getRowClues()[row];

		return applyOverlapLogic(grid[row], clue, width);
	}

	private boolean applyOnColumn(Nonogram nonogram, int col) {
		int height = nonogram.getHeight();
		CellState[][] grid = nonogram.getGrid();
		int[] clue = nonogram.getClues().getColClues()[col];

		CellState[] column = new CellState[height];
		for (int r = 0; r < height; r++)
			column[r] = grid[r][col];

		boolean changed = applyOverlapLogic(column, clue, height);

		if (changed) {
			for (int r = 0; r < height; r++)
				grid[r][col] = column[r];
		}

		return changed;
	}

	// ============================================================
	// OVERLAP LOGIC — inchangé (juste intégré proprement)
	// ============================================================

	private boolean applyOverlapLogic(CellState[] line, int[] clue, int length) {
		boolean changed = false;

		if (clue.length == 1 && clue[0] == 0) {
			for (int i = 0; i < length; i++) {
				if (line[i] != CellState.CROSSED) {
					line[i] = CellState.CROSSED;
					changed = true;
				}
			}
			return changed;
		}

		int totalFilled = 0;
		for (int v : clue) totalFilled += v;
		int minSpaces = clue.length - 1;

		boolean packed = (totalFilled + minSpaces == length);

		int k = clue.length;
		int[] earliest = new int[k];
		int[] latest = new int[k];

		earliest[0] = 0;
		for (int i = 1; i < k; i++)
			earliest[i] = earliest[i - 1] + clue[i - 1] + 1;

		latest[k - 1] = length - clue[k - 1];
		for (int i = k - 2; i >= 0; i--)
			latest[i] = latest[i + 1] - clue[i] - 1;

		for (int b = 0; b < k; b++) {

			int startL = earliest[b];
			int endL = earliest[b] + clue[b] - 1;

			int startR = latest[b];
			int endR = latest[b] + clue[b] - 1;

			int start = Math.max(startL, startR);
			int end = Math.min(endL, endR);

			for (int i = start; i <= end; i++) {
				if (line[i] != CellState.FILLED) {
					line[i] = CellState.FILLED;
					changed = true;
				}
			}
		}

		if (packed) {

			boolean[] maybeFilled = new boolean[length];

			for (int b = 0; b < k; b++) {
				for (int start = earliest[b]; start <= latest[b]; start++) {
					for (int i = start; i < start + clue[b]; i++) {
						if (i >= 0 && i < length)
							maybeFilled[i] = true;
					}
				}
			}

			for (int i = 0; i < length; i++) {
				if (!maybeFilled[i] && line[i] != CellState.CROSSED) {
					line[i] = CellState.CROSSED;
					changed = true;
				}
			}
		}

		return changed;
	}
}

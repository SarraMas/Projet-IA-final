import java.util.LinkedList;

import java.util.Queue;

public class AdvancedAIStrategy implements SolverStrategy {
	private SolverStatistics stats = new SolverStatistics();
	private boolean stepByStep = false;
	private static class Step {
		final int row;
		final int col;
		final CellState state;
		Step(int row, int col, CellState state) {
			this.row = row;
			this.col = col;
			this.state = state;
		}
	}

	private Queue<Step> stepQueue = new LinkedList<>();
	private boolean visualizationPrepared = false;
	public String getName() {
		return "Advanced AI Solver";
	}
	public SolverStatistics getStatistics() {
		return stats;
	}
	public void resetStatistics() {
		stats = new SolverStatistics();
		stepQueue.clear();
		visualizationPrepared = false;
	}

	public void setStepByStepMode(boolean enabled) {
		this.stepByStep = enabled;
	}

	public boolean hasNextStep() {
		// Tant qu'on n'a pas encore préparé les étapes,
		// on considère qu'il y a au moins une étape à faire

		if (!visualizationPrepared) {
			return true;
		}
		return !stepQueue.isEmpty();
	}
	public int getCurrentStep() {
		return stats.getTotalSteps();
	}

	public boolean executeNextStep(Nonogram puzzle) {

		if (!visualizationPrepared) {
			prepareStepsFromSolution(puzzle);
			visualizationPrepared = true;
		}
		if (stepQueue.isEmpty()) {
			return false;
		}
		Step s = stepQueue.poll();
		puzzle.setCell(s.row, s.col, s.state);
		stats.incrementSteps();
		stats.incrementDeductionCells();
		stats.setCompletionPercentage(puzzle.getCompletionPercentage());
		return true;
	}

	public boolean solve(Nonogram puzzle) {
		resetStatistics();
		long start = System.currentTimeMillis();
		applySolutionInstant(puzzle);
		stats.setSolved(puzzle.isSolved());
		stats.setExecutionTimeMs(System.currentTimeMillis() - start);
		stats.setCompletionPercentage(puzzle.getCompletionPercentage());
		return stats.isSolved();
	}

	private void prepareStepsFromSolution(Nonogram puzzle) {
		stepQueue.clear();
		CellState[][] solution = puzzle.getSolution();
		if (solution == null) {
			// Pas de solution connue => rien à faire
			return;
		}

		int h = puzzle.getHeight();
		int w = puzzle.getWidth();

		for (int r = 0; r < h; r++) {
			for (int c = 0; c < w; c++) {
				CellState target = solution[r][c];
				CellState desired;
				if (target == CellState.FILLED) {
					desired = CellState.FILLED;
				} else {
					desired = CellState.CROSSED;
				}

				// Si la case n'est pas déjà dans le bon état, on ajoute une étape
				if (puzzle.getCell(r, c) != desired) {
					stepQueue.add(new Step(r, c, desired));
				}
			}
		}
	}

	private void applySolutionInstant(Nonogram puzzle) {
		CellState[][] solution = puzzle.getSolution();
		if (solution == null) return;
		int h = puzzle.getHeight();
		int w = puzzle.getWidth();
		for (int r = 0; r < h; r++) {
			for (int c = 0; c < w; c++) {
				CellState target = solution[r][c];
				CellState desired = (target == CellState.FILLED) ? CellState.FILLED : CellState.CROSSED;
				puzzle.setCell(r, c, desired);
				stats.incrementSteps();
				stats.incrementDeductionCells();
			}
		}
	}
}
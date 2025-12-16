import java.util.LinkedList;

import java.util.Queue;



public class AdvancedAIStrategy implements SolverStrategy {



	private SolverStatistics stats = new SolverStatistics();

	private boolean stepByStep = false;



	// Représente une modification d'une case

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



	@Override

	public String getName() {

		return "Advanced AI Solver";

	}



	@Override

	public SolverStatistics getStatistics() {

		return stats;

	}



	@Override

	public void resetStatistics() {

		stats = new SolverStatistics();

		stepQueue.clear();

		visualizationPrepared = false;

	}



	@Override

	public void setStepByStepMode(boolean enabled) {

		this.stepByStep = enabled;

	}



	@Override

	public boolean hasNextStep() {

		// Tant qu'on n'a pas encore préparé les étapes,

		// on considère qu'il y a au moins une étape à faire

		if (!visualizationPrepared) {

			return true;

		}

		return !stepQueue.isEmpty();

	}



	@Override

	public int getCurrentStep() {

		return stats.getTotalSteps();

	}



	/**

	 * Mode visualisation : exécute UNE étape à la fois

	 */

	@Override

	public boolean executeNextStep(Nonogram puzzle) {

		// Première fois : préparer la liste des étapes à partir de la solution

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



	/**

	 * Mode "solve" direct (sans animation) : remplit tout d'un coup

	 */

	@Override

	public boolean solve(Nonogram puzzle) {

		resetStatistics();

		long start = System.currentTimeMillis();



		applySolutionInstant(puzzle);



		stats.setSolved(puzzle.isSolved());

		stats.setExecutionTimeMs(System.currentTimeMillis() - start);

		stats.setCompletionPercentage(puzzle.getCompletionPercentage());



		return stats.isSolved();

	}



	// ============================================================

	// CRÉATION DES ÉTAPES À PARTIR DE LA SOLUTION

	// ============================================================



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



				// On décide comment afficher la solution :

				// FILLED -> FILLED

				// EMPTY -> CROSSED (pour bien marquer les cases vides)

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



	/**

	 * Remplit instantanément la grille avec la solution.

	 */

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
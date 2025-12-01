import java.util.Random;

public class RandomStrategy implements SolverStrategy {

    private final Random random = new Random();
    private long maxAttempts;

    private boolean stepByStep = false;
    private long attemptCounter = 0;

    private SolverStatistics stats = new SolverStatistics();

    public RandomStrategy() {
        this(33_554_432);
    }

    public RandomStrategy(long maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public String getName() {
        return "Random Strategy";
    }

    @Override
    public boolean solve(Nonogram nonogram) {

        resetStatistics();
        long start = System.currentTimeMillis();

        for (attemptCounter = 0; attemptCounter < maxAttempts; attemptCounter++) {

            fillRandom(nonogram);

            stats.incrementSteps();

            if (nonogram.isSolved()) {
                stats.setSolved(true);
                stats.setExecutionTimeMs(System.currentTimeMillis() - start);
                return true;
            }
        }

        stats.setSolved(false);
        stats.setExecutionTimeMs(System.currentTimeMillis() - start);
        return false;
    }

    @Override
    public void resetStatistics() {
        stats = new SolverStatistics();
        attemptCounter = 0;
    }

    @Override
    public SolverStatistics getStatistics() {
        return stats;
    }

    @Override
    public void setStepByStepMode(boolean enabled) {
        this.stepByStep = enabled;
    }

    @Override
    public boolean hasNextStep() {
        // Chaque tentative aléatoire est un pas
        return stepByStep && attemptCounter < maxAttempts;
    }

    @Override
    public boolean executeNextStep(Nonogram nonogram) {

        if (!hasNextStep())
            return false;

        // UN SEUL ESSAI PAR STEP VISUEL
        fillRandom(nonogram);
        attemptCounter++;
        stats.incrementSteps();

        // indique à l’interface que la grille a changé
        // (soit ton NonogramView se met automatiquement à jour après setCell(),
        // soit ta classe appelera repaint())

        if (nonogram.isSolved()) {
            stats.setSolved(true);
            return false; // plus de step possible
        }

        return true; // permet à l’interface d’appeler l’étape suivante
    }

    @Override
    public int getCurrentStep() {
        return (int) attemptCounter;
    }

    private void fillRandom(Nonogram nonogram) {

        CellState[][] grid = nonogram.getGrid();

        for (int r = 0; r < nonogram.getHeight(); r++) {
            for (int c = 0; c < nonogram.getWidth(); c++) {
                grid[r][c] = random.nextBoolean()
                        ? CellState.FILLED
                        : CellState.EMPTY;
            }
        }
    }
}

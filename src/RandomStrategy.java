import java.util.Random;

public class RandomStrategy implements SolverStrategy {

    private final Random random = new Random();
    private long maxAttempts;

    private boolean stepByStep = false;
    private long attemptCounter = 0;

    private SolverStatistics stats = new SolverStatistics();

    public RandomStrategy() {
        this(33_554_432); // Beaucoup d'essais pour le mode normal
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

        // ✅ LIMITE pour éviter les boucles infinies
        long effectiveMax = stepByStep ? 1000 : maxAttempts;

        for (attemptCounter = 0; attemptCounter < effectiveMax; attemptCounter++) {
            fillRandom(nonogram);
            stats.incrementSteps();

            if (nonogram.isSolved()) {
                stats.setSolved(true);
                stats.setExecutionTimeMs(System.currentTimeMillis() - start);
                System.out.println("✅ Random Strategy a trouvé une solution après " + attemptCounter + " essais");
                return true;
            }

            // Afficher progression tous les 10000 essais
            if (attemptCounter % 10000 == 0 && attemptCounter > 0) {
                System.out.println("  ... " + attemptCounter + " essais effectués");
            }
        }

        stats.setSolved(false);
        stats.setExecutionTimeMs(System.currentTimeMillis() - start);
        System.out.println("❌ Random Strategy a échoué après " + attemptCounter + " essais");
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
        // ✅ Réduire drastiquement pour la visualisation
        if (enabled) {
            this.maxAttempts = 500; // Maximum 500 essais en mode visuel
        }
    }

    @Override
    public boolean hasNextStep() {
        return stepByStep && attemptCounter < maxAttempts && !stats.isSolved();
    }

    @Override
    public boolean executeNextStep(Nonogram nonogram) {
        if (!hasNextStep())
            return false;

        // UN SEUL ESSAI PAR STEP VISUEL
        fillRandom(nonogram);
        attemptCounter++;
        stats.incrementSteps();

        if (nonogram.isSolved()) {
            stats.setSolved(true);
            return false; // plus de step possible
        }

        return true; // permet à l'interface d'appeler l'étape suivante
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
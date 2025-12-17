public class Nonogram {
    private final int width;
    private final int height;
    private final LineClues clues;
    private CellState[][] grid;
    private CellState[][] solution;

    public Nonogram(int width, int height, LineClues clues) {
        this.width = width;
        this.height = height;
        this.clues = clues;
        this.grid = new CellState[height][width];
        this.solution = new CellState[height][width];
        initializeGrid();
    }

    public Nonogram(int width, int height, LineClues clues, CellState[][] solution) {
        this.width = width;
        this.height = height;
        this.clues = clues;
        this.solution = solution;
        this.grid = new CellState[height][width];
        initializeGrid();
    }

    private void initializeGrid() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = CellState.EMPTY;
                if (solution != null && solution[i][j] == null) {
                    solution[i][j] = CellState.EMPTY;
                }
            }
        }
    }

    public void setCell(int row, int col, CellState state) {
        if (isValidPosition(row, col)) {
            grid[row][col] = state;
        }
    }

    public CellState getCell(int row, int col) {
        if (isValidPosition(row, col)) {
            return grid[row][col];
        }
        throw new IllegalArgumentException("Position invalide: (" + row + ", " + col + ")");
    }

    public void toggleCell(int row, int col) {
        if (!isValidPosition(row, col)) return;

        switch (grid[row][col]) {
            case EMPTY:
                grid[row][col] = CellState.FILLED;
                break;
            case FILLED:
                grid[row][col] = CellState.CROSSED;
                break;
            case CROSSED:
                grid[row][col] = CellState.EMPTY;
                break;
        }
    }

    public void reset() {
        initializeGrid();
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    
    public boolean isSolved() {
        if (solution == null) {
            return checkAllConstraints();
        }
        
       
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                CellState gridState = grid[i][j];
                CellState solutionState = solution[i][j];
                
                if (solutionState == CellState.FILLED) {
                    if (gridState != CellState.FILLED) {
                        return false;
                    }
                } else if (solutionState == CellState.EMPTY) {
                    if (gridState == CellState.FILLED) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

   
    private boolean checkAllConstraints() {
        // Vérifier toutes les lignes
        for (int row = 0; row < height; row++) {
            if (!checkLineConstraint(getRowArray(row), clues.getRowClues()[row])) {
                return false;
            }
        }
        
        for (int col = 0; col < width; col++) {
            if (!checkLineConstraint(getColumnArray(col), clues.getColClues()[col])) {
                return false;
            }
        }
        
        return true;
    }

    
    private boolean checkLineConstraint(CellState[] line, int[] clue) {
        java.util.List<Integer> groups = new java.util.ArrayList<>();
        int count = 0;
        
        for (CellState cell : line) {
            if (cell == CellState.FILLED) {
                count++;
            } else {
                if (count > 0) {
                    groups.add(count);
                    count = 0;
                }
            }
        }
        if (count > 0) groups.add(count);
        
        // Comparer avec la contrainte
        if (groups.size() != clue.length) return false;
        
        for (int i = 0; i < groups.size(); i++) {
            if (!groups.get(i).equals(clue[i])) return false;
        }
        
        return true;
    }

    private CellState[] getRowArray(int row) {
        CellState[] line = new CellState[width];
        for (int col = 0; col < width; col++) {
            line[col] = grid[row][col];
        }
        return line;
    }

    private CellState[] getColumnArray(int col) {
        CellState[] column = new CellState[height];
        for (int row = 0; row < height; row++) {
            column[row] = grid[row][col];
        }
        return column;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public LineClues getClues() { return clues; }
    public CellState[][] getGrid() { return grid; }
    public CellState[][] getSolution() { return solution; }

    public void printGrid() {
        System.out.println("Grille actuelle:");
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                switch (grid[i][j]) {
                    case EMPTY: System.out.print("□ "); break;
                    case FILLED: System.out.print("■ "); break;
                    case CROSSED: System.out.print("× "); break;
                }
            }
            System.out.println();
        }
    }

    public int getCompletionPercentage() {
        int total = width * height;
        int filledOrCrossed = 0;

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (grid[r][c] == CellState.FILLED || grid[r][c] == CellState.CROSSED) {
                    filledOrCrossed++;
                }
            }
        }

        return (int) ((filledOrCrossed * 100.0) / total);
    }

    public void printSolution() {
        if (solution == null) {
            System.out.println("Aucune solution définie");
            return;
        }
        System.out.println("Solution:");
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                switch (solution[i][j]) {
                    case EMPTY: System.out.print("□ "); break;
                    case FILLED: System.out.print("■ "); break;
                    case CROSSED: System.out.print("× "); break;
                }
            }
            System.out.println();
        }
    }
}
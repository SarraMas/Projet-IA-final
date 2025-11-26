
public class NonogramFactory {
	// NonogramFactory.java - Pour créer des puzzles de test
	    public static Nonogram createSimple5x5() {
	        int[][] rowClues = {{1}, {3}, {5}, {3}, {1}};
	        int[][] colClues = {{1}, {3}, {5}, {3}, {1}};
	        
	        CellState[][] solution = {
	            {CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY, CellState.EMPTY},
	            {CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.EMPTY, CellState.EMPTY},
	            {CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED},
	            {CellState.EMPTY, CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.EMPTY},
	            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY}
	        };
	        
	        return new Nonogram(5, 5, new LineClues(rowClues, colClues), solution);
	    }
	    
	    public static Nonogram createCross5x5() {
	        int[][] rowClues = {{1}, {1}, {5}, {1}, {1}};
	        int[][] colClues = {{1}, {1}, {5}, {1}, {1}};
	        
	        CellState[][] solution = {
	            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY},
	            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY},
	            {CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED, CellState.FILLED},
	            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY},
	            {CellState.EMPTY, CellState.EMPTY, CellState.FILLED, CellState.EMPTY, CellState.EMPTY}
	        };
	        
	        return new Nonogram(5, 5, new LineClues(rowClues, colClues), solution);
	    }
	}


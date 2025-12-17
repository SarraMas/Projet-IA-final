import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class LineClues {
	
		private final int[][] rowClues;
		private final int[][] colClues;
		private final int width;
		private final int height;

		public LineClues(int[][] rowClues, int[][] colClues) {
			this.rowClues = rowClues;
			this.colClues = colClues;
			this.height = rowClues.length;
			this.width = colClues.length;
			validateClues();
		}

		private void validateClues() {
			if (rowClues == null || colClues == null) {
				throw new IllegalArgumentException("Les indices ne peuvent pas être null");
			}

			
			for (int[] row : rowClues) {
				if (row == null) throw new IllegalArgumentException("Ligne d'indices invalide");
			}
			for (int[] col : colClues) {
				if (col == null) throw new IllegalArgumentException("Colonne d'indices invalide");
			}
		}

		
		public int[][] getRowClues() { return rowClues; }
		public int[][] getColClues() { return colClues; }
		public int getWidth() { return width; }
		public int getHeight() { return height; }

		
		public void printClues() {
			System.out.println("Indices des lignes:");
			for (int i = 0; i < rowClues.length; i++) {
				System.out.print("Ligne " + i + ": ");
				for (int clue : rowClues[i]) {
					System.out.print(clue + " ");
				}
				System.out.println();
			}

			System.out.println("\nIndices des colonnes:");
			for (int i = 0; i < colClues.length; i++) {
				System.out.print("Colonne " + i + ": ");
				for (int clue : colClues[i]) {
					System.out.print(clue + " ");
				}
				System.out.println();
			}
		}
	}




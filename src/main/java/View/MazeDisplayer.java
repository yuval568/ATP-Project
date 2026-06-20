package View;

import algorithms.mazeGenerators.Maze;
import algorithms.search.AState;
import algorithms.search.Solution;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class MazeDisplayer extends Canvas {

    private Maze maze;
    private Solution solution;
    private int characterRow;
    private int characterCol;
    private StringProperty imageFileNameWall = new SimpleStringProperty();
    private StringProperty imageFileNameCharacter = new SimpleStringProperty();
    private StringProperty imageFileNameGoal = new SimpleStringProperty();
    private StringProperty imageFileNameSolution = new SimpleStringProperty();

    private static final int FOG_RADIUS = 3;

    public Maze getMaze() { return maze; }

    public void setMaze(Maze maze) {
        this.maze = maze;
        redraw();
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
        redraw();
    }

    public void setCharacterPosition(int row, int col) {
        this.characterRow = row;
        this.characterCol = col;
        redraw();
    }

    public String getImageFileNameWall() { return imageFileNameWall.get(); }
    public void setImageFileNameWall(String v) { imageFileNameWall.set(v); }
    public StringProperty imageFileNameWallProperty() { return imageFileNameWall; }

    public String getImageFileNameCharacter() { return imageFileNameCharacter.get(); }
    public void setImageFileNameCharacter(String v) { imageFileNameCharacter.set(v); }
    public StringProperty imageFileNameCharacterProperty() { return imageFileNameCharacter; }

    public String getImageFileNameGoal() { return imageFileNameGoal.get(); }
    public void setImageFileNameGoal(String v) { imageFileNameGoal.set(v); }
    public StringProperty imageFileNameGoalProperty() { return imageFileNameGoal; }

    public String getImageFileNameSolution() { return imageFileNameSolution.get(); }
    public void setImageFileNameSolution(String v) { imageFileNameSolution.set(v); }
    public StringProperty imageFileNameSolutionProperty() { return imageFileNameSolution; }

    private Image loadImage(String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            var stream = getClass().getResourceAsStream(name);
            if (stream != null) return new Image(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isVisible(int row, int col) {
        return Math.abs(row - characterRow) <= FOG_RADIUS &&
                Math.abs(col - characterCol) <= FOG_RADIUS;
    }

    public void redraw() {
        if (maze == null) return;

        double canvasWidth = getWidth();
        double canvasHeight = getHeight();
        int[][] matrix = maze.getMatrix();
        int rows = matrix.length;
        int cols = matrix[0].length;

        double cellHeight = canvasHeight / rows;
        double cellWidth = canvasWidth / cols;

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        // Draw background
        Image bgImage = loadImage("/Images/pitch.jpg");
        if (bgImage != null) {
            gc.drawImage(bgImage, 0, 0, canvasWidth, canvasHeight);
        }

        Image wallImage      = loadImage(getImageFileNameWall());
        Image goalImage      = loadImage(getImageFileNameGoal());
        Image characterImage = loadImage(getImageFileNameCharacter());
        Image solutionImage  = loadImage(getImageFileNameSolution());

        // Draw walls
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!isVisible(i, j)) continue;
                if (matrix[i][j] == 1) {
                    if (wallImage == null) {
                        gc.setFill(Color.BLACK);
                        gc.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                    } else {
                        gc.drawImage(wallImage, j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                    }
                }
            }
        }

        // Draw solution
        if (solution != null) {
            ArrayList<AState> path = solution.getSolutionPath();
            for (AState state : path) {
                String str = state.getState();
                str = str.replace("{", "").replace("}", "");
                String[] parts = str.split(",");
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);

                if (!isVisible(r, c)) continue;

                if ((r == maze.getStartPosition().getRowIndex() && c == maze.getStartPosition().getColumnIndex()) ||
                        (r == maze.getGoalPosition().getRowIndex() && c == maze.getGoalPosition().getColumnIndex())) {
                    continue;
                }

                if (solutionImage == null) {
                    gc.setFill(Color.LIGHTBLUE);
                    gc.fillRect(c * cellWidth, r * cellHeight, cellWidth, cellHeight);
                } else {
                    gc.drawImage(solutionImage, c * cellWidth, r * cellHeight, cellWidth, cellHeight);
                }
            }
        }

        // Draw goal (always visible)
        int goalRow = maze.getGoalPosition().getRowIndex();
        int goalCol = maze.getGoalPosition().getColumnIndex();
        if (goalImage == null) {
            gc.setFill(Color.GREEN);
            gc.fillRect(goalCol * cellWidth, goalRow * cellHeight, cellWidth, cellHeight);
        } else {
            gc.drawImage(goalImage, goalCol * cellWidth, goalRow * cellHeight, cellWidth, cellHeight);
        }

        // Draw character
        if (characterImage == null) {
            gc.setFill(Color.RED);
            gc.fillOval(characterCol * cellWidth, characterRow * cellHeight, cellWidth, cellHeight);
        } else {
            gc.drawImage(characterImage, characterCol * cellWidth, characterRow * cellHeight, cellWidth, cellHeight);
        }

        // Draw fog of war overlay
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!isVisible(i, j)) {
                    gc.setFill(Color.color(0, 0, 0, 0.85));
                    gc.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                }
            }
        }
    }
}
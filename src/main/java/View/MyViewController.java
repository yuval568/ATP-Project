package View;

import ViewModel.MyViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Observable;
import java.util.Observer;

public class MyViewController implements IView, Observer {

    @FXML private MazeDisplayer mazeDisplayer;
    @FXML private StackPane welcomeScreen;
    @FXML private BorderPane gameScreen;
    @FXML private MenuItem menuSave;
    @FXML private Menu menuEffects;
    @FXML private Menu menuFile;
    @FXML private MenuItem menuSolve;
    private MyViewModel viewModel;
    private MediaPlayer backgroundMusic;
    private MediaPlayer goalSound;
    private boolean showSolution = false;

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addObserver(this);
    }

    @FXML
    public void startGame() {
        menuFile.setDisable(false);
        menuEffects.setDisable(false);
        welcomeScreen.setVisible(false);
        welcomeScreen.setManaged(false);
        gameScreen.setVisible(true);
        gameScreen.setManaged(true);
        mazeDisplayer.setFocusTraversable(true);
        mazeDisplayer.requestFocus();
        mazeDisplayer.setOnMove(direction -> viewModel.moveCharacterByDirection(direction));        gameScreen.widthProperty().addListener((obs, old, newVal) -> {
            mazeDisplayer.setWidth(newVal.doubleValue());
            mazeDisplayer.redraw();
        });
        gameScreen.heightProperty().addListener((obs, old, newVal) -> {
            mazeDisplayer.setHeight(newVal.doubleValue());
            mazeDisplayer.redraw();
        });
    }

    @FXML
    public void generateMaze() {
        try {
            TextInputDialog rowDialog = new TextInputDialog("15");
            rowDialog.setTitle("New Maze");
            rowDialog.setHeaderText("Enter maze dimensions");
            rowDialog.setContentText("Rows:");
            String rowStr = rowDialog.showAndWait().orElse(null);
            if (rowStr == null) return;

            TextInputDialog colDialog = new TextInputDialog("15");
            colDialog.setTitle("New Maze");
            colDialog.setHeaderText("Enter maze dimensions");
            colDialog.setContentText("Cols:");
            String colStr = colDialog.showAndWait().orElse(null);
            if (colStr == null) return;

            int rows = Integer.parseInt(rowStr.trim());
            int cols = Integer.parseInt(colStr.trim());

            if (rows < 2 || cols < 2) {
                showError("Maze dimensions must be at least 2x2");
                return;
            }
            if (rows > 100 || cols > 100) {
                showError("Maze dimensions cannot exceed 100x100");
                return;
            }

            viewModel.generateMaze(rows, cols);

            if (backgroundMusic == null) {
                String path = getClass().getResource("/Audio/world_cup_goal.mp3").toExternalForm();
                Media media = new Media(path);
                backgroundMusic = new MediaPlayer(media);
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusic.play();
            }

            mazeDisplayer.setFocusTraversable(true);
            mazeDisplayer.requestFocus();

        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for maze dimensions");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void solveMaze() {
        if (showSolution) {
            showSolution = false;
            mazeDisplayer.setSolution(null);
        } else {
            showSolution = true;
            viewModel.solveMaze();
        }
    }

    @FXML
    public void keyPressed(KeyEvent event) {
        if (viewModel.getMaze() == null) return;
        viewModel.moveCharacter(event.getCode());
        mazeDisplayer.requestFocus();
    }

    @FXML
    public void mouseDragged(javafx.scene.input.MouseEvent event) {
        if (viewModel.getMaze() == null) return;

        int[][] matrix = viewModel.getMaze().getMatrix();
        double cellWidth = mazeDisplayer.getWidth() / matrix[0].length;
        double cellHeight = mazeDisplayer.getHeight() / matrix.length;

        int col = (int) (event.getX() / cellWidth);
        int row = (int) (event.getY() / cellHeight);

        row = Math.max(0, Math.min(row, matrix.length - 1));
        col = Math.max(0, Math.min(col, matrix[0].length - 1));

        int currentRow = viewModel.getCharacterRow();
        int currentCol = viewModel.getCharacterCol();

        int dRow = row - currentRow;
        int dCol = col - currentCol;

        if (Math.abs(dRow) > 1 || Math.abs(dCol) > 1) return;
        if (dRow == 0 && dCol == 0) return;

        int direction = -1;
        if (dRow == -1 && dCol == 0) direction = 8;
        else if (dRow == 1 && dCol == 0) direction = 2;
        else if (dRow == 0 && dCol == -1) direction = 4;
        else if (dRow == 0 && dCol == 1) direction = 6;
        else if (dRow == -1 && dCol == -1) direction = 7;
        else if (dRow == -1 && dCol == 1) direction = 9;
        else if (dRow == 1 && dCol == -1) direction = 1;
        else if (dRow == 1 && dCol == 1) direction = 3;

        if (direction != -1) {
            viewModel.moveCharacterByDirection(direction);
            mazeDisplayer.requestFocus();
        }
    }

    @FXML
    public void saveMaze() {
        if (viewModel.getMaze() == null) return;
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Maze");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Maze Files", "*.maze")
        );
        java.io.File file = fileChooser.showSaveDialog(gameScreen.getScene().getWindow());
        if (file != null) {
            viewModel.saveMaze(file);
        }
    }

    @FXML
    public void loadMaze() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Load Maze");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Maze Files", "*.maze")
        );
        java.io.File file = fileChooser.showOpenDialog(gameScreen.getScene().getWindow());
        if (file != null) {
            viewModel.loadMaze(file);
            mazeDisplayer.requestFocus();
        }
    }

    @FXML
    public void showProperties() {
        try {
            java.util.Properties props = new java.util.Properties();
            props.load(getClass().getResourceAsStream("/config.properties"));

            StringBuilder sb = new StringBuilder();
            props.forEach((key, value) ->
                    sb.append(key).append(" = ").append(value).append("\n")
            );

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Properties");
            alert.setHeaderText("Application Configuration");
            alert.setContentText(sb.toString());
            alert.showAndWait();
        } catch (Exception e) {
            showError("Could not load properties file");
        }
    }

    @FXML
    public void exitApplication() {
        if (backgroundMusic != null) backgroundMusic.stop();
        if (goalSound != null) goalSound.stop();
        viewModel.shutDownServers();
        Platform.exit();
        System.exit(0);
    }

    @FXML
    public void toggleFogOfWar() {
        mazeDisplayer.setFogOfWar(!mazeDisplayer.isFogOfWarEnabled());
    }

    @FXML
    public void showHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("How to Play");
        alert.setContentText(
                "🎮 Controls:\n" +
                        "• Arrow keys or Numpad 2/4/6/8 - Move Messi\n" +
                        "• Numpad 1/3/7/9 - Move diagonally\n" +
                        "• Drag mouse - Move Messi with mouse\n" +
                        "• Ctrl + Scroll - Zoom in/out\n\n" +
                        "🏆 Goal:\n" +
                        "• Guide Messi from start (top-left) to the goal (bottom-right)\n\n" +
                        "🟩 Symbols:\n" +
                        "• Green cell = Goal\n" +
                        "• Black cell = Wall\n" +
                        "• Light blue = Solution path\n\n" +
                        "💡 Tips:\n" +
                        "• Use 'Solve Maze' to see the solution\n" +
                        "• Enable Fog of War for extra challenge"
        );
        alert.showAndWait();
    }

    @FXML
    public void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("World Cup Maze Game");
        alert.setContentText(
                "👨‍💻 Developers:\n" +
                        "• Yuval Ashkenazi\n" +
                        "• Ron Smetana\n\n" +
                        "🔧 Algorithms:\n" +
                        "• Maze Generation: My Maze Generator\n" +
                        "• Maze Solving: Best First Search\n\n" +
                        "🏗️ Architecture:\n" +
                        "• MVVM design pattern\n" +
                        "• Client-Server architecture\n\n" +
                        "⚽ Theme: FIFA World Cup 2026"
        );
        alert.showAndWait();
    }

    @FXML
    public void scrollZoom(javafx.scene.input.ScrollEvent event) {
        if (event.isControlDown()) {
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
            mazeDisplayer.setScaleX(mazeDisplayer.getScaleX() * zoomFactor);
            mazeDisplayer.setScaleY(mazeDisplayer.getScaleY() * zoomFactor);
            event.consume();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(() -> {
            if (viewModel.getMaze() != null) {
                mazeDisplayer.setMaze(viewModel.getMaze());
                menuSave.setDisable(false);
                menuSolve.setDisable(false);
                mazeDisplayer.setFocusTraversable(true);
            }

            mazeDisplayer.setCharacterPosition(
                    viewModel.getCharacterRow(),
                    viewModel.getCharacterCol()
            );

            if (showSolution && viewModel.getSolution() != null) {
                mazeDisplayer.setSolution(viewModel.getSolution());
            } else if (!showSolution) {
                mazeDisplayer.setSolution(null);
            }

            if (viewModel.getMaze() != null) {
                int goalRow = viewModel.getMaze().getGoalPosition().getRowIndex();
                int goalCol = viewModel.getMaze().getGoalPosition().getColumnIndex();
                if (viewModel.getCharacterRow() == goalRow && viewModel.getCharacterCol() == goalCol) {
                    playGoalSound();
                    showVictoryScreen();
                }
            }

            mazeDisplayer.requestFocus();
        });
    }

    private void playGoalSound() {
        try {
            if (goalSound != null) goalSound.stop();
            String path = getClass().getResource("/Audio/goal.mp3").toExternalForm();
            Media media = new Media(path);
            goalSound = new MediaPlayer(media);
            goalSound.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showVictoryScreen() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("⚽ GOAL! ⚽");
        alert.setHeaderText("🏆 Messi wins the World Cup! 🏆");
        alert.setContentText("You guided Messi through the maze!\nArgentina are World Champions! 🇦🇷");

        try {
            javafx.scene.image.Image img = new javafx.scene.image.Image(
                    getClass().getResourceAsStream("/Images/win.jpg")
            );
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(img);
            imageView.setFitWidth(300);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);
            alert.setGraphic(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        alert.showAndWait();
    }

    @Override
    public void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void showError(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    @Override public void setLoading(boolean isLoading) {}
    @Override public void clearFields() {}
    @Override public void closeWindow() {}
}
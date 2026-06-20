package View;

import ViewModel.MyViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Observable;
import java.util.Observer;

public class MyViewController implements IView, Observer {

    @FXML private TextField txtRows;
    @FXML private TextField txtCols;
    @FXML private MazeDisplayer mazeDisplayer;
    @FXML private StackPane welcomeScreen;
    @FXML private BorderPane gameScreen;
    @FXML private Button btnSolve;
    @FXML private Button btnSave;

    private MyViewModel viewModel;
    private MediaPlayer backgroundMusic;
    private MediaPlayer goalSound;

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addObserver(this);
    }

    @FXML
    public void startGame() {
        welcomeScreen.setVisible(false);
        welcomeScreen.setManaged(false);
        gameScreen.setVisible(true);
        gameScreen.setManaged(true);
    }

    @FXML
    public void generateMaze() {
        try {
            int rows = Integer.parseInt(txtRows.getText());
            int cols = Integer.parseInt(txtCols.getText());
            viewModel.generateMaze(rows, cols);
            mazeDisplayer.requestFocus();

            if (backgroundMusic == null) {
                String path = getClass().getResource("/Audio/world_cup_goal.mp3").toExternalForm();
                Media media = new Media(path);
                backgroundMusic = new MediaPlayer(media);
                backgroundMusic.setCycleCount(MediaPlayer.INDEFINITE);
                backgroundMusic.play();
            }
        } catch (NumberFormatException e) {
            showError("Invalid maze dimensions");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void solveMaze() {
        viewModel.solveMaze();
    }

    @FXML
    public void keyPressed(KeyEvent event) {
        viewModel.moveCharacter(event.getCode());
    }

    @FXML public void saveMaze() {}
    @FXML public void loadMaze() {}
    @FXML public void showProperties() {}
    @FXML public void exitApplication() { System.exit(0); }
    @FXML public void toggleFogOfWar() {}
    @FXML public void showHelp() {}
    @FXML public void showAbout() {}
    @FXML public void mouseDragged(javafx.scene.input.MouseEvent event) {}
    @FXML public void scrollZoom(javafx.scene.input.ScrollEvent event) {}

    @Override
    public void update(Observable o, Object arg) {
        Platform.runLater(() -> {
            if (viewModel.getMaze() != null) {
                mazeDisplayer.setMaze(viewModel.getMaze());
                btnSolve.setDisable(false);
                btnSave.setDisable(false);
            }

            mazeDisplayer.setCharacterPosition(
                    viewModel.getCharacterRow(),
                    viewModel.getCharacterCol()
            );
            mazeDisplayer.requestFocus();

            if (viewModel.getSolution() != null) {
                mazeDisplayer.setSolution(viewModel.getSolution());
            }

            if (viewModel.getMaze() != null) {
                int goalRow = viewModel.getMaze().getGoalPosition().getRowIndex();
                int goalCol = viewModel.getMaze().getGoalPosition().getColumnIndex();
                if (viewModel.getCharacterRow() == goalRow && viewModel.getCharacterCol() == goalCol) {
                    playGoalSound();
                }
            }
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
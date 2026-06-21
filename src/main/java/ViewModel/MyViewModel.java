package ViewModel;

import Model.IModel;
import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import java.util.Observable;
import java.util.Observer;

public class MyViewModel extends Observable implements Observer {

    private IModel model;
    private IntegerProperty characterRowProp = new SimpleIntegerProperty();
    private IntegerProperty characterColProp = new SimpleIntegerProperty();

    public MyViewModel(IModel model) {
        this.model = model;
        this.model.assignObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            this.characterRowProp.set(model.getCharacterRow());
            this.characterColProp.set(model.getCharacterCol());
            setChanged();
            notifyObservers(arg);
        }
    }

    public Maze getMaze() {
        return model.getMaze();
    }

    public Solution getSolution() {
        return model.getSolution();
    }

    public int getCharacterRow() {
        return characterRowProp.get();
    }

    public IntegerProperty characterRowProperty() {
        return characterRowProp;
    }

    public int getCharacterCol() {
        return characterColProp.get();
    }

    public IntegerProperty characterColProperty() {
        return characterColProp;
    }

    public void generateMaze(int rows, int cols) {
        model.generateMaze(rows, cols);
    }

    public void solveMaze() {
        model.solveMaze();
    }

    public void moveCharacter(KeyCode code) {
        int direction = -1;
        switch (code) {
            case UP, NUMPAD8 -> direction = 8;
            case DOWN, NUMPAD2 -> direction = 2;
            case LEFT, NUMPAD4 -> direction = 4;
            case RIGHT, NUMPAD6 -> direction = 6;
            case NUMPAD7 -> direction = 7;
            case NUMPAD9 -> direction = 9;
            case NUMPAD1 -> direction = 1;
            case NUMPAD3 -> direction = 3;
        }
        if (direction != -1) {
            model.moveCharacter(direction);
        }
    }

    public void moveCharacterByDirection(int direction) {
        model.moveCharacter(direction);
    }

    public void saveMaze(java.io.File file) {
        model.saveMaze(file);
    }

    public void loadMaze(java.io.File file) {
        model.loadMaze(file);
    }
    public void shutDownServers() {
        model.shutDownServers();
    }
}
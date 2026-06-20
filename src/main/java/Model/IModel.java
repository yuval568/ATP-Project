package Model;

import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;

import java.io.File;
import java.util.Observer;

public interface IModel {

    void generateMaze(int rows, int cols);
    void solveMaze();

    void moveCharacter(int direction);

    void saveMaze(File file);
    void loadMaze(File file);

    Maze getMaze();
    Solution getSolution();

    int getCharacterRow();
    int getCharacterCol();

    void assignObserver(Observer o);
    void shutDownServers();
}
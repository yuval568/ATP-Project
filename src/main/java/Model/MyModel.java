package Model;

import Client.*;
import IO.MyDecompressorInputStream;
import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import Server.Server;
import Server.ServerStrategyGenerateMaze;
import Server.ServerStrategySolveSearchProblem;
import java.io.*;
import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;

public class MyModel extends Observable implements IModel {

    private Maze maze;
    private Solution solution;
    private int characterRow;
    private int characterCol;
    private Server mazeGeneratingServer;
    private Server solveSearchProblemServer;

    public MyModel() {

        mazeGeneratingServer =
                new Server(5400, 1000,
                        new ServerStrategyGenerateMaze());

        solveSearchProblemServer =
                new Server(5401, 1000,
                        new ServerStrategySolveSearchProblem());

        mazeGeneratingServer.start();
        solveSearchProblemServer.start();

        this.maze = null;
        this.solution = null;
        this.characterRow = 0;
        this.characterCol = 0;
    }

    @Override
    public void assignObserver(Observer o) {
        this.addObserver(o);
    }

    @Override
    public Maze getMaze() {
        return maze;
    }

    @Override
    public Solution getSolution() {
        return solution;
    }

    @Override
    public int getCharacterRow() {
        return characterRow;
    }

    @Override
    public int getCharacterCol() {
        return characterCol;
    }

    @Override
    public void generateMaze(int rows, int cols) {
        new Thread(() -> {
            try {
                Client client = new Client(InetAddress.getLocalHost(), 5400, new IClientStrategy() {
                    @Override
                    public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                        try {
                            ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                            ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                            toServer.flush();

                            int[] mazeDimensions = new int[]{rows, cols};
                            toServer.writeObject(mazeDimensions);
                            toServer.flush();

                            byte[] compressedMaze = (byte[]) fromServer.readObject();
                            InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                            byte[] decompressedMaze = new byte[rows * cols + 12];
                            is.read(decompressedMaze);

                            maze = new Maze(decompressedMaze);

                            characterRow = maze.getStartPosition().getRowIndex();
                            characterCol = maze.getStartPosition().getColumnIndex();

                            solution = null;
                            solution = null;

                            setChanged();
                            notifyObservers("maze generated");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                client.communicateWithServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void solveMaze() {
        if (maze == null) return;

        new Thread(() -> {
            try {
                Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                    @Override
                    public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                        try {
                            ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                            ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                            toServer.flush();

                            toServer.writeObject(maze);
                            toServer.flush();

                            solution = (Solution) fromServer.readObject();

                            setChanged();
                            notifyObservers("solution solved");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                client.communicateWithServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void moveCharacter(int direction) {
        if (maze == null) return;

        int[][] matrix = maze.getMatrix();
        int newRow = characterRow;
        int newCol = characterCol;

        switch (direction) {
            case 8 -> newRow--;
            case 2 -> newRow++;
            case 4 -> newCol--;
            case 6 -> newCol++;
            case 7 -> { newRow--; newCol--; }
            case 9 -> { newRow--; newCol++; }
            case 1 -> { newRow++; newCol--; }
            case 3 -> { newRow++; newCol++; }
        }

        if (newRow >= 0 && newRow < matrix.length && newCol >= 0 && newCol < matrix[0].length) {
            if (matrix[newRow][newCol] == 0) {
                characterRow = newRow;
                characterCol = newCol;
                setChanged();
                notifyObservers("character moved");
            }
        }
    }

    @Override
    public void shutDownServers() {
        if (mazeGeneratingServer != null)
            mazeGeneratingServer.stop();

        if (solveSearchProblemServer != null)
            solveSearchProblemServer.stop();
    }

    @Override
    public void saveMaze(File file) {

    }

    @Override
    public void loadMaze(File file) {

    }
}
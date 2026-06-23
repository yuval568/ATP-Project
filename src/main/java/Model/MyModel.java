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
import IO.MyCompressorOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;

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
                            System.out.println("Client received solution. Steps: " + solution.getSolutionPath().size() +
                                    " | Start: " + solution.getSolutionPath().get(0) +
                                    " | End: " + solution.getSolutionPath().get(solution.getSolutionPath().size() -1 ));

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

        boolean diagonal = (direction == 7 || direction == 9 || direction == 1 || direction == 3);

        if (newRow >= 0 && newRow < matrix.length && newCol >= 0 && newCol < matrix[0].length) {
            if (matrix[newRow][newCol] == 0) {
                boolean canMove = !diagonal ||
                        (matrix[newRow][characterCol] == 0 && matrix[characterRow][newCol] == 0);
                if (canMove) {
                    characterRow = newRow;
                    characterCol = newCol;
                    setChanged();
                    notifyObservers("character moved");
                }
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
        if (maze == null) return;
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStream out = new MyCompressorOutputStream(fos)) {
            out.write(maze.toByteArray());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void loadMaze(File file) {
        try {
            byte[] compressed = new FileInputStream(file).readAllBytes();

            // Read header first without decompressing
            byte[] header = new byte[12];
            System.arraycopy(compressed, 0, header, 0, 12);
            int rows = ((header[1] & 0xFF) << 8) | (header[2] & 0xFF);
            int cols = ((header[3] & 0xFF) << 8) | (header[4] & 0xFF);

            System.out.println("rows=" + rows + " cols=" + cols);
            System.out.println("compressed length=" + compressed.length);

            byte[] decompressed = new byte[rows * cols + 12];
            InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressed));
            is.read(decompressed);

            maze = new Maze(decompressed);
            characterRow = maze.getStartPosition().getRowIndex();
            characterCol = maze.getStartPosition().getColumnIndex();
            solution = null;

            setChanged();
            notifyObservers("maze generated");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
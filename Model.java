package com.codegym.task.task35.task3513;

import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    private Stack<Tile[][]> previousStates;
    private Stack<Integer> previousScores;
    int score;
    int maxTile;
    boolean isSaveNeeded = true;

    public Model() {
        this.resetGameTiles();
        this.previousStates = new Stack<>();
        this.previousScores = new Stack<>();
    }

    void resetGameTiles() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
        score = 0;
        maxTile = 0;
    }

    public void rollback() {
        if (!previousStates.empty())
            gameTiles = previousStates.pop();

        if (!previousScores.empty())
            score = previousScores.pop();

    }

    public void left() {
        if (isSaveNeeded)
            saveState(gameTiles);
        boolean changed = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            changed |= consolidateTiles(gameTiles[i]);
            changed |= mergeTiles(gameTiles[i]);
        }

        if (changed)
            addTile();
        isSaveNeeded = true;
    }

    public void right() {
        saveState(gameTiles);
        boolean changed = false;
        for (int i = 0; i < 2; i++)
            gameTiles = rotate90degrees(gameTiles);
        for (Tile[] tiles : gameTiles) {
            changed |= consolidateTiles(tiles);
            changed |= mergeTiles(tiles);
        }
        for (int i = 0; i < 2; i++)
            gameTiles = rotate90degrees(gameTiles);

        if (changed)
            addTile();
    }

    public void up() {
        saveState(gameTiles);
        boolean changed = false;
        for (int i = 0; i < 3; i++)
            gameTiles = rotate90degrees(gameTiles); // rotating so can use mergeTiles


        for (Tile[] row : gameTiles) {
            changed |= consolidateTiles(row);
            changed |= mergeTiles(row);
        }

        gameTiles = rotate90degrees(gameTiles);

        if (changed)
            addTile();
    }

    public void down() {
        saveState(gameTiles);
        boolean changed = false;
        gameTiles = rotate90degrees(gameTiles); // rotating so can use mergeTiles
        for (Tile[] row : gameTiles) {
            changed |= consolidateTiles(row);
            changed |= mergeTiles(row);
        }

        for (int i = 0; i < 3; i++) // rotating so can get default matrix
            gameTiles = rotate90degrees(gameTiles);

        if (changed)
            addTile();
    }

    private Tile[][] rotate90degrees(Tile[][] mat) {
        Tile[][] ret = new Tile[mat.length][mat.length];
        for (int r = 0; r < mat.length; r++)  // r - row
            for (int c = 0; c < mat.length; c++)  // c - column
                ret[c][mat.length - 1 - r] = mat[r][c];

        return ret;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean wasUsed = false;
        for (int i = 0; i < tiles.length - 1; i++) {
            consolidateTiles(tiles);
            if (tiles[i].value == tiles[i + 1].value) {
                int merged = tiles[i].value * 2;
                tiles[i].value = merged;
                tiles[i + 1].value = 0;
                score += merged;

                if (maxTile < merged) {
                    maxTile = merged;
                    wasUsed = true;
                }

            }
        }
        return wasUsed;
    }

    private boolean consolidateTiles(Tile[] tiles) {
        boolean wasUsed = false;
        for (Tile ignored : tiles) {
            for (int j = 0; j < tiles.length - 1; j++) {
                if (tiles[j].value == 0 && tiles[j + 1].value != 0) {
                    tiles[j].value = tiles[j + 1].value;
                    tiles[j + 1].value = 0;
                    wasUsed = true;
                }
            }
        }
        return wasUsed;
    }

    private void addTile() {
        if (!getEmptyTiles().isEmpty()) {
            int min = 0, max = (getEmptyTiles().size() - 1);
            int randomTile = (int) (Math.random() * (max - min + 1) + min);
            int randomWeigth = Math.random() < 0.9 ? 2 : 4;

            getEmptyTiles().get(randomTile).value = randomWeigth;
        }
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> emptyTiles = new ArrayList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].isEmpty())
                    emptyTiles.add(gameTiles[i][j]);
            }
        }
        return emptyTiles;
    }

    public boolean canMove() {
        boolean canMove = false;
        Tile[][] tiles = gameTiles;

        for (int i = 0; i < FIELD_WIDTH; i++) {
            tiles = rotate90degrees(tiles);
            for (Tile[] row : tiles) {
                canMove |= consolidateTiles(row);
                canMove |= mergeTiles(row);
            }
        }

        return canMove;
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    private void saveState(Tile[][] state) {
        Tile[][] copiedTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++)
                copiedTiles[i][j] = new Tile(state[i][j].value);
        }

        previousStates.push(copiedTiles);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void randomMove() {
        int n = ((int) (Math.random() * 100)) % 4;
        if (n == 0) left();
        else if (n == 1) right();
        else if (n == 2) up();
        else if (n == 3) down();
    }

    private boolean hasBoardChanged() {
        int tileWeights = 0;

        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++)
                tileWeights += gameTiles[i][j].value;
        }

        int stackTileWeights = 0;
        Tile[][] helper = previousStates.peek();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++)
                stackTileWeights += helper[i][j].value;
        }

        return (tileWeights != stackTileWeights);
    }

    public MoveFitness getMoveFitness(Move move) {
        move.move();
        MoveFitness moveFitness;
        if (!hasBoardChanged())
            moveFitness = new MoveFitness(-1, 0, move);
        else
            moveFitness = new MoveFitness(getEmptyTiles().size(), score, move);

        rollback();

        return moveFitness;
    }

    public void autoMove() {
        PriorityQueue<MoveFitness> priorityQueue = new PriorityQueue<>(4, Collections.reverseOrder());

        priorityQueue.add(getMoveFitness(this::left));
        priorityQueue.add(getMoveFitness(this::right));
        priorityQueue.add(getMoveFitness(this::up));
        priorityQueue.add(getMoveFitness(this::down));

        Collections.reverse(Arrays.asList(priorityQueue.toArray()));
        priorityQueue.peek().getMove().move();

    }

}
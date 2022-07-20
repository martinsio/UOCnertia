package edu.uoc.nertia.model.levels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

import edu.uoc.nertia.model.exceptions.PositionException;
import edu.uoc.nertia.model.utils.Position;
import edu.uoc.nertia.model.cells.Cell;
import edu.uoc.nertia.model.cells.CellFactory;
import edu.uoc.nertia.model.cells.Element;
import edu.uoc.nertia.model.exceptions.LevelException;
import edu.uoc.nertia.model.stack.StackItem;
import edu.uoc.nertia.model.stack.UndoStack;

/**
 * Level class.
 *
 * @author David García Solórzano
 * @version 1.0
 */
public class Level {

    /**
     * Minimum size of the board in one direction, the board will be sizexsize
     */
    private static final int MIN_SIZE = 3;

    /**
     * Number representing unlimited number of lives for a player.
     */
    private static final int UNLIMITED_LIVES = -1;

    /**
     * Number of rows and columns in the game board. A board is a square of size x size.
     */
    private final int size;

    /**
     * Difficulty of the level
     */
    private LevelDifficulty difficulty;

    /**
     * 2D array representing each cell in the game board.
     */
    private Cell[][] board;

    /**
     * The number of moves performed by the player (excluding invalid moves).
     */
    private int numMoves = 0;

    /**
     * The number of lives the player has.
     */
    private int numLives;

    /**
     * The number of gems the player has got.
     */
    private int numGemsGot = 0;

    /**
     * The number of gems initially on the game board when a {@link Level} instance was created.
     */
    private final int numGemsInit;

    /**
     * Data structure that allows us to undo moves and manage its information.
     */
    private final UndoStack undoStack;

    /**
     * Constructor
     *
     * @param fileName Name of the file that contains level's data.
     * @throws LevelException When there is any error while parsing the file.
     */
    public Level(String fileName) throws LevelException {
        size = parse(fileName);

        numGemsInit = (int)
                Arrays.stream(getBoard()).flatMap(Arrays::stream)
                        .filter(cell -> cell.getElement() == Element.GEM)
                        .count();

        //Uncomment when you create UndoStack class.
        undoStack = new UndoStack();
    }

    /**
     * Parses/Reads level's data from the given file.<br/>
     * It also checks which the board's requirements are met.
     *
     * @param fileName Name of the file that contains level's data.
     * @return The size of the board in one direction (i.e. row or column). The board is {@code size x size}.
     * @throws LevelException When there is any error while parsing the file
     *                        or some board's requirement is not satisfied.     *
     */
    private int parse(String fileName) throws LevelException {
        String line;
        int size = 0;

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = Objects.requireNonNull(classLoader.getResourceAsStream(fileName));

        try (InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            line = getFirstNonEmptyLine(reader);

            if (line != null) {
                setNumLives(Integer.parseInt(line));
            }

            line = getFirstNonEmptyLine(reader);

            if (line != null) {
                size = Integer.parseInt(line);
                if (size < MIN_SIZE) {
                    throw new LevelException(LevelException.SIZE_ERROR);
                }
            }

            line = getFirstNonEmptyLine(reader);

            if (line != null) {
                setDifficulty(LevelDifficulty.valueOf(line));
            }

            board = new Cell[size][size];

            for (int row = 0; row < size; row++) {
                char[] rowChar = Objects.requireNonNull(getFirstNonEmptyLine(reader)).toCharArray();
                for (int column = 0; column < size; column++) {
                    board[row][column] = CellFactory.getCellInstance(row, column, rowChar[column]);
                }
            }

            //Checks if there are more than one finish cell
            if (Stream.of(board).flatMap(Arrays::stream).filter(x -> x.getElement() == Element.PLAYER).count() != 1) {
                throw new LevelException(LevelException.PLAYER_LEVEL_FILE_ERROR);
            }

            //Checks if there are one gem at least.
            if (Stream.of(board).flatMap(Arrays::stream).noneMatch(x -> x.getElement() == Element.GEM)) {
                throw new LevelException(LevelException.MIN_GEMS_ERROR);
            }

        } catch (IllegalArgumentException | IOException | PositionException e) {
            throw new LevelException(LevelException.PARSING_LEVEL_FILE_ERROR);
        }

        return size;
    }

    /**
     * This is a helper method for {@link #parse(String fileName)} which returns
     * the first non-empty and non-comment line from the reader.
     *
     * @param br BufferedReader object to read from.
     * @return First line that is a parsable line, or {@code null} there are no lines to read.
     * @throws IOException if the reader fails to read a line.
     */
    private String getFirstNonEmptyLine(final BufferedReader br) throws IOException {
        do {

            String s = br.readLine();

            if (s == null) {
                return null;
            }
            if (s.isBlank() || s.startsWith("/")) {
                continue;
            }

            return s;
        } while (true);
    }

    //Student's methods-------------------------------------------------------------------------------------------------
    public int getSize() {
        return size;
    }

    private void setDifficulty(LevelDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public LevelDifficulty getDifficulty() {
        return difficulty;
    }

    private boolean hasUnlimitedLives() {
        return UNLIMITED_LIVES == numLives;
    }

    public int getNumLives() {
        if (hasUnlimitedLives())
            return 2147483647; //Max value for an integer

        return numLives;
    }

    private void setNumLives(int numLives) {
        if (numLives <= 0)
            this.numLives = UNLIMITED_LIVES;
        else
            this.numLives = numLives;
    }

    public void increaseNumLives(int numLives) throws LevelException {
        if (numLives <= 0)
            throw new LevelException(LevelException.INCREASE_NUM_LIVES_ERROR);

        if (!(hasUnlimitedLives())) //Player hasn't unlimited lives
            this.numLives += numLives;
    }

    public void decreaseNumLives() {
        if (!(hasUnlimitedLives()) && (getNumLives() > 0)) //Player hasn't unlimited lives and has at least 1 live
            this.numLives--;
    }

    public void increaseNumGemsGot(int numGemsGot) throws LevelException {
        if (numGemsGot < 0)
            throw new LevelException(LevelException.INCREASE_NUM_GEMS_GOT_ERROR);

        this.numGemsGot += numGemsGot;
    }

    public void decreaseNumGemsGot() {
        if (getNumGemsGot() > 0)
            numGemsGot--;
    }

    public int getNumGemsGot() {
        return numGemsGot;
    }

    public int getNumMoves() {
        return numMoves;
    }

    public void increaseNumMoves() {
        numMoves++;
    }

    public int getNumGemsInit() {
        return numGemsInit;
    }

    public boolean hasWon() {
        return getNumGemsGot() == getNumGemsInit();
    }

    public boolean hasLost() {
        return getNumLives() == 0;
    }

    private Cell[][] getBoard() {
        return board;
    }

    public Cell getCell(Position position) throws LevelException, PositionException {
        if (position.getRow() < 0)
            throw new PositionException(PositionException.POSITION_ROW_ERROR);

        if (position.getColumn() < 0)
            throw new PositionException(PositionException.POSITION_COLUMN_ERROR);

        if (!(position.getColumn() < getSize() && position.getRow() < getSize()))  //Checks if position is valid
            throw new LevelException(LevelException.INCORRECT_CELL_POSITION);

        return getBoard()[position.getRow()][position.getColumn()];
    }

    public Cell getCell(int row, int column) throws LevelException, PositionException {
        if (row < 0)
            throw new PositionException(PositionException.POSITION_ROW_ERROR);

        if (column < 0)
            throw new PositionException(PositionException.POSITION_COLUMN_ERROR);

        if (!(column < getSize() && row < getSize()))  //Checks if position is valid
            throw new LevelException(LevelException.INCORRECT_CELL_POSITION);

        return getBoard()[row][column];
    }

    public void setCell(Position position, Element element) throws LevelException {
        if (!(position.getColumn() < getSize() && position.getRow() < getSize()))  //Checks if position is valid
            throw new LevelException(LevelException.INCORRECT_CELL_POSITION);

        if (element != null) {   //Element isn't null
            board[position.getRow()][position.getColumn()].setElement(element);
        }
    }

    public Position getPlayerPosition() {
        for (Cell[] cells : getBoard()) {
            for (Cell cell : cells) {
                if (cell.getElement() == Element.PLAYER || cell.getElement() == Element.PLAYER_STOP)   //This cell contains the player
                    return cell.getPosition();
            }
        }
        return null;    //Impossible but necessary
    }

    public int getScore() {
        return (size * size) + (10 * numGemsGot) - numMoves - (2 * undoStack.getNumPops());
    }

    public void push(StackItem stackItem) {
        undoStack.push(stackItem);
    }

    public boolean undo() throws LevelException {
        StackItem aux = undoStack.pop();

        if (aux == null)
            return false;

        //If stack item isn't null
        setCell(aux.originPosition(), aux.originElement()); //Undo player position

        for (Position position : aux.collectedLives()) { //Undo the lives collected
            decreaseNumLives();
            setCell(position, Element.EXTRA_LIFE);
        }

        for (Position position : aux.collectedGems()) {   //Undo gems collected
            decreaseNumGemsGot();
            setCell(position, Element.GEM);
        }

        return true;
    }

    @Override
    public String toString() {

        String result = null; //We need an initial value

        for (Cell[] cells : getBoard()) {
            for (Cell cell : cells) {
                if (result == null)
                    result = cell.toString();
                else
                    result = result.concat(cell.toString());
            }

            if (result!=null)
                result = result.concat(System.lineSeparator());
        }
        return result;
    }

}

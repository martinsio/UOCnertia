package edu.uoc.nertia.controller;

import edu.uoc.nertia.model.cells.Cell;
import edu.uoc.nertia.model.cells.Element;
import edu.uoc.nertia.model.exceptions.LevelException;
import edu.uoc.nertia.model.exceptions.PositionException;
import edu.uoc.nertia.model.leaderboard.LeaderBoard;
import edu.uoc.nertia.model.levels.Level;
import edu.uoc.nertia.model.stack.StackItem;
import edu.uoc.nertia.model.levels.LevelDifficulty;
import edu.uoc.nertia.model.utils.Direction;
import edu.uoc.nertia.model.utils.MoveResult;
import edu.uoc.nertia.model.utils.Position;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;

/**
 * Controller class of the game. It is the middleware (or bridge) between the model and view classes.
 * <br/>
 * This class is called from the view classes in order to access/modify the model data.
 *
 * @author David García-Solórzano
 * @version 1.0
 */
public class Game {

    /**
     * Name of the folder in which level files are
     */
    private String fileFolder;

    /**
     * Number of the current level.
     */
    private int currentLevel = 0;

    /**
     * Maximum quantity of levels that the game has.
     */
    private final int maxLevels;

    /**
     * Total score of the game, i.e. the sum of the levels' scores.
     */
    private int score;

    /**
     * Level object that contains the information of the current level.
     */
    private Level level;

    /**
     * LeaderBoard object that manages the leaderboard of the game.
     */
    private final LeaderBoard leaderBoard;

    /**
     * Save the number of level pops done by the player
     */
    private int numUndos;

    /**
     * Constructor
     *
     * @param fileFolder Folder name where the configuration/level files are.
     * @throws IOException When there is a problem while retrieving number of levels
     */
    public Game(String fileFolder) throws IOException {
        int num;

        setFileFolder(fileFolder);

        //Get the number of files that are in the fileFolder, i.e. the number of levels.
        URL url = getClass().getClassLoader().getResource(getFileFolder());

        URLConnection urlConnection = Objects.requireNonNull(url).openConnection();

        if (urlConnection instanceof JarURLConnection) {
            //run in jar
            String path = null;
            try {
                path = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            } catch (URISyntaxException e) {
                System.out.println("ERROR: Game Constructor");
                e.printStackTrace();
                System.exit(-1);
            }

            URI uri = URI.create("jar:file:" + path);

            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                num = (int) Files.walk(fs.getPath(getFileFolder()))
                        .filter(Files::isRegularFile).count();
            }
        } else {
            //run in ide
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream inputStream;
            inputStream = Objects.requireNonNull(classLoader.getResourceAsStream(getFileFolder()));

            try (InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(streamReader)) {
                num = (int) reader.lines().count();
            }
        }

        //We load the leaderboard
        leaderBoard = new LeaderBoard(5);

        setScore(0);

        maxLevels = num;
    }

    /**
     * Setter of the attribute {@code fileFolder}.
     *
     * @param fileFolder Folder name where the configuration/level files are.
     */
    private void setFileFolder(String fileFolder) {
        this.fileFolder = fileFolder;
    }

    /**
     * Getter of the attribute {@code fileFolder}.
     *
     * @return Value of the attribute {@code fileFolder}.
     */
    private String getFileFolder() {
        return fileFolder;
    }


    /**
     * Returns the size of the board. The board is NxN.
     *
     * @return Value of the board's size.
     */
    public int getBoardSize() {
        return level.getSize();
    }

    public int getScore() {
        return score;
    }

    private void setScore(int score) {
        this.score = score;
    }

    /**
     * Returns the {@link Cell} object which is in the position {@code (row,column)}.
     *
     * @param row    Row in which the cell we want to retrieve is
     * @param column Column in which the cell we want to retrieve is
     * @return The cell that is in the position {@code (row,column)}.
     * @throws LevelException When either the row or the column is wrong.
     */
    public Cell getCell(int row, int column) throws LevelException {
        try {
            if (row < 0 || column < 0)
                throw new LevelException(LevelException.INCORRECT_CELL_POSITION);

            return level.getCell(row, column);

        } catch (PositionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the difficulty of the current level.
     *
     * @return The difficulty of the current level.
     */
    public LevelDifficulty getDifficulty() {
        return level.getDifficulty();
    }

    /**
     * Returns the number of moves that have been done in the current level so far.
     *
     * @return Number of moves that the player has done so far. If level is null, then returns 0.
     */
    public int getNumMoves() {
        return level.getNumMoves();
    }

    /**
     * Returns the number of lives that the player has.
     *
     * @return Number of lives.
     */
    public int getNumLives() {
        return level.getNumLives();
    }

    /**
     * Indicates if the game is finished ({@code true}) or not ({@code false}).
     * <p>The game is finished when the attribute {@code currentLevel} is equal to attribute {@code maxLevels}.
     * </p>
     *
     * @return True if there are no more levels and therefore the game is finished. Otherwise, false.
     */
    public boolean isFinished() {
        return getCurrentLevel() == maxLevels;
    }

    /**
     * Getter of the attribute {@code currentLevel}.
     *
     * @return Value of the attribute {@code currentLevel} that indicates which level the player is playing.
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Checks if there is a new level to play and loads it.<br/>
     * If the game is finished, it returns {@code false}. Otherwise, it returns {@code true}.
     * The game score must be updated when a level is finished.
     * Thus, when the player is playing the first level, game's score is zero.
     *
     * @return True if there is a next level, and it has been loaded correctly. Otherwise, it returns false.
     * @throws LevelException When there is a level exception/problem loading the new level.
     */
    public boolean nextLevel() throws LevelException {

        //When is loading the first level
        if (level == null) {
            currentLevel = 1;
            loadLevel();
            return true;
        }
        //Set score considering actual score, score of the new level and number of undos done by the game.
        setScore(level.getScore() + getScore() + (2 * getNumUndos()));

        //Restart the number of undos done by the game automatically
        setNumUndos(0);

        //When there are more levels, and we aren't loading the first one
        if (!isFinished()) {
            currentLevel++;
            loadLevel();
            return true;
        }
        return false;
    }

    /**
     * Loads a new level by using the value of the attribute {@code currentLevel}.
     * <p>
     * The pattern of the filename is: fileFolder+"level" + numberLevel + ".txt".
     * </p>
     *
     * @throws LevelException When there is a level exception/problem.
     */
    private void loadLevel() throws LevelException {
        level = new Level(fileFolder.concat("level").concat(Integer.toString(currentLevel)).concat(".txt"));
    }

    /**
     * Checks if the level is completed, i.e. the player has collected all the gems of the board.
     *
     * @return {@code true} if this level is beaten, otherwise {@code false}.
     */
    public boolean isLevelCompleted() {
        return level.hasWon();
    }

    /**
     * Checks if the player has lost, i.e. the number of lives is zero.
     *
     * @return {@code true} if this the player has lost, otherwise {@code false}.
     */
    public boolean hasLost() {
        return level.hasLost();
    }

    /**
     * Undo one move from the level's stack.
     *
     * @return {@code true} if one move has been undone, otherwise {@code false} (e.g. the stack is empty).
     * @throws LevelException When either the row or the column is wrong.
     */
    public boolean undo() throws LevelException {
        return level.undo();
    }

    /**
     * Reloads the current level, i.e. load the level again.
     *
     * @throws LevelException When there is a level exception/problem.
     */
    public void reload() throws LevelException {
        loadLevel();    //Loads the level again
    }

    /**
     * Moves the player in the given direction. If the move ends in:
     * <ul>
     * <li>a mine, then it returns {@link MoveResult#DIE}</li>
     * <li>another kind of cell, then it returns {@link MoveResult#OK}</li>--
     * </ul>
     * If the first cell is out of bounds, then it returns {@link MoveResult#KO} (i.e. INVALID move)--
     *
     * @param direction Direction to move the player in.
     * @return MoveResult object the move is done.
     * @throws LevelException If there are any problems with increaseNumGemsGot.
     */
    public MoveResult movePlayer(Direction direction) throws LevelException {
        try {
            //Destination position of the movement
            Position movDest = level.getPlayerPosition().offsetBy(direction.getRowOffset(), direction.getColumnOffset(), level.getSize());

            //The movement destination isn't inside the board OR is a wall
            if (movDest == null || level.getCell(movDest).getElement() == Element.WALL)
                return MoveResult.KO;

            //Other cases
            return movePlayerAux(direction);

        } catch (PositionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private MoveResult movePlayerAux(Direction direction) throws LevelException, PositionException {

        //Manage push and undos
        String status = "";

        //Stack to save into undoStack
        Element originElement = level.getCell(level.getPlayerPosition()).getElement();
        StackItem stack = new StackItem(level.getPlayerPosition(), originElement, new ArrayList<>(), new ArrayList<>());

        while (status.equals("")) {
            //Initial position of the player
            Position initialPlayerPosition = level.getPlayerPosition();

            //Movement that user wants to do
            Position movDestiny = initialPlayerPosition.offsetBy(direction.getRowOffset(), direction.getColumnOffset(), level.getSize());
            Element cellElement;


            //The movement objective isn't inside the board OR Destiny position is a wall
            if (movDestiny == null || level.getCell(movDestiny).getElement() == Element.WALL)
                status = "finish";
            else {
                cellElement = Element.PLAYER;   //Default value

                switch (level.getCell(movDestiny).getElement()) {
                    case EXTRA_LIFE -> { //Increase gems got and adds his position into collectedLives list.
                        level.increaseNumLives(1);
                        stack.collectedLives().add(level.getCell(movDestiny).getPosition());
                    }
                    case GEM -> { //Increase gems got and adds his position into collectedGems list.
                        level.increaseNumGemsGot(1);
                        stack.collectedGems().add(level.getCell(movDestiny).getPosition());
                    }
                    case STOP -> {
                        cellElement = Element.PLAYER_STOP;
                        status = "finish";
                    }
                    case MINE -> {
                        cellElement = Element.MINE;
                        level.decreaseNumLives();   //Decrease number of lives available
                        status = "die";
                    }
                    default -> {
                    }
                }

                //Set player element into destination position
                level.setCell(movDestiny, cellElement);

                //Change the element of the origin cell
                if (level.getCell(initialPlayerPosition).getElement() == Element.PLAYER_STOP)
                    level.setCell(initialPlayerPosition, Element.STOP); //Puts again the stop in his cell
                else
                    level.setCell(initialPlayerPosition, Element.EMPTY); //Puts a space in his cell
            }
        }

        level.increaseNumMoves();

        //Push movement info to stack
        level.push(stack);

        //If the result is die, game makes undo
        if (Objects.equals(status, "die")) {
            level.undo();
            setNumUndos(getNumUndos() + 1);   //Increment the number of undos done automatically
            return MoveResult.DIE;
        }
        return MoveResult.OK;

    }


    /**
     * Checks if the score gotten by the player deserves to be stored in the leaderboard.
     *
     * @return {@code true} if the score can be stored in the leaderboard. Otherwise, {@code false}.
     */
    public boolean isInLeaderBoard() {
        return leaderBoard.isInTheTop(getScore());
    }

    /**
     * Add the score in the leaderboard.
     *
     * @param name Player's name.
     */
    public void addToLeaderBoard(String name) {
        leaderBoard.add(name, getScore());
    }

    /**
     * Prints the leaderboard.
     */
    public void displayLeaderBoard() {
        System.out.print(leaderBoard.toString());
    }

    /**
     * Getter of numUndos
     *
     * @return value of the attribute numUndos
     */
    private int getNumUndos() {
        return numUndos;
    }

    /**
     * Setter of numUndos
     *
     * @param numUndos is the value to be set into numUndos attribute
     */
    private void setNumUndos(int numUndos) {
        this.numUndos = numUndos;
    }

    /**
     * Returns the status of the game at that moment.
     *
     * @return Textual version of the game. This includes the board and level's status.
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < level.getSize(); i++) {
            for (int j = 0; j < level.getSize(); j++) {
                try {
                    str.append(level.getCell(i, j).toString());
                } catch (LevelException | PositionException e) {
                    e.printStackTrace();
                }
            }
            str.append(System.lineSeparator());
        }

        str.append("#Lives: ")
                .append(level.getNumLives())
                .append(" | #Moves: ")
                .append(level.getNumMoves())
                .append(" | #Gems: ")
                .append(level.getNumGemsGot())
                .append(" | Level Score: ")
                .append(level.getScore())
                .append(" pts")
                .append(" | Game Score: ")
                .append(getScore())
                .append(" pts")
                .append(System.lineSeparator())
                .append("Enter Your Move (UP/DOWN/LEFT/RIGHT/UNDO/QUIT): ");

        return str.toString();
    }
}

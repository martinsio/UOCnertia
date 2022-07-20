package edu.uoc.nertia.model.cells;

public enum Element {
    //Options-----------------------------------------------------------------------------------------------------------
    EMPTY('-', "empty.png"),
    EXTRA_LIFE('L', "life.png"),
    GEM('*', "gem.png"),
    MINE('X', "mine.png"),
    PLAYER('@', "player.png"),
    STOP('S', "stop.png"),
    PLAYER_STOP('$', "player_stop.png"),
    WALL('#', "wall.png");

    //Attributes--------------------------------------------------------------------------------------------------------
    private char symbol;

    private String imageSrc;

    //Constructor-------------------------------------------------------------------------------------------------------
    Element(char symbol, String imageSrc) {
        setSymbol(symbol);
        setImageSrc(imageSrc);
    }

    //Methods-----------------------------------------------------------------------------------------------------------
    private void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    private void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }

    //Find all the options of the Element enum and check if it's symbol is equal to the parameter.
    public static Element symbol2Element(char symbol) {
        for (Element option : Element.values()) {
            if (option.getSymbol() == symbol)
                return option;
        }
        return null;
    }

    public char getSymbol() {
        return symbol;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    @Override
    //Returns the value of the attribute symbol in String format
    public String toString() {
        return String.valueOf(getSymbol());
    }
}

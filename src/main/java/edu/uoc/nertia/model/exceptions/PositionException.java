package edu.uoc.nertia.model.exceptions;

public class PositionException extends Exception{
    //Attributes---------------------------------------------------------------------------------------------------------
    public static final String POSITION_ROW_ERROR = "[ERROR] Position's row cannot be a negative value.";

    public static final String POSITION_COLUMN_ERROR = "[ERROR] Position's column cannot be a negative value.";

    //Constructor-------------------------------------------------------------------------------------------------------
    public PositionException(String msg){
        super(msg);
    }

}

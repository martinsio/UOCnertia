package edu.uoc.nertia.model.utils;

public enum Direction {
    //Options-----------------------------------------------------------------------------------------------------------
    LEFT(0, -1),
    UP(-1, 0),
    RIGHT(0, 1),
    DOWN(1, 0);

    //Attributes--------------------------------------------------------------------------------------------------------
    private final int rowOffset;

    private final int columnOffset;


    //Constructor-------------------------------------------------------------------------------------------------------
        Direction(int rowOffset, int columnOffset){
            this.rowOffset=rowOffset;
            this.columnOffset=columnOffset;
        }



    //Methods-----------------------------------------------------------------------------------------------------------
    public int getColumnOffset(){
        return columnOffset;
    }

    public int getRowOffset(){
        return rowOffset;
    }




}

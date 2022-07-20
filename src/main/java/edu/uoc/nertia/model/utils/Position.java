package edu.uoc.nertia.model.utils;

import edu.uoc.nertia.model.exceptions.PositionException;

import java.util.Objects;

public class Position {

    //Attributes--------------------------------------------------------------------------------------------------------
    private int row;

    private int column;


    //Constructor-------------------------------------------------------------------------------------------------------
    public Position(int row, int column) throws PositionException {
        setRow(row);
        setColumn(column);
    }

    //Methods-----------------------------------------------------------------------------------------------------------
    private void setRow(int row) {
        this.row = row;
    }

    private void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public Position offsetBy(int row, int column) {

        try{
            Position result = new Position(this.row + row, this.column + column);   //New Position element with the new values

            if (result.getRow() < 0 || result.getColumn() < 0)  //Row or column are a negative value.
                return null;

            return result;
        } catch (PositionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Position offsetBy(int row, int column, int size){
        try{
            //New Position element with the new values
            Position result = new Position(this.row + row, this.column + column);

            if (size <= 0)
                return null;

            //Row or column aren't between 0 and size-1.
            if ((result.getRow() < 0 || result.getRow() >= size) || (result.getColumn() < 0 || result.getColumn() >= size))
                return null;

            return result;
        } catch (PositionException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Position))   //Object is not a Position instance
            return false;

        //Returns true if both positions are the same
        return ((Position) o).getRow() == this.getRow() && ((Position) o).getColumn() == this.getColumn();

    }

    @Override
    public int hashCode() {
        return Objects.hash(getRow(), getColumn());
    }
}

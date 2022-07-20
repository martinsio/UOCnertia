package edu.uoc.nertia.model.cells;

import edu.uoc.nertia.model.utils.Position;

public class Cell {
    //Attributes--------------------------------------------------------------------------------------------------------
    private Position position;

    private Element element;

    //Constructor-------------------------------------------------------------------------------------------------------
    public Cell(Position position, Element element){
        setPosition(position);
        setElement(element);
    }

    //Methods-----------------------------------------------------------------------------------------------------------
    public Position getPosition(){
        return position;
    }

    public Element getElement(){
        return element;
    }

    private void setPosition(Position position){
        this.position=position;
    }

    public void setElement(Element element){
        this.element=element;
    }

    @Override
    public String toString(){
        //Considering the name can't be null!!
        return element.toString();
    }
}

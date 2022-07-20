package edu.uoc.nertia.model.stack;

import java.util.Stack;

public class UndoStack extends Stack<StackItem> {
    //Attributes--------------------------------------------------------------------------------------------------------
    private int numPops;

    //Constructor-------------------------------------------------------------------------------------------------------
    public UndoStack(){
        numPops=0;
    }

    //Methods-----------------------------------------------------------------------------------------------------------
    @Override
    public StackItem pop(){
        incrementNumPops();

        if (this.isEmpty()) return null; //Checks if the stack is empty

        StackItem aux = this.peek();    //Saves the last element of the stack.

        super.pop();    //Drops last element from the stack.

        return aux;
    }

    public int getNumPops() {
        return numPops;
    }

    private void incrementNumPops(){
        numPops++;
    }


}

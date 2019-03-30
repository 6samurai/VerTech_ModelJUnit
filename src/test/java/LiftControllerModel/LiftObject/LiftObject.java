package LiftControllerModel.LiftObject;

public class LiftObject {

    int currentFloor = 0;
    boolean doorOpen = false;
    boolean moving = false;
    boolean movingUp = true;
    int destinationFloor = -1;
    double moveTimer = 0;
    double doorOpenTimer = 0;
    LiftState liftState = LiftState.CLOSED;

    public LiftObject(){

    }

    public void setCurrentFloor(int currentFloor){
        this.currentFloor = currentFloor;
    }

    public void setDoorOpen(boolean doorOpen){
        this.doorOpen = doorOpen;
    }

    public void setMoving(boolean moving){
        this.moving = moving;
    }

    public void setMovingUp(boolean movingUp){
        this.movingUp = movingUp;
    }

    public void setDestinationFloor(int destinationFloor){
        this.destinationFloor = destinationFloor;
    }

    public void setMoveTimer(double moveTimer){
        this.moveTimer = moveTimer;
    }

    public void setDoorOpenTimer(double doorOpenTimer){
        this.doorOpenTimer = doorOpenTimer;
    }


    public void setLiftState(LiftState liftState){
        this.liftState = liftState;
    }



    public int getCurrentFloor( ){
        return this.currentFloor ;
    }

    public boolean getDoorOpen(){
        return this.doorOpen;
    }

    public boolean getMoving( ){
        return this.moving;
    }

    public boolean getMovingUp( ){
        return this.movingUp;
    }

    public int getDestinationFloor( ){
        return this.destinationFloor ;
    }

    public double getMoveTimer( ){
        return this.moveTimer;
    }

    public double getDoorOpenTimer(){
        return this.doorOpenTimer;
    }

    public LiftState getLiftState(){
        return this.liftState ;
    }
}


package LiftControllerModel.LiftObject;

public class LiftObject {

    int currentFloor = 0;
    boolean doorOpen = false;
    boolean moving = false;
    boolean movingUp = true;
    int destinationFloor = -1;
    double timer = 0;
   // double doorOpenTimer = 0;
    LiftState liftState = LiftState.CLOSED;

    public LiftObject(){
         currentFloor = 0;
         doorOpen = false;
         moving = false;
         movingUp = true;
         destinationFloor = -1;
         timer = 0;
       //  doorOpenTimer = 0;
         liftState = LiftState.CLOSED;
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

    public void setTimer(double timer){
        this.timer = timer;
    }

/*    public void setDoorOpenTimer(double doorOpenTimer){
        this.doorOpenTimer = doorOpenTimer;
    }*/


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

    public double getTimer( ){
        return this.timer;
    }

  /*  public double getDoorOpenTimer(){
        return this.doorOpenTimer;
    }*/

    public LiftState getLiftState(){
        return this.liftState ;
    }
}


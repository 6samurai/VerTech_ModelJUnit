package LiftModel_DOES_NOT_WORK;

import com.liftmania.Lift;

import java.util.ArrayList;

public class LiftOperator {

 /*   private  boolean DoorOpen = false;
    private  boolean LiftMove = false;
    private  boolean LiftBetweenFloors = false;
    private boolean ButtonPress = false;


    boolean isDoorOpen() {return  DoorOpen;}
    boolean isLiftMovingUp() {return  LiftMove;}
    boolean isButtonPressed(){return ButtonPress;}


    void openLiftDoors(){
        if( !LiftBetweenFloors){
            if(!LiftMove && ButtonPress ){
                DoorOpen = true;
                ButtonPress = false;
            } else if(  LiftMove && !DoorOpen ) {
                LiftMove = false;
                DoorOpen = true;
                ButtonPress = false;
            }
        }
    }

    void closeLiftDoor(){
        if(!LiftMove && DoorOpen && !LiftBetweenFloors ){
            DoorOpen = false;

        }
    }

    void liftMove(){
        if(ButtonPress && !DoorOpen) {
            ButtonPress=false;
            LiftMove = true;
        }
    }

    void buttonPressed(){
        if( !LiftMove){
            ButtonPress = true;
            if(DoorOpen)
                DoorOpen = false;
        } else if(!ButtonPress && !DoorOpen && (LiftMove) ){
            ButtonPress = true;
        }
    }*/

     int numFloors = 0;

     public LiftOperator(int floorCount){
         numFloors = floorCount;
     }

    public void closeLiftDoor(Lift lift) {
        setMoving(lift,false);
        lift.closeDoors();
    }

    public void openLiftDoor(Lift lift) {
        setMoving(lift,false);
        lift.openDoors();
    }

    public void setFloor(Lift lift, int floor){
        lift.setFloor(floor);
    }

    public void setMoving(Lift lift, boolean move){
        lift.setMoving(move);
    }

     public void moveLift(Lift lift, int toFloor){

         setFloor(lift,toFloor);
         setMoving(lift,true);

     }

     public Lift buttonPress(Lift [] lifts, int floor){

         ArrayList<Lift> closestLifts = getClosestLifts(lifts,floor);

         if (closestLifts.size() == 0) {
             throw new RuntimeException("Could not find an available lift. input floor"+  floor+" " + lifts[0].getFloor() +" " + lifts[0].isOpen() +" " +  lifts[0].getIsMovingUp() + " " + lifts[0].isMoving());

         }

         //Pick random lift
         Lift lift = closestLifts.get((int)(Math.random() * (closestLifts.size())));



         return lift;
     }


    public ArrayList<Lift> getClosestLifts(Lift [] lifts, int floor){
        ArrayList<Lift> closestLifts = getClosestStationaryLifts(lifts, floor);
        if(closestLifts.size()>0)
            return closestLifts;
        else{
            return (getClosestMovingLifts(lifts, floor));
        }
    }

    public ArrayList<Lift> getClosestMovingLifts(Lift [] lifts, int floor){
        ArrayList<Lift> result = new ArrayList<Lift>();
        int maxIterations = lifts.length*numFloors;
        int distance = -1;
        while (result.size() == 0 && distance <maxIterations) {
            distance++;

            for (Lift lift : lifts) {
                if(lift.isMoving()){
                    if (lift.distanceFromFloor(floor) == distance && !lift.getIsMovingUp()) {
                        result.add(lift);
                    } else if (lift.distanceFromFloor(floor) == -distance && lift.getIsMovingUp()) {
                        result.add(lift);
                    }

                }
            }
        }

        return result;

    }

    public ArrayList<Lift> getClosestStationaryLifts(Lift [] lifts,int floor) {

        ArrayList<Lift> result = new ArrayList<Lift>();
        int maxIterations = lifts.length*numFloors;
        int distance = -1;
        while (result.size() == 0 && distance <maxIterations) {
            distance++;
            for (Lift lift : lifts) {
                if (Math.abs(lift.distanceFromFloor(floor)) == distance && !lift.isMoving()) {
                    result.add(lift);
                }
            }
        }

        return result;
    }



}

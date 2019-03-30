package LiftModel;

import com.liftmania.Lift;

import java.util.ArrayList;

public class MultipleLiftOperator {

    int numFloors = 0;

    public MultipleLiftOperator(int floorCount){
        numFloors = floorCount;
    }

    public void closeLiftDoor(Lift lift) {
        lift.setMoving(false);
        lift.closeDoors();
    }

    public void openLiftDoor(Lift lift) {
        lift.setMoving(false);
        lift.openDoors();
    }

    public void setFloor(Lift lift, int floor){
        lift.setFloor(floor);
    }

    public void setMoving(Lift lift, boolean move){
        lift.setMoving(move);
    }

    public void moveLift(Lift lift, int toFloor){

        boolean direction = true;
       lift.closeDoors();
        if(lift.getFloor()>toFloor){
            direction = false;
        }
        lift.setIsMovingUp(direction);

        setFloor(lift,toFloor);
        setMoving(lift,true);

    }
   //checks if inputted list has a lift.
    //for the scenario where multiple are present - one is randomly chosen
    public Lift getClosestLift(ArrayList<Lift> closestLifts){
        if (closestLifts.size() == 0) {
            throw new RuntimeException("Could not find an available lift.");
        }

        //Pick random lift
        Lift lift = closestLifts.get((int)(Math.random() * (closestLifts.size())));
        //   moveLift(lift, floor);

        return lift;
    }


    public ArrayList<Lift>getClosestLifts(Lift [] lifts,int floor){
        ArrayList<Lift> result = new ArrayList<Lift>();

        int distance = -1;
        while (result.size() == 0 && distance<numFloors) {
            distance++;

            for (Lift lift : lifts) {
                if(lift.isMoving()){
                    if ( (lift.distanceFromFloor(floor) == distance && !lift.getIsMovingUp() ) ||
                            (lift.distanceFromFloor(floor) == -distance && lift.getIsMovingUp())) {
                        result.add(lift);
                    }
                } else{
                    if (Math.abs(lift.distanceFromFloor(floor)) == distance ) {
                        result.add(lift);
                    }
                }
            }
        }

        return result;

    }

}

package LiftModel;

import com.liftmania.Lift;

import java.util.ArrayList;

public class MultipleLiftOperator {

    int numFloors = 0;

    public MultipleLiftOperator(int floorCount){
        numFloors = floorCount;
    }

    public void closeLiftDoor(Lift lift) {
        lift.closeDoors();
    }

    public void openLiftDoor(Lift lift) {
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
        if(lift.getFloor()>toFloor){
            direction = false;
        }

        lift.setMoveDirection(direction);

        setFloor(lift,toFloor);
        setMoving(lift,true);

        lift.setMoving(true);
        lift.setFloor(toFloor);

        //Open and close doorts
        lift.openDoors();
        try {
            Thread.sleep(2000);
        } catch (Exception e) {}
        lift.closeDoors();

        //Update lift state
        lift.setMoving(false);

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

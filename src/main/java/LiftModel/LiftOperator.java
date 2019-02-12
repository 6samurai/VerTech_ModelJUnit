package LiftModel;

public class LiftOperator {

    private  boolean Door_Open = false;
    private  boolean Lift_Move = false;

  //  private  boolean Lift_At_Destination = false;
    private  boolean Lift_BetweenFloors = false;

    boolean isDoorOpen() {return  Door_Open;}

    boolean isLiftMoving() {return  Lift_Move;}

 //   boolean isLiftAtDestination(){ return  Lift_At_Destination;}

    boolean isLiftBetweenFloors(){ return  Lift_BetweenFloors;}

  /*   void liftArrivedAtDestination(){
        if(!Door_Open && Lift_Stationary){
            Door_Open = true;
        } else if(Door_Close && Lift_Move){
            Lift_Move = false;
            Lift_Stationary = true;
        }
    }*/

    void openLiftDoors(){
        if(!Lift_Move){
           Door_Open = true;
        }

        if(  Lift_Move && !Door_Open ) {
            Door_Open = true;
            Lift_Move = false;
        }
    }

    void closeLiftDoor(){
        if(!Lift_Move&& Door_Open){
            Door_Open = false;

        }
    }

    void liftMoving(){
        if(!Door_Open  && !Lift_Move){

            Lift_Move = true;
        }
    }

}

package LiftModel;

public class LiftOperator {

    private  boolean DoorOpen = false;
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
    }

}

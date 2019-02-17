package LiftModel;

public class LiftOperator {

    private  boolean DoorOpen = false;
    private  boolean LiftMoveUp = false;
    private  boolean LiftMoveDown = false;
    private  boolean LiftBetweenFloors = false;
    private boolean ButtonPress = false;


    boolean isDoorOpen() {return  DoorOpen;}
    boolean isLiftMovingUp() {return  LiftMoveUp;}
    boolean isLiftMovingDown() {return  LiftMoveDown;}
    boolean isLiftBetweenFloors(){ return  LiftBetweenFloors;}
    boolean isButtonPressed(){return ButtonPress;}


    void openLiftDoors(){
        if( !LiftBetweenFloors){
            if(!LiftMoveUp &&!LiftMoveDown && ButtonPress ){
                DoorOpen = true;
                ButtonPress = false;
            } else if(  (LiftMoveUp || LiftMoveDown) && !DoorOpen && !ButtonPress) {
                LiftMoveUp = false;
                LiftMoveDown = false;
                DoorOpen = true;

            }
        }
    }

    void closeLiftDoor(){
        if(!LiftMoveUp &&!LiftMoveDown&& DoorOpen && !LiftBetweenFloors ){
            DoorOpen = false;
        }
    }

    void liftMoveUp(){
        if(ButtonPress  && !LiftMoveUp && !LiftMoveDown && !DoorOpen) {
            ButtonPress=false;
            LiftMoveUp = true;
        }
    }

    void liftMoveDown(){
        if(ButtonPress  && !LiftMoveUp && !LiftMoveDown && !DoorOpen) {
            ButtonPress=false;
            LiftMoveDown = true;
        }
    }

    void buttonPressed(){
        if(!ButtonPress && !LiftMoveDown && !LiftMoveUp){
            ButtonPress = true;
        } else if(ButtonPress && !DoorOpen && (LiftMoveUp || LiftMoveDown) ){
            ButtonPress = false;
        } else if(ButtonPress && !LiftMoveDown && !LiftMoveUp  && DoorOpen){
            DoorOpen = false;
        }
    }

}

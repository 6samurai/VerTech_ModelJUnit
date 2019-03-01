package LiftModel;

import LiftModel.enums.LiftOperatorStates;
import com.liftmania.Lift;
import com.liftmania.LiftController;
import junit.framework.Assert;
import nz.ac.waikato.modeljunit.*;
import nz.ac.waikato.modeljunit.coverage.ActionCoverage;
import nz.ac.waikato.modeljunit.coverage.StateCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionPairCoverage;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

public class LiftOperatorModelTest implements FsmModel {

    //Variables
    private LiftOperatorStates modelState = LiftOperatorStates.DOOR_CLOSED_STATIONARY_LIFT;
    int numFloors = 6;
    int numLifts = 1;

    Lift lift;
    Lift [] lifts;
    LiftController sut = new LiftController(numFloors, numLifts,false);
    LiftOperator liftOperator = new LiftOperator(numFloors);
    private  boolean DoorOpen = false;
    private  boolean LiftMove = false;
    private  boolean ButtonPress = false;
    private Random random = new Random();



    //SUT
  //  private LiftOperator sut = new LiftOperator();

    //Method implementations
    public LiftOperatorStates getState() {
        return modelState;
    }

    public void reset(final boolean reset) {
        modelState = LiftOperatorStates.DOOR_CLOSED_STATIONARY_LIFT;
        DoorOpen = false;
        LiftMove = false;
        ButtonPress = false;
        
        if (reset) {

            sut  = new LiftController(numFloors, numLifts,false);
            liftOperator = new LiftOperator(numFloors);
            lifts = sut.getLifts();

        }
      /*  try {
            Thread.sleep(3000*numFloors +250+ 50*numFloors);
        } catch (Exception e) {}*/
    }

    public boolean openDoorToCloseDoorGuard() {
        return getState().equals(LiftOperatorStates.DOOR_OPEN_STATIONARY_LIFT) && !lifts[0].isMoving();
    }

    public @Action
    void  openDoorToCloseDoor() {
     //   sut.closeLiftDoor(0);
     //   DoorOpen = sut.getLifts()[0].isOpen();
      //  LiftMove = sut.getLifts()[0].isMoving();
        modelState = LiftOperatorStates.DOOR_CLOSED_STATIONARY_LIFT;
        liftOperator.closeLiftDoor(lifts[0]);


        Assert.assertEquals("The model's Door Open state doesn't match the SUT's state.",false,lifts[0].isOpen() );
        Assert.assertEquals("The model's Moving state doesn't match the SUT's state.",false,lifts[0].isMoving() );
    }


    public boolean buttonPressToOpenDoorGuard() {
        return getState().equals(LiftOperatorStates.BUTTON_PRESSED) && !sut.getLifts()[0].isMoving();
    }

    public @Action
    void buttonPressToOpenDoor() {
      //  sut.openLiftDoor(0);
       // DoorOpen =  sut.getLifts()[0].isOpen();
        //LiftMove = sut.getLifts()[0].isMoving();
        modelState = LiftOperatorStates.DOOR_OPEN_STATIONARY_LIFT;
        liftOperator.openLiftDoor(lifts[0]);



        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", true,lifts[0].isOpen());
        Assert.assertEquals("The model's Moving state doesn't match the SUT's state.",false,lifts[0].isMoving() );

    }


    public boolean openDoorToButtonPressGuard() {
        return getState().equals(LiftOperatorStates.DOOR_OPEN_STATIONARY_LIFT) && !sut.getLifts()[0].isMoving();
    }

    public @Action
    void openDoorToButtonPress() {
        int randomLiftCall = random.nextInt(numFloors);
      //  sut.callLiftToFloor(randomLiftCall);
       // DoorOpen =  sut.getLifts()[0].isOpen();
       // LiftMove = sut.getLifts()[0].isMoving();




        modelState = LiftOperatorStates.BUTTON_PRESSED;
        Lift expectedList = liftOperator.buttonPress(lifts,1);
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", expectedList.getId(),lifts[0].getId());
       // Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", DoorOpen,lifts[0].isOpen());
        //Assert.assertEquals("The model's Moving state doesn't match the SUT's state.",LiftMove,lifts[0].isMoving() );

    }


    public boolean closeDoorToButtonPressGuard() {
        return getState().equals(LiftOperatorStates.DOOR_CLOSED_STATIONARY_LIFT) && !sut.getLifts()[0].isMoving();
    }

    public @Action
    void closeDoorToButtonPress() {
        int randomLiftCall = random.nextInt(numFloors);
        //  sut.callLiftToFloor(randomLiftCall);
        // DoorOpen =  sut.getLifts()[0].isOpen();
        // LiftMove = sut.getLifts()[0].isMoving();
        modelState = LiftOperatorStates.BUTTON_PRESSED;
        Lift expectedList = liftOperator.buttonPress(lifts,1);
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", expectedList.getId(),lifts[0].getId());
        // Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", DoorOpen,lifts[0].isOpen());
        //Assert.assertEquals("The model's Moving state doesn't match the SUT's state.",LiftMove,lifts[0].isMoving() );

    }

    public boolean ButtonPressToCloseDoorGuard() {
        return getState().equals(LiftOperatorStates.BUTTON_PRESSED) && !sut.getLifts()[0].isMoving();
    }

    public @Action
    void ButtonPressToCloseDoor() {

        modelState = LiftOperatorStates.DOOR_CLOSED_STATIONARY_LIFT;
        liftOperator.closeLiftDoor(lifts[0]);

        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", false,lifts[0].isOpen());

    }

    public boolean ButtonPressToLiftMoveGuard() {
        return getState().equals(LiftOperatorStates.BUTTON_PRESSED) && !sut.getLifts()[0].isOpen() ;
    }

    public @Action
    void ButtonPressToLiftMove() {
        int randomLiftCall = random.nextInt(numFloors);
        modelState = LiftOperatorStates.DOOR_CLOSED_LIFT_MOVE;
        liftOperator.moveLift(lifts[0], randomLiftCall);

        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", false,lifts[0].isOpen());
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", true,lifts[0].isMoving());

    }


    public boolean liftMoveToButtonPressGuard() {
        return getState().equals(LiftOperatorStates.DOOR_CLOSED_LIFT_MOVE)  && sut.getLifts()[0].isMoving() && !sut.getLifts()[0].isOpen()
                && (!( !lifts[0].getIsMovingUp()  && lifts[0].getFloor()==0 )||( lifts[0].getIsMovingUp()  && lifts[0].getFloor()==numFloors )) ;
    }

    public @Action
    void liftMoveToButtonPress() {
      //  int randomLiftCall = random.nextInt(numFloors);

        modelState = LiftOperatorStates.BUTTON_PRESSED;
        Lift expectedList = liftOperator.buttonPress(lifts,1);
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", expectedList.getId(),lifts[0].getId());

    }

    public boolean liftMoveToOpenDoorGuard() {
        return getState().equals(LiftOperatorStates.DOOR_CLOSED_LIFT_MOVE);
    }

    public @Action
    void liftMoveToOpenDoor() {


        modelState = LiftOperatorStates.DOOR_OPEN_STATIONARY_LIFT;
      liftOperator.openLiftDoor(lifts[0]);
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", true,lifts[0].isOpen());
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", false,lifts[0].isMoving());

    }



  /*  public boolean closeDoorFroOpenDoorGuard() {
        return getState().equals(LiftOperatorStates.DOOR_OPEN_STATIONARY_LIFT);
    }

    public @Action
    void closeDoorFroOpenDoor() {
        sut.closeLiftDoor();
        DoorOpen = false;

        modelState = LiftOperatorStates.DOOR_CLOSED_STATIONARY_LIFT;

        Assert.assertEquals("The model's Door Open state doesn't match the SUT's state.", DoorOpen, sut.isDoorOpen());

    }

    public boolean closeDoorFroOpenDoorWithButtonPressGuard() {
        return getState().equals(LiftOperatorStates.DOOR_OPEN_STATIONARY_LIFT);
    }

    public @Action
    void closeDoorFroOpenDoorWithButtonPress() {
        sut.buttonPressed();
        DoorOpen = false;
        ButtonPress = true;

        modelState = LiftOperatorStates.DOOR_CLOSED_STATIONARY_LIFT;

        Assert.assertEquals("The model's Door Open state doesn't match the SUT's state.", DoorOpen, sut.isDoorOpen());

    }

    public boolean buttonPressFromCloseDoorGuard() {
        return getState().equals(LiftOperatorStates.DOOR_CLOSED_STATIONARY_LIFT);
    }

    public @Action
    void buttonPressFromCloseDoor() {
        sut.buttonPressed();
        ButtonPress = true;

        modelState = LiftOperatorStates.BUTTON_PRESSED;

        Assert.assertEquals("The model's Button press state doesn't match the SUT's state.", ButtonPress, sut.isButtonPressed());
        //Assert.assertEquals("The model's offline Verified state doesn't match the SUT's state.", offVerif, sut.isOfflineVerified());
    }

    public boolean doorCloseFromButtonPressGuard() {
        return getState().equals(LiftOperatorStates.BUTTON_PRESSED);
    }

    public @Action
    void doorCloseFromButtonPress() {
        sut.closeLiftDoor();

        DoorOpen = false;
        modelState = LiftOperatorStates.DOOR_CLOSED_STATIONARY_LIFT;

        Assert.assertEquals("The model's Door Open state doesn't match the SUT's state.", DoorOpen, sut.isDoorOpen());

    }

    public boolean doorOpenFromButtonPressGuard() {
        return getState().equals(LiftOperatorStates.BUTTON_PRESSED);
    }

    public @Action
    void doorOpenFromButtonPress() {
        sut.openLiftDoors();
        ButtonPress = false;
        DoorOpen = true;
        modelState = LiftOperatorStates.DOOR_OPEN_STATIONARY_LIFT;

        Assert.assertEquals("The model's Door Open state doesn't match the SUT's state.", DoorOpen, sut.isDoorOpen());
        Assert.assertEquals("The model's Button Press state doesn't match the SUT's state.", ButtonPress, sut.isButtonPressed());
   }

    public boolean doorOpenFromMoveGuard() {
        return getState().equals(LiftOperatorStates.DOOR_CLOSED_LIFT_MOVE);
    }

    public @Action
    void doorOpenFromMove() {
        sut.openLiftDoors();
        DoorOpen = true;
        LiftMove = false;
        modelState = LiftOperatorStates.DOOR_OPEN_STATIONARY_LIFT;

        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", DoorOpen, sut.isDoorOpen());
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", LiftMove, sut.isLiftMovingUp());
    }



    public boolean moveFromButtonPressGuard() {
        return getState().equals(LiftOperatorStates.BUTTON_PRESSED);
    }

    public @Action
    void moveFromButtonPress() {
        sut.liftMove();
        LiftMove = true;
        ButtonPress = false;
        modelState = LiftOperatorStates.DOOR_CLOSED_LIFT_MOVE;

        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", LiftMove, sut.isLiftMovingUp());
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", ButtonPress, sut.isButtonPressed());

    }

    public boolean buttonPressFromMoveGuard() {
        return getState().equals(LiftOperatorStates.DOOR_CLOSED_LIFT_MOVE);
    }

    public @Action
    void buttonPressFromMove() {
        sut.buttonPressed();

        ButtonPress = true;
        modelState = LiftOperatorStates.BUTTON_PRESSED;
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", ButtonPress, sut.isButtonPressed());

    }

*/
    @Test
    public void LiftSystemModelRunner() throws FileNotFoundException {
        LiftOperatorModelTest myModel = new LiftOperatorModelTest();

         final Tester tester = new GreedyTester(myModel);
    //    final Tester tester = new LookaheadTester(myModel);
      //    ((LookaheadTester) tester).setDepth(3);


      //   final Tester tester  = new RandomTester(myModel);
        tester.setRandom(new Random());
        final GraphListener graphListener = tester.buildGraph();
        tester.addListener(new StopOnFailureListener());
        tester.addListener("verbose");
        tester.addCoverageMetric(new TransitionPairCoverage());
        tester.addCoverageMetric(new StateCoverage());
        tester.addCoverageMetric(new ActionCoverage());

        tester.generate(500);
        tester.printCoverage();
    }
}

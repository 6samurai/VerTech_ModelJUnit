package LiftModel;

import LiftModel.enums.LiftOperatorStates;
import junit.framework.Assert;
import nz.ac.waikato.modeljunit.*;
import nz.ac.waikato.modeljunit.coverage.ActionCoverage;
import nz.ac.waikato.modeljunit.coverage.StateCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionPairCoverage;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Random;

public class LiftOperatorModelTest implements FsmModel {

    //Variables
    private LiftOperatorStates modelState = LiftOperatorStates.DOOR_CLOSED_STATIONARY_LIFT;
    private  boolean DoorOpen = false;
    private  boolean LiftMove = false;
     private  boolean ButtonPress = false;


    //SUT
    private LiftOperator sut = new LiftOperator();

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
            sut = new LiftOperator();
        }
    }


    public boolean closeDoorFroOpenDoorGuard() {
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


    @Test
    public void LiftSystemModelRunner() throws FileNotFoundException {
        LiftOperatorModelTest myModel = new LiftOperatorModelTest();

     //    final Tester tester = new GreedyTester(myModel);
        final Tester tester = new LookaheadTester(myModel);
          ((LookaheadTester) tester).setDepth(3);


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

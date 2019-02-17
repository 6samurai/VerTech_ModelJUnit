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
    private  boolean LiftMoveUp = false;
    private  boolean LiftMoveDown = false;
    private  boolean LiftBetweenFloors = false;
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
        LiftMoveUp = false;
        LiftMoveDown = false;
        LiftBetweenFloors = false;
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
        ButtonPress = false;
        DoorOpen = false;
        modelState = LiftOperatorStates.DOOR_CLOSED_STATIONARY_LIFT;

        Assert.assertEquals("The model's Door Open state doesn't match the SUT's state.", DoorOpen, sut.isDoorOpen());
        Assert.assertEquals("The model's Button Press state doesn't match the SUT's state.", ButtonPress, sut.isButtonPressed());
    }

    public boolean buttonPressFromOpenDoorGuard() {
        return getState().equals(LiftOperatorStates.DOOR_OPEN_STATIONARY_LIFT);
    }

    public @Action
    void buttonPressFromOpenDoor() {
        sut.buttonPressed();
        ButtonPress = true;

        modelState = LiftOperatorStates.BUTTON_PRESSED;

        Assert.assertEquals("The model's Button press state doesn't match the SUT's state.", ButtonPress, sut.isButtonPressed());
        //Assert.assertEquals("The model's offline Verified state doesn't match the SUT's state.", offVerif, sut.isOfflineVerified());
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

    public boolean doorOpenFromMoveUpGuard() {
        return getState().equals(LiftOperatorStates.DOOR_CLOSED_LIFT_MOVE_UP);
    }

    public @Action
    void doorOpenFromMoveUp() {
        sut.openLiftDoors();
        DoorOpen = true;
        LiftMoveUp = false;
        modelState = LiftOperatorStates.DOOR_OPEN_STATIONARY_LIFT;

        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", DoorOpen, sut.isDoorOpen());
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", LiftMoveUp, sut.isLiftMovingUp());
    }

   public boolean doorOpenFromMoveDownGuard() {
        return getState().equals(LiftOperatorStates.DOOR_CLOSED_LIFT_MOVE_DOWN);
    }

    public @Action
    void doorOpenFromMoveDown() {
        sut.openLiftDoors();
        DoorOpen = true;
        LiftMoveDown = false;
        modelState = LiftOperatorStates.DOOR_OPEN_STATIONARY_LIFT;

        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", DoorOpen, sut.isDoorOpen());
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", LiftMoveDown, sut.isLiftMovingDown());

    }

    public boolean moveUpFromButtonPressGuard() {
        return getState().equals(LiftOperatorStates.BUTTON_PRESSED);
    }

    public @Action
    void moveUpFromButtonPress() {
        sut.liftMoveUp();
        LiftMoveUp = true;
        ButtonPress = false;
        modelState = LiftOperatorStates.DOOR_CLOSED_LIFT_MOVE_UP;

        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", LiftMoveUp, sut.isLiftMovingUp());
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", ButtonPress, sut.isButtonPressed());

    }

    public boolean buttonPressFromMoveUpGuard() {
        return getState().equals(LiftOperatorStates.DOOR_CLOSED_LIFT_MOVE_UP);
    }

    public @Action
    void buttonPressFromMoveUp() {
        sut.liftMoveUp();

        ButtonPress = true;
        modelState = LiftOperatorStates.BUTTON_PRESSED;
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", ButtonPress, sut.isButtonPressed());

    }

    public boolean moveDownFromButtonPressGuard() {
        return getState().equals(LiftOperatorStates.BUTTON_PRESSED);
    }
    public @Action
    void moveDownFromButtonPress() {
        sut.liftMoveUp();
        LiftMoveDown = true;
        ButtonPress = false;
        modelState = LiftOperatorStates.DOOR_CLOSED_LIFT_MOVE_DOWN;

        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", LiftMoveDown, sut.isLiftMovingUp());
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", ButtonPress, sut.isButtonPressed());

    }

    public boolean buttonPressFromMoveDownGuard() {
        return getState().equals(LiftOperatorStates.DOOR_CLOSED_LIFT_MOVE_DOWN);
    }

    public @Action
    void buttonPressFromMoveDown() {
        sut.liftMoveUp();

        ButtonPress = true;
        modelState = LiftOperatorStates.BUTTON_PRESSED;
        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", ButtonPress, sut.isButtonPressed());

    }


    @Test
    public void LiftSystemModelRunner() throws FileNotFoundException {
        LiftOperatorModelTest myModel = new LiftOperatorModelTest();

         final Tester tester = new GreedyTester(myModel);
      //  final Tester tester = new LookaheadTester(myModel);
     //     ((LookaheadTester) tester).setDepth(2);


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

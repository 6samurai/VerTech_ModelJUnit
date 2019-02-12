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
    private LiftOperatorStates modelState = LiftOperatorStates.DOOR_IS_CLOSED_STATIONARY;
    private  boolean Door_Open = false;
    private  boolean Lift_Move = false;

    //SUT
    private LiftOperator sut = new LiftOperator();

    //Method implementations
    public LiftOperatorStates getState() {
        return modelState;
    }

    public void reset(final boolean reset) {
        modelState = LiftOperatorStates.DOOR_IS_CLOSED_STATIONARY;
        Door_Open = false;
        Lift_Move = false;

        if (reset) {
            sut = new LiftOperator();
        }
    }


    public boolean openDoorFromClosedGuard() {
        return getState().equals(LiftOperatorStates.DOOR_IS_CLOSED_STATIONARY);
    }

    public @Action
    void openDoorFromClosed() {
        sut.openLiftDoors();
        Door_Open = true;

        modelState = LiftOperatorStates.DOOR_IS_OPEN_STATIONARY;

        Assert.assertEquals("The model's Door Open state doesn't match the SUT's state.", Door_Open, sut.isDoorOpen());

    }


    public boolean openDoorFromOpenGuard() {
        return getState().equals(LiftOperatorStates.DOOR_IS_OPEN_STATIONARY);
    }

    public @Action
    void openDoorFromOpen() {
        sut.openLiftDoors();

        modelState = LiftOperatorStates.DOOR_IS_OPEN_STATIONARY;

      //  Assert.assertEquals("The model's invalid state doesn't match the SUT's state.", invalid, sut.isInvalid());
        //Assert.assertEquals("The model's offline Verified state doesn't match the SUT's state.", offVerif, sut.isOfflineVerified());
    }

    public boolean openDoorFromMovingGuard() {
        return getState().equals(LiftOperatorStates.LIFT_MOVING);
    }

    public @Action
    void openDoorFromMoving() {
        sut.openLiftDoors();
        Lift_Move = false;
        Door_Open = true;
        modelState = LiftOperatorStates.DOOR_IS_OPEN_STATIONARY;

        Assert.assertEquals("The model's Door Open state doesn't match the SUT's state.", Door_Open, sut.isDoorOpen());
        Assert.assertEquals("The model's Lift Move state doesn't match the SUT's state.", Lift_Move, sut.isLiftMoving());
   }

    public boolean closeDoorGuard() {
        return getState().equals(LiftOperatorStates.DOOR_IS_OPEN_STATIONARY);
    }

    public @Action
    void closeDoor() {
        sut.closeLiftDoor();
        Door_Open = false;
        modelState = LiftOperatorStates.DOOR_IS_CLOSED_STATIONARY;

        Assert.assertEquals("The model's authorised state doesn't match the SUT's state.", Door_Open, sut.isDoorOpen());
    }

   public boolean moveLiftGuard() {
        return getState().equals(LiftOperatorStates.DOOR_IS_CLOSED_STATIONARY);
    }

    public @Action
    void moveLift() {
        sut.liftMoving();
        Lift_Move = true;

        modelState = LiftOperatorStates.LIFT_MOVING;

        Assert.assertEquals("The model's Lift Move state doesn't match the SUT's state.", Lift_Move, sut.isLiftMoving());

    }



    @Test
    public void TelephoneSystemModelRunner() throws FileNotFoundException {
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

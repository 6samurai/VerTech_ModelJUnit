package MultipleLiftsModel_OLD_VERSION;

import LiftModel.MultipleLiftOperator;
import LiftModel.ServiceList;
import MultipleLiftsModel_OLD_VERSION.enums.MultipleLiftOperatorStates;
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

public class MultipleLiftsModelTest implements FsmModel {

    int numFloors = 6;
    int numLifts = 3;
    Lift lift;
    Lift[] lifts;
    ArrayList<Lift> listOfLifts;
    LiftController sut = new LiftController(numFloors, numLifts, false);
    MultipleLiftOperator multipleLiftOperator = new MultipleLiftOperator(numFloors);
    ArrayList<ServiceList> serviceList = new ArrayList<ServiceList>();
    //Variables
    private MultipleLiftOperatorStates modelState = MultipleLiftOperatorStates.CLOSED;

    private Random random = new Random();
    ArrayList<Integer> validLifts = new ArrayList<Integer>();;


    //SUT
    //  private LiftOperator sut = new LiftOperator();

    //Method implementations
    public MultipleLiftOperatorStates getState() {
        return modelState;
    }

    public void reset(final boolean reset) {
        modelState = MultipleLiftOperatorStates.CLOSED;

        if (reset) {

            sut = new LiftController(numFloors, numLifts, false);
            multipleLiftOperator = new MultipleLiftOperator(numFloors);
            lifts = sut.getLifts();
            serviceList = new ArrayList<ServiceList>();

        }
      /*  try {
            Thread.sleep(3000*numFloors +250+ 50*numFloors);
        } catch (Exception e) {}*/
    }

    public boolean getButtonPressGuard() {
        return getState().equals(MultipleLiftOperatorStates.CLOSED) || getState().equals(MultipleLiftOperatorStates.MOVE) ;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void getButtonPress() {
        int check = 0;
        modelState = MultipleLiftOperatorStates.LIFT_CALL;

        int randomFloorCall = random.nextInt(numFloors);

        ArrayList<Lift> sutListOfLifts = sut.getClosestLifts(randomFloorCall);

        listOfLifts = multipleLiftOperator.getClosestLifts(lifts, randomFloorCall);


        if(listOfLifts.size()<=1)
            Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[lift.getId()], lift);
        else{
            for(int i = 0; i<listOfLifts.size();i++){
                if(sutListOfLifts.get(i).getId()== listOfLifts.get(i).getId()){
                    check++;
                }
            }
            Assert.assertEquals("Lift in SUT matches lift used: " + check +"list of Lifts size: " +listOfLifts.size() + " sut lift size:" +sutListOfLifts.size()  ,check, listOfLifts.size());
        }


        lift = multipleLiftOperator.getClosestLift(listOfLifts);

        createNewRequest(lift, randomFloorCall);
    }

    public boolean getButtonPress_OpenDoorGuard() {
        return getState().equals(MultipleLiftOperatorStates.OPEN);

    }

    public @Action
    void getButtonPress_OpenDoor() {

        modelState = MultipleLiftOperatorStates.CLOSED;

        int check = 0;
       // modelState = SingleLiftOperatorStates.LIFT_CALL;

        int randomFloorCall = random.nextInt(numFloors);

        ArrayList<Lift> sutListOfLifts = sut.getClosestLifts(randomFloorCall);

        listOfLifts = multipleLiftOperator.getClosestLifts(lifts, randomFloorCall);


        if(listOfLifts.size()<=1)
            Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[lift.getId()], lift);
        else{
            for(int i = 0; i<listOfLifts.size();i++){
                if(sutListOfLifts.get(i).getId()== listOfLifts.get(i).getId()){
                    check++;
                }
            }
            Assert.assertEquals("Lift in SUT matches lift used",check, listOfLifts.size());
        }

        lift = multipleLiftOperator.getClosestLift(listOfLifts);
        createNewRequest(lift, randomFloorCall);

        int randomLift =lift.getId();
        sut.closeLiftDoor(randomLift,lift.getFloor());

        multipleLiftOperator.closeLiftDoor(lifts[randomLift]);

        try {
            Thread.sleep(3000);
        } catch (Exception e) {}

        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].getId(), lifts[randomLift].getId());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isOpen(), lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isMoving(), lifts[randomLift].isMoving());
        Assert.assertEquals("Lift in SUT matches lift used", false, lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used",false, lifts[randomLift].isMoving());

    }

    public boolean getCloseDoorGuard() {
        return getState().equals(MultipleLiftOperatorStates.OPEN);

    }

    public @Action
    void getCloseDoor() {

  /*      if (serviceList.size() > 0) {
            modelState = SingleLiftOperatorStates.SERVICING;
        } else {
            modelState = SingleLiftOperatorStates.IDLE;
        }*/

        modelState = MultipleLiftOperatorStates.CLOSED;

        int randomLift = random.nextInt(numLifts);
        sut.closeLiftDoor(randomLift,lift.getFloor());

        multipleLiftOperator.closeLiftDoor(lifts[randomLift]);

        try {
            Thread.sleep(3000);
        } catch (Exception e) {}

        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].getId(), lifts[randomLift].getId());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isOpen(), lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isMoving(), lifts[randomLift].isMoving());
        Assert.assertEquals("Lift in SUT matches lift used", false, lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used",false, lifts[randomLift].isMoving());
    }

    public boolean getOpenDoorGuard() {
        return getState().equals(MultipleLiftOperatorStates.CLOSED)  || getState().equals(MultipleLiftOperatorStates.MOVE);

    }

    public @Action
    void getOpenDoor() {
        modelState = MultipleLiftOperatorStates.OPEN;

        int randomLift = random.nextInt(numLifts);
        deleteRequest(lifts[randomLift]);

        sut.openLiftDoor(randomLift,lift.getFloor());

        multipleLiftOperator.openLiftDoor(lifts[randomLift]);

        try {
            Thread.sleep(3000);
        } catch (Exception e) {}

        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].getId(), lifts[randomLift].getId());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isOpen(), lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isMoving(), lifts[randomLift].isMoving());
        Assert.assertEquals("Lift in SUT matches lift used", true, lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used",false, lifts[randomLift].isMoving());
    }


    public boolean getMoveLiftGuard() {
        return (getState().equals(MultipleLiftOperatorStates.LIFT_CALL)) && serviceList.size()>0;

    }

    public @Action
    void  getMoveLift() {

        modelState = MultipleLiftOperatorStates.MOVE;
     //   lifts = sut.getLifts();
        for(int i=0;i<numLifts;i++){
            if(!lifts[i].isOpen()){

                validLifts.add(lifts[i].getId());
            }
        }

        int randomID = random.nextInt(validLifts.size());

        int randomLift =validLifts.get(randomID);
        int randomFloor = random.nextInt(numFloors);

        validLifts.clear();
        sut.moveLift(randomLift,randomFloor);

        multipleLiftOperator.moveLift(lifts[randomLift],randomFloor);

        try {
            Thread.sleep(250+ 50*numFloors);
        } catch (Exception e) {}
/*
        for(int i =0; i<serviceList.size();i++){
            if(serviceList.get(i).getLift().getId() == randomLift){


            }
        }*/

     //   sut.m

        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].getId(), lifts[randomLift].getId());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isOpen(), lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isMoving(), lifts[randomLift].isMoving());
        Assert.assertEquals("Lift in SUT matches lift used", false, lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used",true, lifts[randomLift].isMoving());
    }



    private void deleteRequest(Lift lift) {

        ServiceList serviceListEntry;
        ServiceList currentEntry = new ServiceList(lift, lift.getFloor());

        if (!serviceList.isEmpty()) {
            for (int i = 0; i < serviceList.size(); i++) {
                serviceListEntry = serviceList.get(i);
                if ((serviceListEntry.getLift() == currentEntry.getLift() && serviceListEntry.getFloor() == currentEntry.getFloor())) {
                    serviceList.remove(i);
                    break;
                }
            }
        }
    }

    private void createNewRequest(Lift lift, int floor) {
        ServiceList serviceListEntry;
        boolean serviceEntryPresent = false;

        ServiceList newEntry = new ServiceList(lift, floor);

        if (serviceList.isEmpty()) {
            serviceList.add(newEntry);

        } else {

            for (int i = 0; i < serviceList.size(); i++) {
                serviceListEntry = serviceList.get(i);

                if ((serviceListEntry.getLift() == newEntry.getLift() && serviceListEntry.getFloor() == newEntry.getFloor())) {
                    serviceEntryPresent = true;
                    break;
                }
            }

            if (!serviceEntryPresent) {
                serviceList.add(newEntry);
            }
        }
    }

    @Test
    public void LiftSystemModelRunner() throws FileNotFoundException {
        MultipleLiftsModelTest myModel = new MultipleLiftsModelTest();

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

package MultipleLiftsModel;

import LiftModel.MultipleLiftOperator;
import LiftModel.ServiceList;
import MultipleLiftsModel.enums.MultipleLiftOperatorStates;
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
    private MultipleLiftOperatorStates modelState = MultipleLiftOperatorStates.IDLE;
    private boolean DoorOpen = false;
    private boolean LiftMove = false;
    private boolean ButtonPress = false;
    private Random random = new Random();


    //SUT
    //  private LiftOperator sut = new LiftOperator();

    //Method implementations
    public MultipleLiftOperatorStates getState() {
        return modelState;
    }

    public void reset(final boolean reset) {
        modelState = MultipleLiftOperatorStates.IDLE;
        DoorOpen = false;
        LiftMove = false;
        ButtonPress = false;

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

    public boolean getClosestLiftGuard() {
        return getState().equals(MultipleLiftOperatorStates.IDLE) || getState().equals(MultipleLiftOperatorStates.SERVICING) || getState().equals(MultipleLiftOperatorStates.FINALISING);
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void getClosestLift() {
        modelState = MultipleLiftOperatorStates.SERVICING;

        int randomFloorCall = random.nextInt(numFloors);

        ArrayList<Lift> sutListOfLifts = sut.getClosestLifts(randomFloorCall);

        listOfLifts = multipleLiftOperator.getClosestLifts(lifts, randomFloorCall);


        if(listOfLifts.size()<=1)
            Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[lift.getId()], lift);
        else
            Assert.assertEquals("Lift in SUT matches lift used",sutListOfLifts, listOfLifts);

        lift = multipleLiftOperator.getClosestLift(listOfLifts);

        createNewRequest(lift, randomFloorCall);
    }


    public boolean getCloseDoorGuard() {
        return getState().equals(MultipleLiftOperatorStates.FINALISING);

    }

    public @Action
    void getCloseDoor() {

        if (serviceList.size() > 0) {
            modelState = MultipleLiftOperatorStates.SERVICING;
        } else {
            modelState = MultipleLiftOperatorStates.IDLE;
        }

        int randomLift = random.nextInt(numLifts);
        sut.closeLiftDoor(randomLift);

        multipleLiftOperator.closeLiftDoor(lifts[randomLift]);


        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].getId(), lifts[randomLift].getId());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isOpen(), lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isMoving(), lifts[randomLift].isMoving());
        Assert.assertEquals("Lift in SUT matches lift used", false, lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used",false, lifts[randomLift].isMoving());
    }

    public boolean getOpenDoorGuard() {
        return getState().equals(MultipleLiftOperatorStates.SERVICING);

    }

    public @Action
    void getOpenDoor() {
        modelState = MultipleLiftOperatorStates.FINALISING;

        int randomLift = random.nextInt(numLifts);
        deleteRequest(lifts[randomLift]);

        sut.openLiftDoor(randomLift);

        multipleLiftOperator.openLiftDoor(lifts[randomLift]);

        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].getId(), lifts[randomLift].getId());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isOpen(), lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isMoving(), lifts[randomLift].isMoving());
        Assert.assertEquals("Lift in SUT matches lift used", true, lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used",false, lifts[randomLift].isMoving());
    }


    public boolean getMoveLiftGuard() {
        return (getState().equals(MultipleLiftOperatorStates.SERVICING) ||getState().equals(MultipleLiftOperatorStates.FINALISING)) && serviceList.size()>0;

    }

    public @Action
    void  getMoveLift() {

        modelState = MultipleLiftOperatorStates.SERVICING;

        int randomLift = random.nextInt(numLifts);



        for(int i =0; i<serviceList.size();i++){
            if(serviceList.get(i).getLift().getId() == randomLift){


            }
        }

        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].getId(), lifts[randomLift].getId());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isOpen(), lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isMoving(), lifts[randomLift].isMoving());
        Assert.assertEquals("Lift in SUT matches lift used", true, lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used",false, lifts[randomLift].isMoving());
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

        tester.generate(5);
        tester.printCoverage();
    }
}

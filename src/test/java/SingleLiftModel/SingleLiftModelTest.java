package SingleLiftModel;

import LiftModel_DOES_NOT_WORK.MultipleLiftOperator;
import LiftModel_DOES_NOT_WORK.ServiceList;
import SingleLiftModel.enums.SingleLiftOperatorStates;
import com.liftmania.Lift;
import com.liftmania.LiftController;
import junit.framework.Assert;
import nz.ac.waikato.modeljunit.*;
import nz.ac.waikato.modeljunit.coverage.ActionCoverage;
import nz.ac.waikato.modeljunit.coverage.StateCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;
import nz.ac.waikato.modeljunit.coverage.TransitionPairCoverage;
import nz.ac.waikato.modeljunit.timing.Time;
import nz.ac.waikato.modeljunit.timing.TimedFsmModel;
import nz.ac.waikato.modeljunit.timing.TimedModel;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SingleLiftModelTest implements TimedFsmModel {

    private static int callLift = 10;
    private static int probTotal = 100;
    @Time
    public int now;
    int numFloors = 6;
    int numLifts = 1;
    Lift lift;
    Lift[] lifts;
    ArrayList<Lift> listOfLifts;
    LiftController sut = new LiftController(numFloors, numLifts, false);
    MultipleLiftOperator multipleLiftOperator = new MultipleLiftOperator(numFloors);
    ArrayList<ServiceList> serviceList = new ArrayList<ServiceList>();
    ArrayList<Integer> validLifts = new ArrayList<Integer>();
    //Variables
    private SingleLiftOperatorStates modelState = SingleLiftOperatorStates.CLOSED;
    private Random random = new Random();
    private int closeDoorTime;
    private int moveLiftTime;
    private boolean firstOperation = false;
    private boolean openDoorCarriedOut = false;
    private boolean closeDoorCarriedOut = false;

    @Override
    public int getNextTimeIncrement(Random random) {
        return 1;
    }
    //SUT
    //  private LiftOperator sut = new LiftOperator();

    //Method implementations
    public SingleLiftOperatorStates getState() {
        return modelState;
    }

    public void reset(final boolean reset) {
        modelState = SingleLiftOperatorStates.CLOSED;

        if (reset) {


            sut = new LiftController(numFloors, numLifts, false);
            multipleLiftOperator = new MultipleLiftOperator(numFloors);
            lifts = sut.getLifts();
            serviceList = new ArrayList<ServiceList>();
            firstOperation = true;
            lift = lifts[0];
            openDoorCarriedOut = false;
            closeDoorCarriedOut = false;
        }
    }

    public boolean buttonPressGuard() {
        return ((getState().equals(SingleLiftOperatorStates.CLOSED) && !lifts[0].isMoving() ) || (getState().equals(SingleLiftOperatorStates.MOVE)&& lifts[0].isMoving()  )|| (getState().equals(SingleLiftOperatorStates.OPEN)  && !lifts[0].isMoving() ) && random.nextInt(probTotal) < callLift) || firstOperation;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void buttonPress() {
        int check = 0;

        if (modelState.equals(SingleLiftOperatorStates.OPEN)) {
            openDoorCarriedOut = false;
        }


        modelState = SingleLiftOperatorStates.LIFT_CALL;
        firstOperation = false;
        int randomFloorCall = random.nextInt(numFloors);

        ArrayList<Lift> sutListOfLifts = sut.getClosestLifts(randomFloorCall);

        listOfLifts = multipleLiftOperator.getClosestLifts(lifts, randomFloorCall);


        if (listOfLifts.size() <= 1)
            Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[lifts[0].getId()], lifts[0]);
        else {
            for (int i = 0; i < listOfLifts.size(); i++) {
                if (sutListOfLifts.get(i).getId() == listOfLifts.get(i).getId()) {
                    check++;
                }
            }
            Assert.assertEquals("Lift in SUT matches lift used: " + check + "list of Lifts size: " + listOfLifts.size() + " sut lift size:" + sutListOfLifts.size(), check, listOfLifts.size());
        }

        createNewRequest(lifts[0], randomFloorCall);
    }

    public boolean closeDoorGuard() {
        return ((getState().equals(SingleLiftOperatorStates.LIFT_CALL) && lifts[0].isOpen()) || (getState().equals(SingleLiftOperatorStates.OPEN) && now - closeDoorTime == 3) && !closeDoorCarriedOut);

    }

    public @Action
    void closeDoor() {

        closeDoorCarriedOut = true;
        openDoorCarriedOut = false;
        if (modelState.equals(SingleLiftOperatorStates.OPEN)) {
            deleteRequest(lifts[0]);
        }

        moveLiftTime = now;
        modelState = SingleLiftOperatorStates.CLOSED;


        sut.closeLiftDoor(0, lifts[0].getFloor());

        multipleLiftOperator.closeLiftDoor(lifts[0]);


        try {
                Thread.sleep(500);
            } catch (Exception e) {
        }

        Assert.assertEquals("Lift Open door in SUT  does not match lift used", false, lifts[0].isOpen());
        Assert.assertEquals("Lift Moving  in SUT  does not match lift used", false, lifts[0].isMoving());


    }

    public boolean openDoorGuard() {

        ServiceList currentEntry = new ServiceList(lifts[0], lifts[0].getFloor());
        return (getState().equals(SingleLiftOperatorStates.LIFT_CALL) || getState().equals(SingleLiftOperatorStates.MOVE)) && findServiceListEntry(currentEntry) && !openDoorCarriedOut;

    }

    public @Action
    void openDoor() {

        openDoorCarriedOut = true;
        closeDoorCarriedOut = false;
        closeDoorTime = now;

        modelState = SingleLiftOperatorStates.OPEN;

        sut.openLiftDoor(lifts[0].getId(), lifts[0].getFloor());
        multipleLiftOperator.openLiftDoor(lifts[0]);

   /*     try {
            Thread.sleep(3000);
        } catch (Exception e) {
        }*/


        Assert.assertEquals("Lift Open door in SUT  does not match lift used", true, lifts[0].isOpen());
        Assert.assertEquals("Lift Moving  in SUT  does not match lift used", false, lifts[0].isMoving());
    }


    public boolean moveLiftGuard() {
        boolean validTimer = false;

        return (getState().equals(SingleLiftOperatorStates.LIFT_CALL) || getState().equals(SingleLiftOperatorStates.CLOSED)) && serviceList.size() > 0 && !lifts[0].isOpen();

    }

    public @Action
    void moveLift() {

        boolean validTimer = false;
        if (modelState.equals(SingleLiftOperatorStates.CLOSED)) {
            if (now - moveLiftTime <= 3)
                validTimer = true;

            Assert.assertEquals("Move time limit exceeded " + now + " moveLiftTimer " + moveLiftTime, true, validTimer);

        }

        modelState = SingleLiftOperatorStates.MOVE;

        int closestFloor = numFloors;
        int floorDifference = 0;
        int serviceId;
        for (int i = 0; i < serviceList.size(); i++) {
            if (!lifts[0].isMoving()) {
                floorDifference = Math.abs(lifts[0].distanceFromFloor(serviceList.get(i).getFloor())) - lifts[0].getFloor();
                if (floorDifference < closestFloor) {
                    closestFloor = serviceList.get(i).getFloor();
                    serviceId = i;
                }
            } else if ((lifts[0].distanceFromFloor(serviceList.get(i).getFloor()) < closestFloor && !lifts[0].getIsMovingUp()) ||
                    (lifts[0].distanceFromFloor(serviceList.get(i).getFloor()) * -1 < closestFloor && lifts[0].getIsMovingUp())) {
                closestFloor = serviceList.get(i).getFloor();
                serviceId = i;

            }


        }

        sut.moveLift(lifts[0], closestFloor);

        multipleLiftOperator.moveLift(lifts[0], closestFloor);

        try {
            Thread.sleep(50);
        } catch (Exception e) {
        }


        Assert.assertEquals("Lift Open door in SUT  does not match lift used", false, lifts[0].isOpen());
        Assert.assertEquals("Lift Moving  in SUT  does not match lift used", true, lifts[0].isMoving());
    }

    public @Action
    void unguardedAction() throws InterruptedException {
        System.out.println("current time " + now);
        TimeUnit.SECONDS.sleep((getNextTimeIncrement(new Random())));


    }

    private void deleteRequest(Lift lift) {

        ServiceList serviceListEntry;
        ServiceList currentEntry = new ServiceList(lift, lifts[0].getFloor());

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

        // boolean serviceEntryPresent = false;

        ServiceList newEntry = new ServiceList(lift, floor);

        if (serviceList.isEmpty()) {
            serviceList.add(newEntry);

        } else {

            if (!findServiceListEntry(newEntry)) {
                serviceList.add(newEntry);
            }
        }
    }

    private boolean findServiceListEntry(ServiceList newEntry) {
        boolean serviceEntryPresent = false;
        ServiceList serviceListEntry;
        for (int i = 0; i < serviceList.size(); i++) {
            serviceListEntry = serviceList.get(i);

            if ((serviceListEntry.getLift() == newEntry.getLift() && serviceListEntry.getFloor() == newEntry.getFloor())) {
                serviceEntryPresent = true;
                break;
            }
        }

        return serviceEntryPresent;

    }

    @Test
    public void LiftSystemModelRunner() throws FileNotFoundException {

        final TimedModel myModel = new TimedModel(new SingleLiftModelTest());
        final Tester tester = new GreedyTester(myModel);
        //    final Tester tester = new LookaheadTester(myModel);
        //    ((LookaheadTester) tester).setDepth(3);


        //   final Tester tester  = new RandomTester(myModel);
        tester.setRandom(new Random(100));
        final GraphListener graphListener = tester.buildGraph();
        //  graphListener.printGraphDot("/users/Owner/Desktop/output.dot");
        tester.addListener(new StopOnFailureListener());
        tester.addListener("verbose");
        tester.addCoverageMetric(new TransitionPairCoverage());
        tester.addCoverageMetric(new TransitionCoverage());
        tester.addCoverageMetric(new StateCoverage());
        tester.addCoverageMetric(new ActionCoverage());

        tester.generate(500);
        tester.printCoverage();


    }
}

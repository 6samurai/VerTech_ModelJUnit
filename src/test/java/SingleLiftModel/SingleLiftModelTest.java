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

    int numFloors = 6;
    int numLifts = 1;
    Lift lift;
    Lift[] lifts;
    ArrayList<Lift> listOfLifts;
    LiftController sut = new LiftController(numFloors, numLifts, false);
    MultipleLiftOperator multipleLiftOperator = new MultipleLiftOperator(numFloors);
    ArrayList<ServiceList> serviceList = new ArrayList<ServiceList>();
    //Variables
    private SingleLiftOperatorStates modelState = SingleLiftOperatorStates.CLOSED;

    private Random random = new Random();
    ArrayList<Integer> validLifts = new ArrayList<Integer>();

    private int closeDoorTime;
    private int moveLiftTime;

    private static int callLift = 10;
    private static int probTotal = 100;
    private boolean firstOperation= false;
    private boolean openDoorCarriedOut = false;
    private boolean closeDoorCarriedOut = false;
    @Time
    public int now;


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
        return (getState().equals(SingleLiftOperatorStates.CLOSED) || getState().equals(SingleLiftOperatorStates.MOVE) || getState().equals(SingleLiftOperatorStates.OPEN) &&  random.nextInt(probTotal) < callLift ) || firstOperation ;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void buttonPress() {
        int check = 0;

        if(modelState.equals(SingleLiftOperatorStates.OPEN)){
            openDoorCarriedOut = false;
        }


        modelState = SingleLiftOperatorStates.LIFT_CALL;
        firstOperation = false;
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


//        lift = multipleLiftOperator.getClosestLift(listOfLifts);

        createNewRequest(lift, randomFloorCall);
    }

    public boolean closeDoorGuard() {
        return (getState().equals(SingleLiftOperatorStates.LIFT_CALL) || (getState().equals(SingleLiftOperatorStates.OPEN) && now -closeDoorTime ==3) && !closeDoorCarriedOut);

    }

    public @Action
    void closeDoor() {

        closeDoorCarriedOut = true;
        openDoorCarriedOut = false;
        modelState = SingleLiftOperatorStates.CLOSED;

        int randomLift = random.nextInt(numLifts);
        sut.closeLiftDoor(randomLift,lift.getFloor());

        multipleLiftOperator.closeLiftDoor(lifts[randomLift]);

        try {
            Thread.sleep(500);
        } catch (Exception e) {}

        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].getId(), lifts[randomLift].getId());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isOpen(), lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isMoving(), lifts[randomLift].isMoving());
        Assert.assertEquals("Lift in SUT matches lift used", false, lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used",false, lifts[randomLift].isMoving());

        moveLiftTime = now;
    }

    public boolean openDoorGuard() {

        ServiceList currentEntry = new ServiceList(lift, lift.getFloor());
        return (getState().equals(SingleLiftOperatorStates.LIFT_CALL)  || getState().equals(SingleLiftOperatorStates.MOVE)) && findServiceListEntry(currentEntry) && !openDoorCarriedOut ;

    }

    public @Action
    void openDoor() {

        openDoorCarriedOut = true;
        closeDoorCarriedOut = false;
        closeDoorTime = now;
        modelState = SingleLiftOperatorStates.OPEN;

        int randomLift = random.nextInt(numLifts);
        deleteRequest(lifts[randomLift]);

        sut.openLiftDoor(randomLift,lift.getFloor());

        multipleLiftOperator.openLiftDoor(lifts[randomLift]);

        ServiceList currentEntry = new ServiceList(lift, lift.getFloor());
        serviceList.remove(currentEntry);

        try {
            Thread.sleep(3000);
        } catch (Exception e) {}

        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].getId(), lifts[randomLift].getId());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isOpen(), lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[randomLift].isMoving(), lifts[randomLift].isMoving());
        Assert.assertEquals("Lift in SUT matches lift used", true, lifts[randomLift].isOpen());
        Assert.assertEquals("Lift in SUT matches lift used",false, lifts[randomLift].isMoving());
    }


    public boolean moveLiftGuard() {
        boolean validTimer = false;
        if(!lift.isMoving()){
            if(now - moveLiftTime<3)
            validTimer = true;

        } else{
            validTimer = true;
        }
        return (getState().equals(SingleLiftOperatorStates.LIFT_CALL) || getState().equals(SingleLiftOperatorStates.CLOSED) ) && serviceList.size()>0 && !lift.isOpen() && validTimer ;

    }

    public @Action
    void  moveLift() {

        modelState = SingleLiftOperatorStates.MOVE;

        int closestFloor = numFloors;
        int floorDifference = 0;
        int serviceId;
        for(int i =0; i<serviceList.size();i++){
            if(!lift.isMoving()){
                floorDifference =Math.abs(lift.distanceFromFloor(serviceList.get(i).getFloor()))-lift.getFloor();
                if( floorDifference<closestFloor){
                    closestFloor = floorDifference;
                    serviceId = i;
                }
            }else if( (lift.distanceFromFloor(serviceList.get(i).getFloor())< closestFloor && !lift.getIsMovingUp() ) ||
                    (lift.distanceFromFloor(serviceList.get(i).getFloor())*-1 < closestFloor&& lift.getIsMovingUp())) {
                closestFloor = floorDifference;
                serviceId = i;

            }



        }

        sut.moveLift(lift,closestFloor);

        multipleLiftOperator.moveLift(lift,closestFloor);

        try {
            Thread.sleep(250+ 50*(numFloors-lift.getFloor()));
        } catch (Exception e) {}


        Assert.assertEquals("Lift in SUT is open", false, lifts[0].isOpen());
        Assert.assertEquals("Lift in SUT is moving",true, lifts[0].isMoving());
    }

    public @Action void unguardedAction() throws InterruptedException {
           System.out.println("current time "+now);
        TimeUnit.SECONDS.sleep((getNextTimeIncrement(new Random())));


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

    private  boolean findServiceListEntry(ServiceList newEntry){
        boolean serviceEntryPresent = false;
        ServiceList serviceListEntry;
        for (int i = 0; i < serviceList.size(); i++) {
            serviceListEntry = serviceList.get(i);

            if ((serviceListEntry.getLift() == newEntry.getLift() && serviceListEntry.getFloor() == newEntry.getFloor())) {
                serviceEntryPresent = true;
                break;
            }
        }

        return  serviceEntryPresent;

    }

    @Test
    public void LiftSystemModelRunner() throws FileNotFoundException {

        final TimedModel myModel = new TimedModel(new SingleLiftModelTest());
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

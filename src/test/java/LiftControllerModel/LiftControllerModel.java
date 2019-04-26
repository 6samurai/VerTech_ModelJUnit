package LiftControllerModel;

import LiftControllerModel.LiftObject.LiftObject;
import LiftControllerModel.LiftObject.LiftState;
import LiftControllerModel.enums.LiftControllerStates;
import LiftModel.MultipleLiftOperator;
import LiftModel.ServiceList;
import com.liftmania.Lift;
import com.liftmania.LiftController;
import junit.framework.Assert;
import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.GraphListener;
import nz.ac.waikato.modeljunit.GreedyTester;
import nz.ac.waikato.modeljunit.StopOnFailureListener;
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


public class LiftControllerModel implements TimedFsmModel {
    private static int callLift = 10;
    private static int probTotal = 100;
    @Time
    public int now;
    int numFloors = 0;
    int numLifts = 0;
    private Random random = new Random();

    private Lift[] lifts;
    private ArrayList<LiftObject> liftStates;
 //   private ArrayList<Lift> listOfLifts;
    private LiftController sut;
    private MultipleLiftOperator multipleLiftOperator;
    private ArrayList<ServiceList> serviceList;
    private LiftControllerStates modelState = LiftControllerStates.IDLE;
    private int liftCounter = 0;
    private int currentTimeSlot;
    private int currentLiftID;
    private boolean check;
    private int temp = 0;
    private boolean firstOperation = true;
    private int idOffset = 0;

    private ArrayList<Lift> buttonPressLists;
    //Method implementations
    public LiftControllerStates getState() {
        return modelState;
    }

    public void reset(final boolean reset) {
        modelState = LiftControllerStates.IDLE;
        idOffset = 0;
        if (reset) {
            //assuming range of floors is between 0 and random max number
           numFloors = random.nextInt(10) + 3;
           numLifts = random.nextInt(10) + 3;

            sut = new LiftController(numFloors, numLifts, false);

            multipleLiftOperator = new MultipleLiftOperator(numFloors);

            lifts = sut.getLifts();

            serviceList = new ArrayList<ServiceList>();

            liftStates = new ArrayList<LiftObject>();

            buttonPressLists = new ArrayList<Lift>();

            liftCounter = 0;
            now = 0;

            currentTimeSlot = 0;
            currentLiftID = 0;
            check = false;
            firstOperation = true;
            for (int i = 0; i < numLifts; i++) {
                liftStates.add(new LiftObject());
            }
        }
    }

    @Override
    public int getNextTimeIncrement(Random random) {
        return 1;
    }

    public boolean verifyBehaviourGuard() {

        return getState().equals(LiftControllerStates.SERVICING) && (now - idOffset) % (numLifts+2) == numLifts;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void verifyBehaviour() {
        check = false;
        boolean overFloorLimits = false;
        //to check if all of the call requests have been serviced
        modelState = LiftControllerStates.VERIFY;

        lifts = sut.getLifts();

            for (int i = 0; i < liftStates.size(); i++) {

                //if all lifts are closed and stationary with items still required to be serviced - lift is invalid behaviour
                if(serviceList.size() == 0)
                if (!liftStates.get(i).getLiftState().equals(LiftState.CLOSED) ) {
                    check = true;
                    break;
                }

                if (!(lifts[i].getFloor() < numFloors && lifts[i].getFloor() >= 0)) {
                    overFloorLimits = true;
                    break;
                }
            }

        Assert.assertEquals("Expecting lift system to still be servicing requests ", false, check);
        Assert.assertEquals("Exceeded height limits "  + liftStates.get(0).allVarianbles() + liftStates.get(1).allVarianbles(), false, overFloorLimits);
    }

    public boolean liftCallGuard() {
 //       System.out.println("lift call guard");
        return ((getState().equals(LiftControllerStates.IDLE) || getState().equals(LiftControllerStates.SERVICING  ) &&  random.nextInt(probTotal) < callLift) && (now - idOffset) % (numLifts+2) < numLifts|| firstOperation);
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void liftCall() {

        int counter = 0;

        modelState = LiftControllerStates.SERVICING;
        if(firstOperation){
            idOffset = now +1;
            firstOperation = false;
        }

        int randomFloorCall = random.nextInt(numFloors);

        buttonPressLists.clear();
        int randomBehaviour =random.nextInt(2);
        lifts = sut.getLifts();
        int tempID =0;

        if(randomBehaviour==1){
            //this condition represents a move call press
            //random lift selection
            counter = random.nextInt(numLifts);
            tempID =counter;

            if(  liftStates.get(tempID).getDestinationFloor() == -1) {

                liftStates.get(tempID).setDestinationFloor(randomFloorCall);
                //set direction of lift
                if ( liftStates.get(tempID).getCurrentFloor() > randomFloorCall)
                    liftStates.get(tempID).setMovingUp(false);
                else
                    liftStates.get(tempID).setMovingUp(true);


            } else if(liftStates.get(tempID).getCurrentFloor() == randomFloorCall)
                liftStates.get(tempID).setDestinationFloor(randomFloorCall);

            createNewRequest(lifts[counter], randomFloorCall);

        } else{
            //this condition represents a call Lift press
            buttonPressLists = multipleLiftOperator.getClosestLifts(lifts, randomFloorCall);

            if (lifts.length == 1)
                Assert.assertEquals("Lift in SUT matches lift used", lifts[0].getId(), buttonPressLists.get(0));
            else {
                for (int i = 0; i < lifts.length; i++) {
                    for(int j = 0; j <buttonPressLists.size();j++){
                        if (lifts[i].getId() == buttonPressLists.get(j).getId()) {
                            counter++;
                        }
                    }

                }
                Assert.assertEquals("Lift in SUT matches lift used: ", counter, buttonPressLists.size());
            }

            counter =random.nextInt(buttonPressLists.size());
            //current lift
            tempID = buttonPressLists.get(counter).getId();
            createNewRequest(buttonPressLists.get(counter), randomFloorCall);

            LiftObject tempObject =  liftStates.get(tempID);

            //if current lift does not have destination floor
            if(  tempObject.getDestinationFloor() == -1){

                liftStates.get(tempID).setDestinationFloor(randomFloorCall);
                //set direction of lift
                if(  tempObject.getCurrentFloor()>randomFloorCall)
                    liftStates.get(tempID).setMovingUp(false);
                else
                    liftStates.get(tempID).setMovingUp(true);
            } else {
                //if current floor of lift = input received
                if(tempObject.getCurrentFloor() == randomFloorCall){
                    liftStates.get(tempID).setDestinationFloor(randomFloorCall);
                } else{
                    int currentDistance =   tempObject.getDestinationFloor() - tempObject.getCurrentFloor();
                    int tempDistance =  randomFloorCall - tempObject.getCurrentFloor();
                    //checks if current servicing direction of lift is the same as new request
                    if(tempObject.getMovingUp() ){

                        if( tempDistance>0 && tempDistance<currentDistance)
                            liftStates.get(tempID).setDestinationFloor(randomFloorCall);

                    } else{
                        if( tempDistance<0 && tempDistance>currentDistance )
                            liftStates.get(tempID).setDestinationFloor(randomFloorCall);

                    }
                }
            }
        }
       liftStates.get(tempID).setLiftState(LiftState.LIFT_CALL);
    }

    public boolean moveGuard() {

        lifts = sut.getLifts();
        currentLiftID = (now-idOffset) % ( numLifts +2);

        if(currentLiftID<numLifts)
            if (
                    (liftStates.get(currentLiftID).getLiftState().equals(LiftState.LIFT_CALL) ||
                            (
                                    (liftStates.get(currentLiftID).getLiftState().equals(LiftState.MOVING)) ||
                                            (liftStates.get(currentLiftID).getLiftState().equals(LiftState.CLOSED) && (now - liftStates.get(currentLiftID).getTimer()>4))
                            )
                    )
                    && liftStates.get(currentLiftID).getDestinationFloor() != -1
                    && liftStates.get(currentLiftID).getDestinationFloor() != liftStates.get(currentLiftID).getCurrentFloor() && !liftStates.get(currentLiftID).getDoorOpen() && !lifts[currentLiftID].isOpen()

                    && serviceList.size()>0){
                return getState().equals(LiftControllerStates.SERVICING);
            }
        return false;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void move() {
       // System.out.println("in move of id "+currentLiftID+ " " +  liftStates.get(0).allVarianbles() + liftStates.get(1).allVarianbles());
        modelState = LiftControllerStates.SERVICING;
        liftStates.get(currentLiftID).setLiftState(LiftState.MOVING);
        liftStates.get(currentLiftID).setMoving(true);

        temp = liftStates.get(currentLiftID).getCurrentFloor();
        if (liftStates.get(currentLiftID).getMovingUp()) {
            sut.moveLift(currentLiftID, temp + 1);
            liftStates.get(currentLiftID).setCurrentFloor(temp + 1);
        } else {
            sut.moveLift(currentLiftID, temp - 1);
            liftStates.get(currentLiftID).setCurrentFloor(temp - 1);
        }

        liftStates.get(currentLiftID).setTimer(now);
        lifts = sut.getLifts();
        Assert.assertEquals("Lift is moving", true, lifts[currentLiftID].isMoving());
        Assert.assertEquals("Lift's doors are closed", false,  lifts[currentLiftID].isOpen());
    }

    public boolean openDoorGuard() {

        currentLiftID = (now-idOffset) % (numLifts+2);

        if(currentLiftID<numLifts){
            if (
                    (
                            ( liftStates.get(currentLiftID).getLiftState().equals(LiftState.MOVING)  && (now - liftStates.get(currentLiftID).getTimer()>3) )
                                    || liftStates.get(currentLiftID).getLiftState().equals(LiftState.LIFT_CALL)
                    )
                            && liftStates.get(currentLiftID).getDestinationFloor() == liftStates.get(currentLiftID).getCurrentFloor()
            )
                return getState().equals(LiftControllerStates.SERVICING);
        }
        return false;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void openDoor() {
        modelState = LiftControllerStates.SERVICING;
        liftStates.get(currentLiftID).setLiftState(LiftState.OPEN);
        liftStates.get(currentLiftID).setMoving(false);
        liftStates.get(currentLiftID).setDoorOpen(true);

        sut.openLiftDoor(currentLiftID, lifts[currentLiftID].getFloor());
        liftStates.get(currentLiftID).setTimer(now);
        lifts = sut.getLifts();
        Assert.assertEquals("Lift is not moving", false,  lifts[currentLiftID].isMoving());
        Assert.assertEquals("Lift's doors are open", true,  lifts[currentLiftID].isOpen());
    }

    public boolean closeDoorGuard() {
        currentLiftID = (now-idOffset) % (numLifts+2);

        if(currentLiftID<numLifts)
            if (
                    (
                            (
                                    (liftStates.get(currentLiftID).getLiftState().equals(LiftState.LIFT_CALL) && liftStates.get(currentLiftID).getDoorOpen())
                                            || (liftStates.get(currentLiftID).getLiftState().equals(LiftState.OPEN)  && (now - liftStates.get(currentLiftID).getTimer()>31))
                            )

                                && liftStates.get(currentLiftID).getDestinationFloor() == liftStates.get(currentLiftID).getCurrentFloor()
                    )
                            || (liftStates.get(currentLiftID).getLiftState().equals(LiftState.CLOSED) &&  liftStates.get(currentLiftID).getDestinationFloor() == -1)
            )

                return getState().equals(LiftControllerStates.SERVICING);

        return false;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void closeDoor() {
        int tempDest = -1;
        modelState = LiftControllerStates.SERVICING;
        if(liftStates.get(currentLiftID).getDestinationFloor()!=-1){

            liftStates.get(currentLiftID).setLiftState(LiftState.CLOSED);
            liftStates.get(currentLiftID).setMoving(false);
            liftStates.get(currentLiftID).setDoorOpen(false);
            lifts = sut.getLifts();
            if (lifts[currentLiftID].isOpen()) {
                deleteRequest(lifts[currentLiftID]);
            }

            if(serviceList.size() ==0){
                liftStates.get(currentLiftID).setDestinationFloor(-1);

            } else{
                for(int i = 0; i<serviceList.size();i++){
                    if(serviceList.get(i).getLift().getId() == currentLiftID){
                        temp = numFloors;
                        if(tempDest==-1 ||  (Math.abs(lifts[currentLiftID].getFloor() - serviceList.get(i).getFloor()) < temp) ){

                            tempDest = serviceList.get(i).getFloor();
                            temp=Math.abs(lifts[currentLiftID].getFloor() - tempDest);
                        }
                    }
                }
                liftStates.get(currentLiftID).setDestinationFloor(tempDest);
                if(  liftStates.get(currentLiftID).getCurrentFloor()>tempDest)
                    liftStates.get(currentLiftID).setMovingUp(false);
                else
                    liftStates.get(currentLiftID).setMovingUp(true);
            }
            sut.closeLiftDoor(currentLiftID, lifts[currentLiftID].getFloor());
        }

        liftStates.get(currentLiftID).setTimer(now);
        lifts = sut.getLifts();
        Assert.assertEquals("Lift is stationary", false, lifts[currentLiftID].isMoving());
        Assert.assertEquals("Lift's doors are open", false,  lifts[currentLiftID].isOpen());
    }

    public boolean finalizeBehaviourGuard() {
        return getState().equals(LiftControllerStates.VERIFY) && (now-idOffset) % (numLifts + 2) == numLifts + 1;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void finalizeBehaviour() {
        if (serviceList.size() > 0) {
            modelState = LiftControllerStates.SERVICING;

        } else {
            modelState = LiftControllerStates.IDLE;
        }
    }

    public boolean idleGuard() {
        boolean checkEmpty = true;
        for (int i = 0; i < liftStates.size(); i++) {
            if (!liftStates.get(i).getLiftState().equals(LiftState.CLOSED)) {
                checkEmpty = false;
                break;
            }
        }

        return (getState().equals(LiftControllerStates.IDLE) || getState().equals(LiftControllerStates.VERIFY)) && serviceList.size() == 0 && checkEmpty;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void idle() {
        modelState = LiftControllerStates.IDLE;
        Assert.assertEquals("Service lift is not empty ", 0, serviceList.size());

    }


    public @Action
    void unguardedAction() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep((getNextTimeIncrement(new Random())*100));
    }


    @Test
    public void LiftSystemModelRunner() throws FileNotFoundException {

        final TimedModel timedModel = new TimedModel(new LiftControllerModel());
         timedModel.setTimeoutProbability(0.01);

        final GreedyTester tester = new GreedyTester(timedModel);
        tester.setRandom(new Random());
        tester.setResetProbability(0.0001);
        final GraphListener graphListener = tester.buildGraph();
        graphListener.printGraphDot("/users/Owner/Desktop/output.dot");
        tester.addListener(new StopOnFailureListener());
        tester.addListener("verbose");
        tester.addCoverageMetric(new TransitionPairCoverage());
        tester.addCoverageMetric(new TransitionCoverage());
        tester.addCoverageMetric(new StateCoverage());
        tester.addCoverageMetric(new ActionCoverage());
        tester.generate(1000);
        tester.printCoverage();
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
}

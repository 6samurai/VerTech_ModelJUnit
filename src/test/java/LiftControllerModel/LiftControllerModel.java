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
    private ArrayList<Lift> listOfLifts;
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


    private ArrayList<Lift> buttonPressLists;
    //Method implementations
    public LiftControllerStates getState() {
        return modelState;
    }

    public void reset(final boolean reset) {
        modelState = LiftControllerStates.IDLE;

        if (reset) {
            //assuming range of floors is between 0 and random max number
            numFloors = random.nextInt(10) + 1;
            numLifts = random.nextInt(10) + 1;

            sut = new LiftController(numFloors, numLifts, false);

            multipleLiftOperator = new MultipleLiftOperator(numFloors);

            lifts = sut.getLifts();

            serviceList = new ArrayList<ServiceList>();

            liftStates = new ArrayList<LiftObject>();
            listOfLifts = new ArrayList<Lift>();

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

        return getState().equals(LiftControllerStates.SERVICING) && now % numLifts == numLifts;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void verifyBehaviour() {
        check = false;
        //to check if all of the call requests have been serviced
        modelState = LiftControllerStates.VERIFY;

        if (serviceList.size() == 0 && checkIfLiftsAreClosed())
            for (int i = 0; i < liftStates.size(); i++) {

                //if all lifts are closed and stationary with items still required to be serviced - lift is invalid behaviour
                if (liftStates.get(i).getLiftState().equals(LiftState.CLOSED) && serviceList.size() == 0) {
                    check = true;
                    break;
                }

                if (!(listOfLifts.get(i).getFloor() < numFloors && listOfLifts.get(i).getFloor() >= 0)) {
                    check = true;
                    break;
                }
            }
        Assert.assertEquals("Expecting lift system to still be servicing requests ", false, check);
    }

    public boolean liftCallGuard() {
        return ((getState().equals(LiftControllerStates.IDLE) || getState().equals(LiftControllerStates.SERVICING  ) &&  random.nextInt(probTotal) < callLift) || firstOperation);
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void liftCall() {

        //CONTUNE FROM HEREEE
        //
        //
        //
        //

        //
        firstOperation = false;
        modelState = LiftControllerStates.SERVICING;
        firstOperation = false;

        int randomFloorCall = random.nextInt(numFloors);

        buttonPressLists.clear();
        buttonPressLists = multipleLiftOperator.getClosestLifts(lifts, randomFloorCall);
        lifts = sut.getLifts();


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

        Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[lifts[0].getId()], lifts[0]);


        createNewRequest(lifts[0], randomFloorCall);





    }

    public boolean moveGuard() {
        currentLiftID = now % numLifts;

        if ((liftStates.get(currentLiftID).getLiftState().equals(LiftState.MOVING) || liftStates.get(currentLiftID).getLiftState().equals(LiftState.CLOSED) ||
                liftStates.get(currentLiftID).getLiftState().equals(LiftState.LIFT_CALL)) && liftStates.get(currentLiftID).getDestinationFloor() != -1)
            return getState().equals(LiftControllerStates.SERVICING);

        return false;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void move() {
        modelState = LiftControllerStates.SERVICING;
        liftStates.get(currentLiftID).setLiftState(LiftState.MOVING);
        liftStates.get(currentLiftID).setMoving(true);

        //ADD TIMER (TO BE TESTED) FOR NOW CONSIDER WITHOUT TIMER

        temp = liftStates.get(currentLiftID).getCurrentFloor();
        if (liftStates.get(currentLiftID).getMovingUp()) {
            sut.moveLift(currentLiftID, temp + 1);
            liftStates.get(currentLiftID).setCurrentFloor(temp + 1);
        } else {
            sut.moveLift(currentLiftID, temp - 1);
            liftStates.get(currentLiftID).setCurrentFloor(temp - 1);
        }

        Assert.assertEquals("Lift is moving", true, listOfLifts.get(currentLiftID).isMoving());
        Assert.assertEquals("Lift's doors are closed", false, listOfLifts.get(currentLiftID).isOpen());
    }

    public boolean openDoorGuard() {
        currentLiftID = now % numLifts;

        if ((liftStates.get(currentLiftID).getLiftState().equals(LiftState.MOVING) || liftStates.get(currentLiftID).getLiftState().equals(LiftState.LIFT_CALL))
                && liftStates.get(currentLiftID).getDestinationFloor() == liftStates.get(currentLiftID).getCurrentFloor())
            return getState().equals(LiftControllerStates.SERVICING);

        return false;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void openDoor() {
        modelState = LiftControllerStates.SERVICING;
        liftStates.get(currentLiftID).setLiftState(LiftState.OPEN);
        liftStates.get(currentLiftID).setMoving(false);
        liftStates.get(currentLiftID).setDoorOpen(true);

        sut.openLiftDoor(currentLiftID, listOfLifts.get(currentLiftID).getFloor());
        Assert.assertEquals("Lift is stationary", true, listOfLifts.get(currentLiftID).isMoving());
        Assert.assertEquals("Lift's doors are open", true, listOfLifts.get(currentLiftID).isOpen());
    }

    public boolean closeDoorGuard() {
        currentLiftID = now % numLifts;

        if ((liftStates.get(currentLiftID).getLiftState().equals(LiftState.LIFT_CALL) || liftStates.get(currentLiftID).getLiftState().equals(LiftState.OPEN))
                && liftStates.get(currentLiftID).getDestinationFloor() == liftStates.get(currentLiftID).getCurrentFloor())
            return getState().equals(LiftControllerStates.SERVICING);

        return false;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void closeDoor() {
        int tempDest = -1;

        modelState = LiftControllerStates.SERVICING;
        liftStates.get(currentLiftID).setLiftState(LiftState.CLOSED);
        liftStates.get(currentLiftID).setMoving(false);
        liftStates.get(currentLiftID).setDoorOpen(false);


        if (modelState.equals(LiftState.OPEN)) {
            deleteRequest(listOfLifts.get(currentLiftID));
        }

        if(serviceList.size() ==0){
            liftStates.get(currentLiftID).setDestinationFloor(-1);

        } else{
            for(int i = 0; i<serviceList.size();i++){
                if(serviceList.get(i).getLift().getId() == currentLiftID){
                    temp = numFloors;
                    if(tempDest==-1 ||  (Math.abs(listOfLifts.get(currentLiftID).getFloor() - serviceList.get(i).getFloor()) < temp) ){

                        tempDest = serviceList.get(i).getFloor();
                        temp=Math.abs(listOfLifts.get(currentLiftID).getFloor() - tempDest);
                    }

                }

            }
            liftStates.get(currentLiftID).setDestinationFloor(tempDest);
        }

        sut.closeLiftDoor(currentLiftID, listOfLifts.get(currentLiftID).getFloor());

        Assert.assertEquals("Lift is stationary", true, listOfLifts.get(currentLiftID).isMoving());
        Assert.assertEquals("Lift's doors are open", false, listOfLifts.get(currentLiftID).isOpen());

    }


    public boolean finalizeBehaviourGuard() {
        return getState().equals(LiftControllerStates.VERIFY) && now % (numLifts + 1) == numLifts + 1;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void finalizeBehaviour() {


        if (serviceList.size() > 0) {
            modelState = LiftControllerStates.SERVICING;

        } else {
            modelState = LiftControllerStates.IDLE;
        }
        Assert.assertEquals("Expecting lift system to still be servicing requests ", true, check);

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
        System.out.println("now time " + now);
        //System.out.println("current time "+currentTimeSlot);
        TimeUnit.MILLISECONDS.sleep((getNextTimeIncrement(new Random())) / (numLifts+2));
    }


    @Test
    public void LiftSystemModelRunner() throws FileNotFoundException {

        final TimedModel timedModel = new TimedModel(new LiftControllerModel());
        //    timedModel.setTimeoutProbability(0.5);

        final GreedyTester tester = new GreedyTester(timedModel);
        tester.setRandom(new Random(100));
        tester.setResetProbability(0.001);
        final GraphListener graphListener = tester.buildGraph();
        //    graphListener.printGraphDot("/users/Owner/Desktop/output.dot");
        tester.addListener(new StopOnFailureListener());
        tester.addListener("verbose");
        tester.addCoverageMetric(new TransitionPairCoverage());
        tester.addCoverageMetric(new TransitionCoverage());
        tester.addCoverageMetric(new StateCoverage());
        tester.addCoverageMetric(new ActionCoverage());
        tester.generate(500);
        tester.printCoverage();
    }


    boolean checkIfLiftsAreClosed() {
        check = true;
        for (int i = 0; i < liftStates.size(); i++) {

            if (!liftStates.get(i).getLiftState().equals(LiftState.CLOSED)) {
                check = false;
                break;
            }

        }
        return check;
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




  /*  public boolean getButtonPressGuard() {
        return getState().equals(LiftControllerStates.IDLE)  ;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void getButtonPress() {
        int check = 0;
        modelState = LiftControllerStates.SERVICING;

        int randomFloorCall = random.nextInt(numFloors);

        ArrayList<Lift> sutListOfLifts = sut.getClosestLifts(randomFloorCall);

        listOfLifts = multipleLiftOperator.getClosestLifts(lifts, randomFloorCall);


        if(listOfLifts.size()<=1)
            Assert.assertEquals("Lift in SUT matches lift used", sut.getLifts()[sutListOfLifts.get(0).getId()], listOfLifts.get(0));
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
        liftStates.get(lift.getId()).setLiftState(LiftState.LIFT_CALL);

    }



    public boolean systemActionGuard() {

        return  getState().equals(LiftControllerStates.SERVICING) && serviceList.size()!=0;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void systemAction() {


        for(int i = 0; i < liftStates.size();i++){
            if(!liftStates.get(i).getLiftState().equals(LiftState.CLOSED)){


             if(liftStates.get(i).getLiftState().equals(LiftState.LIFT_CALL)){

             } else if(liftStates.get(i).getLiftState().equals(LiftState.OPEN)){



             }else if(liftStates.get(i).getLiftState().equals(LiftState.MOVING)){

             }
            }

        }


        Assert.assertEquals("Service lift is not empty " ,0, serviceList.size());

    }
*/
}

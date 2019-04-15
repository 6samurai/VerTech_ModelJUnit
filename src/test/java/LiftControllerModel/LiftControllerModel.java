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
    private static int callLift = 1;
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
     //      numFloors = random.nextInt(10) + 2;
      //     numLifts = random.nextInt(10) + 2;

            numFloors = 2;
            numLifts = 2;

            sut = new LiftController(numFloors, numLifts, false);

            multipleLiftOperator = new MultipleLiftOperator(numFloors);

            lifts = sut.getLifts();

            serviceList = new ArrayList<ServiceList>();

            liftStates = new ArrayList<LiftObject>();
          //  listOfLifts = new ArrayList<Lift>();

            buttonPressLists = new ArrayList<Lift>();

            liftCounter = 0;
            now = 0;

            currentTimeSlot = 0;
            currentLiftID = 0;
            check = false;
            firstOperation = true;
            for (int i = 0; i < numLifts; i++) {
                liftStates.add(new LiftObject());
               // listOfLifts.add(new Lift(i));
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
        System.out.println("in veify");
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

       /* if (serviceList.size() == 0 && !checkIfLiftsAreClosed()){
            check = true;
        }*/

        Assert.assertEquals("Expecting lift system to still be servicing requests ", false, check);
        Assert.assertEquals("Exceeded height limits ", false, overFloorLimits);
    }

    public boolean liftCallGuard() {
 //       System.out.println("lift call guard");
        return ((getState().equals(LiftControllerStates.IDLE) || getState().equals(LiftControllerStates.SERVICING  ) &&  random.nextInt(probTotal) < callLift) && (now - idOffset) % (numLifts+2) < numLifts|| firstOperation);
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void liftCall() {
        System.out.println("in lift call");
        int counter = 0;

        modelState = LiftControllerStates.SERVICING;
        if(firstOperation){
            idOffset = now +1;
            firstOperation = false;
        }


        int randomFloorCall = random.nextInt(numFloors);


        buttonPressLists.clear();
        buttonPressLists = multipleLiftOperator.getClosestLifts(lifts, randomFloorCall);
        lifts = sut.getLifts();


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
        createNewRequest(buttonPressLists.get(counter), randomFloorCall);

        LiftObject tempObject =  liftStates.get(counter);
        if(  tempObject.getDestinationFloor() == -1){

            liftStates.get(counter).setDestinationFloor(randomFloorCall);

            if(  tempObject.getCurrentFloor()>randomFloorCall)
                liftStates.get(counter).setMovingUp(false);
            else
                liftStates.get(counter).setMovingUp(true);
        } else {
            if(tempObject.getCurrentFloor() == randomFloorCall){
                liftStates.get(counter).setDestinationFloor(randomFloorCall);
            } else{
                int currentDistance =   tempObject.getDestinationFloor() - tempObject.getCurrentFloor();
                int tempDistance =  randomFloorCall - tempObject.getCurrentFloor();
                if(tempObject.getMovingUp() ){

                    if( tempDistance>0 && tempDistance<currentDistance)
                        liftStates.get(counter).setDestinationFloor(randomFloorCall);

                } else{
                    if( tempDistance<0 && tempDistance>currentDistance)
                        liftStates.get(counter).setDestinationFloor(randomFloorCall);

                }

            }
            if( (liftStates.get(counter).getDestinationFloor() -  liftStates.get(counter).getCurrentFloor()> counter && liftStates.get(counter).getMovingUp() )||
                    (liftStates.get(counter).getDestinationFloor()< counter && !liftStates.get(counter).getMovingUp())
             )
            {

            }

        }



        liftStates.get(counter).setLiftState(LiftState.LIFT_CALL);

    }

    public boolean moveGuard() {

        lifts = sut.getLifts();
        currentLiftID = (now-idOffset) % ( numLifts +2);

        if(currentLiftID<numLifts)
        if (
                (liftStates.get(currentLiftID).getLiftState().equals(LiftState.LIFT_CALL) ||
                        (
                                (liftStates.get(currentLiftID).getLiftState().equals(LiftState.MOVING) || liftStates.get(currentLiftID).getLiftState().equals(LiftState.CLOSED))
                                        && (now - liftStates.get(currentLiftID).getTimer()>4)
                        )
                )
                && liftStates.get(currentLiftID).getDestinationFloor() != -1
                && liftStates.get(currentLiftID).getDestinationFloor() != lifts[currentLiftID].getFloor())
            return getState().equals(LiftControllerStates.SERVICING);

        return false;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void move() {
        System.out.println("in move");
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


        liftStates.get(currentLiftID).setTimer(now);
        lifts = sut.getLifts();
        Assert.assertEquals("Lift is moving", true, lifts[currentLiftID].isMoving());
        Assert.assertEquals("Lift's doors are closed", false,  lifts[currentLiftID].isOpen());
    }

    public boolean openDoorGuard() {

        currentLiftID = (now-idOffset) % (numLifts+2);

        if(currentLiftID<numLifts){
      //      System.out.println(" now " + now + " get time " + liftStates.get(currentLiftID).getTimer());
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
     /*   try {
            Thread.sleep(3000);
        } catch (Exception e) {}*/
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

    //    System.out.println("before return false");
        return false;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void closeDoor() {
        int tempDest = -1;
        System.out.println("in close");
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
        System.out.println("in finalize beh");

        if (serviceList.size() > 0) {
            modelState = LiftControllerStates.SERVICING;

        } else {
            modelState = LiftControllerStates.IDLE;
        }

      /*  try {
            Thread.sleep(300);
        } catch (Exception e) {}*/
    //    Assert.assertEquals("Expecting lift system to still be servicing requests ", true, check);

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
        System.out.println("in idle");
        Assert.assertEquals("Service lift is not empty ", 0, serviceList.size());

    }


    public @Action
    void unguardedAction() throws InterruptedException {
        System.out.println("now time " + now);
        //System.out.println("current time "+currentTimeSlot);
        TimeUnit.MILLISECONDS.sleep((getNextTimeIncrement(new Random())*100));
    }


    @Test
    public void LiftSystemModelRunner() throws FileNotFoundException {

        final TimedModel timedModel = new TimedModel(new LiftControllerModel());
        //    timedModel.setTimeoutProbability(0.5);

        final GreedyTester tester = new GreedyTester(timedModel);
        tester.setRandom(new Random(100));
        tester.setResetProbability(0.0001);
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

 /*   // if false - then there are lifts that are not in the Closed state
    boolean checkIfLiftsAreClosed() {
        check = true;
        for (int i = 0; i < liftStates.size(); i++) {

            if (!liftStates.get(i).getLiftState().equals(LiftState.CLOSED)) {
                check = false;
                break;
            }

        }
        return check;
    }*/

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

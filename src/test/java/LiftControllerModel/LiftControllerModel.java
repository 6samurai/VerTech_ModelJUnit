package LiftControllerModel;

import LiftControllerModel.LiftObject.LiftObject;
import LiftControllerModel.LiftObject.LiftState;
import LiftControllerModel.enums.LiftControllerStates;
import LiftModel_DOES_NOT_WORK.MultipleLiftOperator;
import LiftModel_DOES_NOT_WORK.ServiceList;
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
    private static final int PROBABILITY_TOTAL = 100;
    private static final int PROBABILITY_CALL_LIFT = 20;
    @Time
    public int now;
    int numFloors = 0;
    int numLifts = 0;
    private Random random = new Random();
    private Lift lift;
    private Lift[] lifts;
    private ArrayList<LiftObject> liftStates;
    private ArrayList<Lift> listOfLifts;
    private LiftController sut;
    private MultipleLiftOperator multipleLiftOperator;
    private ArrayList<ServiceList> serviceList;
    private LiftControllerStates modelState = LiftControllerStates.IDLE;
    private int liftCounter = 0;
    private int currentTimeSlot;
    private int counter;
    private boolean check;

    //Method implementations
    public LiftControllerStates getState() {
        return modelState;
    }


    public void reset(final boolean reset) {
        modelState = LiftControllerStates.IDLE;

        if (reset) {

            numFloors = random.nextInt(10) + 1;
            numLifts = random.nextInt(10) + 1;

            sut = new LiftController(numFloors, numLifts, false);

            multipleLiftOperator = new MultipleLiftOperator(numFloors);

            lifts = sut.getLifts();

            serviceList = new ArrayList<ServiceList>();
            liftStates = new ArrayList<LiftObject>();
            listOfLifts = new ArrayList<Lift>();
            liftCounter = 0;
            now = 0;
            currentTimeSlot = 0;
            counter = 0;
            check = false;
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


        if(serviceList.size()==0 && checkIfLiftsAreClosed())
        for (int i = 0; i < liftStates.size(); i++) {

            //if all lifts are closed and stationary with items still required to be serviced - lift is invalid behaviour
            if (liftStates.get(i).getLiftState().equals(LiftState.CLOSED) && serviceList.size() == 0) {
                check = true;
                break;
            }

        }


        Assert.assertEquals("Expecting lift system to still be servicing requests ", false, check);

    }


    public boolean finalizeBehaviourGuard() {
        return getState().equals(LiftControllerStates.VERIFY) && now % (numLifts+1) == numLifts;
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
        TimeUnit.MILLISECONDS.sleep((getNextTimeIncrement(new Random())) / 2);
    }




    @Test
    public void LiftSystemModelRunner() throws FileNotFoundException {

        final TimedModel timedModel = new TimedModel(new LiftControllerModel());
        //    timedModel.setTimeoutProbability(0.5);

        final GreedyTester tester = new GreedyTester(timedModel);
        tester.setRandom(new Random());
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


    boolean checkIfLiftsAreClosed(){
        check = true;
        for (int i = 0; i < liftStates.size(); i++) {

           if (!liftStates.get(i).getLiftState().equals(LiftState.CLOSED)) {
                check = false;
                break;
            }

        }
        return  check;
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

package LiftControllerModel;

import LiftControllerModel.LiftObject.LiftObject;
import LiftControllerModel.LiftObject.LiftState;
import LiftControllerModel.enums.LiftControllerStates;
import LiftModel_DOES_NOT_WORK.MultipleLiftOperator;
import LiftModel_DOES_NOT_WORK.ServiceList;
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


public class LiftControllerModel implements FsmModel {
    private Random random = new Random();
    int numFloors = 0;
    int numLifts = 0;

    private static final int PROBABILITY_TOTAL = 100;
    private static final int PROBABILITY_CALL_LIFT = 20;;

    Lift lift;
    Lift[] lifts;
    ArrayList<LiftObject> liftObjects;
    ArrayList<Lift> listOfLifts;
    LiftController sut;
    MultipleLiftOperator multipleLiftOperator ;
    ArrayList<ServiceList> serviceList;
    private LiftControllerStates modelState = LiftControllerStates.IDLE;

    //Method implementations
    public LiftControllerStates getState() {
        return modelState;
    }

    public void reset(final boolean reset) {
        modelState = LiftControllerStates.IDLE;

        if (reset) {

            numFloors = random.nextInt(10)+1;
            numLifts= random.nextInt(10)+1;

            sut = new LiftController(numFloors, numLifts, false);

            multipleLiftOperator = new MultipleLiftOperator(numFloors);

            lifts = sut.getLifts();

            serviceList = new ArrayList<ServiceList>();
            liftObjects = new ArrayList<LiftObject>();
            listOfLifts = new ArrayList<Lift>();

            for(int i =0; i <numLifts;i++){
                liftObjects.add(new LiftObject());
            }

        }

    }


    public boolean getButtonPressGuard() {
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
        liftObjects.get(lift.getId()).setLiftState(LiftState.BUTTON_PRESS);

    }



    public boolean systemActionGuard() {

        return  getState().equals(LiftControllerStates.SERVICING) && serviceList.size()!=0;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void systemAction() {


        for(int i = 0; i < liftObjects.size();i++){
            if(!liftObjects.get(i).getLiftState().equals(LiftState.CLOSED)){

             /*   if(ThreadLocalRandom.current().nextInt(PROBABILITY_TOTAL) < PROBABILITY_CALL_LIFT){

                }*/

             if(liftObjects.get(i).getLiftState().equals(LiftState.BUTTON_PRESS)){

             } else if(liftObjects.get(i).getLiftState().equals(LiftState.OPEN)){



             }else if(liftObjects.get(i).getLiftState().equals(LiftState.MOVING)){

             }
            }

        }


        Assert.assertEquals("Service lift is not empty " ,0, serviceList.size());

    }


    public boolean idleGuard() {
        boolean   checkEmpty = true;
        for(int i =0;i< liftObjects.size();i++){
            if(!liftObjects.get(i).getLiftState().equals(LiftState.CLOSED)){
                checkEmpty = false;
                break;
            }
        }

        return  getState().equals(LiftControllerStates.SERVICING) && serviceList.size()==0 && checkEmpty;
    }

    //idle to servicing states through callLiftToFloor
    public @Action
    void idle() {
        modelState = LiftControllerStates.IDLE;
        Assert.assertEquals("Service lift is not empty " ,0, serviceList.size());

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
        MultipleLiftsModel_OLD_VERSION.MultipleLiftsModelTest myModel = new MultipleLiftsModel_OLD_VERSION.MultipleLiftsModelTest();

        final Tester tester = new GreedyTester(myModel);
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

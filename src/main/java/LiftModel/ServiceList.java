package LiftModel;

import com.liftmania.Lift;

public class ServiceList {
    Lift lift;
    int floor;

    public ServiceList(Lift lift, int floor){
        this.lift = lift;
        this.floor = floor;
    }

    public Lift getLift(){
        return  lift;
    }

    public  int getFloor(){
        return  floor;
    }
}

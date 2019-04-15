package com.liftmania;

import java.util.ArrayList;

import com.liftmania.gui.AnimationCommand;
import com.liftmania.gui.LiftsVisualiser;

public class LiftController {

	int numFloors;
	int numLifts;

	Lift[] lifts;

	LiftsVisualiser visualiser;

	public LiftController(int numFloors, int numLifts, boolean randomizePositons) {

		this.numFloors = numFloors;
		this.numLifts = numLifts;

		lifts = new Lift[numLifts];
		for (int i = 0; i < numLifts; i++) {
			lifts[i] = new Lift(i);
		}

		// Start visualiser
		visualiser = new LiftsVisualiser(this, numFloors, numLifts);

		if (randomizePositons) {
			for (int i=0; i < numLifts; i++) {
				moveLift(lifts[i], (int)(Math.random() * (numFloors)));
			}
		}
	}

	public Lift[] getLifts() {
		return lifts;
	}

	/**
	 * Move a lift to a floor.
	 *
	 * @param liftNumber
	 *            - The lift number (1-based)
	 * @param floorNumber
	 *            - The floor to move to (0-based).
	 */
	public void moveLift(int liftNumber, int floorNumber) {
		moveLift(lifts[liftNumber],  floorNumber);
	}

	public void moveLift(Lift lift, int floorNumber) {
		boolean moveUp = true;

		if(lifts[lift.id].floor > floorNumber){
			moveUp = false;
		}

		lifts[lift.id].setIsMovingUp(moveUp);
        lifts[lift.id].setMoving(true);
		//The animation process is trusted with updating the state of the lift (floorNumber, moving, etc)
		visualiser.animateLiftMovement(lift, floorNumber);
	}




	/**
	 * Closes the door of a particular lift.
	 *
	 * @param liftNumber
	 *            - The lift number (0-based)
	 */
	public void closeLiftDoor(int liftNumber, int floor) {
		Lift lift = lifts[liftNumber];
		lift.closeDoors();
		visualiser.animateLiftClose(lift, floor);
/*		visualiser.addAnimationCommand(new AnimationCommand(
				AnimationCommand.Command.close, liftNumber, lift.getFloor(), -1));
	*/
	}

	/**
	 * Open the door of a particular lift.
	 *
	 * @param liftNumber
	 *            - The lift number (0-based)
	 */
	public void openLiftDoor(int liftNumber, int floor) {
		Lift lift = lifts[liftNumber];
		lift.openDoors();
		lift.setMoving(false);
		visualiser.animateLiftOpen(lift, floor);
		/*visualiser.addAnimationCommand(new AnimationCommand(
				AnimationCommand.Command.open, liftNumber, lift.getFloor(), -1));*/
	}

	/**
	 * Calls a lift to a particular floor.
	 * @param floor - The floor number (0-based).
	 */

	public void callLiftToFloor(int floor) {

		//Find lifts closest to the required floor
		ArrayList<Lift> closestLifts = getClosestLifts(floor);


		if (closestLifts.size() == 0) {
			throw new RuntimeException("Could not find an available lift. input floor"+  floor+" " + lifts[0].floor +" " + lifts[0].doorsOpen +" " +  lifts[0].moveUp + " " + lifts[0].moving);
		}

		//Pick random lift
		Lift lift = closestLifts.get((int)(Math.random() * (closestLifts.size())));

		moveLift(lift, floor);
		openLiftDoor(lift.id,floor);
		closeLiftDoor(lift.id,floor);
	}



	public ArrayList<Lift>getClosestLifts(int floor){
		ArrayList<Lift> result = new ArrayList<Lift>();

		int distance = -1;
		while (result.size() == 0 && distance<numFloors) {
			distance++;

			for (Lift lift : lifts) {
				if(lift.isMoving()){
					if ( (lift.distanceFromFloor(floor) == distance && !lift.getIsMovingUp() ) ||
							(lift.distanceFromFloor(floor) == -distance && lift.getIsMovingUp())) {
						result.add(lift);
					}
				} else{
					if (Math.abs(lift.distanceFromFloor(floor)) == distance ) {
						result.add(lift);
					}
				}
			}
		}

		return result;

	}



	public static void main(String[] args) throws Exception {
		new LiftController(6, 3, false);
		//	new LiftController(6, 3, true);
		//	new LiftController(6, 1, false);
	}

}

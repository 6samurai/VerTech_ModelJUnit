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
		boolean direction = true;
		if(lift.getFloor()>floorNumber){
			direction = false;
		}
		lifts[lift.id].setMoveDirection(direction);

		//The animation process is trusted with updating the state of the lift (floorNumber, moving, etc)
		visualiser.animateLiftMovement(lift, floorNumber);
	}


	/**
	 * Closes the door of a particular lift.
	 *
	 * @param liftNumber
	 *            - The lift number (0-based)
	 */
	public void closeLiftDoor(int liftNumber) {
		Lift lift = lifts[liftNumber];
		lift.closeDoors();

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
	public void openLiftDoor(int liftNumber) {
		Lift lift = lifts[liftNumber];
		lift.openDoors();
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
			throw new RuntimeException("Could not find an available lift.");
		}

		//Pick random lift
		Lift lift = closestLifts.get((int)(Math.random() * (closestLifts.size())));

		moveLift(lift, floor);

	}

	public ArrayList<Lift> getClosestLifts(int floor){
		ArrayList<Lift> closestLifts = getClosestStationaryLifts(floor);
		if(closestLifts.size()>0)
			return closestLifts;
		else{
			return (getClosestMovingLifts(floor));
		}
	}

	public ArrayList<Lift> getClosestMovingLifts(int floor){
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
				}
			}
		}

		return result;

	}

	public ArrayList<Lift> getClosestStationaryLifts(int floor) {

		ArrayList<Lift> result = new ArrayList<Lift>();

		int distance = -1;
		while (result.size() == 0 && distance<numFloors) {
			distance++;
			for (Lift lift : lifts) {
				if (Math.abs(lift.distanceFromFloor(floor)) == distance && !lift.isMoving()) {
					result.add(lift);
				}
			}
		}

		return result;
	}

	public static void main(String[] args) throws Exception {
		//	new LiftController(6, 3, true);
		new LiftController(6, 1, true);
	}

}

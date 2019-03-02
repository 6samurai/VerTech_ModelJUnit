package com.liftmania.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.util.ArrayList;

import com.liftmania.Lift;

@SuppressWarnings("serial")
public class Shaft extends JPanel implements Runnable {

	final Color COLOR_DOORS_OPEN = Color.RED;
	final Color COLOR_DOORS_CLOSED = Color.GREEN;
	final Color COLOR_SHAFT = Color.LIGHT_GRAY;

	LiftsVisualiser visualiser;

	public int numFloors;

	JPanel[] grid;
	Color liftColor;
	Lift lift;

	int animationStepsPerFloor = 10;
	long animationPause = 50;
	int animationStepsPerShaft;

	//Queue<AnimationCommand> animationCommands = new LinkedList<AnimationCommand>();
	ArrayList<AnimationCommand> animationCommands = new ArrayList<AnimationCommand>();
	/**
	 * Constructs a Shaft object which houses a number of floors and lifts.
	 *
	 * @param numFloors
	 *            - The number of floors
	 * @param numLifts
	 *            - The number of lifts
	 */
	public Shaft(LiftsVisualiser visualiser, int numFloors, Lift lift) {
		this.visualiser = visualiser;
		this.numFloors = numFloors;
		this.lift = lift;

		animationStepsPerShaft = numFloors * animationStepsPerFloor;
		grid = new JPanel[animationStepsPerShaft];

		liftColor = COLOR_DOORS_CLOSED;

		init();
	}

	/**
	 * Initialises the UI
	 */
	protected void init() {

		setLayout(new BorderLayout());

		JPanel liftShaft = new JPanel();
		liftShaft.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		liftShaft.setLayout(new GridLayout(animationStepsPerShaft, 1));

		add(liftShaft, BorderLayout.CENTER);

		// Lay out lift shaft
		for (int i = animationStepsPerShaft - 1; i >= 0; i--) {
			JPanel p = new JPanel();
			grid[i] = p;
			liftShaft.add(p);

			if (i < animationStepsPerFloor) {
				p.setBackground(liftColor);
			} else {
				p.setBackground(COLOR_SHAFT);
			}
		}

		// Create top panel
		add(new JLabel("Lift " + lift.getId(), JLabel.CENTER),
				BorderLayout.NORTH);

		// Create floor buttons
		JPanel buttonsPanel = new JPanel(new GridLayout(1, numFloors));
		for (int j = 0; j < numFloors; j++) {
			JButton btn = new JButton(Integer.toString(j));
			btn.setActionCommand("move" + "," + Integer.toString(lift.getId()) + ","
					+ Integer.toString(j));
			btn.addActionListener(visualiser);
			buttonsPanel.add(btn);
		}

		add(buttonsPanel, BorderLayout.SOUTH);

	}

	/**
	 * Places a lift at a particular floor
	 *
	 * @param lift
	 *            - The lift number (0-based)
	 * @param floor
	 *            - The floor number (0-based)
	 */

	public void setLiftFloor(int floor) {

		for (int i = 0; i < animationStepsPerShaft; i++) {
			if (isAnimationStepOnFloor(i, floor)) {
				grid[i].setBackground(liftColor);
			} else {
				grid[i].setBackground(COLOR_SHAFT);
			}
		}

	}

	public void setLiftFloor(int floor, int offset) {

		for (int i = 0; i < animationStepsPerShaft; i++) {
			if (isAnimationStepOnFloor(i + offset, floor)) {
				grid[i].setBackground(liftColor);
			} else {
				grid[i].setBackground(COLOR_SHAFT);
			}
		}

	}

	protected boolean isAnimationStepOnFloor(int step, int floor) {

		int lower = floor * animationStepsPerFloor;
		int upper = lower + animationStepsPerFloor - 1;

		return (step >= lower) && (step <= upper);
	}

	/**
	 * Animates a lift moving to a particular floor
	 *
	 * @param lift
	 *            - The lift (zero-based)
	 * @param fromFloor
	 *            - The floor to start animation from (zero-based)
	 * @param toFloor
	 *            - The floor to animate to (zero-based)
	 */

	public void animateMovement(int toFloor){
		int newFloor = 0;
		//Update lift state
		lift.setMoving(true);

		int fromFloor = lift.getFloor();
		setLiftFloor(fromFloor);
		lift.setMoving(true);

		if (toFloor > fromFloor) {

			for (int i = fromFloor; i < toFloor; i++) {
				serviceOtherRequest(lift);
				animateUp(i);
				newFloor = i + 1;
				lift.setFloor(newFloor);
			}

		} else {

			for (int i = fromFloor; i > toFloor; i--) {
				serviceOtherRequest(lift);
				animateDown(i);
				newFloor = i - 1;
				lift.setFloor(newFloor);
			}

		}

		//lift.setFloor(toFloor);
	}
	public void animateLift(int toFloor) {

		animateMovement(toFloor);
		//Open and close doors
		openDoors(toFloor);
		closeDoors(toFloor);

		//Update lift state



	}

	public void animateUp(int currentFloor) {

		int lower = currentFloor * animationStepsPerFloor;
		int upper = lower + animationStepsPerFloor - 1;

		for (int i = 0; i < animationStepsPerFloor; i++) {
			animationPause();
			lower++;
			upper++;

			highlightGrid(lower, upper);
			invalidate();
			validate();
		}

	}

	public void animateDown(int currentFloor) {

		int lower = currentFloor * animationStepsPerFloor;
		int upper = lower + animationStepsPerFloor - 1;

		for (int i = 0; i < animationStepsPerFloor; i++) {
			animationPause();
			lower--;
			upper--;

			highlightGrid(lower, upper);
		}

	}

	public void highlightGrid(int lower, int upper) {

		for (int i = 0; i < animationStepsPerShaft; i++) {
			if ((i >= lower) && (i <= upper)) {
				grid[i].setBackground(liftColor);
			} else {
				grid[i].setBackground(COLOR_SHAFT);
			}
		}

	}

	protected void animationPause() {
		try {
			Thread.sleep(animationPause);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets a particular lift to open.
	 *
	 * @param lift
	 *            - The lift number (zero-based)
	 */
	public void openDoors(int floor) {
		liftColor = COLOR_DOORS_OPEN;
		setLiftFloor(lift.getFloor());
		visualiser.controller.openLiftDoor(lift.getId(),floor);
		lift.setMoving(false);
		try {
			Thread.sleep(3000);
		} catch (Exception e) {}
	}


	/**
	 * Sets a particular lift to closed.
	 *
	 * @param lift
	 *            - The lift number (zero-based)
	 */
	public void closeDoors(int floor) {
		liftColor = COLOR_DOORS_CLOSED;
		setLiftFloor(lift.getFloor());
		visualiser.controller.closeLiftDoor(lift.getId(), floor);
		lift.setMoving(false);
		try {
			Thread.sleep(500);
		} catch (Exception e) {}
	}

	/**
	 * Adds an animation command to the queue for processing by the animation loop
	 * @param cmd - The command object.
	 */
	public void addAnimationCommand(AnimationCommand cmd) {
		ArrayList<AnimationCommand> listOfCmds = animationCommands;
		boolean cmdPresent = false;

		if(!listOfCmds.isEmpty())
			for(int i =0; i<listOfCmds.size();i++){
				if(listOfCmds.get(i).command== cmd.command && listOfCmds.get(i).toFloor== cmd.toFloor){
					cmdPresent = true;
					break;
				}
			}

		if(!cmdPresent)
			animationCommands.add(cmd);
	}

	public void serviceOtherRequest(Lift lift){
		if(!animationCommands.isEmpty())
			//	if(animationCommands.contains(lift)){
			for(int i = 0; i<animationCommands.size();i++){
				if(animationCommands.get(i).toFloor ==lift.getFloor()){
					lift.setMoving(false);
					//Open and close doors
					openDoors(lift.getFloor());
					try {
						Thread.sleep(3000);
					} catch (Exception e) {}
					closeDoors(lift.getFloor());

					//Update lift state
					lift.setMoving(true);
					animationCommands.remove(i);

					break;
				}
			}


	}

	/**
	 * The animation loop is placed in a separate thread for smoothness purposes.
	 */

	@Override
	public void run() {

		while (true) {

			if (animationCommands.size() > 0) {
				AnimationCommand cmd = animationCommands.get(0);

				if (cmd.command == AnimationCommand.Command.move) {
				//	animateLift(cmd.toFloor);
					animateMovement(cmd.toFloor);
				} else if (cmd.command == AnimationCommand.Command.close) {
					closeDoors(cmd.toFloor);
				//	setLiftFloor(cmd.toFloor);
				//	closeDoors();
				} else if (cmd.command == AnimationCommand.Command.open) {
					//openDoors();
					openDoors(cmd.toFloor);
					//setLiftFloor(cmd.fromFloor);
				}
				animationCommands.remove(0);
			}

			try {
				Thread.sleep(250);
			} catch (Exception e) {}


		}

	}

}

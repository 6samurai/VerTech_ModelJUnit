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

	Queue<AnimationCommand> animationCommands = new LinkedList<AnimationCommand>();
	
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
	public void animateLift(int toFloor) {

		//Update lift state
		lift.setMoving(true);
		
		int fromFloor = lift.getFloor();
		setLiftFloor(fromFloor);
		lift.setMoving(true);

		if (toFloor > fromFloor) {

			for (int i = fromFloor; i < toFloor; i++) {
				animateUp(i);
				lift.setFloor(i);
			}

		} else {

			for (int i = fromFloor; i > toFloor; i--) {
				animateDown(i);
				lift.setFloor(i);
			}

		}
		
		lift.setFloor(toFloor);
		
		//Open and close doorts
		openDoors();
		try {
			Thread.sleep(2000);
		} catch (Exception e) {}
		closeDoors();
		
		//Update lift state
		lift.setMoving(false);
		
		
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
	public void openDoors() {
		liftColor = COLOR_DOORS_OPEN;
		setLiftFloor(lift.getFloor());
	}

	/**
	 * Sets a particular lift to closed.
	 * 
	 * @param lift
	 *            - The lift number (zero-based)
	 */
	public void closeDoors() {
		liftColor = COLOR_DOORS_CLOSED;
		setLiftFloor(lift.getFloor());
	}
	
	/**
	 * Adds an animation command to the queue for processing by the animation loop
	 * @param cmd - The command object.
	 */
	public void addAnimationCommand(AnimationCommand cmd) {
		animationCommands.add(cmd);
	}
	

	/**
	 * The animation loop is placed in a separate thread for smoothness purposes.
	 */
	
	@Override
	public void run() {
		
		while (true) {
			
			if (animationCommands.size() > 0) {
				AnimationCommand cmd = animationCommands.remove();
				
				if (cmd.command == AnimationCommand.Command.move) {
					animateLift(cmd.toFloor);
				} else if (cmd.command == AnimationCommand.Command.close) {
					//closeDoors(cmd.liftNumber);
					setLiftFloor(cmd.toFloor);
				} else if (cmd.command == AnimationCommand.Command.open) {
					openDoors();
					//setLiftFloor(cmd.fromFloor);
				}
			}
			
			try {
				Thread.sleep(250);
			} catch (Exception e) {}
			
			
		}
		
	}

}

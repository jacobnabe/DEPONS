/*
 * Copyright (C) 2017-2019 Jacob Nabe-Nielsen <jnn@bios.au.dk>
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License version 2 and only version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, see 
 * <https://www.gnu.org/licenses>.
 * 
 * Linking DEPONS statically or dynamically with other modules is making a combined work based on DEPONS. 
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 * 
 * In addition, as a special exception, the copyright holders of DEPONS give you permission to combine DEPONS 
 * with free software programs or libraries that are released under the GNU LGPL and with code included in the 
 * standard release of Repast Simphony under the Repast Suite License (or modified versions of such code, with unchanged license). 
 * You may copy and distribute such a system following the terms of the GNU GPL for DEPONS and the licenses of the 
 * other code concerned.
 * 
 * Note that people who make modified versions of DEPONS are not obligated to grant this special exception for 
 * their modified versions; it is their choice whether to do so. 
 * The GNU General Public License gives permission to release a modified version without this exception; 
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */

package dk.au.bios.porpoise;

import java.util.LinkedList;
import java.util.List;

import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.relogo.Utility;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

/**
 * Base class for agents.
 */
public class Agent {

	private final long id;
	private final ContinuousSpace<Agent> space;  // FIXME Global object, should not be an instance variable
	private final Grid<Agent> grid;  // FIXME Global object, should not be an instance variable

	private double heading = Globals.getRandomReplaySource() != null ? SimulationParameters.isHomogenous() ? 134 : 134
			: Globals.getRandomSource() != null ? Globals.getRandomSource().randomInt(0, 360) : 0.0d; // 260 is the initial value in NetLogo replays random scenario.

	protected Agent(final ContinuousSpace<Agent> space, final Grid<Agent> grid, final long id) {
		this.space = space;
		this.grid = grid;
		this.id = id;
	}

	/**
	 * Returns the space position of the agent.
	 *
	 * @return The position of the agent.
	 */
	public NdPoint getPosition() {
		return space.getLocation(this);
	}

	/**
	 * Set the position of the agent.
	 *
	 * @param newPos the new position for the agent.
	 */
	public void setPosition(final NdPoint newPos) {
		space.moveTo(this, newPos.getX(), newPos.getY());

		final GridPoint p = ndPointToGridPoint(newPos);
		grid.moveTo(this, p.getX(), p.getY());
	}

	/**
	 * Changes the NdPoint coordinates to GridPoint coordinates. This function ensures that we always apply the same
	 * rounding when converting from space to grid.
	 *
	 * @param point The point to returns the grid point coordinates of.
	 * @return The GridPoint coordinates for the passed point.
	 */
	public static GridPoint ndPointToGridPoint(final NdPoint point) {
		int x = (int) Math.round(point.getX());
		int y = (int) Math.round(point.getY());

		if (y == Globals.getWorldHeight()) {
			y--;
		}

		if (x == Globals.getWorldWidth()) {
			x--;
		}

		return new GridPoint(x, y);
	}

	/**
	 * Returns the neighboring cells to the current agents current cell.
	 *
	 * @return A list of the neighboring cells.
	 */
	protected List<GridPoint> getNeighbors() {
		final GridPoint currentLocation = grid.getLocation(this);

		// Look one cell in each direction, this is similar to the NetLogo neighborhood
		final GridCellNgh<Object> nghCreator = new GridCellNgh<Object>(grid, currentLocation, Object.class, 1, 1);
		final List<GridCell<Object>> cells = nghCreator.getNeighborhood(false);

		final List<GridPoint> list = new LinkedList<GridPoint>();

		for (final GridCell<Object> g : cells) {
			list.add(g.getPoint());
		}

		SimUtilities.shuffle(list, RandomHelper.getUniform());

		return list;
	}

	/**
	 * Set the agents heading to point in the direction of the passed point.
	 *
	 * @param point The point to turn the agent towards.
	 */
	public void facePoint(final NdPoint point) {
		if (!point.equals(getPosition())) {
			final double[] displacement = space.getDisplacement(getPosition(), point);
			setHeading(normHeading(Utility.angleFromDisplacement(displacement[0], displacement[1])));
		}
		// else - We cannot face the current point, do nothing (NetLogo compatible)
	}

	/**
	 * Calculates the distance from this agent to the passed point.
	 *
	 * @param ndPoint
	 */
	public double distanceXY(final NdPoint ndPoint) {
		return space.getDistance(getPosition(), ndPoint);
	}

	/**
	 * Moves the agent forward
	 *
	 * @param distance The distance to move forward.
	 */
	public void forward(final double distance) {
		final double[] anglesForMoveByVector = { (Math.PI / 2) - getHeadingInRads(), 0.0 };

		moveByVector(distance, anglesForMoveByVector);
	}
	
	public NdPoint moveByVector(double distance, double... anglesInRadians) {
		final NdPoint newPos = getSpace().moveByVector(this, distance, anglesInRadians);

		final GridPoint p = ndPointToGridPoint(newPos);
		getGrid().moveTo(this, p.getX(), p.getY());
		
		return newPos;
	}	


	/**
	 * Gets the heading of the agent. The heading is a degree in the range (-180;180].
	 *
	 * @param ndPoint
	 */
	public final double getHeading() {
		return this.heading;
	}

	/**
	 * Sets the heading of the agent. The heading must be in the range (-180;180].
	 *
	 * @param heading The new heading of the agent.
	 */
	public final void setHeading(final double heading) {
//		assert heading > 360 : "Illegal heading " + heading;
//		assert heading < 0 : "Illegal heading " + heading;

		this.heading = heading;
	}

	/**
	 * Normalizes an angle to be in the range [0;360). 360 would for instance become 0.
	 *
	 * @param angle The angle to normalize
	 * @return normalized angle
	 */
	public final double normHeading(final double angleIn) {
		double angleNormalized = angleIn;
		while (angleNormalized < 0) {
			angleNormalized += 360;
		}

		while (angleNormalized >= 360) {
			angleNormalized -= 360;
		}

		return angleNormalized;
	}

	/**
	 * Increases the heading with the passed degree. This equals turning the agent to the right.
	 *
	 * @param inc The increase in the agents heading.
	 */
	public void incHeading(final double inc) {
		setHeading(normHeading(getHeading() + inc));
	}

	/**
	 * Returns the heading of the agent in radians. The heading will be in the range (-pi;pi].
	 *
	 * @return The heading in radians.
	 */
	protected double getHeadingInRads() {
		return getHeadingInRads(this.heading);
	}

	/**
	 * Converts the passed angle to radians. Will not normalize the angle, hence 360 will become 2pi and not 0.
	 *
	 * @param angle The angle to convert to radians.
	 * @return The angle in radians.
	 */
	protected double getHeadingInRads(final double angle) {
		return (angle * Math.PI / 180.0);
	}

	/**
	 * Gets the id of the agent.
	 *
	 * @return The id of the agent.
	 */
	public long getId() {
		return this.id;
	}

	public ContinuousSpace<Agent> getSpace() {
		return space;
	}

	public Grid<Agent> getGrid() {
		return grid;
	}

}

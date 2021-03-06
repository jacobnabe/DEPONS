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

import dk.au.bios.porpoise.util.SimulationTime;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;

/**
 * A ship agent. This is used in the Kattegat simulation and is not relevant for the current DEPONS model.
 */
public class Ship extends SoundSource {

	private final NdPoint[] route;
	private int nextPoint;
	private boolean forward = true;

	private final dk.au.bios.porpoise.ships.Ship data;
	private final NdPoint surveyAreaPoint;
	
	public Ship(final ContinuousSpace<Agent> space, final Grid<Agent> grid, final NdPoint[] route, final dk.au.bios.porpoise.ships.Ship data) {
		super(space, grid, data.getImpact());
		this.route = route;
		this.data = data;
		this.forward = false;
		this.nextPoint = -1;
		
		if (data.getSurvey() != null && data.getSurvey().getPoint() != null) {
			this.surveyAreaPoint = new NdPoint(data.getSurvey().getPoint().getX(), data.getSurvey().getPoint().getY());
		} else {
			this.surveyAreaPoint = null;
		}
	}

	public void initialize() {
//		final int pastLoc = Globals.getRandomSource().pastLoc(name, route.length);
		final int pastLoc = 0;

		if (pastLoc == route.length - 1) {
			// we are at the end and will move backward
			this.forward = false;
			this.nextPoint = pastLoc - 1;
		} else {
			this.forward = true;
			this.nextPoint = pastLoc + 1;

		}

		this.setPosition(route[pastLoc]);
		facePoint(route[nextPoint]);
	}

	@ScheduledMethod(start = 0, interval = 1, priority = AgentPriority.SHIP_MOVE)
	public void move() {
		if (SimulationTime.getTick() < data.getStart()) {
			return;
		}

		final double moveLength = 2.5 * this.data.getSpeed() / 2; // km / t to steps / 30 min

		// approaching next location on route?
		if (distanceXY(this.route[nextPoint]) <= moveLength) {
			// check wheter position numbers should increase, or if ships should turn around:
			if (forward) {
				this.nextPoint++;
				if (this.nextPoint >= this.route.length) {
					this.nextPoint -= 2;
					forward = false;
				}
			} else {
				this.nextPoint--;
				if (this.nextPoint < 0) {
					this.nextPoint += 2;
					forward = true;
				}
			}

			facePoint(this.route[this.nextPoint]);
		}

		forward(moveLength);
	}

	public double getSpeed() {
		return this.data.getSpeed();
	}

	public String getName() {
		return data.getName();
	}

	@Override
	public String toString() {
		return data.getName();
	}

	public boolean isInSurveyArea() {
		if (this.surveyAreaPoint != null) {
			if (this.getSpace().getDistance(getPosition(), this.surveyAreaPoint) < this.data.getSurvey().getRadius()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public double getImpact() {
		if (isInSurveyArea()) {
			return this.data.getSurvey().getImpact();
		}

		return this.impact;
	}

	
}

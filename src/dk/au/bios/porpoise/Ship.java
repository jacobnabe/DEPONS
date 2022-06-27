/*
 * Copyright (C) 2017-2022 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import java.util.Iterator;
import java.util.Set;

import dk.au.bios.porpoise.ships.JomopansEchoSPL;
import dk.au.bios.porpoise.ships.Route;
import dk.au.bios.porpoise.ships.VesselClass;
import dk.au.bios.porpoise.util.DebugLog;
import dk.au.bios.porpoise.util.SimulationTime;
import repast.simphony.engine.schedule.ScheduledMethod;

/**
 * A ship agent. This is used in the Kattegat simulation and is not relevant for the current DEPONS model.
 */
public class Ship extends SoundSource implements dk.au.bios.porpoise.ships.Ship {

	private static final int JOMOPANS_BAND = 12;

	private String name;
	private VesselClass type;
	private double length;
	private Route route;
	private int tickStart = -1;
	private int tickEnd = Integer.MAX_VALUE;

	private int currentBuoy = -1;
	private int ticksStillPaused = 0;

	public Ship() {
		super();
	}

	public Ship(String name, VesselClass type, double length, Route route) {
		this(name, type, length, route, -1, Integer.MAX_VALUE);
	}

	public Ship(String name, VesselClass type, double length, Route route, int tickStart, int tickEnd) {
		super();
		this.name = name;
		this.type = type;
		this.length = length;
		this.route = route;
		this.tickStart = tickStart;
		this.tickEnd = tickEnd;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTickStart() {
		return tickStart;
	}

	public void setTickStart(int start) {
		this.tickStart = start;
	}

	public int getTickEnd() {
		return tickEnd;
	}

	public void setTickEnd(int end) {
		this.tickEnd = end;
	}

	public VesselClass getType() {
		return type;
	}

	public void setType(VesselClass type) {
		this.type = type;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public void initialize() {
		this.setPosition(route.getRoute().get(0).getNdPoint());

		if (route.getRoute().size() > 1) {
			facePoint(route.getRoute().get(1).getNdPoint());
		} else {
			facePoint(route.getRoute().get(0).getNdPoint());			
		}
	}

	@ScheduledMethod(start = 0, interval = 1, priority = AgentPriority.SHIP_MOVE)
	public void move() {
		if (SimulationTime.getTick() < tickStart) {
			return;
		}
		if (SimulationTime.getTick() > tickEnd) {
			currentBuoy = -1;
			return;
		}

		if (ticksStillPaused > 0) {
			ticksStillPaused--;
		} else {
			currentBuoy++;
			if (currentBuoy >= route.getRoute().size()) {
				currentBuoy = 0;  // repeat the route from the beginning
			}

			var buoy = route.getRoute().get(currentBuoy);
			setPosition(buoy.getNdPoint());
				
			if (buoy.getPause() > 0) {
				ticksStillPaused = buoy.getPause();
			}
		}
	}

	@Override
	public void deterPorpoise() {
		if (currentBuoy < 0) {
			return;
		}
		if (ticksStillPaused > 0) {
			return;
		}

		Set<Porpoise> porps = Globals.getSpatialPartitioning().getPorpoisesInNeighborhood(this.getPosition());
		Iterator<Porpoise> iter = porps.iterator();
		JomopansEchoSPL splCalc = new JomopansEchoSPL();
		while (iter.hasNext()) {
			final Porpoise p = iter.next();

			final double distToShip = this.getSpace().getDistance(getPosition(), p.getPosition()) * 400;

			if (distToShip <= SimulationParameters.getDeterMaxDistance()) {
				final double sourceLevel = splCalc.calculate(distToShip, type, getSpeed(), length, JOMOPANS_BAND);

				final double currentDeterence = (sourceLevel /  SimulationParameters.getDeterResponseThresholdShips()) - SimulationParameters.getDeterrenceCoeffShips();

				if (currentDeterence > 0) {
					p.deter(currentDeterence, this);
				}

				if (DebugLog.isEnabledFor(8)) {
					DebugLog.print8("who: {} dist-to-ship {}: {}", p.getId(), this, distToShip);
				}
			}
		}
	}

	protected double getSpeed() {
		return this.route.getRoute().get(currentBuoy).getSpeed();
	}

	@Override
	public String toString() {
		return getName();
	}

}

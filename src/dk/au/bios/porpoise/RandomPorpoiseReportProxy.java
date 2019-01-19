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

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import dk.au.bios.porpoise.util.DebugLog;
import dk.au.bios.porpoise.util.SimulationTime;
import dk.au.bios.porpoise.util.test.PorpoiseTestDataCapturer;

/**
 * Used to report on a porpoise in the simulation.
 */
public class RandomPorpoiseReportProxy extends Agent {

	private final Porpoise porpoise;

	public RandomPorpoiseReportProxy(final ContinuousSpace<Agent> space, final Grid<Agent> grid, final long id,
			final Porpoise trackedPorpoise) {
		super(space, grid, id);

		this.porpoise = trackedPorpoise;
	}

	@Override
	public long getId() {
		return this.porpoise.getId();
	}

	public Porpoise getPorpoise() {
		return porpoise;
	}

	public int getUtmX() {
		if (porpoise.isAlive()) {
			return porpoise.getUtmX();
		} else {
			return -1;
		}
	}

	public int getUtmY() {
		if (porpoise.isAlive()) {
			return porpoise.getUtmY();
		} else {
			return -1;
		}
	}

	public double getEnergyLevel() {
		if (porpoise.isAlive()) {
			return porpoise.getEnergyLevel();
		} else {
			return -1;
		}
	}

	public double getDeterStrength() {
		if (porpoise.isAlive()) {
			return porpoise.getDeterStrength();
		} else {
			return -1;
		}
	}

	public int getPSMTargetUtmX() {
		if (porpoise.getDispersalBehaviour() == null || !porpoise.getDispersalBehaviour().isDispersing()) {
			return -1;
		}

		return (int) Math.round(porpoise.getDispersalBehaviour().getTargetPosition().getX() * 400
				+ Globals.getXllCorner());
	}

	public int getPSMTargetUtmY() {
		if (porpoise.getDispersalBehaviour() == null || !porpoise.getDispersalBehaviour().isDispersing()) {
			return -1;
		}

		return (int) Math.round(porpoise.getDispersalBehaviour().getTargetPosition().getY() * 400
				+ Globals.getYllCorner());
	}

	public int getDispersalMode() {
		return porpoise.getDispersalMode();
	}

	public boolean isPSMActive() {
		if (porpoise.getDispersalBehaviour() == null) {
			return false;
		}

		return porpoise.getDispersalBehaviour().isDispersing();
	}

	@ScheduledMethod(start = 0, interval = 1, priority = 100)
	public void updatePosition() {
		if (porpoise.isAlive()) {
			this.setPosition(porpoise.getPosition());
			this.setHeading(porpoise.getHeading());

			if (DebugLog.isEnabledFor(10)) {
				DebugLog.print10(porpoise, p -> SimulationTime.getTick() < 10,
						"Porpoise {} @{} ({},{}), presLovMov: {}", porpoise.getId(), porpoise.getPosition(),
						porpoise.getUtmX(), porpoise.getUtmY(), porpoise.getPresLogMov());
			}
			PorpoiseTestDataCapturer.capture(this.getPorpoise());
		}
	}

}

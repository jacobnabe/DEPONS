/*
 * Copyright (C) 2017-2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

package dk.au.bios.porpoise.util;

import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.SimulationConstants;
import repast.simphony.engine.environment.RunEnvironment;

public final class SimulationTime {

	private SimulationTime() {
		// Utility class, prevent instances.
	}

	public static double getTick() {
		return RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}

	public static int getDayOfSimulation() {
		return (int) (getTick() / 48);
	}

	public static int getDayOfYear() {
		return ((int) (getTick() / 48)) % 360;
	}

	public static int getYearOfSimulation() {
		return (int) (getTick() / (360 * 48));
	}

	/**
	 * @return The month of the year, 1 indexed, 1 = January
	 */
	public static int getMonthOfYear() {
		return getMonthOfYear(getTick());
	}

	/***
	 *
	 * @param netLogoMonth Use NetLogos month implementataion (shifted 0 ticks from Repast)
	 * @return
	 */
	public static int getMonthOfYearWithOffset() {
		if (SimulationConstants.OFFSET_MONTH) {
			return getMonthOfYear(getTick() - 1);
		} else {
			return getMonthOfYear();
		}
	}

	public static int getMonthOfYear(final double tick) {
		return (int) ((tick / (30 * 48)) % 12) + 1;
	}

	public static int getQuarterOfYear() {
		return getQuarterOfYear(getTick());
	}

	public static int getQuarterOfYear(final double step) {
		double effectiveStep = step;
		if (SimulationConstants.SHIFT_QUARTER) {
			effectiveStep += 30 * 48;
		}

		return (int) ((effectiveStep) / (3 * 30 * 48)) % 4;

	}

	public static boolean isDaytime() {
		return Globals.getCellData().getSuntimes().map(st -> st.isDaytime((int)getTick())).orElse(true); // default day time
	}

}

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

package dk.au.bios.porpoise.tasks;

import java.text.DecimalFormat;

import repast.simphony.engine.schedule.IAction;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.util.SimulationTime;

/**
 * A scheduled action to run some code once a (simulation) month.
 */
public class MonthlyTasks implements IAction {

	private final long startTime;
	private final DecimalFormat format = new DecimalFormat("#0.00");

	public MonthlyTasks() {
		startTime = System.nanoTime();
	}

	@Override
	public void execute() {
		final long diff = System.nanoTime() - startTime;

		if (SimulationTime.getDayOfSimulation() > 0) {
			final double timePerDay = (diff / (double) SimulationTime.getDayOfSimulation()) / 1000000000;
			System.out.println(format.format(timePerDay) + " seconds per day of simulation");
			int simYears = 30;
			if (Globals.getSimYears() != null) {
				simYears = Globals.getSimYears();
			}
			final double minutesForSimulation = simYears * 360 * timePerDay / 60;
			final double minutesRemaining = ((simYears * 360) - SimulationTime.getDayOfSimulation()) * timePerDay / 60;

			final int year = SimulationTime.getYearOfSimulation();
			final int month = SimulationTime.getMonthOfYear();
			final int quarter = SimulationTime.getQuarterOfYear() + 1;

			System.out.println("y:" + year + " m: " + month + " q: " + quarter + " - " + simYears
					+ " Years simulated in " + format.format(minutesForSimulation) + " minutes. "
					+ format.format(minutesRemaining) + " minutes remaining of simulation.");
		}
	}

}

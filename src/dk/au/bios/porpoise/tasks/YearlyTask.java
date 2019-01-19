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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.IAction;
import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.SimulationParameters;
import dk.au.bios.porpoise.util.SimulationTime;

/**
 * This executes annually. It currently performs two tasks, setting the random mating day of each porpoise for the year
 * and outputs some age and death statistics. This should be refactored to be in two different classes.
 */
public class YearlyTask implements IAction {

	private static Map<Integer, Integer> deathsAgeDistribution;

	private final Context<Agent> context;

	private PrintWriter annualStatisticsOutput = null;

	public YearlyTask(final Context<Agent> context) {
		// Initialization of a static member here is a bit messy but needed to collect the statistics!
		resetDeathAgeDistribution();

		this.context = context;

		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MMM.dd.HH_mm_ss_SS");
		final File annualStatisticsOutputFile = new File("YearlyMortality." + sdf.format(new Date()) + ".csv");
		try {
			annualStatisticsOutput = new PrintWriter(annualStatisticsOutputFile);
			if (RunEnvironment.getInstance().isBatch()) {
				annualStatisticsOutput.printf("\"run\",");
			}
			annualStatisticsOutput.printf("\"year\",\"age\",\"count\",\"deaths\"%n");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static void resetDeathAgeDistribution() {
		deathsAgeDistribution = new TreeMap<>();
		for (int i = 0; i <= SimulationParameters.getMaxAge(); i++) {
			deathsAgeDistribution.put(i, 0);
		}
	}

	public static void recordDeath(final int age) {
		deathsAgeDistribution.put(age, deathsAgeDistribution.get(age) + 1);
	}

	@Override
	public void execute() {
		/* Key is age (years) and value is count (incremented) */
		final Map<Integer, Integer> ageDistribution = new TreeMap<>();
		for (int i = 0; i <= SimulationParameters.getMaxAge(); i++) {
			ageDistribution.put(i, 0);
		}

		for (final Agent a : this.context.getObjects(Porpoise.class)) {
			final Porpoise p = (Porpoise) a;
			p.setRandomMatingDay();

			final int pAge = (int) Math.floor(p.getAge());
			ageDistribution.put(pAge, ageDistribution.get(pAge) + 1);
		}

		// Make list of number of dead per age class and corresp pop nos in prev year
		ageDistribution.forEach((k, v) -> {
			if (RunEnvironment.getInstance().isBatch()) {
				annualStatisticsOutput.printf("%d,", RunState.getInstance().getRunInfo().getRunNumber());
			}
			annualStatisticsOutput.printf("%d,%d,%d,%d%n", SimulationTime.getYearOfSimulation(), k, v,
					deathsAgeDistribution.get(k));
		});
		annualStatisticsOutput.flush();
		resetDeathAgeDistribution();
	}

}

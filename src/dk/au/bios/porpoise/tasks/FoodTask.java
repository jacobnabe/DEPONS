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

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import repast.simphony.engine.schedule.IAction;
import dk.au.bios.porpoise.BackgroundAgent;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.SimulationConstants;
import dk.au.bios.porpoise.SimulationParameters;
import dk.au.bios.porpoise.util.DaemonThreadFactory;
import dk.au.bios.porpoise.util.Pair;

/**
 * Scheduled action responsible for the growing of food.
 */
public class FoodTask implements IAction {

	private static ExecutorService threadPool = Executors.newFixedThreadPool(8, new DaemonThreadFactory());

	/**
	 * The number of patches where the food has been grown 47 times extra
	 */
	private static AtomicInteger extraGrowthCount = new AtomicInteger();

	private final double[][] foodProb;
	private final double[][] foodLevel;
	private double[][] maxEnt;

	public FoodTask() {
		this.foodProb = Globals.getCellData().getFoodProb();
		this.foodLevel = Globals.getCellData().getFoodValue();
	}

	public static int getExtraGrowthCount() {
		return FoodTask.extraGrowthCount.get();
	}

	@Override
	public void execute() {
		extraGrowthCount.set(0);

		// Get the right ent for the season
		this.maxEnt = Globals.getCellData().getMaxEnt();

		// executeSingleThreadBruteForce(); // (0.30 seconds per day)
		// executeSingleThreadOptimized(); // (0.22 seconds per day)
		executeParallel(4096); // (0.23 seconds per day)
	}

	public void executeSingleThreadOptimized() {
		double grownFood = 0;
		final Pair[] points = Globals.getCellData().getFoodProbAboveZeroPatches();

		for (int idx = 0; idx < points.length; idx++) {
			final int i = points[idx].getFirst();
			final int j = points[idx].getSecond();

			// If we add food when eating then we do not want to add it here. (ADD_ARTIFICAL_FOOD = true)
			// If we do not add food in the eat step then we need to add it before calculating the food growth
			// - otherwise the food growth can start on really small amounts which leads to very small increases
			// (the patch is basically dead, and the 47 extra calcs is not sufficient to restore it).
			if (!SimulationConstants.ADD_ARTIFICIAL_FOOD && foodLevel[i][j] < 0.01) {
				foodLevel[i][j] = 0.01;
			}

			if (foodLevel[i][j] < (SimulationParameters.getMaxU() * maxEnt[i][j])) {
				double fLevel = foodLevel[i][j]
						+ (SimulationParameters.getFoodGrowthRate() * foodLevel[i][j] * (1.0 - foodLevel[i][j]
								/ (SimulationParameters.getMaxU() * maxEnt[i][j] / Globals
										.getMeanMaxEntInCurrentQuarter())));

				if (Math.abs(fLevel - foodLevel[i][j]) > SimulationParameters.getRegrowthFoodQualifier()) {
					for (int k = 0; k < 47; k++) {
						fLevel = fLevel
								+ (SimulationParameters.getFoodGrowthRate() * fLevel * (1.0 - fLevel
										/ (SimulationParameters.getMaxU() * maxEnt[i][j] / Globals
												.getMeanMaxEntInCurrentQuarter())));
					}

					extraGrowthCount.incrementAndGet();
				}

				grownFood += (fLevel - foodLevel[i][j]);
				foodLevel[i][j] = fLevel;
			}
		}
		BackgroundAgent.setGrownFood(grownFood);
	}

	public void executeSingleThreadBruteForce() {
		// maxent-level is patch specific, between 0 and 1 (MAXENT-based); food-growth-rate (rU) is global variable
		double grownFood = 0;

		for (int i = 0; i < foodProb.length; i++) {
			for (int j = 0; j < foodProb[0].length; j++) {
				if (foodProb[i][j] > 0 && foodLevel[i][j] < (SimulationParameters.getMaxU() * maxEnt[i][j])) {
					if (!SimulationConstants.ADD_ARTIFICIAL_FOOD && foodLevel[i][j] < 0.01) {
						foodLevel[i][j] = 0.01;
					}

					double fLevel = foodLevel[i][j]
							+ (SimulationParameters.getFoodGrowthRate() * foodLevel[i][j] * (1.0 - foodLevel[i][j]
									/ (SimulationParameters.getMaxU() * maxEnt[i][j] / Globals
											.getMeanMaxEntInCurrentQuarter())));

					if (Math.abs(fLevel - foodLevel[i][j]) > SimulationParameters.getRegrowthFoodQualifier()) {
						for (int k = 0; k < 47; k++) {
							fLevel += SimulationParameters.getFoodGrowthRate()
									* fLevel
									* (1 - fLevel
											/ (SimulationParameters.getMaxU() * maxEnt[i][j] / Globals
													.getMeanMaxEntInCurrentQuarter()));
						}
						extraGrowthCount.incrementAndGet();
					}
					// If the food level is really low, let food grow 48 times -- like growing every half-hour step,
					// only faster

					grownFood += (fLevel - foodLevel[i][j]);

					foodLevel[i][j] = fLevel;
					// here maxent-level is MAXENT prediction and food-growth-rate is a universal calibrated variable
				}
			}
		}

		BackgroundAgent.setGrownFood(grownFood);
	}

	private void executeParallel(final int chunkSize) {
		// We have 4572 patches with foodProb > 0, break them into chunkSize point big jobs
		final Pair[] points = Globals.getCellData().getFoodProbAboveZeroPatches();
		final LinkedList<Future<Double>> tasks = new LinkedList<>();

		int from = 0;
		int to = chunkSize;

		while (from < points.length) {
			if (to < points.length) {
				tasks.add(threadPool.submit(new Task(points, from, to)));
			} else {
				tasks.add(threadPool.submit(new Task(points, from, points.length)));
			}

			from = to;
			to += chunkSize;
		}
		
		double grownFood = 0;
		for (final Future<Double> f : tasks) {
			try {
				double grownFoodTask = f.get();
				grownFood += grownFoodTask;
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		BackgroundAgent.setGrownFood(grownFood);
	}

	private class Task implements Callable<Double> {
		// private CellData data;
		private final Pair[] points;
		private final int from;
		private final int to;

		Task(final Pair[] points, final int from, final int to) {
			this.from = from;
			this.to = to;
			this.points = points;
		}

		@Override
		public Double call() throws Exception {
			double grownFood = 0;

			for (int idx = from; idx < to; idx++) {
				final int i = points[idx].getFirst();
				final int j = points[idx].getSecond();

				if (!SimulationConstants.ADD_ARTIFICIAL_FOOD && foodLevel[i][j] < 0.01) {
					foodLevel[i][j] = 0.01;
				}

				if (foodLevel[i][j] < (SimulationParameters.getMaxU() * maxEnt[i][j])) {
					double fLevel = foodLevel[i][j]
							+ (SimulationParameters.getFoodGrowthRate() * foodLevel[i][j] * (1.0 - foodLevel[i][j]
									/ (SimulationParameters.getMaxU() * maxEnt[i][j] / Globals
											.getMeanMaxEntInCurrentQuarter())));

					if (Math.abs(fLevel - foodLevel[i][j]) > SimulationParameters.getRegrowthFoodQualifier()) {
						for (int k = 0; k < 47; k++) {
							fLevel += SimulationParameters.getFoodGrowthRate()
									* fLevel
									* (1 - fLevel
											/ (SimulationParameters.getMaxU() * maxEnt[i][j] / Globals
													.getMeanMaxEntInCurrentQuarter()));
						}
						extraGrowthCount.incrementAndGet();
					}

					grownFood += (fLevel - foodLevel[i][j]);
					foodLevel[i][j] = fLevel;
				}
			}
			return grownFood;
		}
	}

}

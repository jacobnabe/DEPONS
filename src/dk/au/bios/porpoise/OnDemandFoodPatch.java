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

/**
 * Class for updating a FoodPatch on demand.
 */
public class OnDemandFoodPatch {

	private double value;
	private double lastDayUpdate;
	private final double[] maxEnt;

	public OnDemandFoodPatch(final double[] maxEnt, final double value) {
		this.value = value;
		this.maxEnt = maxEnt;
		this.lastDayUpdate = SimulationTime.getDayOfSimulation();
	}

	public synchronized double getValue() {
		if (lastDayUpdate != SimulationTime.getDayOfSimulation()) {
			calcValue();
		}

		return value;
	}

	public synchronized void eatFood(final double amountEaten) {
		// TODO: Change this so we do not have to recalc the value here.
		if (lastDayUpdate != SimulationTime.getDayOfSimulation()) {
			calcValue();
		}

		value -= amountEaten;

		if (SimulationConstants.ADD_ARTIFICIAL_FOOD && value < 0.01) {
			value = 0.01;
		}
	}

	private double getMaxEnt(final int quarter) {
		return maxEnt[quarter];
	}

	private void calcValue() {
		if (!SimulationConstants.ADD_ARTIFICIAL_FOOD && value < 0.01) {
			value = 0.01;
		}

		for (double i = lastDayUpdate; i < SimulationTime.getDayOfSimulation(); i++) {
			final int quarterOfYear = SimulationTime.getQuarterOfYear(i);
			if (value < (SimulationParameters.getMaxU()) * getMaxEnt(quarterOfYear)) {
				double fLevel = value
						+ (SimulationParameters.getFoodGrowthRate() * value * (1.0 - value
								/ (SimulationParameters.getMaxU() * getMaxEnt(quarterOfYear) / Globals
										.getMeanMaxEntInQuarter(quarterOfYear))));

				if (Math.abs(fLevel - value) > SimulationParameters.getRegrowthFoodQualifier()) {
					for (int k = 0; k < 47; k++) {
						fLevel += SimulationParameters.getFoodGrowthRate()
								* fLevel
								* (1.0 - fLevel
										/ (SimulationParameters.getMaxU() * getMaxEnt(quarterOfYear) / Globals
												.getMeanMaxEntInQuarter(quarterOfYear)));
					}
				}

				value = fLevel;
			} else {
				// We have reached max, we can stop early
				break;
			}
		}

		lastDayUpdate = SimulationTime.getDayOfSimulation();
	}

}

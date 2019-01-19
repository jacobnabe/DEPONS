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

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import dk.au.bios.porpoise.tasks.FoodTask;
import dk.au.bios.porpoise.util.Pair;

/**
 * A proxy Agent to enable using food in a Data Source. This is considered a temporary measure - until a proper use of
 * food with a Data Source can be found.
 */
public class FoodAgentProxy extends Agent {

	public FoodAgentProxy(final ContinuousSpace<Agent> space, final Grid<Agent> grid, final long id) {
		super(space, grid, id);
	}

	public double getFoodEnergyLevel() {
		final Pair[] patches = Globals.getCellData().getFoodProbAboveZeroPatches();
		double foodSum = 0.0f;

		for (int i = 0; i < patches.length; i++) {
			final double value = Globals.getCellData().getFoodLevel(patches[i].getFirst(), patches[i].getSecond());
			foodSum += value;
		}

		return foodSum;
	}

	public int getExtraGrowthCount() {
		return FoodTask.getExtraGrowthCount();
	}

}

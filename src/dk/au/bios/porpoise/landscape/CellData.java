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

package dk.au.bios.porpoise.landscape;

import java.util.LinkedList;

import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.GridPoint;
import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.OnDemandFoodPatch;
import dk.au.bios.porpoise.SimulationConstants;
import dk.au.bios.porpoise.SimulationParameters;
import dk.au.bios.porpoise.util.Pair;
import dk.au.bios.porpoise.util.SimulationTime;

/**
 * Encapsulates the data related to the simulation environment.
 */
public class CellData {

	private final boolean onDemandFood;
	private final boolean[][] mask;
	private final double[][] distanceToCoast;
	private final double[][] depth;
	private final int[][] block;
	private final double[][] foodProb;
	private final double[][] foodValue;

	private final OnDemandFoodPatch[][] foodPatch;

	private final double[][] quarter1;
	private final double[][] quarter2;
	private final double[][] quarter3;
	private final double[][] quarter4;

	private final double[][][] salinityMaps;

	/*
	 * private double[][] blockValQuarter1; private double[][] blockValQuarter2; private double[][] blockValQuarter3;
	 * private double[][] blockValQuarter4;
	 */

	private final Pair[] foodProbAboveZeroCells;
	private final boolean onDemandFoodUpdate;

	public CellData(final double[][] distanceToCoast, final double[][] depth, final double[][] block,
			final double[][] foodProb, final double[][] quarter1, final double[][] quarter2, final double[][] quarter3,
			final double[][] quarter4, final double[][][] salinityMaps, final boolean onDemandFoodUpdate,
			final boolean[][] mask, final boolean onDemandFood) {
		this.onDemandFood = onDemandFood;
		this.distanceToCoast = distanceToCoast;
		this.depth = depth;
		this.foodProb = foodProb;
		this.onDemandFoodUpdate = onDemandFoodUpdate;

		this.quarter1 = quarter1;
		this.quarter2 = quarter2;
		this.quarter3 = quarter3;
		this.quarter4 = quarter4;
		this.mask = mask;

		this.salinityMaps = salinityMaps;

		this.foodValue = new double[this.foodProb.length][this.foodProb[0].length];

		final double[][] blockDouble = block;
		this.block = new int[blockDouble.length][blockDouble[0].length];

		for (int i = 0; i < this.block.length; i++) {
			for (int j = 0; j < this.block[0].length; j++) {
				this.block[i][j] = (int) blockDouble[i][j];
			}
		}

		final LinkedList<Pair> patches = new LinkedList<Pair>();
		for (int i = 0; i < this.foodProb.length; i++) {
			for (int j = 0; j < this.foodProb[0].length; j++) {
				if (this.foodProb[i][j] > 0) {
					patches.add(new Pair(i, j));
				}
			}
		}

		this.foodProbAboveZeroCells = patches.toArray(new Pair[patches.size()]);

		if (onDemandFoodUpdate) {
			foodPatch = new OnDemandFoodPatch[this.foodProb.length][this.foodProb[0].length];

			for (final Pair p : patches) {
				final double value = SimulationParameters.getMaxU() * this.quarter1[p.getFirst()][p.getSecond()]
						/ Globals.getMeanMaxEntInCurrentQuarter();
				foodPatch[p.getFirst()][p.getSecond()] = new OnDemandFoodPatch(new double[] {
						this.quarter1[p.getFirst()][p.getSecond()], this.quarter2[p.getFirst()][p.getSecond()],
						this.quarter3[p.getFirst()][p.getSecond()], this.quarter4[p.getFirst()][p.getSecond()] }, value);
			}
		} else {
			foodPatch = null;
		}

	}

	public double getDistanceToCoast(final int x, final int y) {
		return distanceToCoast[x][y];
	}

	public double getDistanceToCoast(final NdPoint point) {
		final GridPoint p = Agent.ndPointToGridPoint(point);
		return distanceToCoast[p.getX()][p.getY()];
	}

	public double getDepth(final GridPoint point) {
		return getDepth(point.getX(), point.getY());
	}

	public double getDepth(final int x, final int y) {
		try {
			return depth[x][y];
		} catch (final ArrayIndexOutOfBoundsException e) {
			// TODO: Consider handling this better, i.e. propogate the error.
			return -9999; // 0;
		}
	}

	public double getDepth(final NdPoint point) {
		return getDepth(Agent.ndPointToGridPoint(point));
	}

	public double getSalinity(final GridPoint point) {
		final int mapIndex = SimulationTime.getMonthOfYear() - 1;

		final double[][] map = salinityMaps[mapIndex];
		final double salinityValue = map[point.getX()][point.getY()];

		return salinityValue;
	}

	public double getSalinity(final NdPoint point) {
		return getSalinity(Agent.ndPointToGridPoint(point));
	}

	public int getBlock(final GridPoint point) {
		return block[point.getX()][point.getY()];
	}

	public int getBlock(final NdPoint point) {
		return getBlock(Agent.ndPointToGridPoint(point));
	}

	public int[][] getBlock() {
		return block;
	}

	public double getFoodLevel(final GridPoint p) {
		return getFoodLevel(p.getX(), p.getY());
	}

	public double getFoodLevel(final int x, final int y) {
		if (onDemandFoodUpdate) {
			if (foodPatch[x][y] != null) {
				return foodPatch[x][y].getValue();
			} else {
				return 0.0;
			}
		} else {
			return this.foodValue[x][y];
		}
	}

	public synchronized double eatFood(final GridPoint point, final double eatFraction) {
		final double food = getFoodLevel(point.getX(), point.getY());

		if (food > 0.0) {
			final double eaten = food * eatFraction;

			if (onDemandFoodUpdate) {
				this.foodPatch[point.getX()][point.getY()].eatFood(eaten);
			} else {
				this.foodValue[point.getX()][point.getY()] -= eaten;

				// The minimum food level has a strong impact on how fast food gets back
				if (SimulationConstants.ADD_ARTIFICIAL_FOOD && this.foodValue[point.getX()][point.getY()] < 0.01) {
					this.foodValue[point.getX()][point.getY()] = 0.01;
				}
			}

			return eaten;
		} else {
			return 0.0;
		}
	}

	public double[][] getFoodProb() {
		return this.foodProb;
	}

	public double getFoodProb(final NdPoint p) {
		return getFoodProb(Agent.ndPointToGridPoint(p));
	}

	public double getFoodProb(final GridPoint p) {
		return this.foodProb[p.getX()][p.getY()];
	}

	public double[][] getFoodValue() {
		if (this.onDemandFood) {
			throw new RuntimeException("FoodValue array may not be accessed directly when we calculate food on demand");
		}

		return this.foodValue;
	}

	public boolean isPointMasked(final GridPoint p) {
		return isPointMasked(p.getX(), p.getY());
	}

	public boolean isPointMasked(final int x, final int y) {
		if (mask != null) {
			return mask[x][y];
		} else {
			return false;
		}
	}

	public double getMaxEnt(final NdPoint p) {
		return getMaxEnt(Agent.ndPointToGridPoint(p));
	}

	public double getMaxEnt(final GridPoint p) {
		return getMaxEnt()[p.getX()][p.getY()];
	}

	public double[][] getMaxEnt() {
		final int quarter = SimulationTime.getQuarterOfYear();
		switch (quarter) {
		case 0:
			return this.quarter1;
		case 1:
			return this.quarter2;
		case 2:
			return this.quarter3;
		case 3:
			return this.quarter4;
		default:
			throw new IndexOutOfBoundsException("SimulationTime reported unknown quarter: " + quarter);
		}
	}

	/*
	 * public double[][] getBlockValues() { switch (Globals.getQuarterOfYear()) { case 0: return this.blockValQuarter1;
	 * case 1: return this.blockValQuarter2; case 2: return this.blockValQuarter3; case 3: return this.blockValQuarter4;
	 * }
	 *
	 * throw new RuntimeException("implementation error");
	 *
	 * }
	 */

	public Pair[] getFoodProbAboveZeroPatches() {
		return this.foodProbAboveZeroCells;
	}

	public void initializeFoodPatches() {
		final double[][] maxEnt = this.getMaxEnt();

		for (int i = 0; i < foodProb.length; i++) {
			for (int j = 0; j < foodProb[0].length; j++) {
				if (foodProb[i][j] > 0 && maxEnt[i][j] > 0) {
					foodValue[i][j] = SimulationParameters.getMaxU() * maxEnt[i][j]
							/ Globals.getMeanMaxEntInCurrentQuarter();
				} else {
					foodValue[i][j] = 0;
				}
			}
		}
	}

	public boolean isOnDemandFoodUpdate() {
		return onDemandFoodUpdate;
	}

}

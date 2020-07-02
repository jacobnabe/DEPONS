/*
 * Copyright (C) 2017-2020 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.SimulationConstants;
import dk.au.bios.porpoise.SimulationParameters;
import dk.au.bios.porpoise.util.Pair;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.GridPoint;

/**
 * Encapsulates the data related to the simulation environment.
 */
public class CellData {

	private final SimpleDataFile distanceToCoast;
	private final SimpleDataFile depth;
	private final int[][] block;
	private final SimpleDataFile foodProb;
	private final double[][] foodValue;

	private final MonthlyDataFile entropy;
	private final MonthlyDataFile salinityMaps;

	private final Pair[] foodProbAboveZeroCells;

	public CellData(final String landscape, final List<CellDataSource> sources) throws IOException {
		this.distanceToCoast = new SimpleDataFile(landscape, LandscapeLoader.DISTTOCOAST_FILE, sources);
		this.depth = new SimpleDataFile(landscape, LandscapeLoader.BATHY_FILE, sources);
		this.foodProb = new SimpleDataFile(landscape, LandscapeLoader.PATCHES_FILE, sources);
		this.entropy = new MonthlyDataFile(landscape, LandscapeLoader.PREY_FILE_PREFIX, sources);
		this.salinityMaps = new MonthlyDataFile(landscape, LandscapeLoader.SALINITY_FILE_PREFIX, sources);

		this.foodValue = new double[this.foodProb.getData().length][this.foodProb.getData()[0].length];

		final double[][] blockDouble = new SimpleDataFile(landscape, LandscapeLoader.BLOCKS_FILE, sources).getData();
		this.block = new int[blockDouble.length][blockDouble[0].length];

		for (int i = 0; i < this.block.length; i++) {
			for (int j = 0; j < this.block[0].length; j++) {
				this.block[i][j] = (int) blockDouble[i][j];
			}
		}

		final LinkedList<Pair> patches = new LinkedList<Pair>();
		for (int i = 0; i < this.foodProb.getData().length; i++) {
			for (int j = 0; j < this.foodProb.getData()[0].length; j++) {
				if (this.foodProb.getData()[i][j] > 0) {
					patches.add(new Pair(i, j));
				}
			}
		}

		this.foodProbAboveZeroCells = patches.toArray(new Pair[patches.size()]);
	}

	public double getDistanceToCoast(final int x, final int y) {
		return distanceToCoast.getData()[x][y];
	}

	public double getDistanceToCoast(final NdPoint point) {
		final GridPoint p = Agent.ndPointToGridPoint(point);
		return distanceToCoast.getData()[p.getX()][p.getY()];
	}

	public double getDepth(final GridPoint point) {
		return getDepth(point.getX(), point.getY());
	}

	public double getDepth(final int x, final int y) {
		try {
			return depth.getData()[x][y];
		} catch (final ArrayIndexOutOfBoundsException e) {
			// TODO: Consider handling this better, i.e. propogate the error.
			return -9999; // 0;
		}
	}

	public double getDepth(final NdPoint point) {
		return getDepth(Agent.ndPointToGridPoint(point));
	}

	public double getSalinity(final GridPoint point) {
		try {
			final double salinityValue = salinityMaps.getData()[point.getX()][point.getY()];
			return salinityValue;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
		return this.foodValue[x][y];
	}

	public synchronized double eatFood(final GridPoint point, final double eatFraction) {
		final double food = getFoodLevel(point.getX(), point.getY());

		if (food > 0.0) {
			final double eaten = food * eatFraction;

			this.foodValue[point.getX()][point.getY()] -= eaten;

			// The minimum food level has a strong impact on how fast food gets back
			if (SimulationConstants.ADD_ARTIFICIAL_FOOD && this.foodValue[point.getX()][point.getY()] < 0.01) {
				this.foodValue[point.getX()][point.getY()] = 0.01;
			}

			return eaten;
		} else {
			return 0.0;
		}
	}

	public double[][] getFoodProb() {
		return this.foodProb.getData();
	}

	public double getFoodProb(final NdPoint p) {
		return getFoodProb(Agent.ndPointToGridPoint(p));
	}

	public double getFoodProb(final GridPoint p) {
		return this.foodProb.getData()[p.getX()][p.getY()];
	}

	public double[][] getFoodValue() {
		return this.foodValue;
	}

	public double getMaxEnt(final NdPoint p) {
		return getMaxEnt(Agent.ndPointToGridPoint(p));
	}

	public double getMaxEnt(final GridPoint p) {
		return getMaxEnt()[p.getX()][p.getY()];
	}

	public double[][] getMaxEnt() {
		try {
			return entropy.getData();
		} catch (IOException e) {
			throw new RuntimeException(e);
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

		for (int i = 0; i < foodProb.getData().length; i++) {
			for (int j = 0; j < foodProb.getData()[0].length; j++) {
				if (foodProb.getData()[i][j] > 0 && maxEnt[i][j] > 0) {
					foodValue[i][j] = SimulationParameters.getMaxU() * maxEnt[i][j]
							/ Globals.getMeanMaxEntInCurrentQuarter();
				} else {
					foodValue[i][j] = 0;
				}
			}
		}
	}

}

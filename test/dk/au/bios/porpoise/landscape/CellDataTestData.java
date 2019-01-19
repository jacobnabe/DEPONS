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

import java.util.Arrays;

/**
 * Unit tests for CellData.
 */
public final class CellDataTestData {

	private CellDataTestData() {
		// Utility class, prevent instances.
	}

	public static CellData getCellData() throws Exception {
		final CellData cellData = new CellData(getDistanceToCoast(), getDepth(), getBlocks(), getFoodProb(), getQuarter(),
				getQuarter(), getQuarter(), getQuarter(), getSalinity(), false, null, false);
		return cellData;
	}

	public static double[][] getDistanceToCoast() {
		return fillArray(10000.0);
	}

	public static double[][] getDepth() {
		return fillArray(18.10);
	}

	public static double[][] getBlocks() {
		return fillArray(1.0);
	}

	public static double[][] getFoodProb() {
		return fillArray(1.0);
	}

	public static double[][] getQuarter() {
		return fillArray(0.386);
	}

	public static double[][][] getSalinity() {
		final double[][][] salinityMaps = new double[12][][];
		for (int i = 1; i < 13; i++) {
			salinityMaps[i-1] = fillArray(34.069105813295); // Value taken from Homogenous landscape
		}

		return salinityMaps;
	}

	private static double[][] fillArray(final double value) {
		final double[][] data = new double[100][100];
		for (final double[] ds : data) {
			Arrays.fill(ds, value);
		}

		return data;
	}

}

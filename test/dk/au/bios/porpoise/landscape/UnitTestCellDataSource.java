/*
 * Copyright (C) 2020 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UnitTestCellDataSource implements CellDataSource {

	@Override
	public boolean hasData(String fileName) throws IOException {
		try {
			if (getData(fileName) != null) {
				return true;
			}
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public List<String> getNamesMatching(String pattern) throws IOException {
		return Collections.emptyList();
	}

	@Override
	public double[][] getData(String fileName) throws IOException {
		switch (fileName) {
		case LandscapeLoader.BATHY_FILE:
			return getDepth();
		case LandscapeLoader.BLOCKS_FILE:
			return getBlocks();
		case LandscapeLoader.DISTTOCOAST_FILE:
			return getDistanceToCoast();
		case LandscapeLoader.PATCHES_FILE:
			return getFoodProb();
		}

		if (fileName.startsWith(LandscapeLoader.PREY_FILE_PREFIX)) {
			return getQuarter();
		} else if (fileName.startsWith(LandscapeLoader.SALINITY_FILE_PREFIX)) {
			return getSalinity();
		} else {
			throw new IOException("Unknown data " + fileName);
		}
	}

	@Override
	public DataFileMetaData getMetaData(String fileName) throws IOException {
		return new DataFileMetaData(100, 100, -1, -1, 400, null);
	}

	private static double[][] getDistanceToCoast() {
		return fillArray(10000.0);
	}

	private static double[][] getDepth() {
		return fillArray(18.10);
	}

	private static double[][] getBlocks() {
		return fillArray(1.0);
	}

	private static double[][] getFoodProb() {
		return fillArray(1.0);
	}

	private static double[][] getQuarter() {
		return fillArray(0.386);
	}

	private static double[][] getSalinity() {
		return fillArray(34.069105813295);
	}

	private static double[][] fillArray(final double value) {
		final double[][] data = new double[100][100];
		for (final double[] ds : data) {
			Arrays.fill(ds, value);
		}

		return data;
	}

}

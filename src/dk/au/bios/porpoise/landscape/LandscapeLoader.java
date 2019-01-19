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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.SimulationParameters;
import dk.au.bios.porpoise.util.ASCUtil;

/**
 * Loads the landscape data files and returns a CellData instance.
 */
public class LandscapeLoader {

	private static final String BATHY_FILE = "bathy.asc";
	private static final String BLOCKS_FILE = "blocks.asc";
	private static final String DISTTOCOAST_FILE = "disttocoast.asc";
	private static final String PATCHES_FILE = "patches.asc";
	private static final String QUARTER_FILE1 = "quarter1.asc";
	private static final String QUARTER_FILE2 = "quarter2.asc";
	private static final String QUARTER_FILE3 = "quarter3.asc";
	private static final String QUARTER_FILE4 = "quarter4.asc";
	private static final String MASK_FILE = "mask.asc";

	private final String dataPath;

	public LandscapeLoader() {
		dataPath = "data";
	}

	public LandscapeLoader(final String dataPath) {
		this.dataPath = dataPath;
	}

	public CellData load(final String landscape, final boolean onDemandFood) {
		CellData cellData;
		String landscapeToLoad = landscape;
		try {
			if (SimulationParameters.isHomogenous()) {
				landscapeToLoad = SimulationParameters.LANDSCAPE_HOMOGENOUS_NAME;
			}

			final String landscapeDataPath = dataPath + "/" + landscapeToLoad;
			if (Files.exists(Paths.get(landscapeDataPath))) {
				// System.err.println("Loading from directory");
				cellData = loadFromDirectory(dataPath + "/" + landscapeToLoad, onDemandFood);
			} else if (Files.exists(Paths.get(landscapeDataPath + ".zip"))) {
				// System.err.println("Loading from ZIP");
				cellData = loadFromZip(landscapeDataPath + ".zip", onDemandFood);
			} else {
				throw new RuntimeException("The landscape " + landscapeToLoad + " could not be found.");
			}
			return cellData;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private CellData loadFromDirectory(final String landscapeDataPath, final boolean onDemandFood) throws IOException {
		final double[][] distance = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
				new FileInputStream(landscapeDataPath + "/" + DISTTOCOAST_FILE), false);
		final double[][] depth = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
				new FileInputStream(landscapeDataPath + "/" + BATHY_FILE), false);
		final double[][] block = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
				new FileInputStream(landscapeDataPath + "/" + BLOCKS_FILE), true);
		final double[][] foodProb = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
				new FileInputStream(landscapeDataPath + "/" + PATCHES_FILE), false);

		double[][] quarter1;
		double[][] quarter2;
		double[][] quarter3;
		double[][] quarter4;
		if (SimulationParameters.isHomogenous()) {
			quarter1 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(), new FileInputStream(
					landscapeDataPath + "/" + QUARTER_FILE1), false);
			quarter2 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(), new FileInputStream(
					landscapeDataPath + "/" + QUARTER_FILE1), false);
			quarter3 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(), new FileInputStream(
					landscapeDataPath + "/" + QUARTER_FILE1), false);
			quarter4 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(), new FileInputStream(
					landscapeDataPath + "/" + QUARTER_FILE1), false);
		} else {
			quarter1 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(), new FileInputStream(
					landscapeDataPath + "/" + QUARTER_FILE1), false);
			quarter2 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(), new FileInputStream(
					landscapeDataPath + "/" + QUARTER_FILE2), false);
			quarter3 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(), new FileInputStream(
					landscapeDataPath + "/" + QUARTER_FILE3), false);
			quarter4 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(), new FileInputStream(
					landscapeDataPath + "/" + QUARTER_FILE4), false);
		}

		boolean[][] mask;
		if (Files.exists(Paths.get(landscapeDataPath + "/" + MASK_FILE))) {
			mask = ASCUtil.loadBooleanAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(), new FileInputStream(
					landscapeDataPath + "/" + MASK_FILE));
		} else {
			mask = null;
		}

		final double[][][] salinityMaps = new double[12][][];
		for (int i = 1; i < 13; i++) {
			// System.err.println("Loading salinity: " + i);
			salinityMaps[i - 1] = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
					new FileInputStream(landscapeDataPath + "/salinity_" + i + ".asc"), false);
			// System.err.println("Loaded salinity: " + i);
		}

		final CellData cellData = new CellData(distance, depth, block, foodProb, quarter1, quarter2, quarter3,
				quarter4, salinityMaps, onDemandFood, mask, onDemandFood);
		if (!onDemandFood) {
			cellData.initializeFoodPatches();
		}

		return cellData;
	}

	private CellData loadFromZip(final String landscapeZip, final boolean onDemandFood) throws IOException {
		try (ZipFile zf = new ZipFile(landscapeZip)) {
			final double[][] distance = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
					zf.getInputStream(zf.getEntry(DISTTOCOAST_FILE)), false);
			final double[][] depth = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
					zf.getInputStream(zf.getEntry(BATHY_FILE)), false);
			final double[][] block = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
					zf.getInputStream(zf.getEntry(BLOCKS_FILE)), true);
			final double[][] foodProb = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
					zf.getInputStream(zf.getEntry(PATCHES_FILE)), false);

			double[][] quarter1;
			double[][] quarter2;
			double[][] quarter3;
			double[][] quarter4;
			if (SimulationParameters.isHomogenous()) {
				quarter1 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
						zf.getInputStream(zf.getEntry(QUARTER_FILE1)), false);
				quarter2 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
						zf.getInputStream(zf.getEntry(QUARTER_FILE1)), false);
				quarter3 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
						zf.getInputStream(zf.getEntry(QUARTER_FILE1)), false);
				quarter4 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
						zf.getInputStream(zf.getEntry(QUARTER_FILE1)), false);
			} else {
				quarter1 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
						zf.getInputStream(zf.getEntry(QUARTER_FILE1)), false);
				quarter2 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
						zf.getInputStream(zf.getEntry(QUARTER_FILE2)), false);
				quarter3 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
						zf.getInputStream(zf.getEntry(QUARTER_FILE3)), false);
				quarter4 = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
						zf.getInputStream(zf.getEntry(QUARTER_FILE4)), false);
			}

			final ZipEntry maskEntry = zf.getEntry(MASK_FILE);
			boolean[][] mask;
			if (maskEntry != null) {
				final InputStream maskIn = zf.getInputStream(maskEntry);
				mask = ASCUtil.loadBooleanAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(), maskIn);
			} else {
				mask = null;
			}

			final double[][][] salinityMaps = new double[12][][];
			for (int i = 1; i < 13; i++) {
				// System.err.println("Loading salinity: " + i);
				salinityMaps[i - 1] = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(),
						zf.getInputStream(zf.getEntry("salinity_" + i + ".asc")), false);
				// System.err.println("Loaded salinity: " + i);
			}

			final CellData cellData = new CellData(distance, depth, block, foodProb, quarter1, quarter2, quarter3,
					quarter4, salinityMaps, onDemandFood, mask, onDemandFood);
			if (!onDemandFood) {
				cellData.initializeFoodPatches();
			}

			return cellData;
		}
	}

	private int optionToInteger(final String s) {
		final String value = s.substring(s.lastIndexOf(" ") + 1);
		return Integer.parseInt(value);
	}

	private double optionToDouble(final String s) {
		final String value = s.substring(s.lastIndexOf(" ") + 1);
		return Double.parseDouble(value);
	}

	/**
	 * Loads the landscape parameters from the passed asc file.
	 *
	 * This function will update the Global parametes.
	 *
	 * TODO: Get rid of the Global parameters and find a nicer way to distribute these values.
	 *
	 * @param landscapeDataPath The asc to load the landscape parameters from.
	 * @throws Exception Thrown in the asc data cannot be read.
	 */
	public void initLandscape(final String landscapeDataPath) {
		int ncols;
		int nrows;
		double xllcorner;
		double yllcorner;
		int cellsize;
		try {
			if (Files.isDirectory(Paths.get(landscapeDataPath))) {
				try (BufferedReader reader = new BufferedReader(new FileReader(landscapeDataPath + "/bathy.asc"))) {
					ncols = optionToInteger(reader.readLine().trim());
					nrows = optionToInteger(reader.readLine().trim());
					xllcorner = optionToDouble(reader.readLine().trim());
					yllcorner = optionToDouble(reader.readLine().trim());
					cellsize = optionToInteger(reader.readLine().trim());
				}
			} else if (Files.exists(Paths.get(landscapeDataPath + ".zip"))) {
				try (ZipFile zf = new ZipFile(landscapeDataPath + ".zip")) {
					final InputStream in = zf.getInputStream(zf.getEntry(BATHY_FILE));
					final BufferedReader reader = new BufferedReader(
							new InputStreamReader(in, Charset.defaultCharset()));
					ncols = optionToInteger(reader.readLine().trim());
					nrows = optionToInteger(reader.readLine().trim());
					xllcorner = optionToDouble(reader.readLine().trim());
					yllcorner = optionToDouble(reader.readLine().trim());
					cellsize = optionToInteger(reader.readLine().trim());
				}
			} else {
				throw new RuntimeException("Unable to load landscape from " + landscapeDataPath);
			}

			if (cellsize != 400) {
				throw new IOException("Cell size != 400, not supported");
			}

			Globals.setWorldWidth(ncols);
			Globals.setWorldHeight(nrows);
			Globals.setXllCorner(xllcorner);
			Globals.setYllCorner(yllcorner);
		} catch (final IOException e) {
			throw new RuntimeException("Error loading bathy.asc during initialization.", e);
		}
	}

}

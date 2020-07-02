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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.util.SimulationTime;

public class MonthlyDataFile extends AbstractDataFile {

	private enum Mode {
		SINGLE, ANNUALLY, MONTHLY_CYCLE, MONTHLY
	}

	private static final String FILE_EXT =  LandscapeLoader.FILE_EXT;

	private final String filePrefix;

	private int lastLoadedYear = -1;
	private int lastLoadedMonth = -1;

	private final Mode mode;
	private double[][] data = null;

	final List<CellDataSource> sources;
	final private int startingYear;

	public MonthlyDataFile(final String landscape, final String filePrefix, final List<CellDataSource> sources)
			throws IOException {
		super(landscape);

		if (sources == null || sources.size() < 1) {
			throw new IOException("Need at least one data file source.");
		}
		this.sources = sources;
		this.filePrefix = filePrefix;
		this.startingYear = determineStartingYear();
		mode = determineMode(landscape, filePrefix);
		verifyRequiredFiles(mode, filePrefix);
	}

	public double[][] getData() throws IOException {
		final int currentYear = startingYear + SimulationTime.getYearOfSimulation();
		final int currentMonth = SimulationTime.getMonthOfYear();

		if (currentYear != lastLoadedYear || currentMonth != lastLoadedMonth) {
			final String fileName;
			if (mode == Mode.SINGLE) {
				if (data != null) {
					return data;
				}
				fileName = String.format("%s%04d_XX" + FILE_EXT, filePrefix, startingYear);
			} else if (mode == Mode.ANNUALLY) {
				if (currentYear == lastLoadedYear) {
					return data;
				}
				fileName = String.format("%s%04d_XX" + FILE_EXT, filePrefix, currentYear);
			} else if (mode == Mode.MONTHLY_CYCLE) {
				fileName = String.format("%s%04d_%02d" + FILE_EXT, filePrefix, startingYear, currentMonth);
			} else if (mode == Mode.MONTHLY) {
				fileName = String.format("%s%04d_%02d" + FILE_EXT, filePrefix, currentYear, currentMonth);
			} else {
				throw new IOException("Unknown file mode");
			}

			System.out.printf("Loading %s data for %04d-%02d from file %s (mode: %s)%n", filePrefix, currentYear,
					currentMonth, fileName, mode);

			boolean loaded = false;
			for (CellDataSource src : sources) {
				if (src.hasData(fileName)) {
					data = src.getData(fileName);
					loaded = true;
				}
			}
			if (!loaded) {
				throw new IOException(String.format("Could not load %s data for %04d-%02d from file %s (mode: %s)%n",
						filePrefix, currentYear, +currentMonth, fileName, mode));
			}

			lastLoadedYear = currentYear;
			lastLoadedMonth = currentMonth;
		}

		return data;
	}

	private Mode determineMode(final String landscape, final String filePrefix) throws IOException {
		for (CellDataSource src : sources) {
			if (src.hasData(String.format("%s%04d_01" + FILE_EXT, filePrefix, startingYear))) {
				for (CellDataSource src2 : sources) {
					if (src2.hasData(String.format("%s%04d_01" + FILE_EXT, filePrefix, startingYear + 1))) {
						return Mode.MONTHLY;
					}
				}
				return Mode.MONTHLY_CYCLE;
			} else if (src.hasData(String.format("%s%04d_XX" + FILE_EXT, filePrefix, startingYear))) {
				for (CellDataSource src2 : sources) {
					if (src2.hasData(String.format("%s%04d_XX" + FILE_EXT, filePrefix, startingYear + 1))) {
						return Mode.ANNUALLY;
					}
				}
				return Mode.SINGLE;
			}
		}

		throw new IOException("Unable to determine mode for file " + filePrefix + " in landscape " + landscape);
	}

	private int determineStartingYear() throws IOException {
		int startingYear = Integer.MAX_VALUE;
		final String pattern = "^" + filePrefix + "(\\d{4})_(\\d{2}|XX)\\" + FILE_EXT + "$";
		Pattern p = Pattern.compile(pattern);

		for (CellDataSource src : sources) {
			List<String> namesMatching = src.getNamesMatching(pattern);

			for (String f : namesMatching) {
				Matcher m = p.matcher(f);
				if (m.matches()) {
					int year = Integer.valueOf(m.group(1));
					if (year < startingYear) {
						startingYear = year;
					}
				}
			}

			if (startingYear != Integer.MAX_VALUE) {
				break;
			}
		}

		if (startingYear == Integer.MAX_VALUE) {
			return 0;
		}

		System.out.println("Starting year for " + filePrefix + " is " + startingYear);

		return startingYear;
	}

	private void verifyRequiredFiles(final Mode mode, final String filePrefix) throws IOException {
		final String[] filesToVerify;

		if (mode == Mode.SINGLE) {
			return;
		} else if (mode == Mode.MONTHLY) {
			Integer simYears = Globals.getSimYears();
			if (simYears != null) {
				filesToVerify = new String[simYears * 12];
				for (int i = 0; i < simYears; i++) {
					for (int j = 0; j < 12; j++) {
						int offset = (i * 12) + j;
						filesToVerify[offset] = String.format("%s%04d_%02d" + FILE_EXT, filePrefix, startingYear + i, j + 1);
					}
				}
			} else {
				System.out.println("Warning - simulation has no simYears restriction. Data completeness for "
						+ filePrefix + " not performed!");
				filesToVerify = new String[0];
			}
		} else if (mode == Mode.MONTHLY_CYCLE) {
			filesToVerify = new String[12];
			for (int i = 0; i < 12; i++) {
				filesToVerify[i] = String.format("%s%04d_%02d" + FILE_EXT, filePrefix, startingYear, i + 1);
			}
		} else if (mode == Mode.ANNUALLY) {
			Integer simYears = Globals.getSimYears();
			if (simYears != null) {
				filesToVerify = new String[simYears];
				for (int i = 0; i < simYears; i++) {
					filesToVerify[i] = String.format("%s%04d_XX" + FILE_EXT, filePrefix, startingYear + i);
				}
			} else {
				System.out.println("Warning - simulation has no simYears restriction. Data completeness for "
						+ filePrefix + " not performed!");
				filesToVerify = new String[0];
			}
		} else {
			throw new IOException("Unable to verify files. Unknown file mode: " + mode);
		}

		List<String> missingFiles = new ArrayList<>();
		for (String file : filesToVerify) {
			boolean found = false;
			for (CellDataSource src : sources) {
				if (src.hasData(file)) {
					found = true;
					continue;
				}
			}
			if (!found) {
				missingFiles.add(file);
			}
		}

		if (!missingFiles.isEmpty()) {
			throw new IOException(String.format("Found %d missing files for %s. First file is %s", missingFiles.size(),
					filePrefix, missingFiles.get(0)));
		}
	}

}

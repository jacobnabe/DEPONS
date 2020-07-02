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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import dk.au.bios.porpoise.Globals;

/**
 * Loads the landscape data files and returns a CellData instance.
 */
public class LandscapeLoader {

	private static final String DATA_PATH = "data";
	public static final String FILE_EXT_ASC = ".asc";
	public static final String FILE_EXT_TIF = ".tif";
	public static final String FILE_EXT_ZIP = ".zip";
	public static final String FILE_EXT = FILE_EXT_ASC;

	public static final String BATHY_FILE = "bathy" + FILE_EXT;
	public static final String BLOCKS_FILE = "blocks" + FILE_EXT;
	public static final String DISTTOCOAST_FILE = "disttocoast" + FILE_EXT;
	public static final String PATCHES_FILE = "patches" + FILE_EXT;
	public static final String PREY_FILE_PREFIX = "prey";
	public static final String SALINITY_FILE_PREFIX = "salinity";

	private final String landscape;

	public LandscapeLoader(final String landscape) {
		this.landscape = landscape;
	}

	public CellData load() throws IOException {
		List<CellDataSource> sources = new ArrayList<>(2);

		Path basePath = Paths.get(DATA_PATH, landscape);
		if (Files.isDirectory(basePath)) {
			sources.add(new DirectoryCellDataSource(basePath));
		}

		Path zipFilePath = Paths.get(DATA_PATH, landscape + FILE_EXT_ZIP);
		if (Files.exists(zipFilePath)) {
			sources.add(new ZipFileCellDataSource(zipFilePath));
		}

		initLandscape(sources);
		final CellData cellData = new CellData(landscape, sources);
		cellData.initializeFoodPatches();

		return cellData;
	}

	/**
	 * Loads the landscape parameters from the passed asc file.
	 *
	 * This function will update the Global parametes.
	 *
	 * @param landscapeDataPath The asc to load the landscape parameters from.
	 * @throws Exception Thrown in the asc data cannot be read.
	 */
	private void initLandscape(final List<CellDataSource> sources) {
		try {
			DataFileMetaData metadata = null;
			for (CellDataSource src: sources) {
				if (src.hasData(BATHY_FILE)) {
					metadata = src.getMetaData(BATHY_FILE);
					break;
				}
			}

			if (metadata == null) {
				throw new FileNotFoundException("Unable to load landscape " + landscape + " from " + BATHY_FILE);
			}

			if (metadata.getCellsize() != 400) {
				throw new IOException("Cell size != 400, not supported");
			}

			Globals.setLandscapeMetadata(metadata);
		} catch (final IOException e) {
			throw new RuntimeException("Error loading " + BATHY_FILE + " during initialization.", e);
		}
	}

}

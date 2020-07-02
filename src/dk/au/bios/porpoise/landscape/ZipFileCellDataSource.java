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
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.util.ASCUtil;
import dk.au.bios.porpoise.util.GeoTiffUtil;

public class ZipFileCellDataSource implements CellDataSource {

	private final Path zipFilePath;

	public ZipFileCellDataSource(final Path zipFilePath) {
		this.zipFilePath = zipFilePath;
	}

	@Override
	public boolean hasData(final String fileName) throws IOException {
		try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
			ZipEntry entry = zipFile.getEntry(fileName);
			return entry != null;
		}
	}

	@Override
	public List<String> getNamesMatching(final String pattern) throws IOException {
		final List<String> matchingEntries = new ArrayList<>();
		final Pattern p = Pattern.compile(pattern);

		try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (p.matcher(entry.getName()).matches()) {
					matchingEntries.add(entry.getName());
				}
			}

			return matchingEntries;
		}
	}

	@Override
	public double[][] getData(final String fileName) throws IOException {
		try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
			ZipEntry entry = zipFile.getEntry(fileName);
			try (InputStream in = zipFile.getInputStream(entry)) {
				final double[][] data;
				if (fileName.endsWith(LandscapeLoader.FILE_EXT_ASC)) {
					data = ASCUtil.loadDoubleAscFile(Globals.getWorldWidth(), Globals.getWorldHeight(), in, false);
				} else {
					data = GeoTiffUtil.loadGeotif(Globals.getWorldWidth(), Globals.getWorldHeight(), in, false);
				}
				return data;
			}
		}
	}

	@Override
	public DataFileMetaData getMetaData(String fileName) throws IOException {
		try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
			ZipEntry entry = zipFile.getEntry(fileName);
			try (InputStream in = zipFile.getInputStream(entry)) {

				DataFileMetaData metaData;
				if (fileName.endsWith(LandscapeLoader.FILE_EXT_ASC)) {
					metaData = ASCUtil.loadMetaData(in);
				} else {
					metaData = GeoTiffUtil.loadMetaData(in);
				}

				return metaData;
			}
		}
	}

}

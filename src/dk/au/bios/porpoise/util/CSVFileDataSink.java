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

package dk.au.bios.porpoise.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import repast.simphony.data2.DataException;
import repast.simphony.data2.DataSink;
import dk.au.bios.porpoise.PorpoiseInitializer;

/**
 * A custom DataSink for CSV files. This data sink is driven by parameters, which determine whether the data sink is
 * enabled and what kind of data is being logged.
 *
 * This class is not currently being used but is being kept in case it will be needed in the future.
 *
 * @see PorpoiseInitializer
 */
public class CSVFileDataSink implements DataSink {

	private static final String DELIM = ";";

	private final AtomicBoolean enabled = new AtomicBoolean(false);

	private final String fileName;
	private BufferedWriter fileOut;

	private boolean isNewRecord = true;

	public CSVFileDataSink(final String fileName) {
		this.fileName = fileName;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled.set(enabled);
	}

	public void setEnabled(final Boolean enabled) {
		if (enabled != null) {
			this.enabled.set(enabled.booleanValue());
		} else {
			this.enabled.set(false);
		}
	}

	@Override
	public void open(final List<String> sourceIds) {
		// System.out.println("Opening CSV file " + fileName + " [" + enabled + "]");
		// if (enabled.get()) {
		try {
			final File f = new File(fileName);
			// File f = new File("output" + File.separator + fileName);
			final File p = f.getCanonicalFile().getParentFile();
			if (p != null && !p.exists()) {
				p.mkdirs();
			}
			System.out.println("Writing to " + f.getAbsolutePath());
			fileOut = new BufferedWriter(new FileWriter(f));

			boolean first = true;
			for (final String s : sourceIds) {
				if (!first) {
					fileOut.write(DELIM);
				}
				fileOut.write("\"" + s + "\"");
				first = false;
			}
			fileOut.newLine();
		} catch (final IOException e) {
			throw new DataException("Error opening CSVFileDataSink.", e);
		}
		// }
	}

	@Override
	public void rowStarted() {
	}

	@Override
	public void append(final String key, final Object value) {
		// Assuming order is the same as with open()
		if (enabled.get()) {
			try {
				if (!isNewRecord) {
					fileOut.write(DELIM);
				}
				fileOut.write(value.toString());
				isNewRecord = false;
			} catch (final IOException e) {
				throw new DataException("Error appending to CSVFileDataSink.", e);
			}
		}
	}

	@Override
	public void rowEnded() {
	}

	@Override
	public void recordEnded() {
		if (enabled.get()) {
			try {
				fileOut.newLine();
			} catch (final IOException e) {
				throw new DataException("Error ending record for CSVFileDataSink", e);
			}
			isNewRecord = true;
		}
	}

	@Override
	public void flush() {
		if (enabled.get()) {
			try {
				fileOut.flush();
			} catch (final IOException e) {
				throw new DataException("Error flushing CSVFileDataSink", e);
			}
		}
	}

	@Override
	public void close() {
		if (enabled.get()) {
			try {
				fileOut.close();
			} catch (final IOException e) {
				throw new DataException("Error closing CSVFileDataSink", e);
			}
		}
	}

}

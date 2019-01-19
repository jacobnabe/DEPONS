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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;

public class ReplayHelper {

	/**
	 * If this is not null, replay information will be written to this Writer.
	 */
	private static BufferedWriter replayOut = null;

	public void setup() {
		int fileNum = 1;
		String fileName = null;
		do {
			final String fName = "output/replay-" + fileNum + ".txt";
			if (!Files.exists(Paths.get(fName))) {
				fileName = fName;
				break;
			}
			fileNum++;
		} while (fileNum < 10001);

		try {
			replayOut = new BufferedWriter(new FileWriter(fileName));
			System.out.println("Writing replay to " + fileName);
		} catch (final IOException e) {
			throw new RuntimeException("Error creating replay file.", e);
		}
	}

	/**
	 * Write output capturing data for a later replay.
	 *
	 * @param s The replay output to write
	 */
	public static void print(final String s) {
		if (isPrintEnabled()) {
			try {
				replayOut.write(s);
				replayOut.newLine();
				replayOut.flush();
			} catch (final IOException e) {
				throw new RuntimeException("Unable to write to replay file", e);
			}
		}
	}

	public static void print(final String s, final Object... params) {
		if (isPrintEnabled()) {
			print(MessageFormat.format(s, params));
		}
	}

	private static boolean isPrintEnabled() {
		return replayOut != null;
	}

}

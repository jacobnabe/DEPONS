/*
 * Copyright (C) 2022 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Hydrophone;
import repast.simphony.context.Context;
import repast.simphony.space.continuous.NdPoint;

public class HydrophoneLoader {

	private static final String HYDROPHONES_FILE = "hydrophones.csv";

	public static void load(final Context<Agent> context, final String landscape) throws IOException {

		if (Files.exists(Paths.get("data", landscape, HYDROPHONES_FILE))) {
			try (InputStream dataIS = new FileInputStream(Paths.get("data", landscape, HYDROPHONES_FILE).toFile())) {
				loadFromStream(context, dataIS);
			}
		} else if (Files.exists(Paths.get("data", landscape + ".zip"))) {
			try (ZipFile zf = new ZipFile(Paths.get("data", landscape + ".zip").toFile())) {
				var entry = zf.getEntry(HYDROPHONES_FILE);
				if (entry != null) {
					try (InputStream dataIS = zf.getInputStream(entry)) {
						loadFromStream(context, dataIS);
					}
				}
			}
		}
	}

	private static void loadFromStream(final Context<Agent> context, final InputStream source)
			throws JsonParseException, JsonMappingException, IOException {

		try (BufferedReader bIn = new BufferedReader(new InputStreamReader(source))) {
			int numHydrophones = 0;
			do {
				String line = bIn.readLine();
				if (line == null) {
					break;
				}

				numHydrophones++;
				String[] cols = line.split(",");
				final String name = cols[0];
				final double locX = (Double.parseDouble(cols[1]) - Globals.getXllCorner()) / 400.0d;
				final double locY = (Double.parseDouble(cols[2]) - Globals.getYllCorner()) / 400.0d;

				Hydrophone h = new Hydrophone(numHydrophones, name);
				context.add(h);
				h.setPosition(new NdPoint(locX, locY));
			} while (true);
		}
	}
}

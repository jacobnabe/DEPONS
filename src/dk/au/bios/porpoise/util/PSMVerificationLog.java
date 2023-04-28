/*
 * Copyright (C) 2017-2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;

public final class PSMVerificationLog {

	private PSMVerificationLog() {
		// Utility class, prevent instances.
	}

	/**
	 * If not null, information for verifying the PSM behavior is written to this Writer.
	 */
	private static PrintWriter psmVerificationOutput = null;

	public static void setup() {
		if (psmVerificationOutput != null) {
			psmVerificationOutput.close();
		}
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MMM.dd.HH_mm_ss_SS");
		final File psmVerificationOutputFile = new File("PersistentSpatialMemory." + sdf.format(new Date()) + ".csv");
		try {
			psmVerificationOutput = new PrintWriter(psmVerificationOutputFile);

			psmVerificationOutput.printf("\"stepType\",");
			if (RunEnvironment.getInstance().isBatch()) {
				psmVerificationOutput.printf("\"run\",");
			}

			psmVerificationOutput.printf("\"tick\",\"Id\",\"UtmX\",\"UtmY\",\"DispersalMode\",\"PSMActive\","
					+ "\"PSMTargetUtmX\",\"PSMTargetUtmY\",\"psmHeading\",\"distance\",\"distanceLeft\"%n");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static void print(final String stepType, final Porpoise porpoise, final double distanceTravelled) {
		if (psmVerificationOutput == null) {
			return;
		}

		psmVerificationOutput.printf("%s,", stepType);
		if (RunEnvironment.getInstance().isBatch()) {
			psmVerificationOutput.printf("%d,", RunState.getInstance().getRunInfo().getRunNumber());
		}
		double psmUtmX;
		if (porpoise.getPersistentSpatialMemory() == null || !porpoise.getDispersalBehaviour().isDispersing()) {
			psmUtmX = -1;
		} else {
			psmUtmX = Globals.convertGridXToUtm(porpoise.getDispersalBehaviour().getTargetPosition().getX());
		}
		double psmUtxY;
		if (porpoise.getPersistentSpatialMemory() == null || !porpoise.getDispersalBehaviour().isDispersing()) {
			psmUtxY = -1;
		} else {
			psmUtxY = Globals.convertGridYToUtm(porpoise.getDispersalBehaviour().getTargetPosition().getY());
		}

		psmVerificationOutput.printf("%.1f,%d,%.4f,%.4f,%d,%s,%.4f,%.4f,%.3f,%.4f,%.4f%n", SimulationTime.getTick(), porpoise
				.getId(), porpoise.getUtmX(), porpoise.getUtmY(), porpoise.getDispersalMode(),
				Boolean.toString(porpoise.getDispersalBehaviour().isDispersing()).toUpperCase(), psmUtmX, psmUtxY,
				porpoise.getDispersalBehaviour().getTargetHeading(), Globals.convertGridDistanceToUtm(distanceTravelled), 
				Globals.convertGridDistanceToUtm(porpoise.getDispersalBehaviour().getDistanceLeftToTravel()));
		psmVerificationOutput.flush();
	}

}

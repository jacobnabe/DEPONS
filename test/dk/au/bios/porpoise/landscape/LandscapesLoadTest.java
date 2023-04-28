/*
 * Copyright (C) 2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import dk.au.bios.porpoise.Globals;
import repast.simphony.engine.environment.DefaultScheduleRunner;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.parameter.ParametersParser;

public class LandscapesLoadTest {

	@ParameterizedTest
	@ValueSource(strings = { "Kattegat", "NorthSea", "DanTysk", "Gemini", "Homogeneous", "UserDefined" })
	void loadLandscape(String landscape) throws Exception {
		ParametersParser paramSpecParser = new ParametersParser(new File("DEPONS.rs/parameters.xml"));
		var params = paramSpecParser.getParameters();
		Schedule schedule = new Schedule();
		var testRunner = new DefaultScheduleRunner();
		RunEnvironment.init(schedule, testRunner, params, false);

		var loader = new LandscapeLoader(landscape);
		var data = loader.load();
		
		// verify bathy, sediment, disttocoast, prey, salinity
		for (int x = 0; x < Globals.getWorldWidth(); x++) {
			for (int y = 0; y < Globals.getWorldHeight(); y++) {
				double depth = data.getDepth(x, y);
				double sediment = data.getSediment(x, y);
				double salinity = data.getSalinity(x, y);
				double distToCoast = data.getDistanceToCoast(x, y);
				double prey = data.getMaxEnt(x, y);
				double patches = data.getFoodProb(x, y);
				
				if (depth == -9999) {
					assertThat(sediment).isEqualTo(-9999);
//					assertThat(salinity).isEqualTo(-9999);
//					assertThat(distToCoast).isEqualTo(-9999);
//					assertThat(prey).isEqualTo(-9999);
//					assertThat(patches).isEqualTo(-9999);
				} else {
					assertThat(sediment).isNotEqualTo(-9999);
//					assertThat(salinity).isNotEqualTo(-9999);
//					assertThat(distToCoast).isNotEqualTo(-9999);
//					assertThat(prey).isNotEqualTo(-9999);
//					assertThat(patches).isNotEqualTo(-9999);
				}
			}
		}
	}

	
}

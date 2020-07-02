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

import org.junit.Test;

import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals
import dk.au.bios.porpoise.behavior.DispersalFactory
import dk.au.bios.porpoise.landscape.DataFileMetaData
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext
import repast.simphony.engine.environment.RunEnvironment
import repast.simphony.engine.environment.RunState
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.parameter.ParametersCreator;
import repast.simphony.space.continuous.NdPoint;
import spock.lang.Shared;
import spock.lang.Specification

class DebugLogTest extends Specification {

	private Context<Agent> context;
	@Shared Schedule schedule;

	def setup() {
		Globals.landscapeMetaData = new DataFileMetaData(100, 100, 529473, 5972242, 400 , null);

		// Repast initialization
		this.schedule = new Schedule();
		RunEnvironment.init(schedule, null, null, true);
		context = new DefaultContext<>();
		//		System.out.println("Loading model");
		//		context = new PorpoiseSimBuilder().build(context);
		RunState.init().setMasterContext(context);
		DispersalFactory.type = "off";
	}

	def debug10() {
		given:
		final ParametersCreator pcrt = new ParametersCreator();
		pcrt.addParameter("debug", Integer.class, 10, true);
		DebugLog.initialize(pcrt.createParameters());

		when: "execute first tick"
		DebugLog.print10(null, "Tracking random porpoise {} @{} ({},{}), presLovMov: {}", 1, new NdPoint(10, 10),
			65010, 63010, 0.83243243d);

		then:
		true    // Weak assertion, only a basic test
	}

}

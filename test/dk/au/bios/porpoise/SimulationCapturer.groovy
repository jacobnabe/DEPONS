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

package dk.au.bios.porpoise;

import static org.junit.Assert.*
import static spock.util.matcher.HamcrestMatchers.closeTo

import java.util.function.Consumer

import repast.simphony.context.Context
import repast.simphony.context.DefaultContext
import repast.simphony.engine.environment.DefaultScheduleRunner
import repast.simphony.engine.environment.RunEnvironment
import repast.simphony.engine.environment.RunState
import repast.simphony.engine.environment.Runner
import repast.simphony.engine.schedule.Schedule
import repast.simphony.parameter.Parameters
import repast.simphony.parameter.ParametersParser
import repast.simphony.random.RandomHelper
import dk.au.bios.porpoise.util.SimulationTime
import dk.au.bios.porpoise.util.test.PorpoiseTestDataCapturer

captureKattegatDispOff();
captureKattegatUndirected();
captureKattegatIDW();
captureDanTyskDispOffTurbines();
captureDanTyskUndirected();
captureDanTyskPSM2();
captureNorthSeaDispOff();
captureNorthSeaDispOffTurbsUser();
captureNorthSeaPSM2();
captureNorthSeaUndirected();



def captureKattegatDispOff() {
	capture(new File("testdata_Kattegat_DispOff_NoTurbines_1porp.txt"), { params ->
		params.setValue("randomSeed", 1955024704);
		params.setValue("landscape", "Kattegat");
		params.setValue("turbines", "off");
		params.setValue("dispersal", "off");
		params.setValue("porpoiseCount", 1);
	});
}

def captureKattegatUndirected() {
	capture(new File("testdata_Kattegat_Undirected_NoTurbines_1porp.txt"), { params ->
		params.setValue("randomSeed", 1955024704);
		params.setValue("landscape", "Kattegat");
		params.setValue("turbines", "off");
		params.setValue("dispersal", "Undirected");
		params.setValue("porpoiseCount", 1);
	});
}

def captureKattegatIDW() {
	capture(new File("testdata_Kattegat_InnerDanishWaters_NoTurbines_1porp.txt"), { params ->
		params.setValue("randomSeed", 1955024704);
		params.setValue("landscape", "Kattegat");
		params.setValue("turbines", "off");
		params.setValue("dispersal", "InnerDanishWaters");
		params.setValue("porpoiseCount", 1);
	});
}

def captureDanTyskDispOffTurbines() {
	capture(new File("testdata_DanTysk_DispOff_Turbines_10porp.txt"), { params ->
		params.setValue("randomSeed", 267022757);
		params.setValue("landscape", "DanTysk");
		params.setValue("turbines", "DanTysk-construction");
		params.setValue("dispersal", "off");
		params.setValue("porpoiseCount", 10);
	});
}

def captureDanTyskUndirected() {
	capture(new File("testdata_DanTysk_Undirected_NoTurbines_10porp.txt"), { params ->
		params.setValue("randomSeed", 267022757);
		params.setValue("landscape", "DanTysk");
		params.setValue("turbines", "off");
		params.setValue("dispersal", "Undirected");
		params.setValue("porpoiseCount", 10);
	});
}

def captureDanTyskPSM2() {
	capture(new File("testdata_DanTysk_PSM2_NoTurbines_10porp.txt"), { params ->
		params.setValue("randomSeed", 267022757);
		params.setValue("landscape", "DanTysk");
		params.setValue("turbines", "off");
		params.setValue("dispersal", "PSM-Type2");
		params.setValue("porpoiseCount", 10);
	});
}

def captureNorthSeaDispOff() {
	capture(new File("testdata_NorthSea_DispOff_NoTurbines_1porp.txt"), { params ->
		params.setValue("randomSeed", 1955024704);
		params.setValue("landscape", "NorthSea");
		params.setValue("turbines", "off");
		params.setValue("dispersal", "off");
		params.setValue("porpoiseCount", 1);
	});
}

def captureNorthSeaDispOffTurbsUser() {
	capture(new File("testdata_NorthSea_DispOff_UserDefined_1porp.txt"), { params ->
		params.setValue("randomSeed", 1955024704);
		params.setValue("landscape", "NorthSea");
		params.setValue("turbines", "User-def");
		params.setValue("dispersal", "off");
		params.setValue("porpoiseCount", 1);
	});
}

def captureNorthSeaPSM2() {
	capture(new File("testdata_NorthSea_PSM2_NoTurbines_1porp.txt"), { params ->
		params.setValue("randomSeed", 1955024704);
		params.setValue("landscape", "NorthSea");
		params.setValue("turbines", "off");
		params.setValue("dispersal", "PSM-Type2");
		params.setValue("porpoiseCount", 1);
	});
}

def captureNorthSeaUndirected() {
	capture(new File("testdata_NorthSea_Undirected_NoTurbines_1porp.txt"), { params -> 
		params.setValue("randomSeed", 1755024704);
		params.setValue("landscape", "NorthSea");
		params.setValue("turbines", "off");
		params.setValue("dispersal", "Undirected");
		params.setValue("porpoiseCount", 1);
	});
}

def capture(File captureFile, Consumer<Parameters> paramCustomizer) {
	PorpoiseTestDataCapturer.capture = true;
	PorpoiseTestDataCapturer.tickMod = 1000;
	//PorpoiseTestDataCapturer.tickStartCapture = 40000;
	//PorpoiseTestDataCapturer.tickEndCapture = 42010;
	PorpoiseTestDataCapturer.includePosList = false;
	PorpoiseTestDataCapturer.includePopulationSize = true;
	PorpoiseTestDataCapturer.captureFile = captureFile;
	
	// Repast initialization
	ParametersParser paramSpecParser = new ParametersParser(new File("DEPONS.rs/parameters.xml"));
	def params = paramSpecParser.getParameters()
	paramCustomizer.accept(params)
	params.setValue("trackedPorpoiseCount", 0);
	
	RandomHelper.setSeed(params.getInteger("randomSeed"))
	Schedule schedule = new Schedule();
	Runner testRunner = new DefaultScheduleRunner();
	RunEnvironment.init(schedule, testRunner, params, false);
	
	Context<Agent> context = new DefaultContext<>();
	PorpoiseSimBuilder simBuilder = new PorpoiseSimBuilder();
	context = simBuilder.build(context);
	RunState.init().setMasterContext(context);
	
	// The @ScheduledMethod annotation on Porpoise is not automatically processed
	context.getObjects(Porpoise.class).each {
		RunEnvironment.getInstance().getCurrentSchedule().schedule(it)
	}
	
	//(0..86400).each {
	(0..90000).each {
		RunEnvironment.getInstance().getCurrentSchedule().execute()
		println "tick ${SimulationTime.tick}"
	}

}

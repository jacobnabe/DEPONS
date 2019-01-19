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
import groovy.json.JsonSlurper
import repast.simphony.context.Context
import repast.simphony.context.DefaultContext
import repast.simphony.engine.environment.DefaultScheduleRunner
import repast.simphony.engine.environment.RunEnvironment
import repast.simphony.engine.environment.RunState
import repast.simphony.engine.environment.Runner
import repast.simphony.engine.schedule.Schedule
import repast.simphony.parameter.ParametersParser
import repast.simphony.random.RandomHelper
import dk.au.bios.porpoise.util.SimulationTime

/**
 * Abstract base class for replayed tests. Replayed tests are simulations which are captured using the
 * SimulationCapturer and replayed. At certain intervals (based on the capture file) it is verified that
 * the current simulation matches the previously captured simulation.
 */
abstract class AbstractReplayedSimulationTest {

	private Context<Agent> context;
	private Runner testRunner;

	//	private Porpoise p;
	//	private RandomSource random;

	//	private static UnitTestRunner runner;

	//	@BeforeClass
	void setupContext(String paramsJson) {
		// Repast initialization
		ParametersParser paramSpecParser = new ParametersParser(new File("DEPONS.rs/parameters.xml"));
		def params = paramSpecParser.getParameters()
		println "RandomSeed: " + params.getInteger("randomSeed");

		def jsonSlurper = new JsonSlurper()
		def jsonParams = jsonSlurper.parseText(paramsJson)

		jsonParams.parameters.each {
			println "Setting " + it.key + "=" + it.value
			params.setValue(
					it.key,
					it.value)
		}

		RandomHelper.setSeed(params.getInteger("randomSeed"))
		Schedule schedule = new Schedule();
		this.testRunner = new DefaultScheduleRunner();
		RunEnvironment.init(schedule, testRunner, params, false);

		context = new DefaultContext<>();
		PorpoiseSimBuilder simBuilder = new PorpoiseSimBuilder();
		context = simBuilder.build(context);
		RunState.init().setMasterContext(context);

		// The @ScheduledMethod annotation on Porpoise is not automatically processed
		context.getObjects(Porpoise.class).each {
			RunEnvironment.getInstance().getCurrentSchedule().schedule(it)
		}
	}

	void "simulation"(String testDatafileName) {
		def testdata = new File(testDatafileName);
		if (!testdata.exists()) {
			fail("Missing testdata file")
		}
		def jsonSlurper = new JsonSlurper()

		testdata.eachLine { line, lineNum ->
			if (lineNum == 1) {
				setupContext(line)
				return
			}
			def porpData = jsonSlurper.parseText(line)
			if (porpData.tick == null) {
				return;
			}
			//			println "tick ${SimulationTime.tick} (${porpData.tick}) - porp ${porpData.porp.id}"
			while (porpData.tick > SimulationTime.tick) {
				RunEnvironment.getInstance().getCurrentSchedule().execute()
			}
			//			println "tick ${SimulationTime.tick} (${porpData.tick}) - porp ${porpData.porp.id}"

			assert SimulationTime.tick == porpData.tick

			if (porpData.porp != null) {
				def porp = findPorpoiseById(porpData.porp.id)

				assertEquals("Location X mismatch for porpoise " + porp.id + " at tick " + SimulationTime.tick, porpData.porp.x, porp.getPosition().x, 0.09)
				assertEquals("Location Y mismatch for porpoise " + porp.id + " at tick " + SimulationTime.tick, porpData.porp.y, porp.getPosition().y, 0.09)
				assertEquals("Heading mismatch for porpoise " + porp.id + " at tick " + SimulationTime.tick, porpData.porp.heading, porp.getHeading(), 0.09)
				assertEquals("PrevAngle mismatch for porpoise " + porp.id + " at tick " + SimulationTime.tick, porpData.porp.prevAngle, porp.prevAngle, 0.09)
				//				assertEquals("prevLogMov mismatch for porpoise " + porp.id + " at tick " + SimulationTime.tick, porpData.porp.prevLogMov, porp.prevLogMov, 0.09)
			} else if (porpData.sim != null) {
				def populationSize = findPopulationSize()
				assertEquals("Population size at tick " + SimulationTime.tick, porpData.sim.populationSize, populationSize)
				//				println "Sim data: ${line}"
			} else {
				fail "Unknown test data: ${line}"
			}
		}


	}

	def Porpoise findPorpoiseById(int id) {
		for (porp in context.getObjects(Porpoise.class)) {
			if (porp.id == id) {
				return porp;
			}
		}

		return null;
	}

	def int findPopulationSize() {
		int popSize = 0;
		for (porp in context.getObjects(Porpoise.class)) {
			popSize++;
		}

		return popSize;
	}
}

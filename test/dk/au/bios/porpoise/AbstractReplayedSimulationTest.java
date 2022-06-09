/*
 * Copyright (C) 2017-2022 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.au.bios.porpoise.util.SimulationTime;
import dk.au.bios.porpoise.util.test.CapturedSimulation.SimParams;
import dk.au.bios.porpoise.util.test.CapturedSimulation.SimTick;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.engine.environment.DefaultScheduleRunner;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.environment.Runner;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.parameter.ParametersParser;
import repast.simphony.random.RandomHelper;

/**
 * Abstract base class for replayed tests. Replayed tests are simulations which are captured using the
 * SimulationCapturer and replayed. At certain intervals (based on the capture file) it is verified that
 * the current simulation matches the previously captured simulation.
 */
public abstract class AbstractReplayedSimulationTest {

	private Context<Agent> context;
	private Runner testRunner;

	void setupContext(Map<String, Object> simParams) throws Exception {
		// Repast initialization
		ParametersParser paramSpecParser = new ParametersParser(new File("DEPONS.rs/parameters.xml"));
		var params = paramSpecParser.getParameters();
		System.out.println("RandomSeed: " + params.getInteger("randomSeed"));

		simParams.forEach((k, v) -> {
			System.out.println("Setting " + k + "=" + v);
			params.setValue(k, v);
		});

		RandomHelper.setSeed(params.getInteger("randomSeed"));
		Schedule schedule = new Schedule();
		this.testRunner = new DefaultScheduleRunner();
		RunEnvironment.init(schedule, testRunner, params, false);

		context = new DefaultContext<>();
		PorpoiseSimBuilder simBuilder = new PorpoiseSimBuilder();
		context = simBuilder.build(context);
		RunState.init().setMasterContext(context);

		// The @ScheduledMethod annotation on Porpoise is not automatically processed
		context.getObjectsAsStream(Porpoise.class).forEach(p -> {
			RunEnvironment.getInstance().getCurrentSchedule().schedule(p);
		});
	}

	public void simulation(String testDatafileName) throws Exception {
		var testdata = new File(testDatafileName);
		if (!testdata.exists()) {
			fail("Missing testdata file");
		}
		
		var jsonMapper = new ObjectMapper();

		int lineNum = 0;
		List<String> lines = Files.readAllLines(testdata.toPath());

		for (String line : lines) {
			lineNum++;
			if (lineNum == 1) {
				// TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
				// HashMap<String, Object> simParams = jsonMapper.readValue(line, typeRef);
				SimParams simParams = jsonMapper.readValue(line, SimParams.class);
				setupContext(simParams.parameters);
				continue;
			}
			
			var porpData = jsonMapper.readValue(line, SimTick.class);
			
//			if (porpData.tick == null) {
//				return;
//			}

			//			println "tick ${SimulationTime.tick} (${porpData.tick}) - porp ${porpData.porp.id}"
			while (porpData.tick > SimulationTime.getTick()) {
				RunEnvironment.getInstance().getCurrentSchedule().execute();
			}
			//			println "tick ${SimulationTime.tick} (${porpData.tick}) - porp ${porpData.porp.id}"

			assertThat(SimulationTime.getTick()).isEqualTo(porpData.tick);

			if (porpData.porp != null) {
				var porp = findPorpoiseById(porpData.porp.id);

				assertThat(porp.getPosition().getX()).withFailMessage("Location X mismatch for porpoise " + porp.getId() + " at tick " + SimulationTime.getTick()).isEqualTo(porpData.porp.x, within(0.09));
				assertThat(porp.getPosition().getY()).withFailMessage("Location Y mismatch for porpoise " + porp.getId() + " at tick " + SimulationTime.getTick()).isEqualTo(porpData.porp.y, within(0.09));
				assertThat(porp.getHeading()).withFailMessage("Heading mismatch for porpoise " + porp.getId() + " at tick " + SimulationTime.getTick()).isEqualTo(porpData.porp.heading, within(0.09));
				assertThat(porp.getPrevAngle()).withFailMessage("PrevAngle mismatch for porpoise " + porp.getId() + " at tick " + SimulationTime.getTick()).isEqualTo(porpData.porp.prevAngle, within(0.09));
				//				assertEquals("prevLogMov mismatch for porpoise " + porp.id + " at tick " + SimulationTime.tick, porpData.porp.prevLogMov, porp.prevLogMov, 0.09)
			} else if (porpData.sim != null) {
				var populationSize = findPopulationSize();
				assertThat(populationSize).withFailMessage("Population size at tick " + SimulationTime.getTick()).isEqualTo(porpData.sim.populationSize);
				//				println "Sim data: ${line}"
			} else {
				fail("Unknown test data: ${line}");
			}
		}
	}

	public Porpoise findPorpoiseById(long id) {
		return (Porpoise) context.getObjectsAsStream(Porpoise.class).filter(p -> p.getId() == id).findAny().orElse(null);
	}

	public long findPopulationSize() {
		return context.getObjectsAsStream(Porpoise.class).count();
	}

}

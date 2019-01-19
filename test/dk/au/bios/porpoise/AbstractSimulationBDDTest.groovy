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
import repast.simphony.context.Context
import repast.simphony.context.DefaultContext
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder
import repast.simphony.context.space.grid.GridFactoryFinder
import repast.simphony.engine.environment.DefaultScheduleRunner
import repast.simphony.engine.environment.RunEnvironment
import repast.simphony.engine.environment.RunState
import repast.simphony.engine.environment.Runner
import repast.simphony.engine.schedule.Schedule
import repast.simphony.parameter.ParametersParser
import repast.simphony.random.RandomHelper
import repast.simphony.space.continuous.BouncyBorders
import repast.simphony.space.continuous.NdPoint
import repast.simphony.space.continuous.RandomCartesianAdder
import repast.simphony.space.grid.GridBuilderParameters
import repast.simphony.space.grid.SimpleGridAdder
import spock.lang.Specification
import dk.au.bios.porpoise.behavior.FastRefMemTurn
import dk.au.bios.porpoise.behavior.RandomSource
import dk.au.bios.porpoise.landscape.CellDataTestData

/**
 * Abstract base class for replayed tests. Replayed tests are simulations which are captured using the
 * SimulationCapturer and replayed. At certain intervals (based on the capture file) it is verified that
 * the current simulation matches the previously captured simulation.
 */
abstract class AbstractSimulationBDDTest extends Specification {

	protected static Context<Agent> context;
	protected static Schedule schedule;
	protected static RandomSource random;

	def factory;
	def space;
	def gridFactory;
	def grid;
	def cellData;

	private Runner testRunner;

	def aNewWorld(int worldWidth, int worldHeight) {
		Globals.worldWidth = 100;
		Globals.worldHeight = 100;
		random = Mock(RandomSource)
		random.nextCrwAngle() >>> [0.0]
		random.nextCrwAngleWithM() >>> [0.0]
		random.nextStdMove() >>> [0.0]
		random.nextCrwStepLength() >>> [0.0]
		Globals.randomSource = random

		// Repast initialization
		schedule = new Schedule();
		RunEnvironment.init(schedule, null, null, true);
		context = new DefaultContext<>();
		RunState.init().setMasterContext(context);

		factory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
		space = factory.createContinuousSpace("space", context, new RandomCartesianAdder<Agent>(), new BouncyBorders(), [
			Globals.worldWidth,
			Globals.worldHeight] as double[], [0.5f, 0.5f] as double[])
		gridFactory = GridFactoryFinder.createGridFactory(null);
		grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Agent>(new repast.simphony.space.grid.BouncyBorders(), new SimpleGridAdder<Agent>(), true, Globals.worldWidth, Globals.worldHeight));
		cellData = CellDataTestData.getCellData();
		Globals.setCellData(cellData);
	}

	def aHomogenousWorld() {
		ParametersParser paramSpecParser = new ParametersParser(new File("DEPONS.rs/parameters.xml"));
		def params = paramSpecParser.getParameters()
		params.setValue("randomSeed", 873490);
		params.setValue("landscape", "Homogeneous");
		params.setValue("dispersal", "PSM-Type3");
		params.setValue("porpoiseCount", 1);
		params.setValue("trackedPorpoiseCount", 0);

		println "RandomSeed: " + params.getInteger("randomSeed");
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

	def aPorpoise(double x, double y, double heading) {
		def p = new Porpoise(space, grid, context, 1, new FastRefMemTurn())
		context.add(p);
		p.setPosition(new NdPoint(50.0, 50.0));
		p.setHeading(0.0);
		p.moveAwayFromLand();  // Weird side-effect here, updating the initial poslist

		// The @ScheduledMethod annotation on Porpoise is not automatically processed
		schedule.schedule(p);

		return p;
	}

	def Porpoise findPorpoiseById(int id) {
		for (porp in context.getObjects(Porpoise.class)) {
			if (porp.id == id) {
				return porp;
			}
		}

		return null;
	}

}

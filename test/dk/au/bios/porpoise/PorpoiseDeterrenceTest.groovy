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
import repast.simphony.context.Context
import repast.simphony.context.DefaultContext
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder
import repast.simphony.context.space.grid.GridFactoryFinder
import repast.simphony.engine.environment.RunEnvironment
import repast.simphony.engine.environment.RunState
import repast.simphony.engine.schedule.Schedule
import repast.simphony.space.continuous.BouncyBorders
import repast.simphony.space.continuous.NdPoint
import repast.simphony.space.continuous.RandomCartesianAdder
import repast.simphony.space.grid.GridBuilderParameters
import repast.simphony.space.grid.SimpleGridAdder
import spock.lang.Shared
import spock.lang.Specification
import dk.au.bios.porpoise.behavior.DispersalFactory
import dk.au.bios.porpoise.behavior.RandomSource
import dk.au.bios.porpoise.landscape.CellDataTestData
import dk.au.bios.porpoise.landscape.DataFileMetaData

/**
 * Unit test for Porpoise agent with focus on the deterrence behaviour.
 */
class PorpoiseDeterrenceTest extends Specification {

	private Context<Agent> context;
	@Shared Schedule schedule;

	private space;
	private grid;
	private cellData;

	def setup() {
		Globals.landscapeMetaData = new DataFileMetaData(100, 100, 529473, 5972242, 400 , null);

		// Repast initialization
		this.schedule = new Schedule();
		RunEnvironment.init(schedule, null, null, true);
		context = new DefaultContext<>();
		//		System.out.println("Loading model");
		//		context = new PorpoiseSimBuilder().build(context);
		RunState.init().setMasterContext(context);

		def factory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
		space = factory.createContinuousSpace("space", context, new RandomCartesianAdder<Agent>(), new BouncyBorders(), [Globals.worldWidth, Globals.worldHeight] as double[], [0.5f, 0.5f] as double[])
		def gridFactory = GridFactoryFinder.createGridFactory(null);
		grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Agent>(new repast.simphony.space.grid.BouncyBorders(), new SimpleGridAdder<Agent>(), true, Globals.worldWidth, Globals.worldHeight));
		cellData = CellDataTestData.getCellData();
		Globals.cellData = cellData;
		DispersalFactory.type = "off";

				def random = Mock(RandomSource)
		random.nextNormal_0_38() >>> [0.0]
		random.nextNormal_96_28() >>> [0.0]
		random.nextStdMove() >>> [0.0]
		random.nextNormal_42_48() >>> [0.0]

		Globals.randomSource = random
	}

	def "deterrence decay 50"() {
		given: "test has been set up"
		SimulationParameters.deterDecay = 50;
		SimulationParameters.deterTime = 1000;
		def p = new Porpoise(space, grid, context, 1, null)
		context.add(p);
		p.setPosition(new NdPoint(10.0, 10.0));
		p.setHeading(0.0);

		def turb = new Turbine(space, grid, "Test Turbing", 64, 5.0, 5.0, 100, 150, 1);
		context.add(turb);
		turb.setPosition(new NdPoint(5.0, 5.0));

		expect: "initial checks"
		p.getDeterStrength() == 0.0;

		when: "deter porpoise"; p.deter(64, turb);
		then: p.getDeterStrength() == 64;

		[32.0, 16.0, 8.0, 4.0, 2.0, 1.0, 0.5, 0.25, 0.125].each {
			when: p.updateDeterence();
			then: assert p.getDeterStrength() == it;
		}
	}

	def "deterrence decay 25"() {
		given: "test has been set up"
		SimulationParameters.deterDecay = 25;
		SimulationParameters.deterTime = 1000;
		def p = new Porpoise(space, grid, context, 1, null)
		context.add(p);
		p.setPosition(new NdPoint(10.0, 10.0));
		p.setHeading(0.0);

		def turb = new Turbine(space, grid, "Test Turbing", 64, 5.0, 5.0, 100, 150, 1);
		context.add(turb);
		turb.setPosition(new NdPoint(5.0, 5.0));

		expect: "initial checks"
		p.getDeterStrength() == 0.0;

		when: "deter porpoise"; p.deter(64, turb);
		then: p.getDeterStrength() == 64;

		[48.0, 36.0, 27.0, 20.25, 15.1875, 11.390625, 8.54296875, 6.4072265625].each {
			when: p.updateDeterence();
			then: assert p.getDeterStrength() == it;
		}
	}

	def "deterrence time"() {
		given: "test has been set up"
		SimulationParameters.deterDecay = 50;
		SimulationParameters.deterTime = 5;
		def p = new Porpoise(space, grid, context, 1, null)
		context.add(p);
		p.setPosition(new NdPoint(10.0, 10.0));
		p.setHeading(0.0);

		def turb = new Turbine(space, grid, "Test Turbing", 64, 5.0, 5.0, 100, 150, 1);
		context.add(turb);
		turb.setPosition(new NdPoint(5.0, 5.0));

		expect: "initial checks"
		p.getDeterStrength() == 0.0;

		when: "deter porpoise"; p.deter(64, turb);
		then: p.getDeterStrength() == 64;

		[32.0, 16.0, 8.0, 4.0, 2.0, 0.0].each {
			when: p.updateDeterence();
			then: assert p.getDeterStrength() == it;
		}

		// Deter time = 2
		when:
		SimulationParameters.deterTime = 2;
		p.deter(64, turb);

		then: p.getDeterStrength() == 64;

		[32.0, 16.0, 0.0].each {
			when: p.updateDeterence();
			then: assert p.getDeterStrength() == it;
		}

		// Deter time = 7
		when:
		SimulationParameters.deterTime = 7;
		p.deter(64, turb);

		then: p.getDeterStrength() == 64;

		[32.0, 16.0, 8.0, 4.0, 2.0, 1.0, 0.5, 0.0].each {
			when: p.updateDeterence();
			then: assert p.getDeterStrength() == it;
		}
	}

}

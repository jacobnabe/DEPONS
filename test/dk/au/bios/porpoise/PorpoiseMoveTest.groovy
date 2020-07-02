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
import repast.simphony.engine.environment.RunEnvironment
import repast.simphony.engine.environment.RunState
import repast.simphony.engine.schedule.Schedule
import repast.simphony.space.continuous.BouncyBorders
import repast.simphony.space.continuous.NdPoint
import repast.simphony.space.continuous.RandomCartesianAdder
import repast.simphony.space.grid.GridBuilderParameters
import repast.simphony.space.grid.SimpleGridAdder
import spock.lang.Specification
import dk.au.bios.porpoise.behavior.DispersalFactory
import dk.au.bios.porpoise.behavior.FastRefMemTurn
import dk.au.bios.porpoise.behavior.RandomSource
import dk.au.bios.porpoise.landscape.CellDataTestData
import dk.au.bios.porpoise.landscape.DataFileMetaData

/**
 * Unit test for the Porpoise agent.
 */
class PorpoiseMoveTest extends Specification {

	private Context<Agent> context;
	private Schedule schedule;

	def setupSpec() {
		Globals.landscapeMetaData = new DataFileMetaData(100, 100, 529473, 5972242, 400 , null);
		SimulationParameters.model = 4;
	}

	def setup() {
		// Repast initialization
		this.schedule = new Schedule();
		RunEnvironment.init(schedule, null, null, true);
		context = new DefaultContext<>();
		RunState.init().setMasterContext(context);
	}

	def "movement without dispersal"() {
		given: "test has been set up"
		def factory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
		def space = factory.createContinuousSpace("space", context, new RandomCartesianAdder<Agent>(), new BouncyBorders(), [Globals.worldWidth, Globals.worldHeight] as double[], [0.5f, 0.5f] as double[])
		def gridFactory = GridFactoryFinder.createGridFactory(null);
		def grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Agent>(new repast.simphony.space.grid.BouncyBorders(), new SimpleGridAdder<Agent>(), true, Globals.worldWidth, Globals.worldHeight));
		def cellData = CellDataTestData.getCellData();
		Globals.cellData = cellData;
		DispersalFactory.type = "off";

		and: "no randomness"
		def random = Mock(RandomSource)
		random.nextNormal_0_38() >>> [0.0]
		random.nextNormal_96_28() >>> [0.0]
		random.nextStdMove() >>> [0.0]
		random.nextNormal_42_48() >>> [0.0]
		Globals.randomSource = random

		and: "a porpoise at 10,10 heading north"
		def p = new Porpoise(space, grid, context, 1, new FastRefMemTurn())
		context.add(p);
		p.setPosition(new NdPoint(10.0, 10.0));
		p.setHeading(0.0);
		p.moveAwayFromLand();  // Weird side-effect here, updating the initial poslist

		expect: "the porpoise at 10,10 heading north"
		p.getPosition() == new NdPoint(10.0f, 10.0f)
		p.getHeading() == 0.0
		p.getAge() == 1

		when: "moving one step"
		p.move()

		then: "new location"
		7.37 closeTo(p.getPosition().x, 0.009)
		7.28 closeTo(p.getPosition().y, 0.009)
		224.07 closeTo(p.getHeading(), 0.009)
		(-135.93) closeTo(p.prevAngle, 0.009)
		0.0 closeTo(p.presAngle, 0.009)

		when: "moving another step"
		p.move()

		then:
		4.74 closeTo(p.getPosition().x, 0.009)
		4.56 closeTo(p.getPosition().y, 0.009)
		224.07 closeTo(p.getHeading(), 0.009)
		0.0 closeTo(p.prevAngle, 0.009)
		0.0 closeTo(p.presAngle, 0.009)

		when: "moving 10 steps"
		10.times({ p.move() })

		then:
		4.74 closeTo(p.getPosition().x, 0.009)
		4.56 closeTo(p.getPosition().y, 0.009)
		134.07 closeTo(p.getHeading(), 0.009)
		180.0 closeTo(p.prevAngle, 0.009)
		0.0 closeTo(p.presAngle, 0.009)
	}

}

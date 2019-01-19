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
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import dk.au.bios.porpoise.behavior.DispersalFactory
import dk.au.bios.porpoise.behavior.FastRefMemTurn
import dk.au.bios.porpoise.behavior.RandomSource
import dk.au.bios.porpoise.landscape.CellDataTestData

/**
 * Unit test for the Porpoise agent.
 */
class PorpoiseMoveUnrollTest extends Specification {

	@Shared private Context<Agent> context;
	@Shared private Schedule schedule;

	@Shared Porpoise p;
	@Shared RandomSource random;

	def setupSpec() {
		Globals.worldWidth = 100;
		Globals.worldHeight = 100;
		SimulationParameters.model = 4;
	
		random = Mock(RandomSource)
		random.nextCrwAngle() >>> [0.0]
		random.nextCrwAngleWithM() >>> [0.0]
		random.nextStdMove() >>> [0.0]
		random.nextCrwStepLength() >>> [0.0]
		Globals.randomSource = random

		// Repast initialization
		this.schedule = new Schedule();
		RunEnvironment.init(schedule, null, null, true);
		context = new DefaultContext<>();
		RunState.init().setMasterContext(context);

		def factory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
		def space = factory.createContinuousSpace("space", context, new RandomCartesianAdder<Agent>(), new BouncyBorders(), [Globals.worldWidth, Globals.worldHeight] as double[], [0.5f, 0.5f] as double[])
		def gridFactory = GridFactoryFinder.createGridFactory(null);
		def grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Agent>(new repast.simphony.space.grid.BouncyBorders(), new SimpleGridAdder<Agent>(), true, Globals.worldWidth, Globals.worldHeight));
		def cellData = CellDataTestData.getCellData();
		Globals.cellData = cellData;
		DispersalFactory.type = "off";

		p = new Porpoise(space, grid, context, 1, new FastRefMemTurn())
		context.add(p);
		p.setPosition(new NdPoint(10.0, 10.0));
		p.setHeading(0.0);
		p.moveAwayFromLand();  // Weird side-effect here, updating the initial poslist

		assert p.getPosition() == new NdPoint(10.0f, 10.0f)
		assert p.getHeading() == 0.0
		assert p.getAge() == 1
	}

	@Unroll
	def "movement without dispersal - #tick"() {
		when:
		p.move()

		then:
		new Double(x) closeTo(p.getPosition().x, 0.009)
		new Double(y) closeTo(p.getPosition().y, 0.009)
		new Double(heading) closeTo(p.getHeading(), 0.009)
		new Double(prevAngle) closeTo(p.prevAngle, 0.009)
		new Double(prevLogMov) closeTo(p.prevLogMov, 0.009)
		0.0 == p.presAngle
		0.0 == p.deterStrength
		p.enoughWaterAhead
		p.tickMoveAdjustMultiplier == 0.0  // Used up all movement for this tick

		where:
		tick |      x |       y |  heading |  prevAngle | prevLogMov
		   1 |   7.37 |    7.28 |   224.07 |  (-135.93) |       1.18
		   2 |   4.74 |    4.56 |   224.07 |        0.0 |       1.18
		   3 |   2.02 |    7.19 |   314.07 |      90.00 |       1.18
		   4 |   4.74 |    4.56 |   134.07 |     180.00 |       1.18
		   5 |   2.02 |    7.19 |   314.07 |     180.00 |       1.18
		   6 |   4.74 |    4.56 |   134.07 |     180.00 |       1.18
		   7 |   2.02 |    7.19 |   314.07 |     180.00 |       1.18
		   8 |   4.74 |    4.56 |   134.07 |     180.00 |       1.18
		   9 |   2.02 |    7.19 |   314.07 |     180.00 |       1.18
		  10 |   4.74 |    4.56 |   134.07 |     180.00 |       1.18
	}
}

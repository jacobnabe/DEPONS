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
import static org.mockito.Mockito.mock;

import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dk.au.bios.porpoise.behavior.DispersalFactory;
import dk.au.bios.porpoise.behavior.FastRefMemTurn;
import dk.au.bios.porpoise.behavior.RandomSource;
import dk.au.bios.porpoise.landscape.CellData;
import dk.au.bios.porpoise.landscape.CellDataTestData;
import dk.au.bios.porpoise.landscape.DataFileMetaData;
import dk.au.bios.porpoise.landscape.GridSpatialPartitioning;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.Schedule;
import repast.simphony.space.continuous.BouncyBorders;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;

/**
 * Unit test for the Porpoise agent.
 */
class PorpoiseMoveTest {

	private Context<Agent> context;
	private Schedule schedule;
	private CellData cellData;
	private Porpoise p;

	@BeforeEach
	public void setup() throws Exception {
		// Repast initialization
		Globals.setLandscapeMetadata(new DataFileMetaData(100, 100, 529473, 5972242, 400 , null));
		SimulationParameters.setModel(4);
		this.schedule = new Schedule();
		RunEnvironment.init(schedule, null, null, true);
		context = new DefaultContext<>();
		RunState.init().setMasterContext(context);

		var factory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		var space = factory.createContinuousSpace("space", context, new RandomCartesianAdder<Agent>(), new BouncyBorders(), new double[] { Globals.getWorldWidth(), Globals.getWorldHeight() }, new double[] { 0.5f, 0.5f});
		var gridFactory = GridFactoryFinder.createGridFactory(null);
		var grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Agent>(new repast.simphony.space.grid.BouncyBorders(), new SimpleGridAdder<Agent>(), true, Globals.getWorldWidth(), Globals.getWorldHeight()));
		cellData = CellDataTestData.getCellData();
		Globals.setCellData(cellData);
		Globals.setSpace(space);
		Globals.setGrid(grid);
		Globals.setSpatialPartitioning(new GridSpatialPartitioning(25, 25));
		DispersalFactory.setType("off");

		var random = mock(RandomSource.class);
		Globals.setRandomSource(random);

		p = new Porpoise(context, 1, new FastRefMemTurn());
		context.add(p);
		p.setPosition(new NdPoint(10.0, 10.0));
		p.setHeading(0.0);
		p.moveAwayFromLand();  // Weird side-effect here, updating the initial poslist

		assertThat(p.getPosition()).isEqualTo(new NdPoint(10.0f, 10.0f));
		assertThat(p.getHeading()).isEqualTo(0.0);
		assertThat(p.getAge()).isEqualTo(1);
		
	}

	@Test
	public void movementWithoutDispersal() throws Exception {
		p.move();

		assertThat(p.getPosition().getX()).isEqualTo(7.37, within(0.009));
		assertThat(p.getPosition().getY()).isEqualTo(7.28, within(0.009));
		assertThat(p.getHeading()).isEqualTo(224.07, within(0.009));
		assertThat(p.getPrevAngle()).isEqualTo(-135.93, within(0.009));
		assertThat(p.getPresAngle()).isEqualTo(0.0, within(0.009));

		p.move();

		assertThat(p.getPosition().getX()).isEqualTo(4.74, within(0.009));
		assertThat(p.getPosition().getY()).isEqualTo(4.56, within(0.009));
		assertThat(p.getHeading()).isEqualTo(224.07, within(0.009));
		assertThat(p.getPrevAngle()).isEqualTo(0.0 , within(0.009));
		assertThat(p.getPresAngle()).isEqualTo(0.0, within(0.009));

		IntStream.range(0, 10).forEach(i -> p.move());

		assertThat(p.getPosition().getX()).isEqualTo(4.74, within(0.009));
		assertThat(p.getPosition().getY()).isEqualTo(4.56, within(0.009));
		assertThat(p.getHeading()).isEqualTo(134.07, within(0.009));
		assertThat(p.getPrevAngle()).isEqualTo(180.0, within(0.009));
		assertThat(p.getPresAngle()).isEqualTo(0.0, within(0.009));
	}

	@Test
	public void test2() {
		//        x |   y |  heading |  prevAngle | prevLogMov
		double[][] expected = {
		      { 7.37d, 7.28d, 224.07d, -135.93d, 1.18d },
		      { 4.74d, 4.56d, 224.07d, 0.0d, 1.18d},
		      { 2.02d, 7.19d, 314.07d, 90.00d, 1.18d},
		      { 4.74d, 4.56d, 134.07d, 180.00d, 1.18d},
		      { 2.02d, 7.19d, 314.07d, 180.00d, 1.18d},
		      { 4.74d, 4.56d, 134.07d, 180.00d, 1.18d},
		      { 2.02d, 7.19d, 314.07d, 180.00d, 1.18d},
		      { 4.74d, 4.56d, 134.07d, 180.00d, 1.18d},
		      { 2.02d, 7.19d, 314.07d, 180.00d, 1.18d},
		      { 4.74d, 4.56d, 134.07d, 180.00d, 1.18d}
		};

		for (double[] e: expected) {
			p.move();

			assertThat(p.getPosition().getX()).isEqualTo(e[0], within(0.009));
			assertThat(p.getPosition().getY()).isEqualTo(e[1], within(0.009));
			assertThat(p.getHeading()).isEqualTo(e[2], within(0.009));
			assertThat(p.getPrevAngle()).isEqualTo(e[3], within(0.009));
			assertThat(p.getPrevLogMov()).isEqualTo(e[4], within(0.009));
			assertThat(p.getPresAngle()).isEqualTo(0.0);
			assertThat(p.getDeterStrength()).isEqualTo(0.0);
			assertThat(p.isEnoughWaterAhead()).isTrue();
			assertThat(p.getTickMoveAdjustMultiplier()).isEqualTo(0.0);  // Used up all movement for this tick
		}
	}

}

/*
 * Copyright (C) 2022-2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.behavior.DispersalFactory;
import dk.au.bios.porpoise.behavior.FastRefMemTurn;
import dk.au.bios.porpoise.behavior.RandomSource;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.space.continuous.BouncyBorders;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;

class GridSpatialPartitioningTest {

	private Context<Agent> context;
	private GridSpatialPartitioning spatialPart;

	private void initWorld(int width, int height, double xllcorner, double yllcorner) {
		DispersalFactory.setType("off");
		var random = mock(RandomSource.class);
		Globals.setRandomSource(random);
		Globals.setLandscapeMetadata(new DataFileMetaData(width, height, xllcorner, yllcorner, 400 , null));

		context = new DefaultContext<Agent>();
		var factory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		var space = factory.createContinuousSpace("space", context, new RandomCartesianAdder<Agent>(), new BouncyBorders(), new double[] { Globals.getWorldWidth(), Globals.getWorldHeight()}, new double[] {0.5f, 0.5f});
		var gridFactory = GridFactoryFinder.createGridFactory(null);
		var grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Agent>(new repast.simphony.space.grid.BouncyBorders(), new SimpleGridAdder<Agent>(), true, Globals.getWorldWidth(), Globals.getWorldHeight()));
		Globals.setSpace(space);
		Globals.setGrid(grid);
		spatialPart = new GridSpatialPartitioning(25, 25);
		Globals.setSpatialPartitioning(spatialPart);
		space.addProjectionListener(spatialPart);
	}

	@Test
	void baseDimensions() {
		initWorld(250, 250, 529473, 5972242);
		var p = new Porpoise(context, 1, new FastRefMemTurn());
		context.add(p);

		assertThat(spatialPart.getWidth()).isEqualTo(10);
		assertThat(spatialPart.getHeight()).isEqualTo(10);

		p.setPosition(new NdPoint(11, 11));
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(11, 11))).contains(p);

		p.setPosition(new NdPoint(150, 150));
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(11, 11))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(150, 150))).contains(p);

		p.setPosition(new NdPoint(199, 199));
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(11, 11))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(150, 150))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(199, 199))).contains(p);

		p.setPosition(new NdPoint(1, 1));
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(150, 150))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(199, 199))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(1, 1))).contains(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(11, 11))).contains(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(19, 19))).contains(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(0, 0))).contains(p);

		context.remove(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(0, 0))).doesNotContain(p);
	}

	@Test
	void kattegatDimensions() {
		initWorld(600, 1000, 529473, 5972242);
		var p = new Porpoise(context, 1, new FastRefMemTurn());
		context.add(p);

		assertThat(spatialPart.getWidth()).isEqualTo(24);
		assertThat(spatialPart.getHeight()).isEqualTo(40);

		p.setPosition(new NdPoint(11, 11));
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(11, 11))).contains(p);

		p.setPosition(new NdPoint(150, 150));
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(11, 11))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(150, 150))).contains(p);

		p.setPosition(new NdPoint(199, 199));
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(11, 11))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(150, 150))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(199, 199))).contains(p);

		p.setPosition(new NdPoint(1, 1));
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(150, 150))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(199, 199))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(1, 1))).contains(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(11, 11))).contains(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(19, 19))).contains(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(0, 0))).contains(p);

		p.setPosition(new NdPoint(150, 150));
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(11, 11))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(150, 150))).contains(p);  // center
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(126, 176))).contains(p);  // nw
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(150, 180))).contains(p);  // n
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(185, 199))).contains(p);  // ne
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(136, 150))).contains(p);  // w
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(188, 150))).contains(p);  // e
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(130, 126))).contains(p);  // sw
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(150, 130))).contains(p);  // s
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(193, 132))).contains(p);  // se
	}

	@Test
	void northSeaDimensions() {
		initWorld(2088, 2175, 3479625.18158797, 3125583.13019526);
		var p = new Porpoise(context, 1, new FastRefMemTurn());
		context.add(p);

		assertThat(spatialPart.getWidth()).isEqualTo(84);
		assertThat(spatialPart.getHeight()).isEqualTo(87);

		p.setPosition(new NdPoint(11, 11));
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(11, 11))).contains(p);

		p.setPosition(new NdPoint(150, 150));
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(11, 11))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(150, 150))).contains(p);

		p.setPosition(new NdPoint(199, 199));
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(11, 11))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(150, 150))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(199, 199))).contains(p);

		p.setPosition(new NdPoint(1, 1));
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(150, 150))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(199, 199))).doesNotContain(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(1, 1))).contains(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(11, 11))).contains(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(19, 19))).contains(p);
		assertThat(spatialPart.getPorpoisesInPartition(new NdPoint(0, 0))).contains(p);
	}

	@Test
	void neighborhood() {
		initWorld(600, 1000, 529473, 5972242);
		assertThat(spatialPart.getWidth()).isEqualTo(24);
		assertThat(spatialPart.getHeight()).isEqualTo(40);

		// Setup landscape with a single porpoise in each super-grid cell
		for (int gx = 0; gx < spatialPart.getWidth(); gx++) {
			for (int gy = 0; gy < spatialPart.getHeight(); gy++) {
				var porp = new Porpoise(context, 1, new FastRefMemTurn());
				context.add(porp);
				porp.setPosition(new NdPoint(gx * 25 + 12, gy * 25 + 12));
			}
		}

		// Single point, returns 9 (3x3 super-grid cell in the neighborhood)
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(300, 500))).hasSize(9);
		
		// Simple start/end, returns 15 (5x3 & 3x5 super-grid cell in the neighborhood)
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(300, 500), new NdPoint(350, 500))).hasSize(15);
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(300, 500), new NdPoint(300, 550))).hasSize(15);
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(350, 500), new NdPoint(300, 500))).hasSize(15);
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(300, 550), new NdPoint(300, 500))).hasSize(15);
		
		// Current implementation is based on boxes, so expect 25 (5x5 super-grid cells)
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(300, 500), new NdPoint(350, 550))).hasSize(25);

		// 3x9 super-grid cells
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(300, 500), new NdPoint(300, 650))).hasSize(27);
		// 5x9 super-grid cells
		assertThat(spatialPart.getPorpoisesInNeighborhood(new NdPoint(300, 500), new NdPoint(350, 650))).hasSize(45);
	}

}

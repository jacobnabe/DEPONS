/*
 * Copyright (C) 2017-2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import static dk.au.bios.porpoise.Globals.convertGridDistanceToUtm;
import static dk.au.bios.porpoise.Globals.convertUtmXToGrid;
import static dk.au.bios.porpoise.Globals.convertUtmYToGrid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import dk.au.bios.porpoise.behavior.DispersalFactory;
import dk.au.bios.porpoise.behavior.FastRefMemTurn;
import dk.au.bios.porpoise.behavior.RandomSource;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.space.SpatialException;
import repast.simphony.space.continuous.NdPoint;

/**
 * Unit test for the Porpoise agent.
 */
class PorpoiseTest extends AbstractSimulationBDDTest {

	@Test
	public void calltoString() throws Exception {
		aNewWorld(100, 100, 529473, 5972242);
		var random = mock(RandomSource.class);
		Globals.setRandomSource(random);
		var p2 = new Porpoise(null, 1, null);

		assertThat(p2.toString()).isNotNull();
	}

	@Test
	public void standardMove() throws Exception {
		aNewWorld(100, 100, 529473, 5972242);
		SimulationParameters.setModel(1);
		DispersalFactory.setType("off");

		var p = new Porpoise(context, 1, new FastRefMemTurn());
		context.add(p);
		p.setPosition(new NdPoint(10.0, 10.0));
		p.setHeading(0.0);

		ScheduleParameters sp = ScheduleParameters.createRepeating(0, 1, AgentPriority.PORP_MOVE);
		schedule.schedule(sp, p, "move", new Object[0]);

		assertThat(p.getPosition()).isEqualTo(new NdPoint(10.0f, 10.0f));
		assertThat(p.getAge()).isEqualTo(1);
		assertThat(p.distanceXY(new NdPoint(11.0f, 10.0f))).isEqualTo(1);
		assertThat(p.distanceXY(new NdPoint(10.0f, 11.0f))).isEqualTo(1);

		assertThat(schedule.getTickCount()).isEqualTo(-1);

		schedule.execute();
		assertThat(schedule.getTickCount()).isEqualTo(0);
		assertThat(p.getPosition().getX()).isEqualTo(9.993, within(0.009));
		assertThat(p.getPosition().getY()).isEqualTo(10.101, within(0.009));

		schedule.execute();
		assertThat(schedule.getTickCount()).isEqualTo(1);
		assertThat(p.getPosition().getX()).isEqualTo(9.991, within(0.009));
		assertThat(p.getPosition().getY()).isEqualTo(10.140, within(0.009));

		IntStream.range(0, 10).forEach(i -> schedule.execute());
		assertThat(schedule.getTickCount()).isEqualTo(11);
	}

	@Test
	void moveAroundLanscapeBorder() throws Exception {
		aNewWorld(100, 100, 529473, 5972242);
		var p = new Porpoise(context, 1, new FastRefMemTurn());
		context.add(p);
		p.setHeading(0.0);
		
		p.setPosition(new NdPoint(-0.4999, 0.0));
		assertThat(p.getPosition()).isEqualTo(new NdPoint(-0.4999, 0.0));
		
		p.setPosition(new NdPoint(0.0, 0.0));
		assertThat(p.getPosition()).isEqualTo(new NdPoint(0.0, 0.0));
		
		p.setPosition(new NdPoint(99.0, 99.0));
		assertThat(p.getPosition()).isEqualTo(new NdPoint(99.0, 99.0));

		p.setPosition(new NdPoint(99.4999, 99.0));
		assertThat(p.getPosition()).isEqualTo(new NdPoint(99.4999, 99.0));

		assertThatExceptionOfType(SpatialException.class).isThrownBy(() -> {
			p.setPosition(new NdPoint(99.5, 99.0));
		});

		assertThatExceptionOfType(SpatialException.class).isThrownBy(() -> {
			p.setPosition(new NdPoint(100.0, 0.0));
		});

		assertThatExceptionOfType(SpatialException.class).isThrownBy(() -> {
			p.setPosition(new NdPoint(101.0, 0.0));
		});
	}

	@Test
	void moveAroundLanscapeBorderUTM() throws Exception {
		final int xllCorner = 529473;
		final int yllCorner = 5972242;
		aNewWorld(100, 100, xllCorner, yllCorner);
		var p = new Porpoise(context, 1, new FastRefMemTurn());
		context.add(p);
		p.setHeading(0.0);

		p.setPosition(new NdPoint(convertUtmXToGrid(xllCorner), convertUtmYToGrid(yllCorner)));
		assertThat(p.getPosition()).isEqualTo(new NdPoint(-0.5, -0.5));

		p.setPosition(new NdPoint(convertUtmXToGrid(xllCorner + 200), convertUtmYToGrid(yllCorner + 200)));
		assertThat(p.getPosition()).isEqualTo(new NdPoint(0.0, 0.0));

		p.setPosition(new NdPoint(convertUtmXToGrid(xllCorner + convertGridDistanceToUtm(100) - 1),
				convertUtmYToGrid(yllCorner)));
		assertThat(p.getPosition()).isEqualTo(new NdPoint(99.4975, -0.5));

		p.setPosition(new NdPoint(convertUtmXToGrid(xllCorner + convertGridDistanceToUtm(100) - 1),
				convertUtmYToGrid(yllCorner + convertGridDistanceToUtm(100) - 1)));
		assertThat(p.getPosition()).isEqualTo(new NdPoint(99.4975, 99.4975));

		p.setPosition(new NdPoint(convertUtmXToGrid(xllCorner),
				convertUtmYToGrid(yllCorner + convertGridDistanceToUtm(100) - 1)));
		assertThat(p.getPosition()).isEqualTo(new NdPoint(-0.5, 99.4975));

		assertThatExceptionOfType(SpatialException.class).isThrownBy(() -> {
			p.setPosition(new NdPoint(convertUtmXToGrid(xllCorner - 1), convertUtmYToGrid(yllCorner)));
		});

		assertThatExceptionOfType(SpatialException.class).isThrownBy(() -> {
			p.setPosition(new NdPoint(convertUtmXToGrid(xllCorner), convertUtmYToGrid(yllCorner - 1)));
		});

		assertThatExceptionOfType(SpatialException.class).isThrownBy(() -> {
			p.setPosition(new NdPoint(convertUtmXToGrid(xllCorner + convertGridDistanceToUtm(100)),
					convertUtmYToGrid(yllCorner)));
		});

		assertThatExceptionOfType(SpatialException.class).isThrownBy(() -> {
			p.setPosition(new NdPoint(convertUtmXToGrid(xllCorner),
					convertUtmYToGrid(yllCorner + convertGridDistanceToUtm(100))));
		});
	}

}

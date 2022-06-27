/*
 * Copyright (C) 2022 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import dk.au.bios.porpoise.util.SimulationTime;

public class TurbineTest extends AbstractSimulationBDDTest {

	@Test
	public void porpoiseDeterrence() throws Exception {
		aNewWorld(100, 100);
		var porpoise = aPorpoise(50.0, 50.0, 0.0);

		var turbine = new Turbine("turb1", 234, 50.0d, 50.0d, 3, 8, 0);
		
		context.add(turbine);
		turbine.initialize();

		var expectedDeterrence = List.of(0.0d, 0.0d, 0.0d, 5.4103d, 
				2.7051d, 1.3526d, 0.6763d, 0.3381d, 0.1691d, 0.0d,
				0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d);
			
		for (int i = 0; i < expectedDeterrence.size(); i++) {
			schedule.execute();
			var deterrence = expectedDeterrence.get(i);
			assertThat(porpoise.getDeterStrength()).withFailMessage("unexpected deterrence at tick " + SimulationTime.getTick()
					+ " expectedDeterrence.idx: " + i + ". Expected: " + deterrence + ". Actual: " + porpoise.getDeterStrength())
					.isCloseTo(deterrence, within(0.0001d));
		}
	}

	@Test
	public void porpoiseDeterrenceSmallSpace() throws Exception {
		aNewWorld(20, 20);
		var porpoise = aPorpoise(5.0, 10.0, 90.0);

		var turbine1 = new Turbine("turb1", 234, 10.0d, 10.0d, 3, 5, 0);
		var turbine2 = new Turbine("turb2", 234, 7.5d, 7.5d, 9, 12, 0);
		
		context.add(turbine1);
		context.add(turbine2);
		turbine1.initialize();
		turbine2.initialize();

		var expectedDeterrence = List.of(0.0d, 0.0d, 0.0d, 
				5.3759d, 3.7463d, 4.1051d, 2.0525d, 1.0263d, 0.5131d, 
				4.6530d, 4.1917d, 4.5623d, 5.2710d, 2.6355d, 1.3177d, 
				0.6589d, 0.3294d, 0.1647d, 0.0d, 0.0d);
			
		for (int i = 0; i < expectedDeterrence.size(); i++) {
			schedule.execute();
			var deterrence = expectedDeterrence.get(i);
			assertThat(porpoise.getDeterStrength()).withFailMessage("unexpected deterrence at tick " + SimulationTime.getTick()
					+ " expectedDeterrence.idx: " + i + ". Expected: " + deterrence + ". Actual: " + porpoise.getDeterStrength())
					.isCloseTo(deterrence, within(0.0001d));
		}
	}

	@Test
	public void loadUserDefNotDynamic() throws Exception {
		aNewWorld(400, 400, 3976618.40818195d, 3363922.87082193d);
		Turbine.load(context, "User-def", false);
		
		assertThat(context.getObjectsAsStream(Turbine.class)).hasSize(10);
	}

	@Test
	public void loadUserDefDynamic() throws Exception {
		aNewWorld(400, 400, 3976618.40818195d, 3363922.87082193d);
		Turbine.load(context, "User-def", true);
		
		assertThat(context.getObjectsAsStream(Turbine.class)).hasSize(0);
		IntStream.range(0, 50).forEach(i -> schedule.execute());
		assertThat(context.getObjectsAsStream(Turbine.class)).hasSize(0);
		
		schedule.execute();
		assertThat(context.getObjectsAsStream(Turbine.class)).hasSize(1);
		IntStream.range(0, 50).forEach(i -> schedule.execute());
		assertThat(context.getObjectsAsStream(Turbine.class)).hasSize(1);
		
		schedule.execute();
		assertThat(context.getObjectsAsStream(Turbine.class)).hasSize(0);
	}

}

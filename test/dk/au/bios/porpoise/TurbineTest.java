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
import repast.simphony.space.continuous.NdPoint;

public class TurbineTest extends AbstractSimulationBDDTest {

	@Test
	public void porpoiseDeterrence() throws Exception {
		anOldWorld(100, 100);
		var porpoise = aPorpoise(50.0, 50.0, 0.0);

		var turbine = new Turbine("turb1", 234, 50.0d, 50.0d, 3, 8, 0);
		
		context.add(turbine);
		turbine.initialize();

		var expectedDeterrence = List.of(0.0d, 0.0d, 0.0d, 5.4103d, 
				2.7051d, 1.3526d, 0.6763d, 0.3381d, 0.1691d, 0.0d,
				0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d);
			
		var expectedHeadings = List.of(224.0663, 224.0663, 314.0663, 250.6302, 250.6314, 250.6364, 250.6414, 250.6514,
				250.6714, 340.9399, 160.9399, 340.9399, 160.9399, 340.9399, 160.9399, 340.9399);

		var expectedPositions = List.of(new NdPoint(47.3683, 47.2811), new NdPoint(44.7367, 44.5623),
				new NdPoint(42.0178, 47.1939), new NdPoint(38.4481, 45.9390), new NdPoint(34.8783, 44.6840),
				new NdPoint(31.3085, 43.4294), new NdPoint(27.7385, 42.1752), new NdPoint(24.1683, 40.9215),
				new NdPoint(20.5977, 39.6691), new NdPoint(19.3620, 43.2455), new NdPoint(20.5977, 39.6691),
				new NdPoint(19.3620, 43.2455), new NdPoint(20.5977, 39.6691), new NdPoint(19.3620, 43.2455),
				new NdPoint(20.5977, 39.6691), new NdPoint(19.3620, 43.2455));

		assertThat(porpoise.getPosition()).isEqualTo(new NdPoint(50.0d, 50.0d));
		assertThat(porpoise.getHeading()).isEqualTo(0.0d);

		for (int i = 0; i < expectedDeterrence.size(); i++) {
			schedule.execute();
			var deterrence = expectedDeterrence.get(i);
			assertThat(porpoise.getDeterStrength()).withFailMessage("unexpected deterrence at tick " + SimulationTime.getTick()
					+ " expectedDeterrence.idx: " + i + ". Expected: " + deterrence + ". Actual: " + porpoise.getDeterStrength())
					.isCloseTo(deterrence, within(0.0001d));

			var heading = expectedHeadings.get(i);
			assertThat(porpoise.getHeading()).withFailMessage("unexpected heading at tick " + SimulationTime.getTick() 
					+ " expectedHeadings.idx: " + i + ". Expected: " + heading + ". Actual: " + porpoise.getHeading())
					.isCloseTo(heading, within(0.0001d));

			final NdPoint expoectedPos = expectedPositions.get(i);
			assertThat(porpoise.getPosition()).satisfies(pos -> {
				assertThat(pos.getX()).isCloseTo(expoectedPos.getX(), within(0.0001d));
				assertThat(pos.getY()).isCloseTo(expoectedPos.getY(), within(0.0001d));
			});
		}
	}

	@Test
	public void porpoiseDeterrenceSmallSpace() throws Exception {
		anOldWorld(20, 20);
		var porpoise = aPorpoise(5.0, 10.0, 0.0);

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
			
		var expectedHeadings = List.of(224.0663, 224.0663, 314.0663, 250.7099, 246.6838, 235.6119, 235.6084, 235.6165,
				235.6222, 217.4565, 234.3958, 212.4674, 234.9561, 234.9566, 234.9617, 234.9667, 234.9768, 234.9971,
				325.2682, 145.2682);

		var expectedPositions = List.of(new NdPoint(2.3683, 7.2811), new NdPoint(-0.2633, 4.5623),
				new NdPoint(1.9822, 7.1939), new NdPoint(0.5893, 5.9439), new NdPoint(1.8856, 4.4462),
				new NdPoint(0.2370, 2.3091), new NdPoint(1.8855, 0.1718), new NdPoint(0.2373, 0.9651),
				new NdPoint(1.8857, 0.1715), new NdPoint(-0.4155, 1.8323), new NdPoint(2.4920, -0.3707),
				new NdPoint(0.4608, 2.5631), new NdPoint(1.6372, 0.3904), new NdPoint(0.4608, 0.7823),
				new NdPoint(1.6374, 0.3901), new NdPoint(0.4610, 0.7820), new NdPoint(1.6377, 0.3896),
				new NdPoint(0.4617, 0.7809), new NdPoint(0.6941, 3.8907), new NdPoint(2.8499, 0.7809));

		assertThat(porpoise.getPosition()).isEqualTo(new NdPoint(5.0d, 10.0d));
		assertThat(porpoise.getHeading()).isEqualTo(0.0d);

		for (int i = 0; i < expectedDeterrence.size(); i++) {
			schedule.execute();
			var deterrence = expectedDeterrence.get(i);
			assertThat(porpoise.getDeterStrength()).withFailMessage("unexpected deterrence at tick " + SimulationTime.getTick()
					+ " expectedDeterrence.idx: " + i + ". Expected: " + deterrence + ". Actual: " + porpoise.getDeterStrength())
					.isCloseTo(deterrence, within(0.0001d));

			var heading = expectedHeadings.get(i);
			assertThat(porpoise.getHeading()).withFailMessage("unexpected heading at tick " + SimulationTime.getTick() 
					+ " expectedHeadings.idx: " + i + ". Expected: " + heading + ". Actual: " + porpoise.getHeading())
					.isCloseTo(heading, within(0.0001d));

			final NdPoint expoectedPos = expectedPositions.get(i);
			assertThat(porpoise.getPosition()).satisfies(pos -> {
				assertThat(pos.getX()).isCloseTo(expoectedPos.getX(), within(0.0001d));
				assertThat(pos.getY()).isCloseTo(expoectedPos.getY(), within(0.0001d));
			});
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

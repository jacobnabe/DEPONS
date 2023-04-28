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

package dk.au.bios.porpoise;

import static dk.au.bios.porpoise.Globals.convertGridDistanceToUtm;
import static dk.au.bios.porpoise.Globals.convertGridXToUtm;
import static dk.au.bios.porpoise.Globals.convertGridYToUtm;
import static dk.au.bios.porpoise.ships.VesselClass.CONTAINERSHIP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import dk.au.bios.porpoise.ships.Buoy;
import dk.au.bios.porpoise.ships.Route;
import dk.au.bios.porpoise.util.SimulationTime;
import repast.simphony.space.continuous.NdPoint;

public class ShipTest extends AbstractSimulationBDDTest {

	@BeforeEach
	void setup() {
	}

	@Test
	public void move() throws Exception {
		aNewWorld(100, 100);

		var buoys = new ArrayList<Buoy>();
		buoys.add(new Buoy(convertGridXToUtm(20.0), convertGridYToUtm(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(25.0), convertGridYToUtm(55.0), 12.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(30.0), convertGridYToUtm(60.0), 10.0, 0));
		Route route = new Route("route1", buoys);
		var shipInitialPosition = new NdPoint(10, 50);
		var ship = new Ship("ship1", CONTAINERSHIP, 366.00, route);
		context.add(ship);
		ship.setPosition(shipInitialPosition);

		schedule.schedule(ship);

		var expectedPositions = List.of(
				buoys.get(0).getNdPoint(), buoys.get(1).getNdPoint(), buoys.get(2).getNdPoint(),
				buoys.get(0).getNdPoint(), buoys.get(1).getNdPoint(), buoys.get(2).getNdPoint(),
				buoys.get(0).getNdPoint(), buoys.get(1).getNdPoint(), buoys.get(2).getNdPoint(),
				buoys.get(0).getNdPoint(), buoys.get(1).getNdPoint());

		for (int i = 0; i < expectedPositions.size(); i++) {
			schedule.execute();
			var pos = expectedPositions.get(i);
			assertThat(ship.getPosition()).withFailMessage("unexpected position at tick " + SimulationTime.getTick()
					+ " positionList.idx: " + i + ". Expected: " + pos + ". Actual: " + ship.getPosition())
					.isEqualTo(pos);
		}
	}

	@Test
	public void moveWithEnd() throws Exception {
		aNewWorld(100, 100);

		var buoys = new ArrayList<Buoy>();
		buoys.add(new Buoy(convertGridXToUtm(20.0), convertGridYToUtm(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(25.0), convertGridYToUtm(55.0), 12.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(30.0), convertGridYToUtm(60.0), 10.0, 0));
		Route route = new Route("route1", buoys);
		var shipInitialPosition = new NdPoint(10, 50);
		var ship = new Ship("ship1", CONTAINERSHIP, 366.00, route);
		ship.setTickEnd(4);
		context.add(ship);
		ship.setPosition(shipInitialPosition);

		schedule.schedule(ship);

		var expectedPositions = List.of(
				buoys.get(0).getNdPoint(), buoys.get(1).getNdPoint(), buoys.get(2).getNdPoint(),
				buoys.get(0).getNdPoint(), buoys.get(1).getNdPoint(), buoys.get(1).getNdPoint(),
				buoys.get(1).getNdPoint(), buoys.get(1).getNdPoint());

		for (int i = 0; i < expectedPositions.size(); i++) {
			schedule.execute();
			var pos = expectedPositions.get(i);
			assertThat(ship.getPosition()).withFailMessage("unexpected position at tick " + SimulationTime.getTick()
					+ " positionList.idx: " + i + ". Expected: " + pos + ". Actual: " + ship.getPosition())
					.isEqualTo(pos);
		}
	}

	@Test
	public void moveWithDelayedStartAndEnd() throws Exception {
		aNewWorld(100, 100);

		var buoys = new ArrayList<Buoy>();
		buoys.add(new Buoy(convertGridXToUtm(20.0), convertGridYToUtm(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(25.0), convertGridYToUtm(55.0), 12.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(30.0), convertGridYToUtm(60.0), 10.0, 0));
		Route route = new Route("route1", buoys);
		var shipInitialPosition = new NdPoint(10, 50);
		var ship = new Ship("ship1", CONTAINERSHIP, 366.00, route, 3, 8);
		context.add(ship);
		ship.setPosition(shipInitialPosition);

		schedule.schedule(ship);

		var expectedPositions = List.of(shipInitialPosition, shipInitialPosition, shipInitialPosition,
				buoys.get(0).getNdPoint(), buoys.get(1).getNdPoint(), buoys.get(2).getNdPoint(),
				buoys.get(0).getNdPoint(), buoys.get(1).getNdPoint(), buoys.get(2).getNdPoint(),
				buoys.get(2).getNdPoint());

		for (int i = 0; i < expectedPositions.size(); i++) {
			schedule.execute();
			var pos = expectedPositions.get(i);
			assertThat(ship.getPosition()).withFailMessage("unexpected position at tick " + SimulationTime.getTick()
					+ " positionList.idx: " + i + ". Expected: " + pos + ". Actual: " + ship.getPosition())
					.isEqualTo(pos);
		}
	}

	@Test
	public void pauses() throws Exception {
		aNewWorld(100, 100);

		var buoys = new ArrayList<Buoy>();
		buoys.add(new Buoy(convertGridXToUtm(20.0), convertGridYToUtm(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(25.0), convertGridYToUtm(55.0), 12.0, 2));
		buoys.add(new Buoy(convertGridXToUtm(30.0), convertGridYToUtm(60.0), 10.0, 0));
		Route route = new Route("route1", buoys);
		var shipInitialPosition = new NdPoint(10, 50);
		var ship = new Ship("ship1", CONTAINERSHIP, 366.00, route, 2, 99999999);
		context.add(ship);
		ship.setPosition(shipInitialPosition);

		schedule.schedule(ship);

		var expectedPositions = List.of(shipInitialPosition, shipInitialPosition, buoys.get(0).getNdPoint(),
				buoys.get(1).getNdPoint(), buoys.get(1).getNdPoint(), buoys.get(1).getNdPoint(),
				buoys.get(2).getNdPoint(), buoys.get(0).getNdPoint(), buoys.get(1).getNdPoint(),
				buoys.get(1).getNdPoint(), buoys.get(1).getNdPoint(), buoys.get(2).getNdPoint(),
				buoys.get(0).getNdPoint());

		for (int i = 0; i < expectedPositions.size(); i++) {
			schedule.execute();
			var pos = expectedPositions.get(i);
			assertThat(ship.getPosition())
					.withFailMessage(
							"unexpected position at tick " + SimulationTime.getTick() + " positionList.idx: " + i)
					.isEqualTo(pos);
		}
	}

	@Test
	public void porpoiseDeterrence() throws Exception {
		aNewWorld(100, 100);
		SimulationParameters.disableCrwRandomness();
		var porpoise = aPorpoise(22.0, 50.0, 90.0);

		var buoys = new ArrayList<Buoy>();
		buoys.add(new Buoy(convertGridXToUtm(22.0), convertGridYToUtm(44.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(22.0), convertGridYToUtm(48.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(22.0), convertGridYToUtm(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(22.0), convertGridYToUtm(52.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(22.0), convertGridYToUtm(56.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(22.0), convertGridYToUtm(60.0), 10.0, 4));
		Route route = new Route("route1", buoys);
		var shipInitialPosition = new NdPoint(10, 50);
		var ship = new Ship("ship1", CONTAINERSHIP, 366.00, route, 3, 13);
		context.add(ship);
		ship.setPosition(shipInitialPosition);

		schedule.schedule(ship);

		var expectedDeterrence = List.of(0.0000d, 0.0000d, 0.0000d, 0.0000d, 1.5884d, 1.5894d, 1.5438d, 1.5822d,
				1.5263d, 0.0000d, 0.0000d, 0.0000d, 0.0000d);

		var expectedHeadings = List.of(90.0000d, 90.0000d, 90.0000d, 90.0000d, 15.6264d, 45.1240d, 113.1068d, 156.6327d,
				168.5884d, 168.5884d, 168.5884d, 168.5884d, 168.5884d);

		var expectedPorpPositions = List.of(new NdPoint(22.2500d, 50.0000d), new NdPoint(22.5000d, 50.0000d),
				new NdPoint(22.7500d, 50.0000d), new NdPoint(23.0000d, 50.0000d), new NdPoint(23.0673d, 50.2408d),
				new NdPoint(23.2445d, 50.4172d), new NdPoint(23.4744d, 50.3190d), new NdPoint(23.5736d, 50.0895d),
				new NdPoint(23.6231d, 49.8445d), new NdPoint(23.6725d, 49.5994d), new NdPoint(23.7220d, 49.3544d),
				new NdPoint(23.7715d, 49.1093d), new NdPoint(23.8209d, 48.8643d));

		assertThat(porpoise.getPosition()).isEqualTo(new NdPoint(22.0d, 50.0d));
		assertThat(porpoise.getHeading()).isEqualTo(90.0d);

		for (int i = 0; i < expectedDeterrence.size(); i++) {
			schedule.execute();
			var deterrence = expectedDeterrence.get(i);
			assertThat(porpoise.getDeterStrength()).withFailMessage("unexpected deterrence at tick " + SimulationTime.getTick()
					+ " expectedDeterrence.idx: " + i + ". Expected: " + deterrence + ". Actual: " + porpoise.getDeterStrength())
					.isCloseTo(deterrence, within(0.0001d));

			var expectedHeading = expectedHeadings.get(i);
			assertThat(porpoise.getHeading()).withFailMessage("unexpected heading at tick " + SimulationTime.getTick() 
					+ " expectedHeadings.idx: " + i + ". Expected: " + expectedHeading + ". Actual: " + porpoise.getHeading())
					.isCloseTo(expectedHeading, within(0.0001d));

			final NdPoint expoectedPos = expectedPorpPositions.get(i);
			assertThat(porpoise.getPosition()).satisfies(pos -> {
				assertThat(pos.getX()).isCloseTo(expoectedPos.getX(), within(0.0001d));
				assertThat(pos.getY()).isCloseTo(expoectedPos.getY(), within(0.0001d));
			});
		}
	}

	@Test
	public void porpoiseDeterrenceWithShipPause() throws Exception {
		aNewWorld(100, 100);
		SimulationParameters.disableCrwRandomness();
		var porpoise = aPorpoise(22.0, 50.0, 90.0);

		var buoys = new ArrayList<Buoy>();
		buoys.add(new Buoy(convertGridXToUtm(22.0), convertGridYToUtm(44.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(22.0), convertGridYToUtm(48.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(22.0), convertGridYToUtm(50.0), 10.0, 2));
		buoys.add(new Buoy(convertGridXToUtm(22.0), convertGridYToUtm(52.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(22.0), convertGridYToUtm(56.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(22.0), convertGridYToUtm(60.0), 10.0, 4));
		Route route = new Route("route1", buoys);
		var shipInitialPosition = new NdPoint(10, 50);
		var ship = new Ship("ship1", CONTAINERSHIP, 366.00, route, 3, 15);
		context.add(ship);
		ship.setPosition(shipInitialPosition);

		schedule.schedule(ship);

		var expectedDeterrence = List.of(0.0000d, 0.0000d, 0.0000d, 0.0000d, 1.5884d, 1.5894d, 0.0000d, 0.0000d,
				1.5555d, 1.5751d, 1.5306d, 0.0000d, 0.0000d);

		var expectedHeadings = List.of(90.0000d, 90.0000d, 90.0000d, 90.0000d, 15.6264d, 45.1240d, 45.1240d, 45.1240d,
				98.3751d, 148.4884d, 164.9675d, 164.9675d, 164.9675d);

		var expectedPorpPositions = List.of(new NdPoint(22.2500d, 50.0000d), new NdPoint(22.5000d, 50.0000d),
				new NdPoint(22.7500d, 50.0000d), new NdPoint(23.0000d, 50.0000d), new NdPoint(23.0673d, 50.2408d),
				new NdPoint(23.2445d, 50.4172d), new NdPoint(23.4217d, 50.5935d), new NdPoint(23.5988d, 50.7699d),
				new NdPoint(23.8462d, 50.7335d), new NdPoint(23.9768d, 50.5204d), new NdPoint(24.0417d, 50.2789d),
				new NdPoint(24.1065d, 50.0375d), new NdPoint(24.1713d, 49.7961d));

		assertThat(porpoise.getPosition()).isEqualTo(new NdPoint(22.0d, 50.0d));
		assertThat(porpoise.getHeading()).isEqualTo(90.0d);

		for (int i = 0; i < expectedDeterrence.size(); i++) {
			schedule.execute();
			var deterrence = expectedDeterrence.get(i);
			assertThat(porpoise.getDeterStrength()).withFailMessage("unexpected deterrence at tick " + SimulationTime.getTick()
					+ " expectedDeterrence.idx: " + i + ". Expected: " + deterrence + ". Actual: " + porpoise.getDeterStrength())
					.isCloseTo(deterrence, within(0.0001d));

			var expectedHeading = expectedHeadings.get(i);
			assertThat(porpoise.getHeading()).withFailMessage("unexpected heading at tick " + SimulationTime.getTick() 
					+ " expectedHeadings.idx: " + i + ". Expected: " + expectedHeading + ". Actual: " + porpoise.getHeading())
					.isCloseTo(expectedHeading, within(0.0001d));

			final NdPoint expoectedPos = expectedPorpPositions.get(i);
			assertThat(porpoise.getPosition()).satisfies(pos -> {
				assertThat(pos.getX()).isCloseTo(expoectedPos.getX(), within(0.0001d));
				assertThat(pos.getY()).isCloseTo(expoectedPos.getY(), within(0.0001d));
			});
		}
	}

	@Test
	public void moveNearBorder() throws Exception {
		aNewWorld(100, 100);

		var buoys = new ArrayList<Buoy>();
		buoys.add(new Buoy(XLL_CORNER, YLL_CORNER, 10.0, 0));
		buoys.add(new Buoy(XLL_CORNER + convertGridDistanceToUtm(100) - 1, YLL_CORNER, 10.0, 0));
		buoys.add(new Buoy(XLL_CORNER + convertGridDistanceToUtm(100) - 1, YLL_CORNER + convertGridDistanceToUtm(100) - 1, 10.0, 0));
		buoys.add(new Buoy(XLL_CORNER, YLL_CORNER + convertGridDistanceToUtm(100) - 1, 10.0, 0));
		Route route = new Route("route1", buoys);
		var ship = new Ship("ship1", CONTAINERSHIP, 366.00, route);
		context.add(ship);
		ship.initialize();

		schedule.schedule(ship);

		var expectedPositions = List.of(buoys.get(0).getNdPoint(), buoys.get(1).getNdPoint(), buoys.get(2).getNdPoint(),
				buoys.get(3).getNdPoint());

		for (int i = 0; i < expectedPositions.size(); i++) {
			schedule.execute();
			var pos = expectedPositions.get(i);
			assertThat(ship.getPosition()).withFailMessage("unexpected position at tick " + SimulationTime.getTick()
					+ " positionList.idx: " + i + ". Expected: " + pos + ". Actual: " + ship.getPosition())
					.isEqualTo(pos);
		}
	}

	private static Stream<Arguments> provideArgForInterpolate() {
		return Stream.of(
				Arguments.of(new NdPoint(600000.0, 6297000.0), new NdPoint(637040.0, 6297000.0),
						List.of(new NdPoint(601234.7, 6297000), new NdPoint(602469.3, 6297000),
								new NdPoint(603704.0, 6297000), new NdPoint(604938.7, 6297000),
								new NdPoint(606173.3, 6297000), new NdPoint(607408.0, 6297000),
								new NdPoint(608642.7, 6297000), new NdPoint(609877.3, 6297000),
								new NdPoint(611112.0, 6297000), new NdPoint(612346.7, 6297000),
								new NdPoint(613581.3, 6297000), new NdPoint(614816.0, 6297000),
								new NdPoint(616050.7, 6297000), new NdPoint(617285.3, 6297000),
								new NdPoint(618520.0, 6297000), new NdPoint(619754.7, 6297000),
								new NdPoint(620989.3, 6297000), new NdPoint(622224.0, 6297000),
								new NdPoint(623458.7, 6297000), new NdPoint(624693.3, 6297000),
								new NdPoint(625928.0, 6297000), new NdPoint(627162.7, 6297000),
								new NdPoint(628397.3, 6297000), new NdPoint(629632.0, 6297000),
								new NdPoint(630866.7, 6297000), new NdPoint(632101.3, 6297000),
								new NdPoint(633336.0, 6297000), new NdPoint(634570.7, 6297000),
								new NdPoint(635805.3, 6297000), new NdPoint(637040.0, 6297000))),
				Arguments.of(new NdPoint(637040.0, 6297000.0), new NdPoint(600000.0, 6297000.0), 
						List.of(new NdPoint(635805.3, 6297000), new NdPoint(634570.7, 6297000),
								new NdPoint(633336.0, 6297000), new NdPoint(632101.3, 6297000),
								new NdPoint(630866.7, 6297000), new NdPoint(629632.0, 6297000),
								new NdPoint(628397.3, 6297000), new NdPoint(627162.7, 6297000),
								new NdPoint(625928.0, 6297000), new NdPoint(624693.3, 6297000),
								new NdPoint(623458.7, 6297000), new NdPoint(622224.0, 6297000),
								new NdPoint(620989.3, 6297000), new NdPoint(619754.7, 6297000),
								new NdPoint(618520.0, 6297000), new NdPoint(617285.3, 6297000),
								new NdPoint(616050.7, 6297000), new NdPoint(614816.0, 6297000),
								new NdPoint(613581.3, 6297000), new NdPoint(612346.7, 6297000),
								new NdPoint(611112.0, 6297000), new NdPoint(609877.3, 6297000),
								new NdPoint(608642.7, 6297000), new NdPoint(607408.0, 6297000),
								new NdPoint(606173.3, 6297000), new NdPoint(604938.7, 6297000),
								new NdPoint(603704.0, 6297000), new NdPoint(602469.3, 6297000),
								new NdPoint(601234.7, 6297000), new NdPoint(600000.0, 6297000))),
				Arguments.of(new NdPoint(600000.0, 6297000.0), new NdPoint(609668.4, 6332756.0),
						List.of(new NdPoint(600322.3, 6298191.87), new NdPoint(600644.6, 6299383.73),
								new NdPoint(600966.8, 6300575.6), new NdPoint(601289.1, 6301767.47),
								new NdPoint(601611.4, 6302959.33), new NdPoint(601933.7, 6304151.2),
								new NdPoint(602256.0, 6305343), new NdPoint(602578.2, 6306535),
								new NdPoint(602900.5, 6307726.8), new NdPoint(603222.8, 6308918.67),
								new NdPoint(603545.1, 6310110.53), new NdPoint(603867.4, 6311302.4),
								new NdPoint(604189.6, 6312494.27), new NdPoint(604511.9, 6313686.13),
								new NdPoint(604834.2, 6314878), new NdPoint(605156.5, 6316069.87),
								new NdPoint(605478.8, 6317261.73), new NdPoint(605801.0, 6318453.6),
								new NdPoint(606123.3, 6319645.47), new NdPoint(606445.6, 6320837.33),
								new NdPoint(606767.9, 6322029.2), new NdPoint(607090.2, 6323221.07),
								new NdPoint(607412.4, 6324413), new NdPoint(607734.7, 6325604.8),
								new NdPoint(608057.0, 6326796.67), new NdPoint(608379.3, 6327988.53),
								new NdPoint(608701.6, 6329180.4), new NdPoint(609023.8, 6330372.27),
								new NdPoint(609346.1, 6331564.13), new NdPoint(609668.4, 6332756))),
				Arguments.of(new NdPoint(600000.0, 6297000.0), new NdPoint(600000.0, 6297000.0 + 37040.0),
						List.of(new NdPoint(600000.0, 6298234.67), new NdPoint(600000.0, 6299469.33),
								new NdPoint(600000.0, 6300704), new NdPoint(600000.0, 6301938.67),
								new NdPoint(600000.0, 6303173.33), new NdPoint(600000.0, 6304408),
								new NdPoint(600000.0, 6305642.67), new NdPoint(600000.0, 6306877.33),
								new NdPoint(600000.0, 6308112), new NdPoint(600000.0, 6309346.67),
								new NdPoint(600000.0, 6310581.33), new NdPoint(600000.0, 6311816),
								new NdPoint(600000.0, 6313050.67), new NdPoint(600000.0, 6314285.33),
								new NdPoint(600000.0, 6315520), new NdPoint(600000.0, 6316754.67),
								new NdPoint(600000.0, 6317989.33), new NdPoint(600000.0, 6319224),
								new NdPoint(600000.0, 6320458.67), new NdPoint(600000.0, 6321693.33),
								new NdPoint(600000.0, 6322928), new NdPoint(600000.0, 6324162.67),
								new NdPoint(600000.0, 6325397.33), new NdPoint(600000.0, 6326632),
								new NdPoint(600000.0, 6327866.67), new NdPoint(600000.0, 6329101.33),
								new NdPoint(600000.0, 6330336), new NdPoint(600000.0, 6331570.67),
								new NdPoint(600000.0, 6332805.33), new NdPoint(600000.0, 6334040)))
				);
	}

	@ParameterizedTest
	@MethodSource("provideArgForInterpolate")
	void interpolateStep(NdPoint start, NdPoint end, List<NdPoint> expectedSteps) throws Exception {
		var ship = new Ship("ship1", CONTAINERSHIP, 366.00, null, 0, 8);
		var steps = ship.interpolateStep(start, end);

		assertThat(steps).hasSize(30);
		assertThat(steps.get(29)).isEqualTo(end);
		
		for (int i = 0; i < steps.size(); i++) {
			assertThat(steps.get(i).getX()).isCloseTo(expectedSteps.get(i).getX(), within(0.09d));
			assertThat(steps.get(i).getY()).isCloseTo(expectedSteps.get(i).getY(), within(0.09d));
		}
	}

	@Test
	void vhfWeighted() {
		assertThat(Ship.vhfWeighting()).isCloseTo(-1.803332d, within(0.00001));
	}

	@Test
	void predictProbResponse() {
		assertThat(Ship.predictProbResponse(93.73515d, 850.5039/1000, true)).isCloseTo(0.07154668d, within(0.00000001));
		assertThat(Ship.predictProbResponse(93.73515d, 850.5039/1000, false)).isCloseTo(0.03185507d, within(0.00000001));

		assertThat(Ship.predictProbResponse(78.31225d, 3873.1835/1000, true)).isCloseTo(0.05583329d, within(0.00000001));
		assertThat(Ship.predictProbResponse(78.31225d, 3873.1835/1000, false)).isCloseTo(0.03512552d, within(0.00000001));
		
		assertThat(Ship.predictProbResponse(65.82240d, 9058.4212/1000, true)).isCloseTo(0.03835009d, within(0.00000001));
		assertThat(Ship.predictProbResponse(65.82240d, 9058.4212/1000, false)).isCloseTo(0.04150253d, within(0.00000001));
	}

	@Test
	void predictMag() {
		assertThat(Ship.predictMag(99.77292d, 421.3415d / 1000.0d, true)).isCloseTo(22.49963d, within(0.00001));
		assertThat(Ship.predictMag(99.77292d, 421.3415d / 1000.0d, false)).isCloseTo(14.66536d, within(0.00001));

		assertThat(Ship.predictMag(76.88739d, 4305.2533d / 1000.0d, true)).isCloseTo(20.06685d, within(0.00001));
		assertThat(Ship.predictMag(76.88739d, 4305.2533d / 1000.0d, false)).isCloseTo(15.33139d, within(0.00001));

		assertThat(Ship.predictMag(61.24680d, 10786.9112d / 1000.0d, true)).isCloseTo(17.64492d, within(0.00001));
		assertThat(Ship.predictMag(61.24680d, 10786.9112d / 1000.0d, false)).isCloseTo(16.51092d, within(0.00001));
	}

}

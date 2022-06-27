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

import static dk.au.bios.porpoise.ships.VesselClass.CONTAINERSHIP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import dk.au.bios.porpoise.ships.Buoy;
import dk.au.bios.porpoise.ships.Route;
import dk.au.bios.porpoise.util.SimulationTime;
import repast.simphony.space.continuous.NdPoint;

public class ShipTest extends AbstractSimulationBDDTest {

	@Test
	public void move() throws Exception {
		aNewWorld(100, 100);

		var buoys = new ArrayList<Buoy>();
		buoys.add(new Buoy(convertGridXToUtmX(20.0), convertGridYToUtmX(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtmX(25.0), convertGridYToUtmX(55.0), 12.0, 0));
		buoys.add(new Buoy(convertGridXToUtmX(30.0), convertGridYToUtmX(60.0), 10.0, 0));
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
		buoys.add(new Buoy(convertGridXToUtmX(20.0), convertGridYToUtmX(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtmX(25.0), convertGridYToUtmX(55.0), 12.0, 0));
		buoys.add(new Buoy(convertGridXToUtmX(30.0), convertGridYToUtmX(60.0), 10.0, 0));
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
		buoys.add(new Buoy(convertGridXToUtmX(20.0), convertGridYToUtmX(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtmX(25.0), convertGridYToUtmX(55.0), 12.0, 0));
		buoys.add(new Buoy(convertGridXToUtmX(30.0), convertGridYToUtmX(60.0), 10.0, 0));
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
		buoys.add(new Buoy(convertGridXToUtmX(20.0), convertGridYToUtmX(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtmX(25.0), convertGridYToUtmX(55.0), 12.0, 2));
		buoys.add(new Buoy(convertGridXToUtmX(30.0), convertGridYToUtmX(60.0), 10.0, 0));
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
		SimulationParameters.setDeterResponseThresholdShips(20.0);
		var porpoise = aPorpoise(22.0, 50.0, 90.0);

		var buoys = new ArrayList<Buoy>();
		buoys.add(new Buoy(convertGridXToUtmX(20.0), convertGridYToUtmX(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtmX(25.0), convertGridYToUtmX(55.0), 12.0, 0));
		buoys.add(new Buoy(convertGridXToUtmX(30.0), convertGridYToUtmX(60.0), 10.0, 0));
		Route route = new Route("route1", buoys);
		var shipInitialPosition = new NdPoint(10, 50);
		var ship = new Ship("ship1", CONTAINERSHIP, 366.00, route, 3, 8);
		context.add(ship);
		ship.setPosition(shipInitialPosition);

		schedule.schedule(ship);

		var expectedDeterrence = List.of(0.0d, 0.0d, 0.0d, 0.0d, 
				2.5648d, 2.3723d, 1.9218d, 2.1200d, 2.1510d, 1.7736d,
				0.8868d, 0.4434d, 0.2217d, 0.1109d, 0.0554d, 0.0d,
				0.0d, 0.0d, 0.0d);
			
		for (int i = 0; i < expectedDeterrence.size(); i++) {
			schedule.execute();
			var deterrence = expectedDeterrence.get(i);
			assertThat(porpoise.getDeterStrength()).withFailMessage("unexpected deterrence at tick " + SimulationTime.getTick()
					+ " expectedDeterrence.idx: " + i + ". Expected: " + deterrence + ". Actual: " + porpoise.getDeterStrength())
					.isCloseTo(deterrence, within(0.0001d));
		}
	}

	@Test
	public void porpoiseDeterrenceWithShipPause() throws Exception {
		aNewWorld(100, 100);
		SimulationParameters.setDeterResponseThresholdShips(20.0);
		var porpoise = aPorpoise(22.0, 50.0, 90.0);

		var buoys = new ArrayList<Buoy>();
		buoys.add(new Buoy(convertGridXToUtmX(20.0), convertGridYToUtmX(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtmX(25.0), convertGridYToUtmX(55.0), 12.0, 2));
		buoys.add(new Buoy(convertGridXToUtmX(30.0), convertGridYToUtmX(60.0), 10.0, 0));
		Route route = new Route("route1", buoys);
		var shipInitialPosition = new NdPoint(10, 50);
		var ship = new Ship("ship1", CONTAINERSHIP, 366.00, route, 3, 8);
		context.add(ship);
		ship.setPosition(shipInitialPosition);

		schedule.schedule(ship);

		var expectedDeterrence = List.of(0.0d, 0.0d, 0.0d, 0.0d, 
				2.5648d, 1.2824d, 0.6412d, 2.2134d, 1.8182d, 1.9650d,
				0.9825d, 0.4912d, 0.2456d, 0.1228d, 0.0615d, 
				0.0d, 0.0d, 0.0d);
			
		for (int i = 0; i < expectedDeterrence.size(); i++) {
			schedule.execute();
			var deterrence = expectedDeterrence.get(i);
			assertThat(porpoise.getDeterStrength()).withFailMessage("unexpected deterrence at tick " + SimulationTime.getTick()
					+ " expectedDeterrence.idx: " + i + ". Expected: " + deterrence + ". Actual: " + porpoise.getDeterStrength())
					.isCloseTo(deterrence, within(0.0001d));
		}
	}

}

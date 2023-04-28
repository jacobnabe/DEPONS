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

import static dk.au.bios.porpoise.Globals.convertGridXToUtm;
import static dk.au.bios.porpoise.Globals.convertGridYToUtm;
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

public class HydrophoneTest extends AbstractSimulationBDDTest {

	@Test
	public void shipPassing() throws Exception {
		aNewWorld(100, 100);
		var hydrophone = new Hydrophone(1, "test1");
		context.add(hydrophone);
		hydrophone.setPosition(new NdPoint(50.0d, 50.0d));

		var buoys = new ArrayList<Buoy>();
		buoys.add(new Buoy(convertGridXToUtm(24.0), convertGridYToUtm(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(40.0), convertGridYToUtm(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(46.0), convertGridYToUtm(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(50.0), convertGridYToUtm(50.0), 10.0, 2));
		buoys.add(new Buoy(convertGridXToUtm(56.0), convertGridYToUtm(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(60.0), convertGridYToUtm(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(70.0), convertGridYToUtm(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(76.0), convertGridYToUtm(50.0), 10.0, 3));
		Route route = new Route("route1", buoys);
		var ship = new Ship("ship1", CONTAINERSHIP, 366.00, route, 3, 14);
		context.add(ship);
		ship.initialize();

		schedule.schedule(ship);
		
		assertThat(hydrophone.getPosition().getX()).isEqualTo(50.0);
		assertThat(hydrophone.getPosition().getY()).isEqualTo(50.0);
		assertThat(ship.getPosition().getX()).isEqualTo(24.0);
		assertThat(ship.getPosition().getY()).isEqualTo(50.0);

		var expectedShipPositions = List.of(
				new NdPoint(24.0, 50.0),
				new NdPoint(24.0, 50.0),
				new NdPoint(24.0, 50.0),
				new NdPoint(24.0, 50.0),
				new NdPoint(40.0, 50.0),
				new NdPoint(46.0, 50.0),
				new NdPoint(50.0, 50.0),
				new NdPoint(50.0, 50.0),
				new NdPoint(50.0, 50.0),
				new NdPoint(56.0, 50.0),
				new NdPoint(60.0, 50.0),
				new NdPoint(70.0, 50.0),
				new NdPoint(76.0, 50.0),
				new NdPoint(76.0, 50.0)
				);

		var expectedLevels = List.of(0.0d, 0.0d, 0.0d, 0.0d, 
				75.9658d, 86.6498d, 0.0d, 0.0d, 0.0d, 82.5058d, 75.9658d, 63.3043d, 
				56.7044d, 0.0d);

		var expectedDistances = List.of(10400.0d, 10400.0d, 10400.0d, 10400.0d, 
				4000.0d, 1600.0d, 0.0d, 0.0d, 0.0d, 2400.0d, 4000.0d, 8000.0d,
				10400.0d, 10400.0d);

		for (int i = 0; i < expectedLevels.size(); i++) {
			schedule.execute();

			var expectedShipPos = expectedShipPositions.get(i);
			assertThat(ship.getPosition()).satisfies(pos -> {
				assertThat(pos.getX()).isEqualTo(expectedShipPos.getX());
				assertThat(pos.getY()).isEqualTo(expectedShipPos.getY());
			});
			
			var distance = Globals.convertGridDistanceToUtm(ship.getPosition(), hydrophone.getPosition());
			var expectedDistance = expectedDistances.get(i);
			assertThat(distance)
					.withFailMessage(
							"unexpected distance at tick " + SimulationTime.getTick() + " expectedDistance.idx: " + i
									+ ". Expected: " + expectedDistance + ". Actual: " + distance)
					.isCloseTo(expectedDistance, within(0.0001d));

			var expectedLevel = expectedLevels.get(i);
			assertThat(hydrophone.getReceivedSoundLevel())
					.withFailMessage(
							"unexpected sound level at tick " + SimulationTime.getTick() + " expectedLevel.idx: " + i
									+ ". Expected: " + expectedLevel + ". Actual: " + hydrophone.getReceivedSoundLevel())
					.isCloseTo(expectedLevel, within(0.0001d));
		}
	}

}

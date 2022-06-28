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

public class HydrophoneTest extends AbstractSimulationBDDTest {

	@Test
	public void recordShip() throws Exception {
		aNewWorld(100, 100);
		SimulationParameters.setDeterResponseThresholdShips(20.0);
		var hydrophone = new Hydrophone(1, "test1");
		context.add(hydrophone);
		hydrophone.setPosition(new NdPoint(25.0d, 50.0d));

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

		var expectedLevels = List.of(0.0d, 0.0d, 0.0d, 0.0d, 73.3605d, 78.1115d, 66.3709d, 73.3605d, 78.1115d, 66.3709d,
				0.0d, 0.0d);

		var expectedDistances = List.of(6000.0d, 6000.0d, 6000.0d, 2000.0d, 2000.0d, 4472.1360d, 2000.0d, 2000.0d,
				4472.1360d, 4472.1360d, 4472.1360d, 4472.1360d);

		for (int i = 0; i < expectedLevels.size(); i++) {
			schedule.execute();
			var level = expectedLevels.get(i);
			assertThat(hydrophone.getReceivedSoundLevel())
					.withFailMessage(
							"unexpected sound level at tick " + SimulationTime.getTick() + " expectedLevel.idx: " + i
									+ ". Expected: " + level + ". Actual: " + hydrophone.getReceivedSoundLevel())
					.isCloseTo(level, within(0.0001d));

			var distance = space.getDistance(ship.getPosition(), hydrophone.getPosition()) * 400;
			var expectedDistance = expectedDistances.get(i);
			assertThat(expectedDistance)
					.withFailMessage(
							"unexpected distance at tick " + SimulationTime.getTick() + " expectedDistance.idx: " + i
									+ ". Expected: " + expectedDistance + ". Actual: " + distance)
					.isCloseTo(distance, within(0.0001d));

		}
	}

}

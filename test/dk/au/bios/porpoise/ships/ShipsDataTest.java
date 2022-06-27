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

package dk.au.bios.porpoise.ships;

import static dk.au.bios.porpoise.ships.VesselClass.CONTAINERSHIP;
import static dk.au.bios.porpoise.ships.VesselClass.GOVERNMENT_RESEARCH;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class ShipsDataTest {

	@Test
	public void loadShipDataFromJsonFile() throws Exception {
		var objMapper = new ObjectMapper();
		ShipsData shipsData = objMapper.readValue(ShipsDataTest.class.getResourceAsStream("ships.json"),
				ShipsData.class);

		assertThat(shipsData.getRoutes()).hasSize(2);
		assertThat(shipsData.getShips()).hasSize(3);

		assertThat(shipsData.getShips().get(0).getName()).isEqualTo("ao1");
		assertThat(shipsData.getShips().get(0).getType()).isEqualTo(CONTAINERSHIP);
		assertThat(shipsData.getShips().get(0).getLength()).isEqualTo(300.0);
		assertThat(shipsData.getShips().get(0).getRoute().getName()).isEqualTo("Aarhus-Odden");
		assertThat(shipsData.getShips().get(0).getTickStart()).isEqualTo(1);
		assertThat(shipsData.getShips().get(0).getTickEnd()).isEqualTo(999999999);

		assertThat(shipsData.getShips().get(1).getName()).isEqualTo("ao2");
		assertThat(shipsData.getShips().get(1).getType()).isEqualTo(GOVERNMENT_RESEARCH);
		assertThat(shipsData.getShips().get(1).getLength()).isEqualTo(82.90);
		assertThat(shipsData.getShips().get(1).getRoute().getName()).isEqualTo("Aarhus-Odden");
		assertThat(shipsData.getShips().get(1).getTickStart()).isEqualTo(-1);
		assertThat(shipsData.getShips().get(1).getTickEnd()).isEqualTo(Integer.MAX_VALUE);
	}

	@Test
	public void loadOneShip() throws Exception {
		var objMapper = new ObjectMapper();
		ShipsData shipsData = objMapper.readValue(ShipsDataTest.class.getResourceAsStream("oneship.json"),
				ShipsData.class);

		assertThat(shipsData.getRoutes()).hasSize(1);
		assertThat(shipsData.getShips()).hasSize(1);
		var ship = shipsData.getShips().get(0);
		assertThat(ship.getName()).isEqualTo("1");
		assertThat(ship.getType()).isEqualTo(CONTAINERSHIP);
		assertThat(ship.getLength()).isEqualTo(100.0);
		assertThat(ship.getRoute().getName()).isEqualTo("Route_1");
		assertThat(ship.getTickStart()).isEqualTo(1);
		assertThat(ship.getTickEnd()).isEqualTo(999999999);
		
		assertThat(shipsData.getRoutes().get(0).getName()).isEqualTo("Route_1");
		var buoy1 = shipsData.getRoutes().get(0).getRoute().get(0);
		assertThat(buoy1.getX()).isEqualTo(625584);
		assertThat(buoy1.getY()).isEqualTo(6250196);
		assertThat(buoy1.getSpeed()).isEqualTo(1.5852);
		assertThat(buoy1.getPause()).isEqualTo(0);
		
		var buoy2 = shipsData.getRoutes().get(0).getRoute().get(1);
		assertThat(buoy2.getX()).isEqualTo(666684);
		assertThat(buoy2.getY()).isEqualTo(6250196);
		assertThat(buoy2.getSpeed()).isEqualTo(3.121);
		assertThat(buoy2.getPause()).isEqualTo(6);

		var buoy3 = shipsData.getRoutes().get(0).getRoute().get(2);
		assertThat(buoy3.getX()).isEqualTo(695584);
		assertThat(buoy3.getY()).isEqualTo(6250196);
		assertThat(buoy3.getSpeed()).isEqualTo(3.121);
		assertThat(buoy3.getPause()).isEqualTo(0);
	}

}

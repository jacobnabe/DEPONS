/*
 * Copyright (C) 2017-2019 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

package dk.au.bios.porpoise.behaviour;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.behavior.PersistentSpatialMemory;
import dk.au.bios.porpoise.behavior.RandomSource;
import dk.au.bios.porpoise.landscape.DataFileMetaData;
import repast.simphony.space.continuous.NdPoint;

/**
 * Unit test for the PersistentSpatialMemory.
 */
class PersistenSpatialMemoryTest {

	@Test
	public void testCalculateMemCellNumber() {
		final var expectedCell = 210;

		Globals.setLandscapeMetadata(new DataFileMetaData(100, 100, 529473, 5972242, 400 , null));
		var psm = new PersistentSpatialMemory(100, 100, 10);

		assertThat(psm.calculateMemCellNumber(new NdPoint(50.0f, 50.0f))).isEqualTo(expectedCell);

//		and: "NdPoint in same memory cell (5x5)"
		assertThat(psm.calculateMemCellNumber(new NdPoint(50.0f, 50.0f))).isEqualTo(expectedCell);
		assertThat(psm.calculateMemCellNumber(new NdPoint(50.0f, 54.49f))).isEqualTo(expectedCell);
		assertThat(psm.calculateMemCellNumber(new NdPoint(49.5f, 50.0f))).isEqualTo(expectedCell);
		assertThat(psm.calculateMemCellNumber(new NdPoint(49.5f, 54.49f))).isEqualTo(expectedCell);

//		and:  "NdPoint outside memory cell"
		assertThat(psm.calculateMemCellNumber(new NdPoint(47.0f, 50.0f))).isNotEqualTo(expectedCell);
		assertThat(psm.calculateMemCellNumber(new NdPoint(49.49f, 50.0f))).isNotEqualTo(expectedCell);
		assertThat(psm.calculateMemCellNumber(new NdPoint(47.0f, 50.0f))).isNotEqualTo(expectedCell);
		assertThat(psm.calculateMemCellNumber(new NdPoint(47.0f, 50.0f))).isNotEqualTo(expectedCell);
	}

	@Test
	public void recordVisitAndObtainedEnergy() {
		Globals.setLandscapeMetadata(new DataFileMetaData(100, 100, 529473, 5972242, 400, null));
		var psm = new PersistentSpatialMemory(100, 100, 10);

		psm.updateMemory(new NdPoint(21, 21), 0.25);
		psm.updateMemory(new NdPoint(22, 22), 0.25);
		psm.updateMemory(new NdPoint(26, 21), 0.2);
		psm.updateMemory(new NdPoint(26, 21), 0.1);
		psm.updateMemory(new NdPoint(26, 21), 0.25);

//		then:
		assertThat(psm.getMemCellData().get(84).getTicksSpent()).isEqualTo(2);
		assertThat(psm.getMemCellData().get(84).getFoodObtained()).isEqualTo(0.5);
		assertThat(psm.getMemCellData().get(84).getEnergyExpectation()).isEqualTo(0.25, within(0.001));
		assertThat(psm.getMemCellData().get(85).getTicksSpent()).isEqualTo(3);
		assertThat(psm.getMemCellData().get(85).getFoodObtained()).isEqualTo(0.55);
		assertThat(psm.getMemCellData().get(85).getEnergyExpectation()).isEqualTo(0.183, within(0.001));
	}

	@Test
	public void randomSource() {
//		given: "Controlled randomness"
		var rando = mock(RandomSource.class);
		when(rando.nextEnergyNormal()).thenReturn(0.01d, 0.25d);

//		expect: "Getting first random"
		assertThat(rando.nextEnergyNormal()).isEqualTo(0.01d);
		assertThat(rando.nextEnergyNormal()).isEqualTo(0.25d);
	}

}

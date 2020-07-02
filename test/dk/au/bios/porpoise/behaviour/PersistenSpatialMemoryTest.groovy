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

import static org.junit.Assert.*
import static spock.util.matcher.HamcrestMatchers.*
import repast.simphony.space.continuous.NdPoint
import spock.lang.Specification
import dk.au.bios.porpoise.Globals
import dk.au.bios.porpoise.behavior.PersistentSpatialMemory
import dk.au.bios.porpoise.behavior.RandomSource
import dk.au.bios.porpoise.landscape.DataFileMetaData

/**
 * Unit test for the PersistentSpatialMemory.
 */
class PersistenSpatialMemoryTest extends Specification {

	def "testCalculateMemCellNumber"() {
		def final expectedCell = 210;

		given:
		Globals.landscapeMetaData = new DataFileMetaData(100, 100, 529473, 5972242, 400 , null);
		def psm = new PersistentSpatialMemory(100, 100, 10);

		expect:
		expectedCell == psm.calculateMemCellNumber(new NdPoint(50.0f, 50.0f))

		and: "NdPoint in same memory cell (5x5)"
		expectedCell == psm.calculateMemCellNumber(new NdPoint(50.0f, 50.0f))
		expectedCell == psm.calculateMemCellNumber(new NdPoint(50.0f, 54.49f))
		expectedCell == psm.calculateMemCellNumber(new NdPoint(49.5f, 50.0f))
		expectedCell == psm.calculateMemCellNumber(new NdPoint(49.5f, 54.49f))

		and:  "NdPoint outside memory cell"
		expectedCell != psm.calculateMemCellNumber(new NdPoint(47.0f, 50.0f))
		expectedCell != psm.calculateMemCellNumber(new NdPoint(49.49f, 50.0f))
		expectedCell != psm.calculateMemCellNumber(new NdPoint(47.0f, 50.0f))
		expectedCell != psm.calculateMemCellNumber(new NdPoint(47.0f, 50.0f))
	}

	def "Record visit and obtained energy"() {
		given:
		Globals.landscapeMetaData = new DataFileMetaData(100, 100, 529473, 5972242, 400 , null);
		def psm = new PersistentSpatialMemory(100, 100, 10);

		when:
		psm.updateMemory(new NdPoint(21, 21), 0.25);
		psm.updateMemory(new NdPoint(22, 22), 0.25);
		psm.updateMemory(new NdPoint(26, 21), 0.2);
		psm.updateMemory(new NdPoint(26, 21), 0.1);
		psm.updateMemory(new NdPoint(26, 21), 0.25);

		then:
		psm.getMemCellData().get(84).ticksSpent == 2
		psm.getMemCellData().get(84).foodObtained == 0.5
		0.25 closeTo(psm.getMemCellData().get(84).energyExpectation, 0.001)
		psm.getMemCellData().get(85).ticksSpent == 3
		psm.getMemCellData().get(85).foodObtained == 0.55
		0.183 closeTo(psm.getMemCellData().get(85).energyExpectation, 0.001)
	}

	def "Random Source"() {
		given: "Controlled randomness"
		def rando = Mock(RandomSource)
		rando.nextEnergyNormal() >>> [0.01f, 0.25f]

		expect: "Getting first random"
		rando.nextEnergyNormal() == 0.01f;
		rando.nextEnergyNormal() == 0.25f;
	}

}

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
import static spock.util.matcher.HamcrestMatchers.closeTo
import dk.au.bios.porpoise.AbstractSimulationBDDTest
import dk.au.bios.porpoise.Porpoise
import dk.au.bios.porpoise.SimulationParameters
import dk.au.bios.porpoise.behavior.DispersalFactory

/**
 * Unit test for the PersistentSpatialMemory.
 */
class DispersalPSMType3Test extends AbstractSimulationBDDTest {

	def setup() {
		DispersalFactory.setType("PSM-Type3");
	}

	def "Energy calculation"() {
	}

	/**
	 * We create a world 100x100 cells, which means 20x20 PSM cells.
	 */
	def "Find Most Attractive MemCell"() {
		given: "A world with a single porpoise and a baseline PSM setup"

		aNewWorld(100, 100);
		Porpoise p = aPorpoise(50.0, 50.0, 0.0);
		int[] basePsmVisits = [
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
			10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
			20, 25, 27, 31, 33, 34, 36, 43, 46, 47,
			52, 55, 68, 73, 77, 80, 81, 86, 92, 95,
			98, 104, 105, 110, 113, 125, 160, 183, 201,
			214, 217, 219, 245, 305, 356, 381
		]
		basePsmVisits.each { c ->
			def c0 = p.getPersistentSpatialMemory().calcMemCellCenterPoint(c)
			p.getPersistentSpatialMemory().updateMemory(c0, 0.1);
		}
		def cell1 = p.getPersistentSpatialMemory().calcMemCellCenterPoint(214);
		def cell2 = p.getPersistentSpatialMemory().calcMemCellCenterPoint(217);
		def cell3 = p.getPersistentSpatialMemory().calcMemCellCenterPoint(219);

		p.getPersistentSpatialMemory().updateMemory(cell1, 0.3);
		p.getPersistentSpatialMemory().updateMemory(cell2, 0.8);
		p.getPersistentSpatialMemory().updateMemory(cell3, 1.2);
		p.getPersistentSpatialMemory().updateMemory(cell3, 0.8);

		when: "no travel cost (Q1 is 0.0)"
		p.getDispersalBehaviour().activate()

		then: "cell3 is selected"
		p.getDispersalBehaviour().getTargetPosition() == cell3;

		when: "very low travel cost (Q1 is 0.01)"
		SimulationParameters.setQ1(0.01);
		p.getDispersalBehaviour().deactivate()
		p.getDispersalBehaviour().activate()

		then: "cell3 is selected"
		p.getDispersalBehaviour().getTargetPosition() == cell3;

		when: "low travel cost (Q1 is 0.05)"
		SimulationParameters.setQ1(0.05);
		p.getDispersalBehaviour().deactivate()
		p.getDispersalBehaviour().activate()

		then: "cell2 is selected"
		p.getDispersalBehaviour().getTargetPosition() == cell2;

		when: "high travel cost (Q1 is 0.8)"
		SimulationParameters.setQ1(0.8);
		p.getDispersalBehaviour().deactivate()
		p.getDispersalBehaviour().activate()

		then: "cell1 is selected"
		p.getDispersalBehaviour().getTargetPosition() == cell1;
	}

}

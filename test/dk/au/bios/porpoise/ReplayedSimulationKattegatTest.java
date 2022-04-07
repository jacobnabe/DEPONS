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

package dk.au.bios.porpoise;

import org.junit.jupiter.api.DisplayName;

import dk.au.bios.porpoise.util.test.SystemTest;

/**
 * Unit test for the DanTysk landscape.
 */
public class ReplayedSimulationKattegatTest extends AbstractReplayedSimulationTest {

	@SystemTest
	@DisplayName("Kattegat, Dispersal Off, No Turbines, 1 Porpoises")
	public void dispOffNoTurbines() throws Exception {
		simulation("test/dk/au/bios/porpoise/testdata_Kattegat_DispOff_NoTurbines_1porp.txt");
	}

	@SystemTest
	@DisplayName("Kattegat, Dispersal Undirected, No Turbines, 1 Porpoises")
	public void dispUndirectedNoTurbines() throws Exception {
		simulation("test/dk/au/bios/porpoise/testdata_Kattegat_Undirected_NoTurbines_1porp.txt");
	}

	@SystemTest
	@DisplayName("Kattegat, Dispersal InnerDanishWaters, No Turbines, 1 Porpoises")
	public void dispIDWNoTurbines() throws Exception {
		simulation("test/dk/au/bios/porpoise/testdata_Kattegat_InnerDanishWaters_NoTurbines_1porp.txt");
	}

//	@SystemTest
//	void "DanTysk, Dispersal Undirected, Turbines Off, 10 Porpoises"() {
//		simulation("test/dk/au/bios/porpoise/testdata_DanTysk_Undirected_NoTurbines_10porp.txt");
//	}
//
//	@SystemTest
//	void "DanTysk, Dispersal PSM-Type2, Turbines Off, 10 Porpoises"() {
//		simulation("test/dk/au/bios/porpoise/testdata_DanTysk_PSM2_NoTurbines_10porp.txt");
//	}

	/* PSM-Type3 is currently disabled, re-enable in parameters.xml to run these
	@SystemTest
	void "DanTysk, Dispersal PSM-Type3, Turbines DanTysk-construction, 10 Porpoises"() {
		simulation("test/dk/au/bios/porpoise/testdata_DanTysk_PSM3_DanTysk-construction_10porp.txt");
	}

	@SystemTest
	void "DanTysk, Dispersal PSM-Type3, Turbines Off, 10 Porpoises"() {
		simulation("test/dk/au/bios/porpoise/testdata_DanTysk_PSM3_NoTurbines_10porp.txt");
	}

	@SystemTest
	void "DanTysk, Dispersal PSM-Type3-randdir, Turbines Off, 10 Porpoises"() {
		simulation("test/dk/au/bios/porpoise/testdata_DanTysk_PSM3randdir_NoTurbines_10porp.txt");
	}

	@SystemTest
	void "DanTysk, Dispersal PSM-Type3-randdist, Turbines Off, 10 Porpoises"() {
		simulation("test/dk/au/bios/porpoise/testdata_DanTysk_PSM3randdist_NoTurbines_10porp.txt");
	}

	*/
}

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

package dk.au.bios.porpoise.behavior;

import dk.au.bios.porpoise.Porpoise;

public final class DispersalFactory {

	private static DispersalType type;

	public enum DispersalType {
		Off, PSM_Type1, PSM_Type2, PSM_Type3, PSM_Type3randdir, PSM_Type3randdist, InnerDanishWaters, Undirected
	}

	private DispersalFactory() {
		// Utility class
	}

	public static void setType(final String typeName) {
		if ("PSM-Type1".equals(typeName)) {
			DispersalFactory.type = DispersalType.PSM_Type1;
		} else if ("PSM-Type2".equals(typeName)) {
			DispersalFactory.type = DispersalType.PSM_Type2;
		} else if ("PSM-Type3".equals(typeName)) {
			DispersalFactory.type = DispersalType.PSM_Type3;
		} else if ("PSM-Type3-randdir".equals(typeName)) {
			DispersalFactory.type = DispersalType.PSM_Type3randdir;
		} else if ("PSM-Type3-randdist".equals(typeName)) {
			DispersalFactory.type = DispersalType.PSM_Type3randdist;
		} else if ("InnerDanishWaters".equals(typeName)) {
			DispersalFactory.type = DispersalType.InnerDanishWaters;
		} else if ("Undirected".equals(typeName)) {
			DispersalFactory.type = DispersalType.Undirected;
		} else {
			DispersalFactory.type = DispersalType.Off;
		}
	}

	public static boolean isOff() {
		return type == DispersalType.Off;
	}

	public static Dispersal getPSMDispersal(final Porpoise owner) {
		switch (type) {
		case Off:
			return new DispersalOff();
		case PSM_Type1:
			return new DispersalPSMType1(owner);
		case PSM_Type2:
			return new DispersalPSMType2(owner);
		case PSM_Type3:
			return new DispersalPSMType3(owner);
		case PSM_Type3randdir:
			return new DispersalPSMType3randdir(owner);
		case PSM_Type3randdist:
			return new DispersalPSMType3randdist(owner);
		case InnerDanishWaters:
			return new InnerDanishWatersDispersal(owner);
		case Undirected:
			return new UndirectedDispersal(owner);
		default:
			throw new RuntimeException("Unknown dispersal type.");
		}
	}

}

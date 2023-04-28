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

package dk.au.bios.porpoise.ships;

import repast.simphony.parameter.IllegalParameterException;

public class JomopansEchoSPL {

	public double calculate(VesselClass vesselClass, double speed, double length, int band) {
		double decidecadeBandSourceLevel = calculateSourceLevel(vesselClass, speed, length, band);
		return decidecadeBandSourceLevel;
	}

	protected double calculateSourceLevel(VesselClass vesselClass, double speed, double length, int band) {
		if (speed == 0) {
			return 0.0d;
		}

		double frequency = Math.pow(10, ((double) band) / 10.0) * 1000;

		double $B$10 = speed;
		double $B$11 = length;
		double B48 = frequency;

		double D48 = lookupVC(vesselClass);
		boolean lfHump = lookupCargo(vesselClass) && (frequency < 100);
		double I48 = lfHump ? 208 : 191;
		double J48 = lfHump ? 2 : 0;
		double K48 = lfHump ? lookupDlo(vesselClass) : lookupDhi(vesselClass);
		double L48 = lfHump ? 600 / D48: 480 / D48;
		double C48 = 300 / 3.28084; // L_ref

		double spSpectralDensity = I48 - 10 * (J48 + 2) * Math.log10(L48) + 5 * J48 * Math.log10(B48)
				- 10 * Math.log10(Math.pow((1 - Math.pow((B48 / L48), (0.5 * (J48 + 2)))), 2) + Math.pow(K48, 2))
				+ 60 * Math.log10($B$10 / D48) + 20 * Math.log10($B$11 / C48);

		double decidecadeBandSourceLevel = spSpectralDensity + 10 * Math.log10(0.231 * B48);

		return decidecadeBandSourceLevel;
	}

	private double lookupVC(VesselClass vc) {
		switch (vc) {
		case BULKER:
			return 13.9;
		case CONTAINERSHIP:
			return 18.0;
		case CRUISE:
			return 17.1;
		case DREDGER:
			return 9.5;
		case FISHING:
			return 6.4;
		case GOVERNMENT_RESEARCH:
			return 8.0;
		case NAVAL:
			return 11.1;
		case OTHER:
			return 7.4;
		case PASSENGER:
			return 9.7;
		case RECREATIONAL:
			return 10.6;
		case TANKER:
			return 12.4;
		case TUG:
			return 3.7;
		case VEHICLE_CARRIER:
			return 15.8;
		default:
			throw new IllegalParameterException("Unknown VesselType: " + vc);
		}
	}

	private boolean lookupCargo(VesselClass vc) {
		switch (vc) {
		case BULKER:
		case CONTAINERSHIP:
		case TANKER:
		case VEHICLE_CARRIER:
			return true;
		case CRUISE:
		case DREDGER:
		case FISHING:
		case GOVERNMENT_RESEARCH:
		case NAVAL:
		case OTHER:
		case PASSENGER:
		case RECREATIONAL:
		case TUG:
			return false;
		default:
			throw new IllegalParameterException("Unknown VesselType: " + vc);
		}
	}

	private double lookupDlo(VesselClass vc) {
		switch(vc) {
		case BULKER:
		case CONTAINERSHIP:
			return 0.8;
		case CRUISE:
		case DREDGER:
		case FISHING: 
		case GOVERNMENT_RESEARCH:
		case NAVAL:
		case OTHER:
		case PASSENGER:
		case RECREATIONAL:
		case TANKER:
		case TUG:
		case VEHICLE_CARRIER:
			return 1;
		default:
			throw new IllegalParameterException("Unknown VesselType: " + vc);
		}
	}

	private double lookupDhi(VesselClass vc) {
		switch(vc) {
		case CRUISE:
			return 4;
		case BULKER:
		case CONTAINERSHIP:
		case DREDGER:
		case FISHING: 
		case GOVERNMENT_RESEARCH:
		case NAVAL:
		case OTHER:
		case PASSENGER:
		case RECREATIONAL:
		case TANKER:
		case TUG:
		case VEHICLE_CARRIER:
			return 3;
		default:
			throw new IllegalParameterException("Unknown VesselType: " + vc);
		}
	}

}

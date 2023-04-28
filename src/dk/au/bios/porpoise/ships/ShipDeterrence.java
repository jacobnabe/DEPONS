/*
 * Copyright (C) 2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import dk.au.bios.porpoise.Ship;

public class ShipDeterrence {

	private static class Step {
		public Ship ship;
		public double deterX;
		public double deterY;
		public double mag;
		public double receivedLevelVHF;
	}

	private Step[] deterSteps = new Step[30];

	public void recordStep(int step, Ship ship, double deterX, double deterY, double mag, double receivedLevelVHF) {
		var ds = new Step();
		ds.ship = ship;
		ds.deterX = deterX;
		ds.deterY = deterY;
		ds.mag = mag;
		ds.receivedLevelVHF = receivedLevelVHF;

		if (deterSteps[step] == null) {
			deterSteps[step] = ds;
		} else if (deterSteps[step].receivedLevelVHF < receivedLevelVHF) {
			deterSteps[step] = ds;
		}
	}
	
	public void resetSteps() {
		for (int i = 0; i < deterSteps.length; i++) {
			deterSteps[i] = null;
		}
	}

	public double deterrenceStrength() {
		double deterX = 0.0d;
		double deterY = 0.0d;
		for (Step ds : deterSteps) {
			if (ds != null) {
				deterX += ds.deterX;
				deterY += ds.deterY;
			}
		}

		var strength = Math.sqrt(Math.pow(deterX, 2) + Math.pow(deterY, 2));
		return strength;
	}

	public double deterrenceVtX() {
		double deterX = 0.0d;
		for (Step ds : deterSteps) {
			if (ds != null) {
				deterX += ds.deterX;
			}
		}
		
		return deterX;
	}

	public double deterrenceVtY() {
		double deterY = 0.0d;
		for (Step ds : deterSteps) {
			if (ds != null) {
				deterY += ds.deterY;
			}
		}
		
		return deterY;
	}

	public double getLoudestShipSPL() {
		double loudest = 0.0d;
		for (Step ds : deterSteps) {
			if (ds != null) {
				if (ds.receivedLevelVHF > loudest) {
					loudest = ds.receivedLevelVHF;
				}
			}
		}

		return loudest;
	}

}

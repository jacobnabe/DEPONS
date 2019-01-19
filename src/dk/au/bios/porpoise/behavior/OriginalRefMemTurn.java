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

import repast.simphony.space.continuous.NdPoint;
import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.landscape.CellData;
import dk.au.bios.porpoise.util.CircularBuffer;
import dk.au.bios.porpoise.util.DebugLog;
import dk.au.bios.porpoise.util.ReplayHelper;

/**
 * This is the original ref-mem-turn as converted from NetLogo. The simulation is normally using the improved
 * FastRefMemTurn implementation.
 */
public class OriginalRefMemTurn implements RefMemTurnCalculator {

	@Override
	public double[] refMemTurn(final Porpoise p, final CellData cellData, final CircularBuffer<Double> storedUtilList,
			final CircularBuffer<NdPoint> posList) {
		// Move towards places visited previously if food was found there and they aren't too far away or forgotten.

		// Stationary food species. The stored intrisic patch utility for t=0. Initially it is either 0, 1, or -9999,
		// but grows logistically after food is eaten
		double bb = cellData.getFoodLevel(Agent.ndPointToGridPoint(p.getPosition()));

		if (!(Math.abs(bb) > 0)) {
			// There are errors in food availability -- sometimes Na is calculated even though depth is > 0. Catch error
			// here
			bb = 0;
			if (DebugLog.isEnabledFor(4)) {
				DebugLog.print4("Replaced NaN food value with 0");
				DebugLog.print4("{}", Agent.ndPointToGridPoint(p.getPosition()));
			}
		}

		storedUtilList.add(bb);
		/*
		 * System.out.println("debug-pos " + Agent.ndPointToGridPoint(p.getPosition())); System.out.println("debug-bb "
		 * + bb); System.out.println("debug-storedUtilList " + storedUtilList);
		 */

		// Update reference memory strength for past locations
		final double maxMem = 0.999;

		// Set patch value for each past location -- perceived patch utility (= reference memory x intrinsic patch
		// utility (stuff eaten)), divided by DIRECT distance
		final int entries = posList.size();
		final double[] perceivedUtilList = new double[entries + 1];

		perceivedUtilList[0] = storedUtilList.get(0) * maxMem;

		// each element in the list is a list with an x-and a y-direction. Vector for first element (this place) has
		// length 0, the others have length 1
		final NdPoint[] attrVectorList = new NdPoint[entries + 1];
		attrVectorList[0] = new NdPoint(0, 0);

		NdPoint oneAttrVector = new NdPoint(0, 0);
		double vectorLength = 0;

		int ii = 1;
		double distToFoodPos = 0;

		ReplayHelper.print("porp-ref-mem-turn-1. pos-list: {0}", posList);
		while (ii < entries) {
			if (storedUtilList.get(ii) == 0) { // save time by skipping dist measure when there is no food
				perceivedUtilList[ii] = 0;
				attrVectorList[ii] = new NdPoint(0, 0);
			}
			if (!(storedUtilList.get(ii) == 0)) { // save time by skipping dist measure when there is no food
				distToFoodPos = p.distanceXY(posList.get(ii));

				if (distToFoodPos < 1E-20) {
					perceivedUtilList[ii] = 9999; // arbitrary large value for close dist
				} else {
					perceivedUtilList[ii] = storedUtilList.get(ii) * RefMem.getRefMemStrength(ii) / distToFoodPos;
				}

				final NdPoint pos = p.getPosition();

				// Create attraction vectors; unit-vectors pointing towards the patches in memory
				oneAttrVector = new NdPoint(posList.get(ii).getX() - pos.getX(), posList.get(ii).getY() - pos.getY());

				// make sure that it works with wrapping landscapes:
				if (oneAttrVector.getX() > Globals.getWorldWidth() / 2) {
					oneAttrVector = new NdPoint(oneAttrVector.getX() - Globals.getWorldWidth(), oneAttrVector.getY());
				}
				if (oneAttrVector.getX() < -Globals.getWorldWidth() / 2) {
					oneAttrVector = new NdPoint(oneAttrVector.getX() + Globals.getWorldWidth(), oneAttrVector.getY());
				}
				if (oneAttrVector.getY() > Globals.getWorldHeight() / 2) {
					oneAttrVector = new NdPoint(oneAttrVector.getX(), oneAttrVector.getY() + Globals.getWorldHeight());
				}
				if (oneAttrVector.getY() < -Globals.getWorldHeight() / 2) {
					oneAttrVector = new NdPoint(oneAttrVector.getX(), oneAttrVector.getY() - Globals.getWorldHeight());
				}

				vectorLength = Math.sqrt(oneAttrVector.getX() * oneAttrVector.getX() + oneAttrVector.getY()
						* oneAttrVector.getY());

				if (vectorLength == 0) {
					return null;
				}

				attrVectorList[ii] = new NdPoint(oneAttrVector.getX() / vectorLength, oneAttrVector.getY()
						/ vectorLength);
			}
			ii++;
		}

		// Calculate resultant attraction vector vt as sum of products of individual values and attraction vectors (eqn
		// 5). May have length != 1

		ii = 1; // no attraction to current pos (t=0)
		double vtx = 0;
		double vty = 0;

		while (ii < entries) {
			vtx += perceivedUtilList[ii] * attrVectorList[ii].getX();
			vty += perceivedUtilList[ii] * attrVectorList[ii].getY();

			ii++;
		}

		return new double[] { vtx, vty };
	}

}

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
import repast.simphony.space.grid.GridPoint;
import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.landscape.CellData;
import dk.au.bios.porpoise.util.CircularBuffer;
import dk.au.bios.porpoise.util.DebugLog;

/**
 * Improved implementation of the RefMemTurnCalculator.
 */
public class FastRefMemTurn implements RefMemTurnCalculator {

	@Override
	public double[] refMemTurn(final Porpoise p, final CellData cellData, final CircularBuffer<Double> storedUtilList,
			final CircularBuffer<NdPoint> posList) {
		// Move towards places visited previously if food was found there and they aren't too far away or forgotten.

		final GridPoint pos = Agent.ndPointToGridPoint(p.getPosition());

		// Stationary food species. The stored intrisic patch utility for t=0. Initially it is either 0, 1, or -9999,
		// but grows logistically after food is eaten
		double bb = cellData.getFoodLevel(pos);

		if (Double.isNaN(bb)) { // bb = Na ?!
			// There are errors in food availability -- sometimes Na is calculated even though depth is > 0. Catch error
			// here
			bb = 0;

			DebugLog.print4("Replaced NaN food value with 0");
			DebugLog.print4("{}", pos);
		}

		storedUtilList.add(bb);
		// this.storedUtilList.addFirst(bb);

		double vectorLgt = 0;
		int ii = 1;

		double distToFoodpos = 0;
		double vtX = 0;
		double vtY = 0;

		while (ii < posList.size()) {
			if (storedUtilList.get(ii) != 0) {
				distToFoodpos = p.distanceXY(posList.get(ii));

				double factor;

				if (distToFoodpos < 1E-20) {
					factor = 9999;
				} else {
					factor = storedUtilList.get(ii) * RefMem.getRefMemStrength(ii) / distToFoodpos;
				}

				final NdPoint posII = posList.get(ii);
				final NdPoint curPos = p.getPosition();
				double attrX = posII.getX() - curPos.getX();
				double attrY = posII.getY() - curPos.getY();

				if (attrX > Globals.getWorldWidth() / 2) {
					attrX -= Globals.getWorldWidth();
				}
				if (attrX < -Globals.getWorldWidth() / 2) {
					attrX += Globals.getWorldWidth();
				}
				if (attrY > Globals.getWorldHeight() / 2) {
					attrY -= Globals.getWorldHeight();
				}
				if (attrY < -Globals.getWorldHeight() / 2) {
					attrY += Globals.getWorldHeight();
				}

				vectorLgt = Math.sqrt(attrX * attrX + attrY * attrY);

				if (vectorLgt == 0) {
					if (DebugLog.isEnabledFor(4)) {
						DebugLog.print4("{} attr-vector-lgt = {} skipping to next porp", p.getId(), vectorLgt);
					}
					return null;
				}

				attrX /= vectorLgt;
				attrY /= vectorLgt;

				vtX += (factor * attrX);
				vtY += (factor * attrY);
			}
			ii++;
		}

		DebugLog.print4("Food here: {}, Attr.v: {},{}", bb, vtX, vtY);

		return new double[] { vtX, vtY };
	}

}

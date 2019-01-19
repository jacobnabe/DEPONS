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
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.landscape.CellData;
import dk.au.bios.porpoise.util.CircularBuffer;

/**
 * Interface for implementation of the reference memory (ref-mem)
 */
public interface RefMemTurnCalculator {

	/**
	 * Calculates the ref mem turn for the passed porpoise. The ref mem turn vector, the vector to affect the movement
	 * of the porpoise is returned a double array with two elements.
	 *
	 * @param p The porpoise to calculate the ref mem turn vector for.
	 * @param cellData The cell data containing information about the food and wather depth levels for the model.
	 * @param storedUtilList The list of found food levels in the past TODO: We can optimize this list a lot, if we only
	 * stored patches with food and the step they were found then we can calculate the memory decay on demand and will
	 * have a shorter list.
	 * @param posList The list of the previous positions of the porpoise.
	 * @return The resulting ref mem turn vector, or NULL if it could not be calculated.
	 */
	double[] refMemTurn(Porpoise p, CellData cellData, CircularBuffer<Double> storedUtilList,
			CircularBuffer<NdPoint> posList);

}

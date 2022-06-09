/*
 * Copyright (C) 2017-2022 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import dk.au.bios.porpoise.util.SimulationTime;
import repast.simphony.context.Context;

/**
 * Dummy placeholder for the squares in the model.
 */
public class Block extends Agent {

	private static double lastUpdate = -1;
	private static int[] blockCount;
	private static Context<Agent> context;

	/**
	 * Constructor.
	 *
	 * @param space A reference to the space.
	 * @param grid A reference to the grid.
	 * @param id The id number of the square, this is the number from the
	 * @param context The current context.
	 */
	public Block(final long id, final Context<Agent> context) {
		super(id);
		Block.context = context;
	}

	public int getPorpoiseCount() {
		update();

		return blockCount[(int) this.getId()];
	}

	public static void initialize(final int numBlocks) {
		blockCount = new int[numBlocks];
	}

	// TODO Internal synchronization object
	public static synchronized void update() {
		if (lastUpdate != SimulationTime.getTick()) {
			// update is required

			for (int i = 0; i < blockCount.length; i++) {
				blockCount[i] = 0;
			}

			for (final Agent a : context.getObjects(Porpoise.class)) {
				final Porpoise p = (Porpoise) a;
				final int block = p.getBlock();

				if (block >= 0) {
					blockCount[block]++;
					//} else {
					// Some water fields seems to be marked as "no block" -999
					// System.out.println("Porpoise " + p.getId() + " is in block " + p.getBlock() + "?! " +
					// p.getPosition());
				}
			}

			// Ensure that we only update the table once per tick.
			lastUpdate = SimulationTime.getTick();
		}
	}

}

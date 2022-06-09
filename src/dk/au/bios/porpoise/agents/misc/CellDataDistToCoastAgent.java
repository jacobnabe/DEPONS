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

package dk.au.bios.porpoise.agents.misc;

import java.awt.Color;

import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import repast.simphony.space.continuous.NdPoint;

/**
 * Utility-agent to display the distance to the coast based on the CellData. This is the most efficient way to display
 * the image. The agent does not perform any tasks beyond being displayed.
 */
public class CellDataDistToCoastAgent extends Agent {

	private static final Color LAND_COLOR = new Color(0.921f, 0.886f, 0.854f);

	private double minValue = Double.MAX_VALUE;
	private double maxValue = Double.MIN_VALUE;

	public CellDataDistToCoastAgent() {
		super(0);

		for (int x = 0; x < Globals.getWorldWidth(); x++) {
			for (int y = 0; y < Globals.getWorldHeight(); y++) {
				final double val = Globals.getCellData().getDistanceToCoast(x, y);
				if (val > maxValue) {
					maxValue = val;
				} else if (val < minValue && val > 0.00000f) { // Should really use the NODATA_value instead
					minValue = val;
				}
			}
		}
		System.out.println("CellDataDistToCoastAgent : " + minValue + " - " + maxValue);
	}

	public void initialize() {
		setPosition(new NdPoint(Globals.getWorldWidth() / 2, Globals.getWorldHeight() / 2));
	}

	public int getPointRGB(final int x, final int y) {
		final int realY = Globals.getWorldHeight() - y - 1;

		final double val = Globals.getCellData().getDistanceToCoast(x, realY);
		if (val > 0.0000000f) {
			final Color color = Color.RED;
			int alpha = 255;
			final double pctInRange = (val - minValue) / maxValue;
			alpha *= pctInRange;
			if (alpha < 1) {
				alpha = 1;
			}
			// if (pctInRange < 0.0001f) {
			// alpha = 0;
			System.out.println("CellDataDistToCoastAgent : " + val + " (" + pctInRange + " / " + alpha + ")");
			// }
			return color.getRGB() + (alpha << 24);
		} else {
			// System.out.println("CellDataDistToCoastAgent : " + val + " (LAND)");
			return LAND_COLOR.getRGB();
		}

	}

}

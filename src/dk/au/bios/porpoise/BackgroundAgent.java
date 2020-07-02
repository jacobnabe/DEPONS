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

import java.awt.Color;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

/**
 * Utility-agent to display the background image. This is the most efficient way to display the background image. The
 * agent does not perform any tasks beyond being displayed.
 */
public class BackgroundAgent extends Agent {

	private static final Color LAND_COLOR = new Color(0.921f, 0.886f, 0.854f);
	private static final Color[] SHADES_OF_BLUE = new Color[120];

	private static double grownFood;

	private double minValue = Double.MAX_VALUE;
	private double maxValue = Double.MIN_VALUE;
	private final double rangeDiv;

	public BackgroundAgent(final ContinuousSpace<Agent> space, final Grid<Agent> grid) {
		super(space, grid, 0);

		for (int x = 0; x < Globals.getWorldWidth(); x++) {
			for (int y = 0; y < Globals.getWorldHeight(); y++) {
				final double val = Globals.getCellData().getDepth(new GridPoint(x, y));
				if (val > maxValue) {
					maxValue = val;
				} else if (val < minValue && val > 0.00000f) { // Should really use the NODATA_value instead
					minValue = val;
				}
			}
		}

		final double range = maxValue - minValue;
		rangeDiv = range / SHADES_OF_BLUE.length;

		// Calculate shades
		for (int i = 0; i < SHADES_OF_BLUE.length; i++) {
			final float factor = ((SHADES_OF_BLUE.length - i) * (0.6f / SHADES_OF_BLUE.length));
			SHADES_OF_BLUE[i] = Color.getHSBColor(0.64f, 1.0f - factor, 0.75f);
		}
	}

	public void initialize() {
		setPosition(new NdPoint(Globals.getWorldWidth() / 2, Globals.getWorldHeight() / 2));
	}

	public int getPointRGB(final int x, final int y) {
		final int realY = Globals.getWorldHeight() - y - 1;
		final double val = Globals.getCellData().getDepth(new GridPoint(x, realY));
		Color color;
		if (val >= 0.0000f) {
			int idx = (int) Math.round((val - minValue) / rangeDiv);
			if (idx > SHADES_OF_BLUE.length - 1) {
				idx = SHADES_OF_BLUE.length - 1;
			}
			color = SHADES_OF_BLUE[idx];

		} else {
			color = LAND_COLOR;
		}

		return color.getRGB();
	}

	public double getGrownFood() {
		return BackgroundAgent.grownFood;
	}

	public static void setGrownFood(final double grownFood) {
		BackgroundAgent.grownFood = grownFood;
	}

}

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

import repast.simphony.space.Dimensions;
import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;

/**
 * Renders the ValueLayer containing the land-mass values.
 *
 * The areas with land (no water) contain the value 0. Any other value between 0 and 1 denotes the water depth.
 *
 * The water depth is rendered with shades of blue. The setup and usage (index) is fairly rudimentary and likely to
 * break if changed (e.g. more shades).
 *
 * This has been replaced by the BackgroundAgent.
 */
public class LandValueLayerStyleOGL implements ValueLayerStyleOGL {

	private static final Color LAND_COLOR = new Color(0.921f, 0.886f, 0.854f);
	private static final Color[] SHADES_OF_BLUE = new Color[20];
	private ValueLayer valueLayer;
	private double minValue = Double.MAX_VALUE;
	private double maxValue = Double.MIN_VALUE;
	private double rangeDiv;

	@Override
	public void init(final ValueLayer layer) {
		this.valueLayer = layer;

		// Calculate and normalize value range
		final Dimensions vlDims = layer.getDimensions();

		double width = vlDims.getWidth();
		double height = vlDims.getHeight();
		while (width >= 0) {
			while (height >= 0) {
				final double val = layer.get(width, height);
				if (val > maxValue) {
					maxValue = val;
				} else if (val < minValue && val > 0.00000f) { // Should really use the NODATA_value instead
					minValue = val;
				}
				height -= 1;
			}
			height = vlDims.getHeight();
			width -= 1;
		}

		final double range = maxValue - minValue;
		rangeDiv = range / SHADES_OF_BLUE.length;
		System.out.printf("Min value: %f - max value: %f - range: %f - rangeDiv %f\n", minValue, maxValue, range,
				rangeDiv);

		// Calculate shades
		for (int i = 0; i < SHADES_OF_BLUE.length; i++) {
			SHADES_OF_BLUE[i] = Color.getHSBColor(0.64f, (1.0f - (SHADES_OF_BLUE.length - i) * 0.03f), 0.8f);
			// SHADES_OF_BLUE[i] = new Color(0.0f, 0.0f, 1.0f - (shadeIncrement * i));
		}
	}

	/**
	 * Get the cell size. This should correspond with the cell size defined for the display.
	 *
	 * @return The cell size (2.5f)
	 */
	@Override
	public float getCellSize() {
		return 2.5f;
	}

	/**
	 * Get the color for a cell.
	 *
	 * If the value is 0, then the land color is returned. For any other value between 0 and 1, a shade of blue is
	 * returned.
	 *
	 * @return Color for the cell.
	 */
	@Override
	public Color getColor(final double... coordinates) {
		final double val = valueLayer.get(coordinates);
		if (val >= 0.0000f) {
			int idx = (int) Math.round((val - minValue) / rangeDiv);
			if (idx > SHADES_OF_BLUE.length - 1) {
				idx = SHADES_OF_BLUE.length - 1;
			}
			return SHADES_OF_BLUE[idx];
		} else {
			return LAND_COLOR;
		}
	}

}

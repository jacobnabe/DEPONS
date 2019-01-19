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

package dk.au.bios.porpoise.ui;

import java.awt.Color;

import repast.simphony.gis.styleEditor.SimpleMarkFactory;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.ShapeFactory2D;
import saf.v3d.scene.VSpatial;
import dk.au.bios.porpoise.FoodPatch;
import dk.au.bios.porpoise.SimulationParameters;

/**
 * The visual style for the FoodPatch agent.
 */
public class FoodPatchStyle extends DefaultStyleOGL2D {

	private static SimpleMarkFactory markFac = new SimpleMarkFactory();

	@Override
	public void init(final ShapeFactory2D factory) {
		super.init(factory);
	}

	@Override
	public VSpatial getVSpatial(final Object agent, final VSpatial spatial) {
		if (spatial == null) {
			final VSpatial newSpatial = shapeFactory.createShape(markFac.getMark("square"), true);

			return newSpatial;
		}
		return spatial;
	}

	@Override
	public Color getColor(final Object object) {
		if (object instanceof FoodPatch) {
			final FoodPatch p = (FoodPatch) object;

			final double foodValue = p.getFoodValue();
			final double maxU = SimulationParameters.getMaxU();

			if (foodValue <= 0) {
				return Color.WHITE;
			} else if (foodValue <= 0.02 * maxU) {
				return Color.RED;
			} else if (foodValue <= 0.1 * maxU) {
				return Color.PINK;
			} else if (foodValue <= 0.25 * maxU) {
				return Color.ORANGE;
			} else if (foodValue <= 0.5 * maxU) {
				return Color.YELLOW;
			} else {
				return Color.GREEN;
			}
		}

		return Color.MAGENTA;
	}

	@Override
	public float getScale(final Object object) {
		return 2;
	}

}

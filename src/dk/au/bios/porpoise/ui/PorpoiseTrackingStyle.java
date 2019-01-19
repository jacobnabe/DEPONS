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

import aragost.repast.visualization.UpdatableTexture2D;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VImage2D;
import saf.v3d.scene.VSpatial;

import dk.au.bios.porpoise.agents.misc.TrackingDisplayAgent;

/**
 * The visual style for the TrackingDisplayAgent. This holds the updateable image used to display the tracking paths of
 * one or more tracked porpoises.
 */
public class PorpoiseTrackingStyle extends DefaultStyleOGL2D {

	private UpdatableTexture2D td;

	@Override
	public VSpatial getVSpatial(final Object agent, final VSpatial spatial) {
		final TrackingDisplayAgent tda = (TrackingDisplayAgent) agent;
		if (spatial == null) {
			td = new UpdatableTexture2D(tda.getImage(), tda.getDirtyRegion());
			final VSpatial newSpatial = new VImage2D(td);
			// spatial = new VImage2D(new Texture2D(tda.getImage()));
			return newSpatial;
		}

		return spatial;
	}

	@Override
	public Color getColor(final Object object) {
		return Color.MAGENTA;
	}

	@Override
	public float getScale(final Object object) {
		return 2.5f;
	}

}

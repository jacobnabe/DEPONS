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
import java.awt.image.BufferedImage;

import aragost.repast.visualization.DirtyRegion;
import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import repast.simphony.space.continuous.NdPoint;

/**
 * A helper-agent to display the cells visited by a tracked porpoise (RandomPorpoiseReportProxy)
 */
public class TrackingDisplayAgent extends Agent {

	private final BufferedImage img;
	private final DirtyRegion dr = new DirtyRegion(Globals.getWorldWidth(), Globals.getWorldHeight(), 0, 0);

	public TrackingDisplayAgent(final long id) {
		super(id);
		img = new BufferedImage(Globals.getWorldWidth(), Globals.getWorldHeight(), BufferedImage.TYPE_INT_ARGB);
	}

	public void initialize() {
		setPosition(new NdPoint(Globals.getWorldWidth() / 2, Globals.getWorldHeight() / 2));
	}

	public BufferedImage getImage() {
		return img;
	}

	public void visited(final int x, final int y, final int visitType) {
		Color c;
		if (visitType == 2) {
			c = Color.DARK_GRAY; // PSM active
		} else if (visitType == 1) {
			c = Color.RED; // standard move
		} else {
			c = Color.MAGENTA; // Unknown visit!
		}
		if (x < img.getWidth() && y > 0 && y < img.getHeight()) { // Bounds-check for cases of "rounding" issues
			img.setRGB(x, img.getHeight() - y, c.getRGB());
			synchronized (dr) {
				dr.markPoint(x, img.getHeight() - y);
			}
		}
	}

	public DirtyRegion getDirtyRegion() {
		return dr;
	}

}

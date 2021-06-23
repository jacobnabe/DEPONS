/*
 * Copyright (C) 2017-2021 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import java.io.IOException;
import java.io.InputStream;

import cern.colt.Arrays;
import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.SimulationConstants;
import dk.au.bios.porpoise.SimulationParameters;
import dk.au.bios.porpoise.util.ASCUtil;
import dk.au.bios.porpoise.util.DebugLog;
import dk.au.bios.porpoise.util.ReplayHelper;
import dk.au.bios.porpoise.util.SimulationTime;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.GridPoint;

public class InnerDanishWatersDispersal implements Dispersal {

	private static final String DISPERSAL_SHORT_NAME = "IDW";

	private final static double MIN_DIST_TO_TARGET = 100; // This was hardcoded in Globals before

	// Originally found in Globals
	public static double[] BLOCK_VAL_HOMO = new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			1, 1, 1, 1 };
	public static double[] BLOCK_VAL_KAT_1 = new double[] { 0, 0.007, 0.0042, 0.0022, 8e-04, 0, 0, 0.0454, 0.0522,
			0.0176, 0.0185, 0, 0, 0.0458, 0.0348, 0.0062, 0.0182, 0.4446, 0, 0.2898, 0.1013, 0.0769, 0.126, 0.4615,
			0.4439, 0.5104, 0.5741, 0.4593, 0.6447, 0.6498, 0.8685, 0.5063, 0.971, 0.1697, 0.27, 0.1261, 0.9438, 0.7346,
			1, 0.3994, 0.1576, 0.2355, 0.7718, 0.9082, 0.8524, 0.2161, 0.4916, 0.2975, 0.8397, 0.7533, 0.7199, 0.885,
			0.727, 0.1057, 0, 0, 0.8284, 0.7116, 0.4866, 0 };
	public static double[] BLOCK_VAL_KAT_2 = new double[] { 0, 0.5443, 0.4624, 0.5383, 0.6492, 0, 0, 0.5012, 0.4789,
			0.4938, 0.6435, 0, 0, 0.5225, 0.5076, 0.4741, 0.5791, 0.5663, 0, 0.9995, 0.6703, 0.6761, 0.8228, 0.6815,
			0.871, 0.9514, 0.8679, 0.5161, 0.6802, 0.7584, 0.9344, 0.9618, 1, 0.3191, 0.2393, 0.2157, 0.815, 0.813,
			0.9247, 0.5042, 0.212, 0.1856, 0.769, 0.7131, 0.711, 0.2889, 0.1534, 0.118, 0.7809, 0.5221, 0.5176, 0.374,
			0.244, 0.1908, 0, 0, 0.4939, 0.4498, 0.5051, 0 };
	public static double[] BLOCK_VAL_KAT_3 = new double[] { 0, 0.5465, 0.4406, 0.4862, 0.4404, 0, 0, 0.4853, 0.3784,
			0.4794, 0.5802, 0, 0, 0.567, 0.4035, 0.4414, 0.5272, 0.6176, 0, 1, 0.5381, 0.382, 0.6403, 0.6813, 0.8465,
			0.9411, 0.8096, 0.5441, 0.7883, 0.7735, 0.9179, 0.8931, 0.9637, 0.3427, 0.3232, 0.3117, 0.9205, 0.8392,
			0.8785, 0.4564, 0.3102, 0.305, 0.8802, 0.7424, 0.6202, 0.322, 0.2576, 0.2078, 0.9401, 0.5889, 0.5387,
			0.4164, 0.2382, 0.1777, 0, 0, 0.5698, 0.4275, 0.4118, 0 };
	public static double[] BLOCK_VAL_KAT_4 = new double[] { 0, 0.4315, 0.3406, 0.2776, 0.5347, 0, 0, 0.5089, 0.3525,
			0.259, 0.3855, 0, 0, 0.6569, 0.4462, 0.3708, 0.4415, 0.7523, 0, 0.8659, 0.5877, 0.4377, 0.6658, 0.8918,
			0.8696, 0.8685, 0.9511, 0.8375, 0.7861, 0.8468, 0.9827, 0.9134, 0.932, 0.6272, 0.6809, 0.5617, 0.9968, 1,
			0.9454, 0.8803, 0.6762, 0.4845, 0.9085, 0.9043, 0.8077, 0.6977, 0.5416, 0.4581, 0.8676, 0.7344, 0.7909,
			0.7304, 0.64, 0.6211, 0, 0, 0.8921, 0.8097, 0.8367, 0 };
	public static int[] BLOCK_CENTRES_X = new int[] { 50, 150, 250, 350, 450, 550, 50, 150, 250, 350, 450, 550, 50, 150,
			250, 350, 450, 550, 50, 150, 250, 350, 450, 550, 50, 150, 250, 350, 450, 550, 50, 150, 250, 350, 450, 550,
			50, 150, 250, 350, 450, 550, 50, 150, 250, 350, 450, 550, 50, 150, 250, 350, 450, 550, 50, 150, 250, 350,
			450, 550 };
	public static int[] BLOCK_CENTRES_Y = new int[] { 950, 950, 950, 950, 950, 950, 850, 850, 850, 850, 850, 850, 750,
			750, 750, 750, 750, 750, 650, 650, 650, 650, 650, 650, 550, 550, 550, 550, 550, 550, 450, 450, 450, 450,
			450, 450, 350, 350, 350, 350, 350, 350, 250, 250, 250, 250, 250, 250, 150, 150, 150, 150, 150, 150, 50, 50,
			50, 50, 50, 50 };

	private static int[][] NAV_BLOCKS;
	static {
		try (InputStream in = InnerDanishWatersDispersal.class.getResourceAsStream("InnerDanishWatersDispersalNavigationBlocks.asc")) {
			double[][] navBlocksDouble = ASCUtil.loadDoubleAscFile(600, 1000, in, false);
			int[][] navBlocks = new int[navBlocksDouble.length][navBlocksDouble[0].length];

			for (int i = 0; i < navBlocks.length; i++) {
				for (int j = 0; j < navBlocks[0].length; j++) {
					navBlocks[i][j] = (int) navBlocksDouble[i][j];
				}
			}
			InnerDanishWatersDispersal.NAV_BLOCKS = navBlocks;
		} catch (IOException e) {
			throw new RuntimeException("Unable to load navigation blocks", e);
		}
	}

	/**
	 * Number of potential dispersal targets = 40x40 km blocks (calibrated visually). In parameters.xml: q
	 */
	private static int N_DISP_TARGET = 12; // This used to be parameter q but this has been removed!

	private final Porpoise owner;
	private byte dispType; // Disperse away from low-energy area. 0 if not dispersing, 1 if dispersing far, 2 if...
	private NdPoint dispTarget; // List with x and y coord of the patch that the porp attempts to disperse to (not UTM)

	public InnerDanishWatersDispersal(final Porpoise owner) {
		this.owner = owner;

		if (!isLandscapeHomogenous() && !isLandscapeKattegat()) {
			throw new RuntimeException(
					"This dispersal behaviour only supports the Kattegat and Homogenous landscapes!");
		}
	}

	@Override
	public boolean isDispersing() {
		return dispType != 0;
	}

	@Override
	public int getDispersalType() {
		return dispType;
	}

	@Override
	public String getDispersalShortName() {
		return DISPERSAL_SHORT_NAME;
	}

	@Override
	public void activate() {
		this.dispType = 1;
		dispTargetSelect();
	}

	@Override
	public void deactivate() {
		this.dispType = 0;
		this.dispTarget = new NdPoint(0, 0);
	}

	@Override
	public void disperse() {
		if (!isDispersing()) {
			return;
		}

		if (this.dispType == 1) {
			disp1();
		}

		// Must be if, 'else if' will not produce correct result as disp1 can change dispType
		if (this.dispType == 2) {
			disp2();
		}
	}

	@Override
	public NdPoint getTargetPosition() {
		return dispTarget;
	}

	@Override
	public double getTargetHeading() {
		return -1;
	}

	@Override
	public double getDistanceLeftToTravel() {
		return -1;
	}

	@Override
	public boolean calfHasPSM() {
		return false;
	}

	@Override
	public boolean calfInheritsPsmDist() {
		return false;
	}

	/**
	 * 
	 * @return true if the execution should stop after this step.
	 */
	private boolean disp1() {
		ReplayHelper.print("disp1");

		// When the porp get closer to land than min-dist-to-land it tries to turn away, else it shifts to disp-2
		double minDistToland = 2000;

		NdPoint pos = owner.getPosition();
		// If porp is SE of Sealand, don't do directed dispersal
		if (pos.getX() > 438 && pos.getY() < 478) {
			this.dispType = 2;
			return true;
		}

		int currentBlock = getNavigationBlock(Agent.ndPointToGridPoint(owner.getPosition()));
		// If porp is N of Djursland or in Little Belt, don't do directed dispersal
		if (currentBlock == 14 || currentBlock == 31) {
			this.dispType = 2;
			return true;
		}

		// Go north if north of Fyn / Funen
		if (currentBlock == 32) {
			owner.setHeading(0);
			this.dispType = 2;
			owner.forward(1);
			return true;
		}

		// Find distance to target block centre
		double theDx = this.dispTarget.getX() - pos.getX();
		double theDy = this.dispTarget.getY() - pos.getY();
		double theDist = Math.round(Math.sqrt(theDx * theDx + theDy * theDy));

		owner.facePoint(new NdPoint(pos.getX() + theDx / 2, pos.getY() + theDy / 2));

		// @formatter:off
		// adjust angle to swim towards deep areas
		double[] bathymetryAhead = new double[] { -999, -999,
				Globals.getCellData().getDepth(owner.getPointAtHeadingAndDist(owner.getHeading() - 20, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDepth(owner.getPointAtHeadingAndDist(owner.getHeading() - 10, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDepth(owner.getPointAtHeadingAndDist(owner.getHeading(), SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDepth(owner.getPointAtHeadingAndDist(owner.getHeading() + 10, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDepth(owner.getPointAtHeadingAndDist(owner.getHeading() + 20, SimulationParameters.getMeanDispDist())),
				-999, -999 };

		double[] bathymetryFarAhead = new double[] { -999,
				Globals.getCellData().getDepth(owner.getPointAtHeadingAndDist(owner.getHeading() - 30, SimulationParameters.getMeanDispDist() * 8)),
				Globals.getCellData().getDepth(owner.getPointAtHeadingAndDist(owner.getHeading() - 20, SimulationParameters.getMeanDispDist() * 8)),
				Globals.getCellData().getDepth(owner.getPointAtHeadingAndDist(owner.getHeading() - 10, SimulationParameters.getMeanDispDist() * 8)),
				Globals.getCellData().getDepth(owner.getPointAtHeadingAndDist(owner.getHeading(), SimulationParameters.getMeanDispDist() * 8)),
				Globals.getCellData().getDepth(owner.getPointAtHeadingAndDist(owner.getHeading() + 10, SimulationParameters.getMeanDispDist() * 8)),
				Globals.getCellData().getDepth(owner.getPointAtHeadingAndDist(owner.getHeading() + 20, SimulationParameters.getMeanDispDist() * 8)),
				Globals.getCellData().getDepth(owner.getPointAtHeadingAndDist(owner.getHeading() + 30, SimulationParameters.getMeanDispDist() * 8)),
				-999 };
		// @formatter:on

		// Turn up to 20 degr towards deepest water, provided that there is no land
		// further away in that direction
		boolean[] goodHeading = new boolean[] { false, false, false, false, false, false, false, false, false };
		for (int i = 0; i < goodHeading.length; i++) {
			goodHeading[i] = (bathymetryFarAhead[i] > 0);
		}

		int[] angles = new int[] { -40, -30, -20, -10, 0, 10, 20, 30, 40 };

		double bathymetryChoice = -999;
		double selAngle = 0;

		for (int i = 0; i < bathymetryAhead.length; i++) {
			bathymetryChoice = Math.max(bathymetryAhead[i], bathymetryChoice);
		}

		for (int i = 0; i < bathymetryAhead.length; i++) {
			if (bathymetryAhead[i] == bathymetryChoice) {
				selAngle = angles[i];
			}
		}

		owner.incHeading(selAngle);

		// @formatter:off
		// Turn to areas far from land if there is land ahead
		double[] distToCoastAhead = new double[] { -999,
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() - 30, SimulationParameters.getMeanDispDist() * 2)),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() - 20, SimulationParameters.getMeanDispDist() * 2)),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() - 10, SimulationParameters.getMeanDispDist() * 2)),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading(), SimulationParameters.getMeanDispDist() * 2)),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() + 10, SimulationParameters.getMeanDispDist() * 2)),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() + 20, SimulationParameters.getMeanDispDist() * 2)),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() + 30, SimulationParameters.getMeanDispDist() * 2)),
				-999 };
		// @formatter:on

		// make sure that there is also water far away
		for (int i = 0; i < distToCoastAhead.length; i++) {
			if (!goodHeading[i]) {
				distToCoastAhead[i] = -999;
			}
		}

		double disttocoastChoice = -999;

		boolean lowWaterAhead = !(bathymetryAhead[1] > SimulationParameters.getMinDispDepth()
				&& bathymetryAhead[2] > SimulationParameters.getMinDispDepth()
				&& bathymetryAhead[3] > SimulationParameters.getMinDispDepth()
				&& bathymetryAhead[4] > SimulationParameters.getMinDispDepth()
				&& bathymetryAhead[5] > SimulationParameters.getMinDispDepth());

		if (lowWaterAhead || Globals.getCellData().getDistanceToCoast(owner.getPosition()) < minDistToland) {
			for (int i = 0; i < distToCoastAhead.length; i++) {
				disttocoastChoice = Math.max(disttocoastChoice, distToCoastAhead[i]);
			}

			for (int i = 0; i < distToCoastAhead.length; i++) {
				if (distToCoastAhead[i] == disttocoastChoice) {
					selAngle = angles[i];
				}
			}
		}

		owner.incHeading(selAngle);

		// ### when to stop dispersing: ###

		// shift to dispersal away from here, along coast, if porp cannot move for one day
		if (owner.distanceXY(owner.getPosListDaily().get(1)) < 2) {
			this.dispType = 2;
			if (DebugLog.isEnabledFor(7) && (owner.getId() == 0 || owner.getId() == 1)) {
				DebugLog.print("Not mov 1 d, chg to disp-type 2 (porp " + owner.getId() + ")");
			}
		}

		// shift to dispersal away from here, along coast, if porp moves too little
		if (owner.distanceXY(owner.getPosListDaily().get(8)) < 6) {
			this.dispType = 2;
			if (DebugLog.isEnabledFor(7) && (owner.getId() == 0 || owner.getId() == 1)) {
				DebugLog.print("Mov <15 km in 8d, chg to disp-type 2 (porp 1)");
			}
		}

		// Close to coast, chg. disp. mode
		if (Globals.getCellData().getDistanceToCoast(owner.getPosition()) < minDistToland) {
			this.dispType = 2;
			if (DebugLog.isEnabledFor(7) && (owner.getId() == 0 || owner.getId() == 1)) {
				DebugLog.print(owner.getId() + " TOO CLOSE to land, chg to disp-type 2");
			}
		}

		// Close to target, chg. disp. mode
		if (theDist < 50) {
			this.dispType = 2;
			if (DebugLog.isEnabledFor(7) && (owner.getId() == 0 || owner.getId() == 1)) {
				DebugLog.print(owner.getId() + " Close to target, chg to disp-type 2");
			}
		}

		if (!(Globals.getCellData().getDepth(owner.getPointAhead(SimulationParameters.getMeanDispDist() * 4)) > 0
				&& Globals.getCellData().getDepth(owner.getPointAhead(SimulationParameters.getMeanDispDist() * 3)) > 0
				&& Globals.getCellData()
						.getDepth(owner.getPointAhead(SimulationParameters.getMeanDispDist() * 2)) > 0)) {
			this.dispType = 2;
			if (DebugLog.isEnabledFor(7) && (owner.getId() == 0 || owner.getId() == 1)) {
				DebugLog.print(owner.getId() + " LAND ahead, chg to disp-type 2 (porp 1)");
			}
		}

		if (!owner.isEnoughWaterAhead()) {
			this.dispType = 0;

			if (DebugLog.isEnabledFor(7) && (owner.getId() == 0 || owner.getId() == 1)) {
				DebugLog.print("NO WATER, stop dispersing (1) (porp " + owner.getId() + ")");
			}
		}

		if (this.dispType == 1) {
			owner.forward(SimulationParameters.getMeanDispDist() / 0.4);

			if (!(Globals.getCellData().getDepth(owner.getPosition()) >= 0)) {
				owner.forward(-SimulationParameters.getMeanDispDist() / 0.4);
				this.dispType = 0;
			}

			double consumed = 0.001 * SimulationConstants.E_USE_PER_KM * SimulationParameters.getMeanDispDist() / 0.4;
			owner.consumeEnergy(consumed);
		}

		return false;
	}

	/*
	 * disperse along coast, away from prev position ; Disperse at least this dist from coast (same
	 */
	private void disp2() {
		ReplayHelper.print("disp2");

		// Turn away from place visited 1 day ago
		ReplayHelper.print("disp2-heading-before: " + owner.getHeading());
		owner.facePoint(owner.getPosListDaily().get(1));

		owner.incHeading(180);
		ReplayHelper.print(
				"disp2-heading-before after rot 180: " + owner.getHeading() + " mean-disp-dist " + "?mean-disp-dist?");

		// @formatter:off
		// adjust angle to swim at const dist from land
		double[] disttocoastAhead = new double[] {
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() - 80, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() - 70, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() - 60, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() - 50, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() - 40, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() - 30, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() - 20, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() - 10, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading(), SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() + 10, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() + 20, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() + 30, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() + 40, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() + 50, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() + 60, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() + 70, SimulationParameters.getMeanDispDist())),
				Globals.getCellData().getDistanceToCoast(owner.getPointAtHeadingAndDist(owner.getHeading() + 80, SimulationParameters.getMeanDispDist())) };
		// @formatter:on
		ReplayHelper.print("disp2-heading-before disttocoast-ahead:" + java.util.Arrays.toString(disttocoastAhead));

		// Stay on current dist from land if 1-4 km from land, or try to get there
		double dtcDiff = 9999999;
		double dtcMax = -9999;
		double dtcMin = 9999;
		int dtcNbrAlike = 9;
		int dtcNbrMax = 9;
		int dtcNbrMin = 9;
		double tmp = 9999;
		double dtcHere = Globals.getCellData().getDistanceToCoast(owner.getPosition());

		for (int i = 0; i < disttocoastAhead.length; i++) {
			tmp = Math.abs(disttocoastAhead[i] - dtcHere);

			if (tmp <= dtcDiff) {
				dtcDiff = tmp;
				dtcNbrAlike = i;
			}

			if (tmp <= dtcMin) {
				dtcMin = tmp;
				dtcNbrMin = i;
			}

			if (tmp > dtcMax) {
				dtcMax = tmp;
				dtcNbrMax = i;
			}
		}

		int[] angles = new int[] { -80, -70, -60, -50, -40, -30, -20, -10, 0, 10, 20, 30, 40, 50, 60, 70, 80 };
		int selAngle = -9999;

		if (Globals.getCellData().getDistanceToCoast(owner.getPosition()) > 4000) {
			selAngle = angles[dtcNbrMin];
		}
		if (Globals.getCellData().getDistanceToCoast(owner.getPosition()) < 1000) {
			selAngle = angles[dtcNbrMax];
		}
		if (Globals.getCellData().getDistanceToCoast(owner.getPosition()) <= 4000
				&& Globals.getCellData().getDistanceToCoast(owner.getPosition()) >= 2000) {
			selAngle = angles[dtcNbrAlike];
		}
		ReplayHelper.print("disp2-heading-before dtc-max:" + dtcMax + " dtc-min " + dtcMin + " dtc-nbr-alike "
				+ dtcNbrAlike + " dtc-nbr-max " + dtcNbrMax + " dtc-nbr-min " + dtcNbrMin + " dist to coast "
				+ Globals.getCellData().getDistanceToCoast(owner.getPosition()) + " sel-angle " + selAngle + " heading "
				+ owner.getHeading());
		owner.incHeading(selAngle);
		ReplayHelper.print("disp2-heading-before new head:" + owner.getHeading());

		// ### when to stop dispersing: ###

		if (!owner.isEnoughWaterAhead()) {
			this.dispType = 0;

			if (DebugLog.isEnabledFor(7) && (owner.getId() == 0 || owner.getId() == 1)) {
				DebugLog.print("NO WATER, stop dispersing (2 - not enough water ahead) (porp " + owner.getId() + ")");
			}
		}

		if (!(Globals.getCellData().getDepth(owner
				.getPointAhead(SimulationParameters.getMeanDispDist() / 0.4)) > SimulationParameters.getMinDepth())) {
			this.dispType = 0;

			if (DebugLog.isEnabledFor(7) && (owner.getId() == 0 || owner.getId() == 1)) {
				DebugLog.print(owner.getId() + " LOW water, stop dispersing (2)");
			}
		}

		if (this.dispType == 2) {
			owner.forward(SimulationParameters.getMeanDispDist() / 0.4);

			double consumed = 0.001 * SimulationConstants.E_USE_PER_KM * SimulationParameters.getMeanDispDist() / 0.4;
			ReplayHelper.print("energy before consume food disp2 " + owner.getEnergyLevel() + " consumed  " + consumed);
			owner.consumeEnergy(consumed);
		}
	}

	private void dispTargetSelect() {
		// deciding where to disperse to based on knowledge of other blocks (each block
		// is 100 x 100 cells = 40 000 x 40 000 m)
		int nbr = 0;
		double[] blockQuality = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0 };

		double[] blockValues = getBlockValues();

		while (nbr < blockQuality.length) {
			NdPoint pos = owner.getPosition();
			double oneDX = Math.abs(pos.getX() - BLOCK_CENTRES_X[nbr]);
			double oneDY = Math.abs(pos.getY() - BLOCK_CENTRES_Y[nbr]);

			double blockDist = Math.round(Math.sqrt(oneDX * oneDX + oneDY * oneDY));

			blockQuality[nbr] = blockValues[nbr];

			// set quality to 0 for blocks that are too close or too far (divide by 0.4 to
			// convert from km to cells)
			if (blockDist < MIN_DIST_TO_TARGET / 0.4) {
				blockQuality[nbr] = 0;
			}
			nbr++;
		}

		if (DebugLog.isEnabledFor(7) && (owner.getId() == 0 || owner.getId() == 1)) {
			DebugLog.print(Arrays.toString(blockQuality));
		}

		// numbers of the blocks with highest quality (blocks numbered 0-59, best block
		// first)
		int[] highQualityBlocks = new int[N_DISP_TARGET];

		double[] blockQualitySorted = java.util.Arrays.copyOf(blockQuality, blockQuality.length);
		java.util.Arrays.sort(blockQualitySorted);

		for (int i = 0; i < highQualityBlocks.length; i++) {
			double hiQualI = blockQualitySorted[blockQualitySorted.length - i - 1];

			for (int j = 0; j < blockValues.length; j++) {
				if (blockQuality[j] == hiQualI) {
					highQualityBlocks[i] = j;
				}
			}
		}

		// select block at random from the twelve blocks with highest quality (where
		// qual = mean.food / dist)
		// Make sure that porps far north do not try to disperse west
		int theNbr = Globals.getRandomSource().nextDispTargetSelect(0, highQualityBlocks.length);
		int selBlock = highQualityBlocks[theNbr];

		// find block slightly more to the east if currently far north
		if (getNavigationBlock(Agent.ndPointToGridPoint(owner.getPosition())) < 20
				&& (selBlock == 30 || selBlock == 31 || selBlock == 36 || selBlock == 37 || selBlock == 43)) {
			selBlock += 2;
		}

		// coordinates of cell to move towards
		this.dispTarget = new NdPoint(BLOCK_CENTRES_X[selBlock], BLOCK_CENTRES_Y[selBlock]);

		if (DebugLog.isEnabledFor(7) && (owner.getId() == 0 || owner.getId() == 1)) {
			DebugLog.print("");
			DebugLog.print(owner.getId() + "Disp from: "
					+ getNavigationBlock(Agent.ndPointToGridPoint(owner.getPosition())) + " -> "
					+ dispTarget);
		}
	}

	private double[] getBlockValues() {
		if (isLandscapeHomogenous()) {
			return BLOCK_VAL_HOMO;
		} else {
			// The same values are returned for all quarters. The cause is that update-block-values is only called during setup.
			switch (SimulationTime.getQuarterOfYear()) {
			case 0:
				return BLOCK_VAL_KAT_1;
			case 1:
				return BLOCK_VAL_KAT_1;
			// return Globals.BLOCK_VAL_KAT_2;
			case 2:
				return BLOCK_VAL_KAT_1;
			// return Globals.BLOCK_VAL_KAT_3;
			case 3:
				return BLOCK_VAL_KAT_1;
			// return Globals.BLOCK_VAL_KAT_4;
			default:
				throw new RuntimeException("Unexpected quarter");
			}
		}
	}

	private boolean isLandscapeHomogenous() {
		return "Homogenous".equals(SimulationParameters.getLandscape());
	}

	private boolean isLandscapeKattegat() {
		return "Kattegat".equals(SimulationParameters.getLandscape());
	}

	private int getNavigationBlock(final GridPoint point) {
		return InnerDanishWatersDispersal.NAV_BLOCKS[point.getX()][point.getY()];
	}

}

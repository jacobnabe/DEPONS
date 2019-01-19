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

import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.SimulationConstants;
import dk.au.bios.porpoise.SimulationParameters;
import dk.au.bios.porpoise.util.DebugLog;
import dk.au.bios.porpoise.util.PSMVerificationLog;
import dk.au.bios.porpoise.util.ReplayHelper;

/**
 * Implementation of PSM-Type1 dispersal behaviour.
 *
 * NB! Not tested after refactoring!
 */
public class DispersalPSMType1 extends AbstractPSMDispersal implements Dispersal {

	private static final int DISPERSAL_ID = 3;
	private static final String DISPERSAL_SHORT_NAME = "PSM1";

	private final boolean active = false;

	public DispersalPSMType1(final Porpoise owner) {
		super(owner);
	}

	@Override
	public int getDispersalType() {
		if (active) {
			return DISPERSAL_ID;
		} else {
			return 0;
		}
	}


	@Override
	public String getDispersalShortName() {
		return DISPERSAL_SHORT_NAME;
	}

	@Override
	public void disperse() {
		if (!active) {
			return;
		}

		if (shouldStopDispersing()) {
			deactivate();
			return;
		}

		// int startUtmX = getUtmX(); // PSM Verification
		// int startUtmY = getUtmY(); // PSM Verification
		// double startHeading = getHeading(); // PSM Verification

		ReplayHelper.print("disp3-heading-before: {0}", getOwner().getHeading());

		getOwner().setHeading(getCalculatedHeading());
		ReplayHelper.print("disp3-heading-before after PSM: {0} mean-disp-dist ?mean-disp-dist?", getOwner()
				.getHeading());

		if (!getOwner().isEnoughWaterAhead()) {
			this.deactivate();

			if (DebugLog.isEnabledFor(7) && (getOwner().getId() == 0 || getOwner().getId() == 1)) {
				DebugLog.print("NO WATER, stop dispersing (3 - not enough water ahead) (porp {})", getOwner().getId());
			}
		}

		final double depthAhead = Globals.getCellData().getDepth(
				getOwner().getPointAhead(SimulationParameters.getMeanDispDist() / 0.4));
		if (!(depthAhead > SimulationParameters.getMinDispDepth())) {
			this.deactivate();

			if (DebugLog.isEnabledFor(7) && (getOwner().getId() == 0 || getOwner().getId() == 1)) {
				DebugLog.print("{} LOW water, stop dispersing (3)", getOwner().getId());
			}
		}

		if (getOwner().getDispersalMode() == 3) {
			final double percentageToTravel = calculateDistancePercentageToTravel();
			final double distanceToTravel = percentageToTravel * (SimulationParameters.getMeanDispDist() / 0.4);
			getOwner().forward(distanceToTravel);
			addDistanceTravelled(distanceToTravel);
			// The Porpoise tickMoveAdjustMultiplier should be subtracted by percentageToTravel. PSM-1 not currently in use.

			final double consumed = 0.001 * SimulationConstants.E_USE_PER_KM * distanceToTravel;
			ReplayHelper.print("energy before consume food disp3 {0} consumed  {1}", getOwner().getEnergyLevel(),
					consumed);
			getOwner().consumeEnergy(consumed);

			if (getOwner().isWritePsmSteps()) {
				PSMVerificationLog.print("PSM", getOwner(), distanceToTravel);
			}
		}
	}

	/**
	 * Calculates the percentage of the maximum dispersal distance to travel in this tick. This depends on the distance
	 * already travelled compared to the distance from the starting point (when dispersal was activated) to the PSM
	 * target cell. The travelled percentage is used as input to a logistic decrease function.
	 *
	 * This is used in PSM-Type1 dispersal.
	 *
	 * @return
	 */
	public double calculateDistancePercentageToTravel() {
		final double distPerc = getDistanceTravelled() / getTargetDistanceAtActivation();
		final double distLogX = (3 * distPerc) - 1.5;
		final double logDistPerc = 1 - (SimulationParameters.getPsmLogisticDecreaseFunction().calculate(distLogX));
		// param must be distance travelled

		return logDistPerc;
	}

	@Override
	public double calculateNewHeading() {
		return getOwner().getHeading();
	}

}

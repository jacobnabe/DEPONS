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
import dk.au.bios.porpoise.SimulationParameters;

public class DispersalPSMType2 extends AbstractPSMDispersal implements Dispersal {

	private static final int DISPERSAL_ID = 3;
	private static final String DISPERSAL_SHORT_NAME = "PSM2";

	public DispersalPSMType2(final Porpoise owner) {
		super(owner);
	}

	@Override
	public int getDispersalType() {
		if (isDispersing()) {
			return DISPERSAL_ID;
		} else {
			return 0;
		}
	}

	@Override
	public String getDispersalShortName() {
		return DISPERSAL_SHORT_NAME;
	}

	/**
	 * Calculates the angle to turn during dispersal. The angle is a random angle in the range +/- parameter
	 * psm-type2-turn-angle multiplied by the output of a logistic increase function. The input to the logistic increase
	 * function is the distance travelled from the starting location (when dispersal was activated) to the PSM target
	 * cell.
	 *
	 * This is used in PSM-Type2 dispersal.
	 *
	 * @return
	 */
	@Override
	public double calculateNewHeading() {
		if (!isDispersing()) {
			return getOwner().getHeading();
		}

		double angleDelta = ((SimulationParameters.getPsmType2RandomAngle() * 2) * Globals.getRandomSource()
				.nextDouble()) - (SimulationParameters.getPsmType2RandomAngle());

		final double distPerc = getDistanceTravelled() / getTargetDistanceAtActivation();
		final double distLogX = (3 * distPerc) - 1.5;
		// param must be distance travelled
		final double logDistPerc = SimulationParameters.getPsmLogisticDecreaseFunction().calculate(distLogX);

		angleDelta = angleDelta * logDistPerc;

		final double newHeading = getPreviousStepHeading() + angleDelta;
		setPreviousStepHeading(newHeading);

		return newHeading;
	}

	@Override
	protected double calculateTargetDistanceAtActivation() {
		return super.calculateTargetDistanceAtActivation() * 0.95;  // For PSM-Type2 stop when 95% of the distance
	}

	@Override
	public boolean calfHasPSM() {
		return true;
	}

}

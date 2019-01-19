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

import java.util.Map.Entry;

import repast.simphony.space.continuous.NdPoint;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.SimulationParameters;
import dk.au.bios.porpoise.behavior.PersistentSpatialMemory.MemCellData;

/**
 * Implementation of the PSM-Type3 dispersal.
 */
public class DispersalPSMType3 extends AbstractPSMDispersal implements Dispersal {

	private static final int DISPERSAL_ID = 4;
	private static final String DISPERSAL_SHORT_NAME = "PSM3";

	public DispersalPSMType3(final Porpoise owner) {
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

	@Override
	public double calculateNewHeading() {
		if (!isDispersing()) {
			return getOwner().getHeading();
		}

		final double x0 = getTargetDistanceAtActivation() / 2;
		final double z = 0 - SimulationParameters.getPsmLog() * (getDistanceTravelled() - x0);
		double angleDelta = SimulationParameters.getPsmType2RandomAngle() / (1 + Math.exp(z));
		angleDelta = Globals.getRandomSource().randomPlusMinusOne() * angleDelta;
		
		final double newHeading = getOwner().getHeading() + angleDelta;

		return newHeading;
	}

	@Override
	public boolean shouldStopDispersing() {
		final double distanceToStart = this.getOwner().distanceXY(getStartPosition());
		return distanceToStart >= getTargetDistanceAtActivation();
	}

	@Override
	protected int findMostAttractiveMemCell() {
		// Ensure we have a reasonable memory
		if (getOwner().getPersistentSpatialMemory().getMemCellData().size() < 50) {
			return -1;
		}

		double targetCellFitness = -1;
		int targetCell = -1;

		for (final Entry<Integer, MemCellData> entry : getOwner().getPersistentSpatialMemory().getMemCellData()
				.entrySet()) {
			final NdPoint center = getOwner().getPersistentSpatialMemory().calcMemCellCenterPoint(entry.getKey());

			// Expected energy in cell - U[c]
			final double energyExpectation = entry.getValue().getEnergyExpectation();

			final double distanceToCellNeg = 0 - this.getOwner().distanceXY(center);
			final double cost = energyExpectation * (1 - Math.exp(distanceToCellNeg * SimulationParameters.getQ1()));
			final double cellFitness = energyExpectation - cost;

			if (cellFitness > targetCellFitness) {
				targetCell = entry.getKey();
				targetCellFitness = cellFitness;
			}
		}

		return targetCell;
	}

	@Override
	public boolean calfHasPSM() {
		return true;
	}

}

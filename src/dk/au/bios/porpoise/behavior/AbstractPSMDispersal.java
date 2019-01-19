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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.GridPoint;
import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.SimulationConstants;
import dk.au.bios.porpoise.SimulationParameters;
import dk.au.bios.porpoise.behavior.PersistentSpatialMemory.MemCellData;
import dk.au.bios.porpoise.util.DebugLog;
import dk.au.bios.porpoise.util.PSMVerificationLog;
import dk.au.bios.porpoise.util.ReplayHelper;

public abstract class AbstractPSMDispersal implements Dispersal {

	private final Porpoise owner;
	private boolean active = false;

	private double distanceTravelled;
	private double targetDistanceAtActivation;
	private double previousStepHeading;
	private int targetMemCell;
	private NdPoint targetPos;
	private NdPoint startPos;
	private double targetHeading;

	protected AbstractPSMDispersal(final Porpoise owner) {
		this.owner = owner;
	}

	public Porpoise getOwner() {
		return owner;
	}

	@Override
	public void activate() {
		if (this.active) {
			return; // Already active
		}

		DebugLog.print4("PSM - activating. {}", this);

		startPos = new NdPoint(owner.getPosition().getX(), owner.getPosition().getY());

		targetMemCell = findMostAttractiveMemCell();
		if (targetMemCell >= 0) {
			targetPos = getOwner().getPersistentSpatialMemory().calcMemCellCenterPoint(targetMemCell);
			final double targetHeadingRadian = Math.atan2(targetPos.getY() - startPos.getY(), targetPos.getX()
					- startPos.getX());
			targetHeading = 90.0d - Math.toDegrees(targetHeadingRadian);
			if (targetHeading < 0.0) {
				targetHeading += 360.0;
			}
		} else {
			int maxAttempts = 1000;
			while (maxAttempts > 0) {
				maxAttempts--;
				final double newTargetHeading = Globals.getRandomSource().nextDouble() * 360; // Not sure where to go
				final double targetHeadingRads = Math.toRadians(newTargetHeading);
				final double randomToleranceAdjustment = (Globals.getRandomSource().nextDouble() * (SimulationParameters
						.getPsmPreferredDistanceTolerance() * 2))
						- SimulationParameters.getPsmPreferredDistanceTolerance();
				final double preferredDistance = getOwner().getPersistentSpatialMemory().getPreferredDistance();
				final double distanceToTravel = (preferredDistance + randomToleranceAdjustment) / 0.4;
				final NdPoint newTargetPos = new NdPoint(startPos.getX()
						+ (distanceToTravel * Math.sin(targetHeadingRads)), startPos.getY()
						+ (distanceToTravel * Math.cos(targetHeadingRads)));
				final GridPoint targetGridPoint = Agent.ndPointToGridPoint(newTargetPos);

				if (Globals.getCellData().getDepth(targetGridPoint) > 0
						&& !Globals.getCellData().isPointMasked(targetGridPoint)) {
					if (Globals.getCellData().getMaxEnt()[targetGridPoint.getX()][targetGridPoint.getY()] > 0.0f) {
						this.targetHeading = newTargetHeading;
						this.targetPos = newTargetPos;
						break;
					}
				}
			}
			if (maxAttempts < 1) {
				// Failed to find a suitable target, deactivate PSM
				//				System.err.println("" + owner.getId() + "Failed to find a suitable random PSM cell! Current position: "
				//						+ owner.getPosition());
				this.active = false;
				return;
			}
		}
		this.targetDistanceAtActivation = calculateTargetDistanceAtActivation();
		// has been travelled
		this.distanceTravelled = 0;
		this.previousStepHeading = targetHeading;
		this.active = true;
	}

	protected double calculateTargetDistanceAtActivation() {
		final double startDistX = targetPos.getX() - startPos.getX();
		final double startDistY = targetPos.getY() - startPos.getY();
		return Math.hypot(startDistX, startDistY);
	}

	@Override
	public void deactivate() {
		this.targetMemCell = -1;
		this.targetPos = null;
		this.startPos = null;
		this.distanceTravelled = 0;
		this.targetHeading = 0;
		this.active = false;
	}

	@Override
	public boolean isDispersing() {
		return active;
	}

	@Override
	public void disperse() {
		if (!isDispersing()) {
			return;
		}

		if (shouldStopDispersing()) {
			deactivate();
			return;
		}

		ReplayHelper.print("disp3-heading-before: {0}", getOwner().getHeading());

		final double startHeading = getOwner().getHeading();
		final double newHeading = calculateNewHeading();
		getOwner().setHeading(newHeading);
		ReplayHelper.print("disp3-heading-before after PSM: {0} mean-disp-dist ?mean-disp-dist?", getOwner()
				.getHeading());

		// Stop dispersing if wanting to move to low water (based on Porpoise.enoughWaterAhead property.
		if (!getOwner().isEnoughWaterAhead()) {
			deactivate();
			getOwner().setHeading(startHeading);

			if (DebugLog.isEnabledFor(7) && (getOwner().getId() == 0 || getOwner().getId() == 1)) {
				DebugLog.print("NO WATER, stop dispersing (3 - not enough water ahead) (porp {})", getOwner().getId());
			}

			return;
		}

		// Stop dispersing if trying to leave landscape
		final NdPoint directPointAhead = getOwner().getPointAtHeadingAndDistNoBorder(getOwner().getHeading(),
				SimulationParameters.getMeanDispDist() / 0.4);
		if (directPointAhead.getX() < 0.0 || directPointAhead.getY() < 0.0
				|| directPointAhead.getX() >= (Globals.getWorldWidth() - 0.5)
				|| directPointAhead.getY() >= (Globals.getWorldHeight() - 0.5)) {
			deactivate();
			getOwner().setHeading(startHeading);

			if (DebugLog.isEnabledFor(7) && (getOwner().getId() == 0 || getOwner().getId() == 1)) {
				DebugLog.print("{} moving outside landscape, stop dispersing (3)", getOwner().getId());
			}

			return;
		}

		final NdPoint pointAhead = getOwner().getPointAhead(SimulationParameters.getMeanDispDist() / 0.4);

		// Stop dispersing if wanting to move to low water
		if (!(Globals.getCellData().getDepth(pointAhead) > SimulationParameters.getMinDispDepth())) {
			deactivate();
			getOwner().setHeading(startHeading);

			if (DebugLog.isEnabledFor(7) && (getOwner().getId() == 0 || getOwner().getId() == 1)) {
				DebugLog.print(getOwner().getId() + " LOW water, stop dispersing (3)");
			}

			return;
		}

		// If still dispersing, we make the dispersal move
		if (isDispersing()) {
			final double distanceToTravel = SimulationParameters.getMeanDispDist() / 0.4;
			getOwner().forward(distanceToTravel);
			addDistanceTravelled(distanceToTravel);

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
	 * Get the heading for the porpoise.
	 *
	 * @return The new heading.
	 */
	public abstract double calculateNewHeading();

	public double getDistanceTravelled() {
		return distanceTravelled;
	}

	public double getTargetDistanceAtActivation() {
		return targetDistanceAtActivation;
	}

	public void addDistanceTravelled(final double distance) {
		if (isDispersing()) {
			this.distanceTravelled += distance;
		}
	}

	public boolean shouldStopDispersing() {
		// System.err.printf("PSM startPos: {%.2f, %.2f}, targetPos: {%.2f, %.2f}%n", refMemStartPos.getX(),
		// refMemStartPos.getY(), refMemTargetPos.getX(), refMemTargetPos.getY());
		// System.err.printf("PSM(%s) distStart: %.2f, distanceTravelled: %.2f, still to go: %.2f%n",
		// Boolean.toString(active), distStart, distanceTravelled, distStart - distanceTravelled);
		return distanceTravelled >= targetDistanceAtActivation;
	}

	@Override
	public double getDistanceLeftToTravel() {
		return targetDistanceAtActivation - distanceTravelled;
	}

	public boolean isPointInTargetMemCell(final GridPoint point) {
		return getOwner().getPersistentSpatialMemory().isPointInMemCell(targetMemCell, point);
	}

	/**
	 * Returns the target position (most attractive cell) if the PSM is active. If the PSM is not active,
	 * <code>null</code> is returned.
	 *
	 * @return
	 */
	@Override
	public NdPoint getTargetPosition() {
		return targetPos;
	}

	/**
	 * Returns the position where the PSM was activated. If the PSM is not active, <code>null</code> is returned.
	 *
	 * @return
	 */
	public NdPoint getStartPosition() {
		return startPos;
	}

	@Override
	public double getTargetHeading() {
		return targetHeading;
	}

	public double getCalculatedHeading() {
		if (!isDispersing()) {
			return owner.getHeading();
		}

		return targetHeading;
	}

	protected int findMostAttractiveMemCell() {
		// Ensure we have a reasonable memory
		if (getOwner().getPersistentSpatialMemory().getMemCellData().size() < 50) {
			return -1;
		}

		// Find candidate PSM cells considered preferred travel distance
		final double preferredDistance = getOwner().getPersistentSpatialMemory().getPreferredDistance();
		final double preferredMinDist = (preferredDistance - SimulationParameters.getPsmPreferredDistanceTolerance()) / 0.4;
		final double preferredMaxDist = (preferredDistance + SimulationParameters.getPsmPreferredDistanceTolerance()) / 0.4;
		final Map<Integer, MemCellData> candidates = new HashMap<>();
		for (final Entry<Integer, MemCellData> entry : getOwner().getPersistentSpatialMemory().getMemCellData()
				.entrySet()) {
			final NdPoint center = getOwner().getPersistentSpatialMemory().calcMemCellCenterPoint(entry.getKey());

			final double distanceToCell = this.owner.distanceXY(center);
			if (distanceToCell >= preferredMinDist && distanceToCell <= preferredMaxDist) {
				candidates.put(entry.getKey(), entry.getValue());
			}
		}

		// Check the candidate cells for food over time
		int mostAttractiveCellNumber = -1;
		double mostAttractiveFitness = -1.0d;
		final Set<Entry<Integer, MemCellData>> data = candidates.entrySet();
		for (final Entry<Integer, MemCellData> entry : data) {
			// Cells fitness is determined by food eaten divided by time spent in cell.
			final double cellFitness = entry.getValue().getFoodObtained() / entry.getValue().getTicksSpent();
			if (cellFitness > mostAttractiveFitness) {
				mostAttractiveFitness = cellFitness;
				mostAttractiveCellNumber = entry.getKey();
			}
		}

		return mostAttractiveCellNumber;
	}

	@Override
	public boolean calfHasPSM() {
		return false;
	}

	@Override
	public boolean calfInheritsPsmDist() {
		return true;
	}

	public double getPreviousStepHeading() {
		return previousStepHeading;
	}

	protected void setPreviousStepHeading(final double previousStepHeading) {
		this.previousStepHeading = previousStepHeading;
	}

}

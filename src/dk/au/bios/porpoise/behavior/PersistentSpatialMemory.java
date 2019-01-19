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

/**
 * Implements a spatial memory. The memory cells are coarser grained than the general world grid. Each memory cell is 5
 * "world grid units" (2 km).
 */
public class PersistentSpatialMemory {

	private static final int MEM_CELL_SIZE = 5; // each grid cell is 400m, mem cells are 2 km.

	public static class MemCellData {
		private long ticksSpent = 0;
		private double foodObtained = 0.0d;

		public void updateFoodEaten(final double foodEaten) {
			this.foodObtained += foodEaten;
			this.ticksSpent++;
		}

		public long getTicksSpent() {
			return ticksSpent;
		}

		public double getFoodObtained() {
			return foodObtained;
		}

		public double getEnergyExpectation() {
			return foodObtained / ticksSpent;
		}

		@Override
		public String toString() {
			return "[ticksSpent=" + ticksSpent + ", foodObtained=" + foodObtained + "]";
		}

	}

	// Map of cell data keyed by cellNumber (id). Only holds data for actually visited cells.
	private final Map<Integer, MemCellData> memCellData = new HashMap<>();
	private final int cellsPerRow;
	private final double preferredDistance;

	public static double generatedPreferredDistance() {
		double prefDistance = Globals.getRandomSource().nextPSMDistanceStddev();
		prefDistance = Math.max(prefDistance, 1.0); // Just a fail-safe to have at least 1KM

		return prefDistance;
	}

	public PersistentSpatialMemory(final int worldWidth, final int worldHeight, final double preferredDistance) {
		cellsPerRow = worldWidth / MEM_CELL_SIZE;
		this.preferredDistance = preferredDistance;
	}

	/**
	 * Calculates the MemCell id for a map location.
	 *
	 * @param position The location to calculate for.
	 * @return The MemCell for the location.
	 */
	public int calculateMemCellNumber(final NdPoint position) {
		final GridPoint gridPoint = Agent.ndPointToGridPoint(position);
		final int x = gridPoint.getX();
		final int y = gridPoint.getY();

		final int cellX = (int) Math.floor(x / MEM_CELL_SIZE);
		final int cellY = (int) Math.floor(y / MEM_CELL_SIZE);

		final int cellNumber = (cellY * cellsPerRow) + cellX;

		return cellNumber;
	}

	/**
	 * Update the PSM. This is to be called at every tick.
	 *
	 * @param position The current position.
	 * @param foodEaten The amount of food eaten at this position.
	 */
	public void updateMemory(final NdPoint position, final double foodEaten) {
		if (foodEaten > 0.0f) {
			final int cellNumber = calculateMemCellNumber(position);
			MemCellData cell = memCellData.get(cellNumber);
			if (cell == null) {
				cell = new MemCellData();
				memCellData.put(cellNumber, cell);
			}

			cell.updateFoodEaten(foodEaten);
		}
	}

	public NdPoint calcMemCellCenterPoint(final int cellNumber) {
		final int cellsPerRow = Globals.getWorldWidth() / MEM_CELL_SIZE;

		final int cellX = (cellNumber % cellsPerRow) * MEM_CELL_SIZE;
		final int cellY = (cellNumber / cellsPerRow) * MEM_CELL_SIZE;

		return new NdPoint(cellX + (MEM_CELL_SIZE / 2), cellY + (MEM_CELL_SIZE / 2));
	}

	public boolean isPointInMemCell(final int memCell, final GridPoint point) {
		if (memCell < 0) {
			return false;
		}

		final int cellX = (memCell % cellsPerRow) * MEM_CELL_SIZE;
		final int cellY = (memCell / cellsPerRow) * MEM_CELL_SIZE;

		if (point.getX() >= cellX && point.getX() < (cellX + MEM_CELL_SIZE) && point.getY() >= cellY
				&& point.getY() < (cellY + MEM_CELL_SIZE)) {
			return true;
		}

		return false;
	}

	public double getPreferredDistance() {
		return preferredDistance;
	}

	public Map<Integer, MemCellData> getMemCellData() {
		return memCellData;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PersistentSpatialMemory: ");

		boolean first = true;
		final Set<Entry<Integer, MemCellData>> data = memCellData.entrySet();
		for (final Entry<Integer, MemCellData> entry : data) {
			if (!first) {
				sb.append(", ");
			}
			sb.append("{");
			sb.append(entry.getKey()).append(": ");
			sb.append(entry.getValue().getTicksSpent());
			sb.append(", ").append(entry.getValue().getFoodObtained());
			sb.append("}");
			first = false;
		}
		return sb.toString();
	}

}

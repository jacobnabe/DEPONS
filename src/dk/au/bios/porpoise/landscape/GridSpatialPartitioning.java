package dk.au.bios.porpoise.landscape;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.projection.ProjectionEvent;
import repast.simphony.space.projection.ProjectionEvent.Type;
import repast.simphony.space.projection.ProjectionListener;

public class GridSpatialPartitioning implements ProjectionListener<Agent> {

	private static class Partition {

		private final int x;
		private final int y;
		private Set<Porpoise> porpoises = new HashSet<>();

		Partition(int x, int y) {
			this.x = x;
			this.y = y;
		}

		void add(Porpoise p) {
			porpoises.add(p);
		}

		void remove(Porpoise p) {
			porpoises.remove(p);
		}

		Set<Porpoise> getPorpoises() {
			return porpoises;
		}

		@Override
		public String toString() {
			return "GSP_Partition[" + x + "," + y + "]{porpoiseCount: " + porpoises.size() + "}";
		}
	}

	private Partition[][] partitions;
	private Map<Long, Partition> porpToPartitionMap = new HashMap<>();

	/**
	 * Constructor.
	 *
	 * @param xCells The number of regular cells on the x-axis per cell
	 * @param yCells The number of regular cells on the y-axis per cell
	 */
	public GridSpatialPartitioning(int xCells, int yCells) {
		final int xDim = (int) Math.ceil((double) Globals.getWorldWidth() / xCells);
		final int yDim = (int) Math.ceil((double) Globals.getWorldHeight() / yCells);
		partitions = new Partition[yDim][];
		for (int y = 0; y < yDim; y++) {
			partitions[y] = new Partition[xDim];
			for (int x = 0; x < xDim; x++) {
				partitions[y][x] = new Partition(x, y);
			}
		}
	}

	public int getWidth() {
		return partitions[0].length;
	}

	public int getHeight() {
		return partitions.length;
	}

	public Set<Porpoise> getPorpoisesInPartition(NdPoint point) {
		return getPartitionForNdPoint(point).getPorpoises();
	}

	public Set<Porpoise> getPorpoisesInNeighborhood(NdPoint point) {
		Set<Porpoise> porps = new HashSet<>();

		int yIdx = calculateIdxY(point);
		int xIdx = calculateIdxX(point);
		for (int y = yIdx - 1; y <= yIdx + 1; y++) {
			if (y >= 0 && y < partitions.length) {
				for (int x = xIdx - 1; x <= xIdx + 1; x++) {
					if (x >= 0 && x < partitions[y].length) {
						porps.addAll(partitions[y][x].getPorpoises());
					}
				}
			}
		}

		return porps;
	}

	private Partition getPartitionForNdPoint(NdPoint point) {
		return partitions[calculateIdxY(point)][calculateIdxX(point)];
	}

	private int calculateIdxX(NdPoint point) {
		int cellWidth = (int) Math.ceil((double) Globals.getWorldWidth() / partitions[0].length);
		int xIdx = (int) Math.floor(point.getX() / cellWidth);
		if (xIdx < 0) {
			xIdx = 0;
		}

		return xIdx;
	}

	private int calculateIdxY(NdPoint point) {
		int cellHeight = (int) Math.ceil((double) Globals.getWorldHeight() / partitions.length);
		int yIdx = (int) Math.floor(point.getY() / cellHeight);
		if (yIdx < 0) {
			yIdx = 0;
		}

		return yIdx;
	}

	@Override
	public void projectionEventOccurred(ProjectionEvent<Agent> evt) {
		if (evt.getSubject() instanceof Porpoise) {
			var p = (Porpoise) evt.getSubject();
			var pId = Long.valueOf(p.getId());
			NdPoint pos = p.getPosition();

			if (evt.getType() == Type.OBJECT_MOVED) {
				Partition oldPartition = porpToPartitionMap.get(pId);
				Partition newPartition = getPartitionForNdPoint(pos);
				if (newPartition != oldPartition) {
					if (oldPartition != null) {
						oldPartition.remove(p);
					}
					newPartition.add(p);
					this.porpToPartitionMap.put(pId, newPartition);
				}
			} else if (evt.getType() == Type.OBJECT_ADDED) {
				Partition newPartition = getPartitionForNdPoint(pos);
				newPartition.add(p);
				this.porpToPartitionMap.put(pId, newPartition);
			} else if (evt.getType() == Type.OBJECT_REMOVED) {
				var oldPartition = this.porpToPartitionMap.get(pId);
				if (oldPartition != null) {
					oldPartition.remove(p);
				}
			}
		}
	}

}

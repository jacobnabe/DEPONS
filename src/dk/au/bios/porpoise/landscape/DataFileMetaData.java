package dk.au.bios.porpoise.landscape;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class DataFileMetaData {

	private final int ncols;
	private final int nrows;
	private final double xllcorner;
	private final double yllcorner;
	private final int cellsize;
	private final CoordinateReferenceSystem crs;

	public DataFileMetaData(final int ncols, final int nrows, final double xllcorner, final double yllcorner, final int cellsize, final CoordinateReferenceSystem crs) {
		super();
		this.ncols = ncols;
		this.nrows = nrows;
		this.xllcorner = xllcorner;
		this.yllcorner = yllcorner;
		this.cellsize = cellsize;
		this.crs = crs;
	}

	public int getNcols() {
		return ncols;
	}

	public int getNrows() {
		return nrows;
	}

	public double getXllcorner() {
		return xllcorner;
	}

	public double getYllcorner() {
		return yllcorner;
	}

	public int getCellsize() {
		return cellsize;
	}

	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return crs;
	}

}

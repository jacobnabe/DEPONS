package dk.au.bios.porpoise.util;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.resources.coverage.CoverageUtilities;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.landscape.DataFileMetaData;
import it.geosolutions.jaiext.range.NoDataContainer;

public class GeoTiffUtil {

	public static double[][] loadGeotif(final int width, final int height, final InputStream in,
			final boolean replaceNoDataWithNull) throws IOException {

		GeoTiffReader reader = new GeoTiffReader(in);
		GridCoverage2D coverage = (GridCoverage2D) reader.read(null);

		CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
		if (!Globals.getCoordinateReferenceSystem().equals(crs)) {
			throw new IOException("Coordinate Reference System mismatch. Required "
					+ Globals.getCoordinateReferenceSystem().getName().getCode() + " but found "
					+ crs.getName().getCode());
		}

		NoDataContainer ndc = CoverageUtilities.getNoDataProperty(coverage);
		
		System.err.printf("dims: %d%n", coverage.getNumSampleDimensions());
		
		double[] noDataValues = coverage.getSampleDimension(0).getNoDataValues();
		if (noDataValues.length != 1) {
			throw new RuntimeException("Invalid no-data-value in data file.");
		}
		double noDataValue = noDataValues[0];
		System.err.printf("nodata: %f%n", noDataValue);
		System.err.printf("ndc.value: %f%n", ndc.getAsSingleValue());
		
		RenderedImage image = coverage.getRenderedImage();
		Object prop = image.getProperty(NoDataContainer.GC_NODATA);
		System.err.println("ndc.class: " + prop.getClass());
//		NoDataContainer ndci = (NoDataContainer) prop;
//		System.err.println("ndc: " + ndci.getAsSingleValue());
		Raster raster = image.getData();

		double[] data = new double[width * height];
		raster.getSamples(raster.getMinX(), raster.getMinY(), raster.getWidth(), raster.getHeight(), 0, data);

		double[][] tifdata = new double[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				double val = data[(y * width) + x];
				if (ndc.getAsSingleValue() == val) {
//					System.err.println("Setting no-data value");
					val = -9999.0; // aligned with ASC datafiles
//				} else if (ndc.getAsRange().contains(val)) {
//					System.err.println("Setting no-data value");
//					val = -9999.0; // aligned with ASC datafiles
//				} else if (val < -330000000000000000000000000000000000000f) {
//					System.err.printf("suspecious value: %f%n", val);
				}
				tifdata[x][height - (y + 1)] = val;
			}
		}
		System.err.printf("double.min: %f%n", Double.MIN_VALUE);
		System.err.printf("double.max: %f%n", Double.MAX_VALUE);

		return tifdata;
	}

	public static DataFileMetaData loadMetaData(InputStream in) throws IOException {
		GeoTiffReader reader = new GeoTiffReader(in);
		GridCoverage2D coverage = (GridCoverage2D) reader.read(null);
		Raster raster = coverage.getRenderedImage().getData();
		Envelope envelope = coverage.getEnvelope();
		CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();

		int ncols = raster.getWidth();
		int nrows = raster.getHeight();
		double xllcorner = envelope.getMinimum(0);
		double yllcorner = envelope.getMinimum(1);

		double dWidth = envelope.getMaximum(0) - envelope.getMinimum(0);
		double dHeight = envelope.getMaximum(1) - envelope.getMinimum(1);
		double cellWidth = dWidth / ncols;
		double cellHeight = dHeight / nrows;

		if (cellWidth != cellHeight) {
			throw new RuntimeException("Grid cells not square.");
		}

		int cellsize = (int) Math.round(cellWidth);

		return new DataFileMetaData(ncols, nrows, xllcorner, yllcorner, cellsize, crs);

	}

}

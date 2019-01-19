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

package dk.au.bios.porpoise.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Utility class to load data froo ASCII (text) files.
 */
public final class ASCUtil {

	private ASCUtil() {
		// Utility class, prevent instances.
	}

	public static boolean[][] loadBooleanAscFile(final int width, final int height, final InputStream in)
			throws IOException {
		// File f = new File(file);
		// if (f.exists()) {
		final Boolean[][] data = new Boolean[width][height];
		loadData(data, in, new BooleanParser(), false);

		final boolean[][] boolVal = new boolean[width][height];
		for (int x = 0; x < data.length; x++) {
			for (int y = 0; y < data[x].length; y++) {
				boolVal[x][y] = data[x][y];
			}
		}

		return boolVal;
		// } else {
		// return null;
		// }
	}

	public static double[][] loadDoubleAscFile(final int width, final int height, final InputStream in,
			final boolean replaceNoDataWithNull) throws IOException {
		final Double[][] data = new Double[width][height];
		loadData(data, in, new DoubleParser(), replaceNoDataWithNull);

		final double[][] primitive = new double[width][height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				primitive[x][y] = data[x][y];
			}
		}

		return primitive;
	}

	private static <T> void loadData(final T[][] array, final InputStream in, final StringParser<T> parser,
			final boolean replaceNoDataWithNull) throws IOException {
		// FileReader freader = new FileReader(file);
		// BufferedReader reader = new BufferedReader(freader);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()));

		String noDataValue = null;

		for (int i = 0; i < 6; i++) {
			final String line = reader.readLine();

			if (line.startsWith("NODATA_value")) {
				noDataValue = line.substring("NODATA_value".length()).trim();
			}
		}

		int y = 0;
		String line;
		while ((line = reader.readLine()) != null) {
			final String[] points = line.split(" ");
			for (int x = 0; x < points.length; x++) {
				if (replaceNoDataWithNull && points[x].trim().equals(noDataValue)) {
					array[x][array[x].length - y - 1] = parser.getNullValue();
				} else {
					array[x][array[x].length - y - 1] = parser.parse(points[x]);
				}
			}
			y++;
		}

		reader.close();
		// freader.close();
	}

	private interface StringParser<T> {
		T parse(String s);

		T getNullValue();
	}

	private static class BooleanParser implements StringParser<Boolean> {

		@Override
		public Boolean parse(final String s) {
			return !s.trim().equals("1");
		}

		@Override
		public Boolean getNullValue() {
			return false;
		}

	}

	private static class DoubleParser implements StringParser<Double> {

		@Override
		public Double parse(final String s) {
			return Double.parseDouble(s);
		}

		@Override
		public Double getNullValue() {
			return Double.NaN;
		}

	}

}

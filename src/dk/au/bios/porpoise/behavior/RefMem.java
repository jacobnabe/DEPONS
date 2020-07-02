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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public final class RefMem {

	private RefMem() {
		// Utility class, prevent instances
	}

	// replaces ref-mem-strength-list, uses rR = 0.10
	private static double[] refMemStrengthListFixed = new double[] { 0.999, 0.9989, 0.9988, 0.9987, 0.9985, 0.9984,
		0.9982, 0.9981, 0.9979, 0.9976, 0.9974, 0.9972, 0.9969, 0.9966, 0.9962, 0.9958, 0.9954, 0.995, 0.9945,
		0.9939, 0.9933, 0.9926, 0.9919, 0.9911, 0.9902, 0.9893, 0.9882, 0.987, 0.9858, 0.9843, 0.9828, 0.9811,
		0.9793, 0.9772, 0.975, 0.9726, 0.9699, 0.967, 0.9638, 0.9603, 0.9565, 0.9523, 0.9478, 0.9428, 0.9375,
		0.9316, 0.9252, 0.9183, 0.9108, 0.9027, 0.8939, 0.8844, 0.8742, 0.8632, 0.8514, 0.8387, 0.8252, 0.8108,
		0.7954, 0.7792, 0.7619, 0.7438, 0.7248, 0.7048, 0.684, 0.6624, 0.64, 0.617, 0.5934, 0.5692, 0.5447, 0.5199,
		0.4949, 0.4699, 0.445, 0.4203, 0.396, 0.3721, 0.3487, 0.326, 0.304, 0.2828, 0.2626, 0.2432, 0.2248, 0.2074,
		0.1909, 0.1755, 0.161, 0.1475, 0.1349, 0.1233, 0.1125, 0.1025, 0.0933, 0.0848, 0.0771, 0.0699, 0.0634,
		0.0575, 0.0521, 0.0471, 0.0426, 0.0386, 0.0349, 0.0315, 0.0284, 0.0257, 0.0232, 0.0209, 0.0189, 0.017,
		0.0153, 0.0138, 0.0125, 0.0112, 0.0101, 0.0091, 0.0082, 0.0074 };

	// replaces work-mem-strength-list, uses rW = 0.20
	private static double[] workMemStrengthListFixed = new double[] { 0.9990, 0.9988, 0.9986, 0.9983, 0.9979,
		0.9975, 0.9970, 0.9964, 0.9957, 0.9949, 0.9938, 0.9926, 0.9911, 0.9894, 0.9873, 0.9848, 0.9818, 0.9782,
		0.9739, 0.9689, 0.9628, 0.9557, 0.9472, 0.9372, 0.9254, 0.9116, 0.8955, 0.8768, 0.8552, 0.8304, 0.8022,
		0.7705, 0.7351, 0.6962, 0.6539, 0.6086, 0.5610, 0.5117, 0.4617, 0.4120, 0.3636, 0.3173, 0.2740, 0.2342,
		0.1983, 0.1665, 0.1388, 0.1149, 0.0945, 0.0774, 0.0631, 0.0513, 0.0416, 0.0336, 0.0271, 0.0218, 0.0176,
		0.0141, 0.0113, 0.0091, 0.0073, 0.0058, 0.0047, 0.0037, 0.0030, 0.0024, 0.0019, 0.0015, 0.0012, 0.0010,
		0.0008, 0.0006, 0.0005, 0.0004, 0.0003, 0.0003, 0.0002, 0.0002, 0.0001, 0.0001, 0.0001, 0.0001, 0.0001,
		0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000,
		0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000,
		0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000 };

	/**
	 * Initialization of the REF_MEM_STRENGTH_LIST_FIXED (rR) and WORK_MEM_STRENGTH_LIST_FIXED (rS).
	 */
	public static void initMemLists(final double rS, final double rR) {
		printDoubleArray(refMemStrengthListFixed);
		refMemStrengthListFixed = calcArray(0.999, rR, refMemStrengthListFixed.length, 4);
		printDoubleArray(refMemStrengthListFixed);

		printDoubleArray(workMemStrengthListFixed);
		workMemStrengthListFixed = calcArray(0.999, rS, workMemStrengthListFixed.length, 4);
		printDoubleArray(workMemStrengthListFixed);
	}

	public static double getRefMemStrength(final int index) {
		return refMemStrengthListFixed[index];
	}

	public static double getWorkMemStrenth(final int index) {
		return workMemStrengthListFixed[index];
	}

	public static int getWorkMemSize() {
		return workMemStrengthListFixed.length;
	}

	public static String getWorkMemAsString() {
		return Arrays.toString(workMemStrengthListFixed);
	}

	private static void printDoubleArray(final double[] arr) {
		for (final double d : arr) {
			System.out.printf("%f ", d);
		}
		System.out.println();
	}

	private static double[] calcArray(final double firstValue, final double coff, final int size,
			final int roundingDecimals) {
		final double[] answer = new double[size];
		answer[0] = firstValue;
		for (int i = 1; i < size; i++) {
			final double prev = answer[i - 1];
			final double curr = prev - coff * prev * (1 - prev);
			answer[i] = curr;
		}
		if (roundingDecimals > 0) {
			roundArray(answer, roundingDecimals);
		}
		return answer;
	}

	private static void roundArray(final double[] arr, final int decimals) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new BigDecimal(arr[i]).setScale(4, RoundingMode.HALF_UP).doubleValue();
		}
	}

}

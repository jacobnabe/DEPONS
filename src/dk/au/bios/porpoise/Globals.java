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

package dk.au.bios.porpoise;

import java.util.LinkedList;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import dk.au.bios.porpoise.behavior.RandomSource;
import dk.au.bios.porpoise.landscape.CellData;
import dk.au.bios.porpoise.landscape.DataFileMetaData;
import dk.au.bios.porpoise.landscape.GridSpatialPartitioning;
import dk.au.bios.porpoise.util.SimulationTime;

/**
 * Placeholder for NETLOGO globals and various utility functionality.
 */
public final class Globals {

	private Globals() {
		// Utility class, prevent instances.
	}

	private static CellData cellData;

	// The source for random values - if not null, we are doing a replay scenario.
	// public static String RANDOM_REPLAY_SOURCE = "data/RandomReplay/test.txt";
	private static String randomReplaySource = null; // disable

	private static RandomSource randomSource = null; // Defines the source of random numbers. Either generated or
	// replayed.

	private static PorpoiseStatistics monthlyStats = null;

	// Age of death for all animals that die. Reset every year
	private static LinkedList<Integer> listOfDeadAge = new LinkedList<Integer>();

	// Day of death for all animals that die. Reset every year
	private static LinkedList<Integer> listOfDeadDay = new LinkedList<Integer>();

	// public static double[] MEAN_MAXENT_IN_QUATERS = {0.515686364223653, 0.888541219760357, 0.841346010536882, 1}; //
	// standardized average maxent level in each quarter
	private static double[] meanMaxEntInQuarters = { 1, 1, 1, 1 }; // standardized average maxent level in each quarter

	private static DataFileMetaData landscapeMetaData;

	private static Integer simYears = null; // Limit simulation to number of years.

	private static GridSpatialPartitioning spatialPartitioning;

	public static RandomSource getRandomSource() {
		return randomSource;
	}

	public static void setRandomSource(final RandomSource randomSource) {
		Globals.randomSource = randomSource;
	}

	public static String getRandomReplaySource() {
		return randomReplaySource;
	}

	public static PorpoiseStatistics getMonthlyStats() {
		return monthlyStats;
	}

	public static void resetMonthlyStats() {
		monthlyStats = new PorpoiseStatistics();
	}

	public static GridSpatialPartitioning getSpatialPartitioning() {
		return spatialPartitioning;
	}

	public static void setSpatialPartitioning(GridSpatialPartitioning gsp) {
		spatialPartitioning = gsp;
	}

	public static LinkedList<Integer> getListOfDeadAge() {
		return listOfDeadAge;
	}

	public static LinkedList<Integer> getListOfDeadDay() {
		return listOfDeadDay;
	}

	public static double getMeanMaxEntInCurrentQuarter() {
		return meanMaxEntInQuarters[SimulationTime.getQuarterOfYear()];
	}

	public static double getMeanMaxEntInQuarter(final int quarter) {
		return meanMaxEntInQuarters[quarter];
	}

	public static void setLandscapeMetadata(DataFileMetaData metadata) {
		Globals.landscapeMetaData = metadata;
	}

	public static double getXllCorner() {
		return Globals.landscapeMetaData.getXllcorner();
	}

	public static double getYllCorner() {
		return Globals.landscapeMetaData.getYllcorner();
	}

	public static int getWorldWidth() {
		return Globals.landscapeMetaData.getNcols();
	}

	public static int getWorldHeight() {
		return Globals.landscapeMetaData.getNrows();
	}

	public static CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return Globals.landscapeMetaData.getCoordinateReferenceSystem();
	}

	public static Integer getSimYears() {
		return simYears;
	}

	public static void setSimYears(final Integer simYears) {
		Globals.simYears = simYears;
	}

	public static CellData getCellData() {
		return cellData;
	}

	public static void setCellData(final CellData cellData) {
		Globals.cellData = cellData;
	}

}

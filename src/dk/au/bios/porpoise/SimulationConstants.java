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

public final class SimulationConstants {

	private SimulationConstants() {
		// Utility class, prevent instances.
	}

	// Whether or not to invent food if the porpoise eats all the food in a patch. NetLogo default behavior
	public static final boolean ADD_ARTIFICIAL_FOOD = true;

	// The NetLogo model shifted the quarters of the year 30 days, when this is enabled we do the same.
	public static final boolean SHIFT_QUARTER = true;

	// Netlogo shifts the month one tick "too" late, when this enabled then this model does the same.
	public static final boolean OFFSET_MONTH = true;

	// Maximum number of half-hour steps the amount of food can be remembered (120 steps is 2.5 days)
	public static final int MEMORY_MAX = 120;

	// TODO: make this user definable
	public static final double M_MORT_PROB_CONST = 1.0;

	// TODO: make this user definable
	public static final double E_USE_PER_KM = 0.0;

	// Affects whether porpoises die and reproduce. Hardcoded value - (Boolean)params.getValue("mortality");
	public static final boolean MORTALITY_ENABLED = true;

	/*
	 * If stuck and heavily deterred (more than ship noise, impact > c), moving less than a in the last b steps, while
	 * being deterred. Go back to random move for y steps, ignoring noise.
	 */
	public static final double IGNORE_DETER_MIN_IMPACT = 0.0; // (Double) params.getValue("EBimpact"); // = 10; // c
	public static final double IGNORE_DETER_MIN_DISTANCE = 0.0; // ((Double) params.getValue("EBmin")) / 400; // = 10;
	// // a
	public static final int IGNORE_DETER_STUCK_TIME = 0; // (Integer) params.getValue("EBstucktime"); // = 10; // b
	public static final int IGNORE_DETER_NUMBER_OF_STEPS_IGNORE = 0; // (Integer) params.getValue("EBsteps"); // = 10;
	// // y
}

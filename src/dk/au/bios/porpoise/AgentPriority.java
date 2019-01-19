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

/**
 * The priority of the different tasks in the model. The higher the number the earlier it is executed.
 *
 * This is using "public static final" fields rather than being an enum because it is being used with Repast
 * annotations.
 */
public final class AgentPriority {

	private AgentPriority() {
		// Utility class, prevent instances
	}

	public static final double PORP_DETERRENCE = 1100;
	public static final double PORP_MOVE = 1000;
	public static final double SHIP_MOVE = 800;

	// Looks like it has lower prio in Netlogo but it is in fact higher as it happens after time-step++ (mind blown!!)
	public static final double FOOD = 1700;

	public static final double FIRST_EVERY_TICK = 9999;
	public static final double YEARLY = 1600;
	public static final double DAILY = 1500;
	public static final double MONTHLY = 1400;

}

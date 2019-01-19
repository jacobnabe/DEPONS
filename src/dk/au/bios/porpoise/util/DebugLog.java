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

import java.util.function.Predicate;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import dk.au.bios.porpoise.Porpoise;

public final class DebugLog {

	private DebugLog() {
		// Utility class, prevent instances.
	}

	private static int debugLevel = 0;

	public static void initialize(final Parameters params) {
		debugLevel = (Integer) params.getValue("debug");
	}

	public static boolean isEnabledFor(final int level) {
		return level == debugLevel;
	}

	public static void print(final String msg, final Object... args) {
		printInternal(replacePlaceholders(msg, args));
	}

	public static void print1(final String msg, final Object... args) {
		if (DebugLog.debugLevel == 1) {
			printInternal(replacePlaceholders(msg, args));
		}
	}

	public static void print3(final String msg, final Object... args) {
		if (DebugLog.debugLevel == 3) {
			printInternal(replacePlaceholders(msg, args));
		}
	}

	public static void print4(final String msg, final Object... args) {
		if (DebugLog.debugLevel == 4) {
			printInternal(replacePlaceholders(msg, args));
		}
	}

	public static void print5(final String msg, final Object... args) {
		if (DebugLog.debugLevel == 5) {
			printInternal(replacePlaceholders(msg, args));
		}
	}

	public static void print8(final String msg, final Object... args) {
		if (DebugLog.debugLevel == 8) {
			printInternal(replacePlaceholders(msg, args));
		}
	}

	public static void print9(final String msg, final Object... args) {
		if (DebugLog.debugLevel == 9) {
			printInternal(replacePlaceholders(msg, args));
		}
	}

	public static void print10(final Porpoise porp, final String msg, final Object... args) {
		print10(porp, null, msg, args);
	}

	public static void print10(final Porpoise porp, final Predicate<Porpoise> predicate, final String msg,
			final Object... args) {
		if (DebugLog.debugLevel == 10 && (predicate == null || predicate.test(porp))) {
			printInternal(replacePlaceholders(msg, args));
		}
	}

	/**
	 * Used for writing debug statements. Refactor!
	 *
	 * @param s
	 */
	private static void printInternal(final String msg) {
		final long tick = (long) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		System.out.println("" + tick + " - " + msg);
	}

	private static String replacePlaceholders(final String msg, final Object... args) {
		int i = 0;
		String messageReplaced = msg;
		do {
			final int idx = messageReplaced.indexOf("{}");
			if (idx < 0) {
				break;
			}

			messageReplaced = messageReplaced.replaceFirst("\\{\\}", args[i].toString());
			i++;
		} while (true);

		return messageReplaced;
	}

}

/*
 * Copyright (C) 2017-2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import dk.au.bios.porpoise.util.DebugLog;
import dk.au.bios.porpoise.util.SimulationTime;
import repast.simphony.context.Context;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.SpatialException;
import repast.simphony.space.continuous.NdPoint;

/**
 * A turbine in a wind farm.
 *
 * Loaded from a data file in the "data/wind-farms" sub-folder.
 */
public class Turbine extends Agent {

	/**
	 * Hold a sorted list of turbines to create. If this is not null, the turbines
	 * will be dynamically added and removed from the context based on their start
	 * and end tick.
	 */
	private static LinkedList<Turbine> turbineCreateQueue;

	private final String name;

	/** Deterrence effect relative to standard Roedsand-turbine */
	private final double impact;

	private final double locX;
	private final double locY;

	private final int startTick;
	private final int endTick;

	protected Turbine(final String name, final double impact, final double locX, final double locY, final int startTick,
			final int endTick, final int id) {
		super(id);
		this.name = name;
		this.impact = impact;
		this.locX = locX;
		this.locY = locY;
		this.startTick = startTick;
		this.endTick = endTick;
	}

	/**
	 * Load turbines from a data file. The corresponding methods in Netlogo are
	 * turbs-import-pos and turbs-setup.
	 *
	 * @param fileName The name of the turbines file to load (excluding path and
	 *                 extension).
	 * @return List of turbines read from the file.
	 * @throws Exception Error reading the file.
	 */
	public static void load(final Context<Agent> context, final String fileName, final boolean dynamicCreation)
			throws Exception {
		if (dynamicCreation) {
			turbineCreateQueue = new LinkedList<>();
		} else {
			turbineCreateQueue = null;
		}

		final File file = new File("data/wind-farms", fileName + ".txt");
		int numTurbines = 0;

		try (BufferedReader fr = new BufferedReader(new FileReader(file))) {
			fr.readLine(); // Header is ignored.
			String line;
			while ((line = fr.readLine()) != null) {
				if (line.trim().length() < 1) {
					continue; // skip empty lines
				}
				final String[] cols = line.split("\\s+");
				final String name = cols[0];
				final double locX = Globals.convertUtmXToGrid(Double.parseDouble(cols[1]));
				final double locY = Globals.convertUtmYToGrid(Double.parseDouble(cols[2]));
				final double impact = Double.parseDouble(cols[3]);
				int startTick = 0;
				int endTick = Integer.MAX_VALUE;
				if (cols.length >= 5) {
					startTick = Integer.parseInt(cols[4]);
				}
				if (cols.length >= 6) {
					endTick = Integer.parseInt(cols[5]);
				}

				final Turbine t = new Turbine(name, impact, locX, locY, startTick, endTick, numTurbines);
				if (turbineCreateQueue != null) {
					turbineCreateQueue.add(t);
				} else {
					context.add(t);
					t.initialize();
				}

				numTurbines++;
			}
		}

		System.out.println("Showing wind turbines at: " + fileName);
		if (turbineCreateQueue != null) {
			if (numTurbines < 1) {
				System.out.println("No wind turbines plotted");
			} else {
				Collections.sort(turbineCreateQueue, new Comparator<Turbine>() {
					@Override
					public int compare(final Turbine o1, final Turbine o2) {
						if (o1.startTick > o2.startTick) {
							return 1;
						} else if (o1.startTick == o2.startTick) {
							return 0;
						} else {
							return -1;
						}
					}
				});
			}
		}
	}

	public static void activateTurbines(final Context<Agent> context) {
		if (turbineCreateQueue == null) {
			return; // Turbines not loaded or not using dynamic creation
		}

		final double now = SimulationTime.getTick();
		do {
			if (turbineCreateQueue.isEmpty()) {
				break;
			}
			final Turbine first = turbineCreateQueue.getFirst();
			if (first != null && first.startTick <= now) {
				context.add(first);
				try {
					first.initialize();
				} catch (final SpatialException e) {
					context.remove(first);
					System.err.println("Did not add turbine " + first.name + " due to an error." + e.getMessage());
				}
				turbineCreateQueue.removeFirst();
			} else {
				break;
			}
		} while (true);
	}

	public static void deactiveTurbines(final Context<Agent> context) {
		if (turbineCreateQueue == null) {
			return;
		}

		final double now = SimulationTime.getTick();
		final List<Turbine> turbinesToRemove = new LinkedList<>();
		for (final Agent a : context.getObjects(Turbine.class)) {
			final Turbine t = (Turbine) a;
			if (t.getEndTick() < now) {
				turbinesToRemove.add(t);
			} else {
				t.deterPorpoise();
			}
		}
		for (final Turbine t : turbinesToRemove) {
			context.remove(t);
		}
	}

	public void initialize() {
		this.setPosition(new NdPoint(locX, locY));
	}

	public void deterPorpoise() {

		final int simTick = (int) SimulationTime.getTick();
		if (simTick >= startTick && simTick <= endTick) {
			// the actual distance up to which porpoises react to noise from
			// a turbine with a specific impact (where impact = sound source level (SL), in
			// dB).
			// this is the distance where the sound level drops below the threshold
			final double radius = Math.pow(10, ((impact - SimulationParameters.getDeterResponseThreshold()) / 20));
			final ContinuousWithin<Agent> affectedSpace = new ContinuousWithin<Agent>(this.getSpace(), this, radius);
			final Iterable<Agent> agents = affectedSpace.query();
			for (final Agent a : agents) {
				if (a instanceof Porpoise) {
					final Porpoise p = (Porpoise) a;
					final double distToTurb = Globals.convertGridDistanceToUtm(getPosition(), p.getPosition());
					if (distToTurb <= SimulationParameters.getDeterMaxDistance()) {
						// current amount of deterring
						// the received-level (RL) gives the amount of noise that the porpoise is
						// exposed to
						// at a given distance, assuming cylindrical sound spreading; RL = SL �
						// 20Log10(dist)
						final double currentDeterence = impact
								- (SimulationParameters.getBetaHat() * Math.log10(distToTurb)
										+ (SimulationParameters.getAlphaHat() * distToTurb))
								- SimulationParameters.getDeterResponseThreshold();

						if (currentDeterence > 0) {
							p.deter(currentDeterence, this);
						}

						if (DebugLog.isEnabledFor(8)) {
							DebugLog.print8("(porp {}) dist-to-turb {}: {} m, curr.deter: {}", p.getId(), this.name,
									Math.round(distToTurb), Math.round(currentDeterence));
						}
					}
				}
			}
		}
	}

	public double getImpact() {
		return impact;
	}

	public int getStartTick() {
		return startTick;
	}

	public int getEndTick() {
		return endTick;
	}

}

/*
 * Copyright (C) 2017-2022 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

package dk.au.bios.porpoise.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.AgentPriority;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.RandomPorpoiseReportProxy;
import dk.au.bios.porpoise.util.DebugLog;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.IAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.space.continuous.NdPoint;

/**
 * A scheduled action to run some code once a (simulation) day.
 */
public class AddTrackedPorpoisesTask implements IAction {

	private final Context<Agent> context;
	private final String landscape;
	private final int trackedPorpoisesCount;

	// Used in case of delayed selection
	private int tick;
	private NdPoint delayedSelectionPoint = null;

	public AddTrackedPorpoisesTask(final Context<Agent> context, final String landscape,
			final int trackedPorpoisesCount) {
		this.context = context;
		this.landscape = landscape;
		this.trackedPorpoisesCount = trackedPorpoisesCount;
	}

	public void setup() {
		final Path trackedPorpoisePath = Paths.get("data", landscape, "/trackedporpoise.txt");
		final List<String> trackedPorpoiseLocations = new ArrayList<>();
		List<String> linesInFile = Collections.emptyList();
		if (Files.exists(trackedPorpoisePath)) {
			try {
				linesInFile = Files.readAllLines(trackedPorpoisePath);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		String delayedSelection = null;
		for (final String tpl : linesInFile) {
			if (tpl.startsWith("#")) {
				continue;
			} else if (tpl.startsWith("delayedSelection;")) {
				delayedSelection = tpl;
				break;
			} else {
				trackedPorpoiseLocations.add(tpl);
			}
		}

		if (delayedSelection != null) {
			final String[] delayedSelectionParts = delayedSelection.substring("delayedSelection;".length()).split(",");
			this.tick = Integer.valueOf(delayedSelectionParts[0]);

			if (delayedSelectionParts.length > 1) {
				// this.locX = (Double.valueOf(delayedSelectionParts[1]) - Globals.XLLCORNER) / 400;
				// this.locY = (Double.valueOf(delayedSelectionParts[2]) - Globals.YLLCORNER) / 400;
				final double locX = (Double.parseDouble(delayedSelectionParts[1]) - Globals.getXllCorner()) / 400;
				final double locY = (Double.parseDouble(delayedSelectionParts[2]) - Globals.getYllCorner()) / 400;
				this.delayedSelectionPoint = new NdPoint(locX, locY);
				// this.locX = Double.valueOf(delayedSelectionParts[1]);
				// this.locY = Double.valueOf(delayedSelectionParts[2]);
			}

			final ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
			final ScheduleParameters schedParams = ScheduleParameters.createOneTime(this.tick, AgentPriority.DAILY);
			schedule.schedule(schedParams, this);
		} else {
			selectRandomPorpoises(trackedPorpoiseLocations);
		}
	}

	private void selectRandomPorpoises(final List<String> trackedPorpoiseLocations) {
		final Iterable<Agent> agents = context.getRandomObjects(Porpoise.class, trackedPorpoisesCount);
		int ti = 0;
		for (final Agent a : agents) {
			final Porpoise p = (Porpoise) a;

			final RandomPorpoiseReportProxy randomPorpoiseAgent = new RandomPorpoiseReportProxy(1, p);
			if (trackedPorpoiseLocations.size() > ti) {
				final String[] locParams = trackedPorpoiseLocations.get(ti).split(",");
				final double locX = (Double.parseDouble(locParams[0]) - Globals.getXllCorner()) / 400;
				final double locY = (Double.parseDouble(locParams[1]) - Globals.getYllCorner()) / 400;
				randomPorpoiseAgent.getPorpoise().setPosition(new NdPoint(locX, locY));
				randomPorpoiseAgent.getPorpoise().reinitializePoslist();
				randomPorpoiseAgent.getPorpoise().setHeading(Double.parseDouble(locParams[2]));
			}
			if (DebugLog.isEnabledFor(10)) {
				DebugLog.print10(p, "Tracking random porpoise {} @{} ({},{}), presLovMov: {}", p.getId(),
						p.getPosition(), p.getUtmX(), p.getUtmY(), p.getPresLogMov());
			}
			context.add(randomPorpoiseAgent);
			ti++;

			randomPorpoiseAgent.getPorpoise().setTrackVisitedCells(true);
			randomPorpoiseAgent.getPorpoise().setWritePsmSteps(true);
		}
	}

	/**
	 * Method called if the selection is delayed
	 */
	@Override
	public void execute() {
		if (delayedSelectionPoint == null) {
			final Iterable<Agent> agents = context.getRandomObjects(Porpoise.class, trackedPorpoisesCount);
			for (final Agent a : agents) {
				final Porpoise p = (Porpoise) a;
				if (DebugLog.isEnabledFor(10)) {
					DebugLog.print10(p, "Tracking random porpoise {}", p.getId());
				}

				final RandomPorpoiseReportProxy randomPorpoiseAgent = new RandomPorpoiseReportProxy(1, p);
				context.add(randomPorpoiseAgent);
				randomPorpoiseAgent.getPorpoise().setTrackVisitedCells(true);
				randomPorpoiseAgent.getPorpoise().setWritePsmSteps(true);
			}
		} else {
			final List<Porpoise> porpoisesToTrack = getClosestPorpoises(delayedSelectionPoint, trackedPorpoisesCount);
			for (final Porpoise p : porpoisesToTrack) {
				final RandomPorpoiseReportProxy randomPorpoiseAgent = new RandomPorpoiseReportProxy(1, p);
				context.add(randomPorpoiseAgent);

				randomPorpoiseAgent.getPorpoise().setTrackVisitedCells(true);
				randomPorpoiseAgent.getPorpoise().setWritePsmSteps(true);
			}
		}
	}

	/**
	 * Return a number of porpoises closest to a point. This is not an optimized implementation!
	 *
	 * @param point
	 * @param numPorpoises
	 * @return
	 */
	private List<Porpoise> getClosestPorpoises(final NdPoint point, final int numPorpoises) {
		final List<Porpoise> allPorps = new ArrayList<Porpoise>();
		final Iterable<Agent> agents = Globals.getSpace().getObjects();
		for (final Agent a : agents) {
			if (a instanceof Porpoise) {
				final Porpoise p = (Porpoise) a;
				allPorps.add(p);
			}
		}
		Collections.sort(allPorps, new Comparator<Porpoise>() {

			@Override
			public int compare(final Porpoise p1, final Porpoise p2) {
				final double p1Dist = Globals.getSpace().getDistance(point, p1.getPosition());
				final double p2Dist = Globals.getSpace().getDistance(point, p2.getPosition());
				return (int) p1Dist - (int) p2Dist;
			}
		});

		final List<Porpoise> selectedPorps = new ArrayList<>(numPorpoises);
		for (int i = 0; i < allPorps.size() && i < numPorpoises; i++) {
			selectedPorps.add(allPorps.get(i));
		}

		return selectedPorps;
	}

}

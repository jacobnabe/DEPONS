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

package dk.au.bios.porpoise;

import java.util.concurrent.atomic.AtomicLong;

import dk.au.bios.porpoise.util.DebugLog;
import repast.simphony.context.Context;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.space.continuous.NdPoint;

/**
 * A generic sound source agent. This has been used for the deterrence testing.
 * It is not directly used in the DEPONS model.
 */
public class SoundSource extends Agent {

	private static AtomicLong soundSourceId = new AtomicLong();

	protected final double impact;

	public SoundSource(final double impact) {
		super(soundSourceId.incrementAndGet());
		this.impact = impact;
	}

	public SoundSource(final Context<Agent> context, final Porpoise p, final double angleFromPorpoise,
			final double distanceFromPorpoise, final double impact) {
		this(impact);

		context.add(this);
		setPosition(p.getPosition());
		final NdPoint position = getSpace().moveByVector(this, distanceFromPorpoise,
				((angleFromPorpoise - 90.0) * -Math.PI / 180.0), 0);
		this.setPosition(position);
	}

	/**
	 * Deters nearby porpoises
	 */
	public void deterPorpoise() {
		// number of grid-cells where a wind turbine or ship with impact 1 (standard
		// deterrence strength) affects a
		// porpoise
		final double radius = Math.pow(10, ((getImpact() - SimulationParameters.getDeterResponseThreshold()) / 20));

		// i.e. porps <deter-dist away (although porps can hear only ships <200 away,
		// the dist has to be larger to
		// account for porp jumping)
		final ContinuousWithin<Agent> affectedSpace = new ContinuousWithin<Agent>(this.getSpace(), this, radius);
		final Iterable<Agent> agents = affectedSpace.query();

		for (final Agent a : agents) {
			if (a instanceof Porpoise) {
				final Porpoise p = (Porpoise) a;
				final double distToShip = this.getSpace().getDistance(getPosition(), p.getPosition()) * 400;
				if (distToShip <= SimulationParameters.getDeterMaxDistance()) {
					// deterring-strength decreases linearly with distance to turbine, decreases to
					// 0 at 400 m
					final double currentDeterence = impact
							- (SimulationParameters.getBetaHat() * Math.log10(distToShip)
									+ (SimulationParameters.getAlphaHat() * distToShip))
							- SimulationParameters.getDeterResponseThreshold();

					if (currentDeterence > 0) {
						p.deter(currentDeterence, this);
					}

					if (DebugLog.isEnabledFor(8)) {
						DebugLog.print8("who: {} dist-to-ship {}: {}", p.getId(), this, distToShip);
					}
				}
			}
		}
	}

	public double getImpact() {
		return this.impact;
	}

}

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import dk.au.bios.porpoise.ships.Buoy;
import dk.au.bios.porpoise.ships.JomopansEchoSPL;
import dk.au.bios.porpoise.ships.Route;
import dk.au.bios.porpoise.ships.VesselClass;
import dk.au.bios.porpoise.ships.WestonFlux;
import dk.au.bios.porpoise.util.SimulationTime;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.ContextUtils;

/**
 * A ship agent. This is used in the Kattegat simulation and is not relevant for the current DEPONS model.
 */
public class Ship extends SoundSource implements dk.au.bios.porpoise.ships.Ship {

	public static double MAX_DETER_DIST = 10.0d * 1000;  // Max deter dist is 10km, regardless of dmax_deter value

	private static final int JOMOPANS_BAND = 12;
	private static final double WATER_TEMP = 10.0d;

	private static final double VHF_WEIGHTING = vhfWeighting();
	
	private JomopansEchoSPL splCalc = new JomopansEchoSPL();

	private String name;
	private VesselClass type;
	private double length;
	private Route route;
	private int tickStart = -1;
	private int tickEnd = Integer.MAX_VALUE;

	private int currentBuoyIdx = -1;
	private int ticksStillPaused = 0;

	public Ship() {
		super();
	}

	public Ship(String name, VesselClass type, double length, Route route) {
		this(name, type, length, route, -1, Integer.MAX_VALUE);
	}

	public Ship(String name, VesselClass type, double length, Route route, int tickStart, int tickEnd) {
		super();
		this.name = name;
		this.type = type;
		this.length = length;
		this.route = route;
		this.tickStart = tickStart;
		this.tickEnd = tickEnd;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTickStart() {
		return tickStart;
	}

	public void setTickStart(int start) {
		this.tickStart = start;
	}

	public int getTickEnd() {
		return tickEnd;
	}

	public void setTickEnd(int end) {
		this.tickEnd = end;
	}

	public VesselClass getType() {
		return type;
	}

	public void setType(VesselClass type) {
		this.type = type;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public void initialize() {
		this.setPosition(route.getRoute().get(0).getNdPoint());

		if (route.getRoute().size() > 1) {
			facePoint(route.getRoute().get(1).getNdPoint());
		} else {
			facePoint(route.getRoute().get(0).getNdPoint());			
		}
	}
	
	@ScheduledMethod(start = 0, interval = 1, priority = AgentPriority.SHIP_MOVE)
	public void move() {
		if (SimulationTime.getTick() < tickStart) {
			return;
		}
		if (SimulationTime.getTick() > tickEnd) {
			currentBuoyIdx = -1;
			return;
		}
		if (ticksStillPaused > 0) {
			ticksStillPaused--;
			return;
		} 

		currentBuoyIdx++;
		if (currentBuoyIdx >= route.getRoute().size()) {
			currentBuoyIdx = 0;  // repeat the route from the beginning
		}

		var buoy = route.getRoute().get(currentBuoyIdx);
		setPosition(buoy.getNdPoint());

		if (buoy.getPause() > 0) {
			ticksStillPaused = buoy.getPause();
		}
		
		var nextBuoy = findNextBuoy();
		nextBuoy.ifPresent(b -> {
			this.facePoint(b.getNdPoint());
		});
	}

	private Optional<Buoy> findNextBuoy() {
		if (SimulationTime.getTick() + 1 < tickStart) {
			return Optional.empty();
		}
		if (SimulationTime.getTick() + 1 > tickEnd) {
			return Optional.empty();
		}

		if (ticksStillPaused > 0) {
			return Optional.empty();
		} else {
			int nextBuoy = currentBuoyIdx + 1;
			if (nextBuoy >= route.getRoute().size()) {
				return Optional.empty();
			}
			var buoy = route.getRoute().get(nextBuoy);
			return Optional.of(buoy);
		}
	}

	@Override
	public void deterPorpoise() {
		if (currentBuoyIdx < 0) {
			return;
		}
		if (ticksStillPaused > 0) {
			return;
		}

		double sourceLevel = calculateSourceLevel();
		boolean isDay = SimulationTime.isDaytime();
		var startPos = this.getPosition();
		var endPos = findNextBuoyPoint();
		var tickSteps = interpolateStep(startPos, endPos);
		Set<Porpoise> porps = Globals.getSpatialPartitioning().getPorpoisesInNeighborhood(startPos, endPos);
		Iterator<Porpoise> porpsIter = porps.iterator();

		while (porpsIter.hasNext()) {
			final Porpoise p = porpsIter.next();
			
			int step = 0;
			for (NdPoint position : tickSteps) {
				final double distToShip = Globals.convertGridDistanceToUtm(position, p.getPosition());

				if (distToShip > SimulationParameters.getDeterMinDistanceShips()
						&& distToShip <= MAX_DETER_DIST
						&& distToShip <= SimulationParameters.getDeterMaxDistance()) {
					double receivedLevel = calculateReceivedLevelFor(sourceLevel, position, distToShip);
					double receivedLevelVHF = receivedLevel + VHF_WEIGHTING;
					if (receivedLevelVHF < 0) {
						receivedLevelVHF = 0;
					}

					if (receivedLevelVHF > 0) {
						var theProbOfReacting = predictProbResponse(receivedLevelVHF, distToShip / 1000.0d, isDay);

						double deterVxUnscaled = p.getPosition().getX() - position.getX();
						double deterVyUnscaled = p.getPosition().getY() - position.getY();

						double deterVLength = distToShip;
						double deterVxUnity = deterVxUnscaled / deterVLength;
						double deterVyUnity = deterVyUnscaled / deterVLength;

						var deterMagnitude = predictMag(receivedLevelVHF, distToShip / 1000.0d, isDay);

						var reactingOrNot = Globals.getRandomSource().nextDouble() < theProbOfReacting ? 1 : 0;
						var deterXStep = deterVxUnity * deterMagnitude * reactingOrNot;
						var deterYStep = deterVyUnity * deterMagnitude * reactingOrNot;

						p.deterShipStep(step, this, deterXStep, deterYStep, deterMagnitude, receivedLevelVHF);
					}
				}
				step++;
			}
		}
		
		Iterator<Hydrophone> hydrophones = ContextUtils.getContext(this).getObjects(Hydrophone.class).iterator();

		while (hydrophones.hasNext()) {
			var hydrophone = hydrophones.next();

			final double distToShip = Globals.convertGridDistanceToUtm(endPos, hydrophone.getPosition());
			final double receivedLevel = calculateReceivedLevelFor(sourceLevel, endPos, distToShip);

			hydrophone.receiveSoundLevel(this, endPos, sourceLevel, receivedLevel);
		}
	}

	protected NdPoint findNextBuoyPoint() {
		Optional<Buoy> nextBuoy = findNextBuoy();
		var endBuoy = nextBuoy.map(Buoy::getNdPoint).orElse(getPosition());
		return endBuoy;
	}

	protected List<NdPoint> interpolateStep(NdPoint start, NdPoint end) {
		final int steps = 30;
		List<NdPoint> points = new ArrayList<>(steps);
		double[] stepVector = {
				end.getX() - start.getX(),
				end.getY() - start.getY()
		};
		for (int i = 1; i <= steps; i++) {
			var stepScale = ((double) i) / ((double)steps);
			var stepPos = new NdPoint(start.getX() + (stepVector[0] * stepScale), start.getY() + (stepVector[1] * stepScale));
			
			points.add(stepPos);
		}

		return points;
	}

	private double calculateSourceLevel() {
		final double decidecadeBandSourceLevel = splCalc.calculate(type, getSpeed(), length, JOMOPANS_BAND);
		return decidecadeBandSourceLevel;
	}

	private double calculateReceivedLevelFor(double sourceLevel, NdPoint shipPos, double distToShip) {
		final double depthAtShip = Globals.getCellData().getDepth(shipPos);
		final double grainSize = Globals.getCellData().getSediment(shipPos);
		final double temp = WATER_TEMP;
		final double salinity = Globals.getCellData().getSalinity(shipPos);

		// If we are missing data, then set produced sound to 0.0
		if (valueIsNoData(depthAtShip) ||
			valueIsNoData(grainSize) ||
			valueIsNoData(salinity)) {
			return 0.0d;
		}

		final double soundTransmissionLoss = WestonFlux.calc(distToShip, depthAtShip, grainSize, temp, salinity);
		
		final double receivedLevel = sourceLevel - soundTransmissionLoss;
		
		return receivedLevel;
	}
	
	private boolean valueIsNoData(double value) {
		return value <= -9999;
	}

	protected static double vhfWeighting() {
		double f = WestonFlux.FREQUENCY;
		double c = 1.36d;
		double f1 = 12000.0d;
		double f2 = 140000.0d;
		double a = 1.8d;
		double b = 2.0d;
		double weighting = c + 10 * Math.log10(( Math.pow((f/f1),(2*a)) ) / (Math.pow((1+(f/f1)),(a)) * Math.pow((1+(f/f2)),(b))));

		return weighting;
	}
	
	protected double getSpeed() {
		return this.route.getRoute().get(currentBuoyIdx).getSpeed();
	}

	@Override
	public String toString() {
		return getName();
	}

	protected static double predictProbResponse(double receivedNoise, double distInKm, boolean day) {
		final double noiseDayProb = SimulationParameters.getShipNoiseDayProb();
		final double distDayProb = SimulationParameters.getShipDistDayProb();
		final double noisedistDayProb = SimulationParameters.getShipNoisedistDayProb();
		final double noiseNightProb = SimulationParameters.getShipNoiseNightProb();
		final double distNightProb = SimulationParameters.getShipDistNightProb();
		final double noisedistNightProb = SimulationParameters.getShipNoisedistNightProb();

		final double p_ship_int_day = SimulationParameters.getShipInterceptDayProb();
		final double p_ship_int_night = SimulationParameters.getShipInterceptNightProb();

		// Determine whether it's day or night
		double ProbTrans;
		if (day) {
			// Scale noise & distance using mean & sd of predictor variables from model dataset
			double distScale = (distInKm - 5.801812) / 2.602801;
			double noiseScale = (receivedNoise - 65.95304) / 18.25469;

			//  Equation from model      
			double Prob = p_ship_int_day + noiseDayProb * noiseScale + distDayProb * distScale + noisedistDayProb * noiseScale * distScale;

			// Transform from logit scale  
			ProbTrans = Math.exp(Prob) / (Math.exp(Prob) + 1);
		} else {
			// Otherwise we have different coefficients for darkness

			// Scale noise & distance using mean & sd of predictor variables from model dataset
			double distScale = (distInKm - 6.243703) / 2.548173;
			double noiseScale = (receivedNoise - 68.9993) / 14.81663;

			// Equation from model
			double Prob = p_ship_int_night + noiseNightProb * noiseScale + distNightProb * distScale + noisedistNightProb * noiseScale * distScale;

			// Transform from logit scale
			ProbTrans = Math.exp(Prob) / (Math.exp(Prob) + 1);
		  }

		  return ProbTrans;
	}

	protected static double predictMag(double receivedNoise, double distInKm, boolean day) {
		final double noiseDayMag = SimulationParameters.getShipNoiseDayMag();
		final double distDayMag = SimulationParameters.getShipDistDayMag();
		final double noisedistDayMag = SimulationParameters.getShipNoisedistDayMag();
		final double noiseNightMag = SimulationParameters.getShipNoiseNightMag();
		final double distNightMag = SimulationParameters.getShipDistNightMag();
		final double noisedistNightMag = SimulationParameters.getShipNoisedistNightMag();

		final double cship_int_day = SimulationParameters.getShipInterceptDayMag();
		final double cship_int_night = SimulationParameters.getShipInterceptNightMag();

		double MagTrans;
		if (day) {
			// Scale noise & distance using mean & sd of predictor variables from model dataset
			var distScale = (distInKm - 5.311561)/2.698996;
			var noiseScale = (receivedNoise - 69.28605)/17.09946;

			// Equation from model      
			var Mag = cship_int_day + noiseDayMag * noiseScale + distDayMag * distScale + noisedistDayMag * distScale * noiseScale;

			// Transform from logit scale  
			MagTrans = Math.exp(Mag);
		} else {
			// Scale noise & distance using mean & sd of predictor variables from model dataset
			var distScale = (distInKm - 6.442084) / 2.48903;
			var noiseScale = (receivedNoise - 68.86555) / 15.09977;

			// Equation from model
			var Mag = cship_int_night + noiseNightMag * noiseScale + distNightMag * distScale + noisedistNightMag * distScale * noiseScale;

			// Transform from logit scale  
			MagTrans = Math.exp(Mag);
		}

		return MagTrans;
	}

}

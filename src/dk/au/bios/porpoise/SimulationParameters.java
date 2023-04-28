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

import org.apache.log4j.Level;

import dk.au.bios.porpoise.behavior.DispersalFactory;
import dk.au.bios.porpoise.util.LogisticDecreaseSSLogis;
import repast.simphony.parameter.Parameters;
import repast.simphony.ui.RSApplication;
import simphony.util.messages.MessageEvent;

public final class SimulationParameters {

	private SimulationParameters() {
		// Utility class, prevent instances.
	}

	public static final String LANDSCAPE_HOMOGENOUS_NAME = "Homogeneous";

	private static String landscape;
	private static String turbines;
	private static boolean shipsEnabled;
	private static int porpoiseCount;
	private static int trackedPorpoiseCount;

	/**
	 * Inertia constant; the animal's tendency to keep moving using CRW irrespective
	 * of foraging success. In parameters.xml: k
	 */
	private static double inertiaConst = 0.001; // A, set to 0.001 like in ms

	private static double corrLogmovLength = 0.94; // correlation in movement distance in CRW (a1)
	private static double corrLogmovBathy = 0.94; // correlation in movement distance in CRW (a2)
	private static double corrLogmovSalinity = 0.94; // correlation in movement distance in CRW (a3)
	private static double corrAngleBase = 0.26; // correlation in direction in CRW +
	private static double corrAngleBathy = 0.26; // correlation in direction in CRW +
	private static double corrAngleSalinity = 0.26; // correlation in direction in CRW +
	private static double corrAngleBaseSD = 38.0; // // correlation in direction in CRW +

	/**
	 * Deterrence coefficient [unitless] (calibrated). In parameters.xml: c
	 */
	private static double deterrenceCoeff = 0.07; // Not declared in NETLOGO, only used

	private static double m = Math.pow(10, 0.74); // Limit for when turning angles stop decreasing with speed

	/**
	 * The response-threshold for deterrence. In parameters.xml: RT
	 */
	private static double deterResponseThreshold = 158;

	/**
	 * Deterrence decay; In parameter.xml: Psi_deter
	 */
	private static double deterDecay = 50;

	/**
	 * Maximum deterrence distance. Animals that are more than this far from the
	 * noise source shold stop being deterred. Parameter is specified in KM but
	 * stored in this variable in meters. In parameter.xml: dmax_deter
	 */
	private static double deterMaxDistance = 50 * 1000; // 50 KM

	/**
	 * Minimum deterrence distance for ships. Animals that are less than this far from the
	 * ship will not be deterred. Parameter is specified in KM but
	 * stored in this variable in meters. In parameter.xml: dmin_deter_ships
	 */
	private static double deterMinDistanceShips = 0.1d * 1000; // 100m

	private static double pship_int_day = -3.0569351d;  // pship_int_day - intercept of equation defining effect of ship noise &amp; distance on prob reacting during daylight
	private static double pship_int_night = -3.233771d;  // pship_int_night - intercept of equation defining effect of ship noise &amp; distance on prob reacting during darkness
	private static double cship_int_day = 2.9647996d;  // cship_int_day - intercept of equation defining effect of ship noise & distance on mag reacting during daylight
	private static double cship_int_night = 2.7543376d;  // cship_int_night - intercept of equation defining effect of ship noise & distance on mag reacting during darkness

	private static double noiseDayProb = 0.2172813d;  // pship_noise_day coefficient determining effect of ship noise level on probability of deterrence
	private static double distDayProb = -0.1303880d;  // pship_dist_day coefficient determining effect of distance to ship on probability of deterrence
	private static double noisedistDayProb = 0.0293443d;  // pship_dist_x_noise_day coefficient determining effect of interaction between ship noise and distance on probability of deterrence
	private static double noiseNightProb = 0.0d;
	private static double distNightProb = 0.085242d;
	private static double noisedistNightProb = 0.0d;
	private static double noiseDayMag = 0.0472709d;  // cship_noise_day coefficient determining effect of ship noise level on magnitude of deterrence
	private static double distDayMag = -0.0355541d;  // cship_dist_day coefficient determining effect of distance to ship on magnitude of deterrence
	private static double noisedistDayMag = 0.0d;    // cship_dist_x_noise_day coefficient determining effect of interaction between ship noise and distance on magnitude of deterrence
	private static double noiseNightMag = 0.0d;
	private static double distNightMag = 0.0284629d;
	private static double noisedistNightMag = 0.0d;

	/**
	 * absorption coefficient for sound. In parameter.xml: alpha_hat
	 */
	private static double alphaHat = 0;

	/**
	 * spreading loss factor for sound. In parameter.xml: beta_hat
	 */
	private static double betaHat = 20;

	/**
	 * Dispersal distance per time step [km] (J. Teilmann, unpublished satellite
	 * data). In parameters.xml: ddisp
	 */
	private static double meanDispDist = 1.6;

	/**
	 * Porpoise max distance. In parameters.xml: dmax_mov
	 */
	private static double maxMov = 1.18;

	/**
	 * Energy use per half-hour step in May-September (calibrated). In
	 * parameters.xml: Euse
	 */
	private static double eUsePer30Min = 4.5;

	/**
	 * Energy use multiplyer for lactating mammals (Magnus Wahlberg, unpubl. data).
	 * In parameters.xml: Elact
	 */
	private static double eLact = 1.4;

	/**
	 * Energy use multiplier in warm water (Lockyer et al. 2003). In parameters.xml:
	 * Ewarm
	 */
	private static double eWarm = 1.3;

	/**
	 * Deterrence time; number of time steps the deterrence effect lasts [time
	 * steps] (arbitrary). In parameters.xml: tdeter
	 */
	private static int deterTime = 5;

	/**
	 * Maximum energy content in a food patch. In parameters.xml: Umax
	 */
	private static double maxU = 1.0; // ; Maximum utility of a patch, set to 1 here

	/**
	 * Minimum water depth when dispersing [m] (visual calibration). In
	 * parameters.xml: wdisp
	 */
	private static double minDispDepth = 4.0;

	/**
	 * Minimum water depth [m] required by porpoises (J. Tougaard, pers. obs). In
	 * parameters.xml: wmin
	 */
	private static double minDepth = 1.0; // Match NetLogo

	/**
	 * Survival probability constant (calibrated). In parameters.xml: beta
	 */
	private static double xSurvivalProbConst = 0.4;

	/**
	 * Probability of becoming pregnant (Read and Hohn, 1995). In parameters.xml: h
	 */
	private static double conceiveProb = 0.68;

	/**
	 * Gestation time [days] (Lockyer et al., 2003). In parameters.xml: tgest
	 */
	private static int gestationTime = 300;

	/**
	 * Nursing time [days] (Lockyer et al., 2003; Lockyer and Kinze, 2003). In
	 * parameters.xml: tnurs
	 */
	private static int nursingTime = 240;

	/**
	 * Porpoise maximum age. In parameters.xml: tmaxage
	 */
	private static double maxAge = 30.0;

	private static double maturityAge = 3.44;

	private static double regrowthFoodQualifier = 0.001;

	private static double pstPreferredDistanceTolerance;
	private static double psmType2RandomAngle;

	private static double q1;
	private static double rS;
	private static double rR;


	private static LogisticDecreaseSSLogis psmLogisticDecreaseFunction;

	private static boolean homogenous = true;
	private static boolean wrapBorderHomo = true;

	private static int model = 1; // model seems to be a global variable in NETLOGO

	/**
	 * Days of declining energy before activating dispersal. In parameters.xml:
	 * tdisp
	 */
	private static int tDisp = 3;

	/**
	 * Food replenishment rate [unitless] (Nabe-Nielsen et al., 2013). In
	 * parameters.xml: rU
	 */
	private static double foodGrowthRate = 0.10;

	/**
	 * In parameters.xml: bycatchProb
	 */
	private static double bycatchProb = 0.0;

	private static Double psmLog;

	public static void initialize(final Parameters params) {
		landscape = params.getString("landscape");
		turbines = params.getString("turbines");
		porpoiseCount = params.getInteger("porpoiseCount");
		trackedPorpoiseCount = params.getInteger("trackedPorpoiseCount");
		shipsEnabled = params.getBoolean("ships");
		inertiaConst = convertStringToDouble(params, "k");
		corrLogmovLength = convertStringToDouble(params, "a0");
		corrLogmovBathy = convertStringToDouble(params, "a1");
		corrLogmovSalinity = convertStringToDouble(params, "a2");
		corrAngleBase = convertStringToDouble(params, "b0");
		corrAngleBathy = convertStringToDouble(params, "b1");
		corrAngleSalinity = convertStringToDouble(params, "b2");
		corrAngleBaseSD = convertStringToDouble(params, "b3");
		deterrenceCoeff = convertStringToDouble(params, "c");
		m = 0.00001; // Hardcoded value - Math.pow(10, params.getDouble("m"));
		deterResponseThreshold = convertStringToDouble(params, "RT");
		deterDecay = convertStringToDouble(params, "Psi_deter");
		deterMaxDistance = convertStringToDouble(params, "dmax_deter") * 1000; // entered in KM but stored in meters.
		deterMinDistanceShips = convertStringToDouble(params, "dmin_deter_ships") * 1000;  // entered in KM but stored in meters.
		pship_int_day = convertStringToDouble(params, "pship_int_day");
		pship_int_night = convertStringToDouble(params, "pship_int_night");
		cship_int_day = convertStringToDouble(params, "cship_int_day");
		cship_int_night = convertStringToDouble(params, "cship_int_night");
		noiseDayProb = convertStringToDouble(params, "pship_noise_day");
		distDayProb = convertStringToDouble(params, "pship_dist_day");
		noisedistDayProb = convertStringToDouble(params, "pship_dist_x_noise_day");
		noiseNightProb = convertStringToDouble(params, "pship_noise_night");
		distNightProb = convertStringToDouble(params, "pship_dist_night");
		noisedistNightProb = convertStringToDouble(params, "pship_dist_x_noise_night");
		noiseDayMag = convertStringToDouble(params, "cship_noise_day");
		distDayMag = convertStringToDouble(params, "cship_dist_day");
		noisedistDayMag = convertStringToDouble(params, "cship_dist_x_noise_day");
		noiseNightMag = convertStringToDouble(params, "cship_noise_night");
		distNightMag = convertStringToDouble(params, "cship_dist_night");
		noisedistNightMag = convertStringToDouble(params, "cship_dist_x_noise_night");

		alphaHat = convertStringToDouble(params, "alpha_hat");
		betaHat = convertStringToDouble(params, "beta_hat");
		meanDispDist = convertStringToDouble(params, "ddisp");
		maxMov = convertStringToDouble(params, "dmax_mov");
		eUsePer30Min = convertStringToDouble(params, "Euse");
		eLact = convertStringToDouble(params, "Elact");
		eWarm = convertStringToDouble(params, "Ewarm");
		deterTime = params.getInteger("tdeter");
		maxU = 1; // Hardcoded value - params.getDouble("Umax");
		minDispDepth = convertStringToDouble(params, "wdisp");
		minDepth = convertStringToDouble(params, "wmin");
		xSurvivalProbConst = convertStringToDouble(params, "beta");
		conceiveProb = convertStringToDouble(params, "h");
		gestationTime = params.getInteger("tgest");
		nursingTime = params.getInteger("tnurs");
		maxAge = convertStringToDouble(params, "tmaxage");
		maturityAge = convertStringToDouble(params, "tmature");
		regrowthFoodQualifier = convertStringToDouble(params, "Umin");
		pstPreferredDistanceTolerance = convertStringToDouble(params, "PSM_tol");
		psmType2RandomAngle = convertStringToDouble(params, "PSM_angle");
		q1 = 0.02d; // Obsolete, hardcoded value - convertStringToDouble(params, "q1");
		rS = convertStringToDouble(params, "rS");
		rR = convertStringToDouble(params, "rR");

		final String psmLogParam = params.getString("PSM_log");
		final String[] psmLogDecrease = psmLogParam.split(";");
		if (psmLogDecrease.length == 1) {
			final double phi1 = 1.0;
			final double phi2 = 0.0;
			final double phi3 = Double.parseDouble(psmLogDecrease[0]);
			psmLog = Double.valueOf(phi3);
			psmLogisticDecreaseFunction = new LogisticDecreaseSSLogis(phi1, phi2, phi3);
		} else if (psmLogDecrease.length == 3) {
			final double phi1 = Double.parseDouble(psmLogDecrease[0]);
			final double phi2 = Double.parseDouble(psmLogDecrease[1]);
			final double phi3 = Double.parseDouble(psmLogDecrease[2]);
			System.err.println("WARNING - PSM_log is not a single value, "
					+ "this may cause an error in the PSM heading calculation!");
			psmLog = null;
			psmLogisticDecreaseFunction = new LogisticDecreaseSSLogis(phi1, phi2, phi3);
		} else {
			System.err.println("Invalid value for PSM_log - " + psmLogParam);
			throw new RuntimeException("Invalid value for PSM_log - " + psmLogParam);
		}

		final String landscape = (String) params.getValue("landscape");
		homogenous = landscape.equals(LANDSCAPE_HOMOGENOUS_NAME);
		wrapBorderHomo = params.getBoolean("wrapBorderHomo");

		model = 4; // Hardcoded value - (Integer)params.getValue("model");

		final String dispersalTypeStr = params.getString("dispersal");
		DispersalFactory.setType(dispersalTypeStr);

		tDisp = params.getInteger("tdisp");
		foodGrowthRate = convertStringToDouble(params, "rU");
		bycatchProb = convertStringToDouble(params, "bycatchProb");
	}
	
	public static void resetToDefaultsForUnitTest() {
		landscape = null;
		turbines = null;
		shipsEnabled = false;
		porpoiseCount = 0;
		trackedPorpoiseCount = 0;
		inertiaConst = 0.001;
		corrLogmovLength = 0.35;
		corrLogmovBathy = 0.0005;
		corrLogmovSalinity = -0.02;
		corrAngleBase = -0.024;
		corrAngleBathy = -0.008;
		corrAngleSalinity = 0.93;
		corrAngleBaseSD = -14;
		deterrenceCoeff = 0.07;
		m = Math.pow(10, 0.74);
		deterResponseThreshold = 152.9;
		deterDecay = 50.0;
		deterMaxDistance = 1000.0 * 1000; // in KM
		deterMinDistanceShips = 0.1d * 1000; // 100m
		alphaHat = 0.0;
		betaHat = 20.0;
		meanDispDist = 1.05;
		maxMov = 1.73;
		eUsePer30Min = 4.5;
		eLact = 1.4;
		eWarm = 1.3;
		deterTime = 0;
		maxU = 1.0;
		minDispDepth = 4.0;
		minDepth = 1.0;
		xSurvivalProbConst = 0.4;
		conceiveProb = 0.68;
		gestationTime = 300;
		nursingTime = 240;
		maxAge = 30.0;
		maturityAge = 3.44;
		regrowthFoodQualifier = 0.001;
		pstPreferredDistanceTolerance = 5.0d;
		psmType2RandomAngle = 20.0d;
		q1 = 0.02d;
		rS = 0.04;
		rR = 0.04;
		psmLogisticDecreaseFunction = null;
		homogenous = true;
		wrapBorderHomo = true;
		model = 4;
		tDisp = 3;
		foodGrowthRate = 0.10;
		bycatchProb = 0.0;
		psmLog = 0.6;
		psmLogisticDecreaseFunction = new LogisticDecreaseSSLogis(1.0, 0.0, psmLog);
		
		pship_int_day = -3.0569351d;
		pship_int_night = -3.233771d;
		cship_int_day = 2.9647996d;
		cship_int_night = 2.7543376d;
		noiseDayProb = 0.2172813d;
		distDayProb = -0.1303880d;
		noisedistDayProb = 0.0293443d;
		noiseNightProb = 0.0d;
		distNightProb = 0.085242d;
		noisedistNightProb = 0.0d;
		noiseDayMag = 0.0472709d;
		distDayMag = -0.0355541d;
		noisedistDayMag = 0.0d;
		noiseNightMag = 0.0d;
		distNightMag = 0.0284629d;
		noisedistNightMag = 0.0d;
	}

	public static void resetToDefaultsForOldUnitTest() {
		landscape = null;
		turbines = null;
		shipsEnabled = false;
		porpoiseCount = 0;
		trackedPorpoiseCount = 0;
		inertiaConst = 0.001;
		corrLogmovLength = 0.94;
		corrLogmovBathy = 0.94;
		corrLogmovSalinity = 0.94;
		corrAngleBase = 0.26;
		corrAngleBathy = 0.26;
		corrAngleSalinity = 0.26;
		corrAngleBaseSD = 38.0;
		deterrenceCoeff = 1;
		m = Math.pow(10, 0.74);
		deterResponseThreshold = 158;
		deterDecay = 50;
		deterMaxDistance = 50 * 1000; // 50 KM
		alphaHat = 0;
		betaHat = 20;
		meanDispDist = 1.6;
		maxMov = 1.18;
		eUsePer30Min = 4.5;
		eLact = 1.4;
		eWarm = 1.3;
		deterTime = 5;
		maxU = 1.0;
		minDispDepth = 4.0;
		minDepth = 1.0;
		xSurvivalProbConst = 0.4;
		conceiveProb = 0.68;
		gestationTime = 300;
		nursingTime = 240;
		maxAge = 30.0;
		maturityAge = 3.44;
		regrowthFoodQualifier = 0.001;
		pstPreferredDistanceTolerance = 0.0d;
		psmType2RandomAngle = 0.0d;
		q1 = 0.0d;
		rS = 0.04;
		rR = 0.04;
		psmLogisticDecreaseFunction = null;
		homogenous = true;
		wrapBorderHomo = true;
		model = 1;
		tDisp = 3;
		foodGrowthRate = 0.10;
		bycatchProb = 0.0;
		psmLog = null;
	}

	public static void disableCrwRandomness() {
		corrLogmovLength = 0.0;
		corrLogmovBathy = 0.0;
		corrLogmovSalinity = 0.0;
		corrAngleBase = 0.0;
		corrAngleBathy = 0.0;
		corrAngleSalinity = 0.0;
		corrAngleBaseSD = 0.0;
		
		// R1 and R2
		maxMov = 1.25;
		
		pship_int_day = 0.0;
		noiseDayProb = 0.0;
		distDayProb = -100;
		noisedistDayProb = 0.0;
	}
	
	public static String getLandscape() {
		return landscape;
	}

	public static String getTurbines() {
		return turbines;
	}

	public static int getPorpoiseCount() {
		return porpoiseCount;
	}

	public static int getTrackedPorpoiseCount() {
		return trackedPorpoiseCount;
	}

	public static double getInertiaConst() {
		return inertiaConst;
	}

	public static double getCorrLogmovLength() {
		return corrLogmovLength;
	}

	public static double getCorrLogmovBathy() {
		return corrLogmovBathy;
	}

	public static double getCorrLogmovSalinity() {
		return corrLogmovSalinity;
	}

	public static double getCorrAngleBase() {
		return corrAngleBase;
	}

	public static double getCorrAngleBathy() {
		return corrAngleBathy;
	}

	public static double getCorrAngleSalinity() {
		return corrAngleSalinity;
	}

	public static double getCorrAngleBaseSD() {
		return corrAngleBaseSD;
	}

	public static double getDeterrenceCoeff() {
		return deterrenceCoeff;
	}

	public static double getM() {
		return m;
	}

	public static double getDeterResponseThreshold() {
		return deterResponseThreshold;
	}

	public static double getDeterDecay() {
		return deterDecay;
	}
	
	public static void setDeterDecay(double deterDecay) {
		SimulationParameters.deterDecay = deterDecay;
	}

	public static double getDeterMaxDistance() {
		return deterMaxDistance;
	}

	public static double getDeterMinDistanceShips() {
		return deterMinDistanceShips;
	}

	public static double getShipInterceptDayProb() {
		return pship_int_day;
	}
	public static double getShipInterceptNightProb() {
		return pship_int_night;
	}
	public static double getShipInterceptDayMag() {
		return cship_int_day;
	}
	public static double getShipInterceptNightMag() {
		return cship_int_night;
	}

	public static double getShipNoiseDayProb() {
		return noiseDayProb;
	}
	public static double getShipDistDayProb() {
		return distDayProb;
	}
	public static double getShipNoisedistDayProb() {
		return noisedistDayProb;
	}
	public static double getShipNoiseNightProb() {
		return noiseNightProb;
	}
	public static double getShipDistNightProb() {
		return distNightProb;
	}
	public static double getShipNoisedistNightProb() {
		return noisedistNightProb;
	}
	public static double getShipNoiseDayMag() {
		return noiseDayMag;
	}
	public static double getShipDistDayMag() {
		return distDayMag;
	}
	public static double getShipNoisedistDayMag() {
		return noisedistDayMag;
	}
	public static double getShipNoiseNightMag() {
		return noiseNightMag;
	}
	public static double getShipDistNightMag() {
		return distNightMag;
	}
	public static double getShipNoisedistNightMag() {
		return noisedistNightMag;
	}

	public static double getAlphaHat() {
		return alphaHat;
	}

	public static double getBetaHat() {
		return betaHat;
	}

	public static double getMeanDispDist() {
		return meanDispDist;
	}

	public static double getMaxMov() {
		return maxMov;
	}

	public static double getEUsePer30Min() {
		return eUsePer30Min;
	}

	public static double getELact() {
		return eLact;
	}

	public static double getEWarm() {
		return eWarm;
	}

	public static int getDeterTime() {
		return deterTime;
	}
	
	public static void setDeterTime(int deterTime) {
		SimulationParameters.deterTime = deterTime;
	}

	public static double getMaxU() {
		return maxU;
	}

	public static double getMinDispDepth() {
		return minDispDepth;
	}

	public static double getMinDepth() {
		return minDepth;
	}

	public static double getXSurvivalProbConst() {
		return xSurvivalProbConst;
	}

	public static double getConceiveProb() {
		return conceiveProb;
	}

	public static double getGestationTime() {
		return gestationTime;
	}

	public static double getNursingTime() {
		return nursingTime;
	}

	public static double getMaxAge() {
		return maxAge;
	}

	public static double getMaturityAge() {
		return maturityAge;
	}

	public static double getRegrowthFoodQualifier() {
		return regrowthFoodQualifier;
	}

	public static double getPsmPreferredDistanceTolerance() {
		return pstPreferredDistanceTolerance;
	}

	public static double getPsmType2RandomAngle() {
		return psmType2RandomAngle;
	}

	public static Double getPsmLog() {
		return psmLog;
	}

	public static double getQ1() {
		return q1;
	}

	public static double getRS() {
		return rS;
	}

	public static double getRR() {
		return rR;
	}

	public static LogisticDecreaseSSLogis getPsmLogisticDecreaseFunction() {
		return psmLogisticDecreaseFunction;
	}

	public static boolean isHomogenous() {
		return homogenous;
	}

	public static boolean isWrapBorderHomo() {
		return wrapBorderHomo;
	}

	public static boolean isShipsEnabled() {
		return shipsEnabled;
	}

	public static int getModel() {
		return model;
	}
	
	public static void setModel(int model) {
		SimulationParameters.model = model;
	}

	public static int getTDisp() {
		return tDisp;
	}
	
	public static void setTDisp(int tDisp) {
		SimulationParameters.tDisp = tDisp;
	}

	public static double getFoodGrowthRate() {
		return foodGrowthRate;
	}

	public static double getBycatchProb() {
		return bycatchProb;
	}

	/**
	 * Used for unit testing - until a better model for setting variables is found.
	 *
	 * @param newQ1 New value for the Q1 parameter.
	 */
	public static void setQ1(final double newQ1) {
		q1 = newQ1;
	}

	/**
	 * This conversion was introduced in response to issues with the precision in
	 * the Repast UI. The decimal would be restricted to maximum 6. As a workaround
	 * the parameter type has been changed to string and the conversion performed
	 * with the following method. The actual conversion is the same as used
	 * internally in Repast but it does not include the restriction of maximum 6
	 * decimals.
	 * 
	 * @param params    The Parameters map
	 * @param paramName The parameter name
	 * @return The value of the string parameter as a Double
	 */
	private static Double convertStringToDouble(final Parameters params, final String paramName) {
		String strVal = params.getString(paramName);
		try {
			return Double.valueOf(strVal);
		} catch (NumberFormatException e) {
			if (RSApplication.getRSApplicationInstance() != null) {
				RSApplication.getRSApplicationInstance().getErrorLog().addError(new MessageEvent(paramName, Level.FATAL,
						"Value " + strVal + " for parameter " + paramName + " is not a valid floating point value."));
				RSApplication.getRSApplicationInstance().getErrorLog().show();
			}
			throw e;
		}
	}
}

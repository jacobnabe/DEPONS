/*
 * Copyright (C) 2017-2021 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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
	private static double deterrenceCoeff = 1; // Not declared in NETLOGO, only used

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
		inertiaConst = params.getDouble("k");
		corrLogmovLength = params.getDouble("a0");
		corrLogmovBathy = params.getDouble("a1");
		corrLogmovSalinity = params.getDouble("a2");
		corrAngleBase = params.getDouble("b0");
		corrAngleBathy = params.getDouble("b1");
		corrAngleSalinity = params.getDouble("b2");
		corrAngleBaseSD = params.getDouble("b3");
		deterrenceCoeff = params.getDouble("c");
		m = 0.00001; // Hardcoded value - Math.pow(10, params.getDouble("m"));
		deterResponseThreshold = params.getDouble("RT");
		deterDecay = params.getDouble("Psi_deter");
		deterMaxDistance = params.getDouble("dmax_deter") * 1000; // entered in KM but stored in meters.
		alphaHat = convertStringToDouble(params, "alpha_hat");
		betaHat = convertStringToDouble(params, "beta_hat");
		meanDispDist = params.getDouble("ddisp");
		maxMov = params.getDouble("dmax_mov");
		eUsePer30Min = params.getDouble("Euse");
		eLact = params.getDouble("Elact");
		eWarm = params.getDouble("Ewarm");
		deterTime = params.getInteger("tdeter");
		maxU = 1; // Hardcoded value - params.getDouble("Umax");
		minDispDepth = params.getDouble("wdisp");
		minDepth = params.getDouble("wmin");
		xSurvivalProbConst = params.getDouble("beta");
		conceiveProb = params.getDouble("h");
		gestationTime = params.getInteger("tgest");
		nursingTime = params.getInteger("tnurs");
		maxAge = params.getDouble("tmaxage");
		maturityAge = params.getDouble("tmature");
		regrowthFoodQualifier = params.getDouble("Umin");
		pstPreferredDistanceTolerance = params.getDouble("PSM_tol");
		psmType2RandomAngle = params.getDouble("PSM_angle");
		q1 = params.getDouble("q1");

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
		foodGrowthRate = params.getDouble("rU");
		bycatchProb = params.getDouble("bycatchProb");
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

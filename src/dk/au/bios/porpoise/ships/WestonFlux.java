/*
 * Copyright (C) 2022-2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

package dk.au.bios.porpoise.ships;

import org.apache.commons.math3.special.Erf;

public class WestonFlux {

	public static final double FREQUENCY = Math.pow(10,(12.0d/10.0d))*1000.0d; // 15848.93d; // 15848.9d;
	private static final double SPEED_IN_SEDIMENT = 1700.0d; // (c_s) Sound speed in sediment (1700 m/s)
	private static final double PH = 8.0d; // ph<-8 # Ph

	public static double calc(double distInMeters, double depthAtSource, double grainSize, double temp, double salinity) {
		double sspRatioHigh = sspRatio(grainSize);
		double beta = beta(grainSize, sspRatioHigh);
		double gamma = gamma(FREQUENCY, temp, salinity, PH, 0.0d);
		double sspRatio = sspRatioHigh;
		double rhoRatio = rhoRatio(grainSize);

		return rangeIndependent(distInMeters, depthAtSource, FREQUENCY, SPEED_IN_SEDIMENT, beta, gamma, sspRatio, rhoRatio);
	}

	protected static double rangeIndependent(double r, double h, double f, double c_s, double beta_s, double gamma_w, double SSPratio_row, double RhoRatio_row) {
		  // Some useful variables
		  
		  double SSPratio = SSPratio_row;
		  double RhoRatio = RhoRatio_row;
		  double alpha_w = Math.log(10)/20 * gamma_w; // % convert from dB/m to Np/m

		  if (SSPratio < 1.04) {
			  SSPratio=1.04;
		  }
		  
		  // Determine eta
		  double eta;
		  if (SSPratio > 1 ) {
			double epsilon = Math.log(10.0d) / (40.0d * Math.PI) * beta_s;
			eta = 2.0d * RhoRatio * (SSPratio / (Math.pow((Math.pow(SSPratio,2) - 1),(3.0d/2.0d)))) * epsilon;
		  } else  if (SSPratio < 1) {
		    eta = 2*RhoRatio * SSPratio /Math.sqrt(1- Math.pow(SSPratio,2));  
		  } else {
		    eta=0;
		  }  

		  // #theta_crit_intermediate = acos(SSPratio_rads)
		  double theta_crit=Math.acos(1/SSPratio);
		  
		  
		  // Compute limiting angle, note range-independent: theta_crit == theta_limit theta_limit = theta_crit;
		  double theta_limit=theta_crit; // for range-independent only
		  
		  // Compute effective water depth: range-independent 
		  double H_eff = h; // for range-independent only
		  
		  // Compute propagation factor
		  double F = Math.pow(r, (-3d/2d)) * Math.sqrt(Math.PI/(eta*H_eff)) * Erf.erf(Math.sqrt(eta*r/h)*theta_limit) * Math.exp(-2* alpha_w *r);

		  // Compute propagation loss (in dB re 1m^2) 
		  double PL = -10*Math.log10(F);

		  if (PL < 0.0d) {
			  PL = 0-0d;
		  }

		  return PL;
	}

	protected static double sspRatio(double grainSize) {
		// High frequencies  
		double val;
		if (grainSize < -8.0d) {
			throw new IllegalArgumentException("grainSize " + grainSize + " is smaller than -8.0");
		} else if (grainSize < 1.0d) {
			val = 1.2778 - 0.056452 * grainSize + 0.002709 * Math.pow(grainSize, 2); 
		} else if (grainSize < 5.3d) {
			val = 1.3425 - 0.1382798*grainSize + 0.0213937 * Math.pow(grainSize, 2) - 0.0014881 * Math.pow(grainSize, 3);
		} else if (grainSize <= 9.0d) {
			val = 1.0019 - 0.0024324 * grainSize;
		} else {
			val = 1.0019 - 0.0024324 * 9.0d; // Restricted to 9
		}

		return val;  
	}

	protected static double rhoRatio(double grainSize) {
		// High frequencies
		double val;
		if (grainSize < -8.0d) {
			throw new IllegalArgumentException("grainSize " + grainSize + " is not valid");
		} else if (grainSize < 1.0d) {
			// TNO communicated that grain sizes < -1 should have same properties as grain sizes of -1
			val = 2.3139 - 0.17057 * grainSize + 0.007797 * Math.pow(grainSize, 2);
		} else if (grainSize < 5.3d) {
			val = 3.0455 - 1.1069031 * grainSize + 0.2290201 * Math.pow(grainSize, 2)
					- 0.0165406 * Math.pow(grainSize, 3);
		} else if (grainSize <= 9.0d) {
			val = 1.1565 - 0.0012973 * grainSize;
		} else {
			val = 1.1565 - 0.0012973 * 9.0d; // Restricted to 9
		}

		return val;
	}

	protected static double beta(double grainSize, double sspRatioHigh) {
		// High frequencies
		double val;
		if (grainSize < -8.0d) {
			throw new IllegalArgumentException();
		} else if (grainSize < 0.0d) {
			val = 1.490 * 0.4556;
		} else if (grainSize < 2.6d) {
			val = 1.490 * sspRatioHigh * (0.4556 + 0.0245 * grainSize);
		} else if (grainSize < 4.5) {
			val = 1.490 * sspRatioHigh * (0.1978 + 0.1245 * grainSize);
		} else if (grainSize < 6) { // NB CHECK R has <= here and below!
			val = 1.490 * sspRatioHigh * (8.0399 - 2.5228 * grainSize + 0.20098 * Math.pow(grainSize, 2));
		} else if (grainSize < 9.5) {
			val = 1.490 * sspRatioHigh * (0.9431 - 0.2041 * grainSize + 0.0117 * Math.pow(grainSize, 2));
		} else {
			val = 1.490 * sspRatioHigh * 0.0601;
		}

		return val;
	}

	protected static double gamma(double f, double Temp, double salinity, double ph, double depthAtSource) {
		double euler = Math.exp(1);

		double f1 = 0.91 * Math.pow((salinity / 35), (0.5)) * Math.pow(euler, (Temp / 33));

		double f2 = 46.6 * Math.exp(Temp / 18);

		double A3;
		if (Temp <= 20) {
			A3 = 4.937 * Math.pow(10, (-4)) - 2.59 * Math.pow(10, (-5)) * Temp
					+ 9.11 * Math.pow(10, (-7)) * Math.pow(Temp, 2) - 1.5 * Math.pow(10, (-8)) * Math.pow(Temp, 3);
		} else {
			A3 = 3.964 * Math.pow(10, (-4)) - 1.146 * Math.pow(10, (-5)) * Temp
					+ 1.45 * Math.pow(10, (-7)) * Math.pow(Temp, 2) - 6.5 * Math.pow(10, (-10)) * Math.pow(Temp, 3);
		}

		double P3 = 1 - 3.83 * Math.pow(10, (-5)) * depthAtSource + 4.9 * Math.pow(10, (-4)) * Math.pow((depthAtSource / 1000), 2);

		double y1 = 0.101 * ((f1 * Math.pow((f / 1000), 2)) / (Math.pow(f1, 2) + Math.pow((f / 1000), 2)))
				* Math.pow(euler, ((ph - 8) / 0.57));

		double y2 = 0.56 * (1 + Temp / 76) * (salinity / 35)
				* ((f2 * Math.pow((f / 1000), 2)) / (Math.pow(f2, 2) + Math.pow((f / 1000), 2))) * Math.exp(-depthAtSource / 4900);

		double y3 = A3 * P3 * Math.pow((f / 1000), 2);

		double absorption = (y1 + y2 + y3) / 1000;

		return absorption;
	}

}

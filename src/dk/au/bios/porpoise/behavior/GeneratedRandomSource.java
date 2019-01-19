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

package dk.au.bios.porpoise.behavior;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.NdPoint;
import cern.jet.random.Normal;
import cern.jet.random.Uniform;

/**
 * Implementation of RandomSource which generates the random numbers using the Repast RandomHelper. This is the
 * implementation normally used in the simulation.
 */
public class GeneratedRandomSource implements RandomSource {

	private static final Pattern SPEC_PATTERN = Pattern.compile("N\\((\\d+(\\.\\d+)?);(\\d+(\\.\\d+)?)\\)");

	private final Normal randomMatingDayNormal;
	private final Normal randomEnergyNormal;
	private final Normal randomNormalCrwAngle;
	private final Normal randomNormalCrwAngleWithM;
	private final Normal randomNormalCrwStepLength;
	private final Normal randomPsmDistStddev;
	private final Uniform randomStdmove;
	private final Uniform randomEnergy;
	private final Uniform randomMortality;
	private final Uniform randomPregConceive;
	private final Uniform randomPregGender;
	private final Uniform randomAvoidLand;
	private final Uniform randomDefaultUniform;
	private final Uniform randomUniformGeneral;
	private final Normal randomInitialDaysSinceMating;

	public GeneratedRandomSource(final Parameters params) {
		randomMatingDayNormal = createNormalFromParameter("tmating", params);
		randomEnergyNormal = createNormalFromParameter("Einit", params);
		randomNormalCrwAngle = createNormalFromParameter("R2", params);

		// Hardcoded value - createNormalFromParameter("R3", params);
		randomNormalCrwAngleWithM = RandomHelper.createNormal(0, 1);
		randomNormalCrwStepLength = createNormalFromParameter("R1", params);
		randomPsmDistStddev = createNormalFromParameter("PSM_dist", params);
		randomStdmove = RandomHelper.createUniform(0, 20);
		randomEnergy = RandomHelper.createUniform(0, 1);
		randomMortality = RandomHelper.createUniform(0, 1);
		randomPregConceive = RandomHelper.createUniform(0, 1);
		randomPregGender = RandomHelper.createUniform(0, 1);
		randomAvoidLand = RandomHelper.createUniform(0, 10);
		randomDefaultUniform = RandomHelper.createUniform(0, 1);
		randomUniformGeneral = RandomHelper.createUniform();

		// values taken from Porpoise ctor
		randomInitialDaysSinceMating = RandomHelper.createNormal(7.5 * 360 / 12, 20);
	}

	private Normal createNormalFromParameter(final String paramName, final Parameters params) {
		final String normalSpec = params.getString(paramName);
		final Matcher matcher = SPEC_PATTERN.matcher(normalSpec);
		if (matcher.matches()) {
			final double mean = Double.parseDouble(matcher.group(1));
			final double stddev = Double.parseDouble(matcher.group(3));

			return RandomHelper.createNormal(mean, stddev);
		} else {
			throw new IllegalArgumentException("The value of parameter " + paramName
					+ " is not a valid Random Normal. Value: " + normalSpec);
		}
	}

	@Override
	public double nextEnergeticUpdate(final double from, final double to) {
		// return Globals.randomFloat(from, to);
		return randomEnergy.nextDouble();
	}

	@Override
	public double nextMortality(final double from, final double to) {
		// return Globals.randomFloat(from, to);
		return randomMortality.nextDouble();
	}

	@Override
	public double nextPregnancyStatusConceive(final double from, final double to) {
		// return Globals.randomFloat(from, to);
		return randomPregConceive.nextDouble();
	}

	@Override
	public double nextPregnancyStatusBoyGirl(final double from, final double to) {
		// return Globals.randomFloat(from, to);
		return randomPregGender.nextDouble();
	}

	@Override
	public double nextDouble() {
		return randomDefaultUniform.nextDouble();
	}

	@Override
	public int nextAvoidLand(final int from, final int to) {
		// return Globals.random(from, to);
		return randomAvoidLand.nextInt();
	}

	@Override
	public int nextDisp3(final int from, final int to) {
		return this.randomInt(from, to);
	}

	@Override
	public int nextDispTargetSelect(final int from, final int to) {
		// return Globals.random(from, to);
		return (int) (randomPregGender.nextDouble() * to);
	}

	@Override
	public int nextStdMove(final int from, final int to) {
		// return Globals.random(from, to);
		return randomStdmove.nextInt();
	}

	@Override
	public int nextAgeDistrib(final int from, final int to) {
		return this.randomInt(from, to);
	}

	@Override
	public double nextEnergyNormal() {
		return randomEnergyNormal.nextDouble();
	}

	@Override
	public double nextMatingDayNormal() {
		return randomMatingDayNormal.nextDouble();
	}

	@Override
	public double nextCrwAngle() {
		return randomNormalCrwAngle.nextDouble();
	}

	@Override
	public double nextCrwStepLength() {
		return randomNormalCrwStepLength.nextDouble();
	}

	@Override
	public double nextCrwAngleWithM() {
		return randomNormalCrwAngleWithM.nextDouble();
	}

	@Override
	public int pastLoc(final String id, final int max) {
		return this.randomInt(0, max);
	}

	@Override
	public NdPoint getInitialPoint() {
		return null;
	}

	@Override
	public Double getInitialHeading() {
		return null;
	}

	@Override
	public double nextPSMDistanceStddev() {
		return randomPsmDistStddev.nextDouble();
	}

	@Override
	public int randomInt(final int from, final int to) {
		return randomUniformGeneral.nextIntFromTo(from, to - 1);
	}

	@Override
	public int getInitialDaysSinceMating() {
		return (int) (360 - Math.round(randomInitialDaysSinceMating.nextDouble()));
	}

	@Override
	public double randomPlusMinusOne() {
		return randomUniformGeneral.nextDoubleFromTo(-1, 1);
	}

}

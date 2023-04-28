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

import static dk.au.bios.porpoise.ships.WestonFlux.FREQUENCY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

public class WestonFluxTest {

	@Test
	void sspRatio() {
		assertThat(WestonFlux.sspRatio(-4)).isCloseTo(1.546952d, within(0.0000005));
		assertThat(WestonFlux.sspRatio(0.9)).isCloseTo(1.229187d, within(0.0000005));
		assertThat(WestonFlux.sspRatio(1.5)).isCloseTo(1.178194d, within(0.0000005));
		assertThat(WestonFlux.sspRatio(5.3)).isCloseTo(0.9890083d, within(0.0000005));
		assertThat(WestonFlux.sspRatio(6)).isCloseTo(0.9873056d, within(0.0000005));
		assertThat(WestonFlux.sspRatio(6.5)).isCloseTo(0.9860894d, within(0.0000005));
		assertThat(WestonFlux.sspRatio(7)).isCloseTo(0.9848732d, within(0.0000005));
		assertThat(WestonFlux.sspRatio(1.750718)).isCloseTo(1.157998d, within(0.0000005));
		assertThat(WestonFlux.sspRatio(9.0)).isCloseTo(0.980008d, within(0.0000005));
		assertThat(WestonFlux.sspRatio(9.1)).isCloseTo(0.980008d, within(0.0000005));
		assertThat(WestonFlux.sspRatio(10.0)).isCloseTo(0.980008d, within(0.0000005));

		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> WestonFlux.sspRatio(-8.0001d));
	}

	@Test
	void rhoRatio() {
		assertThat(WestonFlux.rhoRatio(-4)).isCloseTo(3.120932d, within(0.0000005));
		assertThat(WestonFlux.rhoRatio(0.9)).isCloseTo(2.166703d, within(0.0000005));
		assertThat(WestonFlux.rhoRatio(1.5)).isCloseTo(1.844616d, within(0.0000005));
		assertThat(WestonFlux.rhoRatio(5.3)).isCloseTo(1.149624d, within(0.0000005));
		assertThat(WestonFlux.rhoRatio(6)).isCloseTo(1.148716d, within(0.0000005));
		assertThat(WestonFlux.rhoRatio(6.5)).isCloseTo(1.148068d, within(0.0000005));
		assertThat(WestonFlux.rhoRatio(7)).isCloseTo(1.147419d, within(0.0000005));
		assertThat(WestonFlux.rhoRatio(1.750718)).isCloseTo(1.720818d, within(0.0000005));
		assertThat(WestonFlux.rhoRatio(9.0)).isCloseTo(1.144824d, within(0.0000005));
		assertThat(WestonFlux.rhoRatio(9.1)).isCloseTo(1.144824d, within(0.0000005));
		assertThat(WestonFlux.rhoRatio(10.0)).isCloseTo(1.144824d, within(0.0000005));

		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> WestonFlux.rhoRatio(-8.0001d));
	}

	@Test
	void beta() {
		assertThat(WestonFlux.beta(-8.0d, 0.9873056d)).isCloseTo(0.678844d, within(0.0000005));
		assertThat(WestonFlux.beta(2.3d, 0.9873056d)).isCloseTo(0.7531221d, within(0.0000005));
		assertThat(WestonFlux.beta(3.7d, 0.9873056d)).isCloseTo(0.9686361d, within(0.0000005));
		assertThat(WestonFlux.beta(5.6d, 0.9873056d)).isCloseTo(0.3162139d, within(0.0000005));
		assertThat(WestonFlux.beta(6, 0.9873056d)).isCloseTo(0.2055106d, within(0.0000005));
		assertThat(WestonFlux.beta(6.5, 0.9860894d)).isCloseTo(0.1627587d, within(0.0000005));
		assertThat(WestonFlux.beta(7, 0.9848732d)).isCloseTo(0.1286963d, within(0.0000005));
		assertThat(WestonFlux.beta(9.0d, 0.9873056d)).isCloseTo(0.0792915d, within(0.0000005));
		assertThat(WestonFlux.beta(9.5d, 0.9873056d)).isCloseTo(0.08841223d, within(0.0000005));
		assertThat(WestonFlux.beta(12.5d, 0.9873056d)).isCloseTo(0.08841223d, within(0.0000005));
		assertThat(WestonFlux.beta(1.750718d, 1.157998d)).isCloseTo(0.8601076d, within(0.0000005));

		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> WestonFlux.beta(-8.0001d, 0.9860894d));
	}
	
	@Test
	void gamma() {
		assertThat(WestonFlux.gamma(FREQUENCY, 10, 34, 8, 0)).isCloseTo(0.002033978d, within(0.0000000005));
		assertThat(WestonFlux.gamma(FREQUENCY, 20, 34, 8, 0)).isCloseTo(0.001423818d, within(0.0000000005));
		assertThat(WestonFlux.gamma(FREQUENCY, 10, 24, 8, 0)).isCloseTo(0.001475261d, within(0.0000000005));
		assertThat(WestonFlux.gamma(FREQUENCY, 10, 34, 8, 40)).isCloseTo(0.002018948d, within(0.0000000005));
		assertThat(WestonFlux.gamma(FREQUENCY, 10, 34, 8, 80)).isCloseTo(0.00200404d, within(0.0000000005));
		assertThat(WestonFlux.gamma(FREQUENCY, 21, 34, 8, 40)).isCloseTo(0.00136598d, within(0.0000000005));
	}

	@Test
	void rangeIndependent() {
		assertThat(WestonFlux.rangeIndependent(800, 40, FREQUENCY, 1700, 0.9860894d, 0.002053996d, 0.9860894d, 1.148068d)).isCloseTo(52.12412d, within(0.000005));
		assertThat(WestonFlux.rangeIndependent(2000, 40, FREQUENCY, 1700, 0.9860894d, 0.002053996d, 0.9860894d, 1.148068d)).isCloseTo(60.48575, within(0.000005));
		assertThat(WestonFlux.rangeIndependent(6000, 40, FREQUENCY, 1700, 0.9860894d, 0.002053996d, 0.9860894d, 1.148068d)).isCloseTo(75.85788d, within(0.000005));
		assertThat(WestonFlux.rangeIndependent(2000, 40, FREQUENCY, 1700, 0.9860894d, 0.002053996d, 1.5860894d, 1.148068d)).isCloseTo(52.31901d, within(0.000005));
		assertThat(WestonFlux.rangeIndependent(2000, 40, FREQUENCY, 1700, 0.9860894, 0.002053996, 1.5860894, 1.148068)).isCloseTo(52.31901d, within(0.000005));
		assertThat(WestonFlux.rangeIndependent(26778.99, 31.978592, FREQUENCY, 1700, 0.8601076d, 0.002033978d, 1.157998d, 1.720818d)).isCloseTo(123.4182d, within(0.00005));
	}

	@Test
	void rangeIndependentNegative() {
		assertThat(WestonFlux.rangeIndependent(0.5d, 0.5d, FREQUENCY, 1700, 0.9860894d, 0.002053996d, 0.9860894d, 1.148068d)).isZero();
	}

	@Test
	void calc() {
		assertThat(WestonFlux.calc(2000, 40, 6.5d, 10.0, 34)).isCloseTo(57.1084d, within(0.000005));
		assertThat(WestonFlux.calc(1600, 50, 1.5d, 18.0, 37)).isCloseTo(54.05893d, within(0.000005));
		assertThat(WestonFlux.calc(800, 110, 8.5d, 11.0, 34)).isCloseTo(53.67857d, within(0.000005));
		assertThat(WestonFlux.calc(6000, 20, 6.5d, 16.0, 37)).isCloseTo(68.75413d, within(0.000005));
		assertThat(WestonFlux.calc(4800, 50, 3.5d, 13.0, 37)).isCloseTo(71.15447d, within(0.000005));
		assertThat(WestonFlux.calc(26778.99d, 31.978592d, 1.750718d, 10.0, 34.0d)).isCloseTo(123.41823d, within(0.000005));
	}

	@Test
	void calcNegative() {
		assertThat(WestonFlux.calc(0.5d, 0.5d, 6.5d, 10.0, 34)).isZero();
	}

}

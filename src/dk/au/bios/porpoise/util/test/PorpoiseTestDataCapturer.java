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

package dk.au.bios.porpoise.util.test;

import java.io.File;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.RandomPorpoiseReportProxy;
import dk.au.bios.porpoise.util.SimulationTime;
import dk.au.bios.porpoise.util.test.CapturedSimulation.SimParams;
import dk.au.bios.porpoise.util.test.CapturedSimulation.SimPorpoise;
import dk.au.bios.porpoise.util.test.CapturedSimulation.SimState;
import dk.au.bios.porpoise.util.test.CapturedSimulation.SimTick;
import repast.simphony.context.Context;
import repast.simphony.parameter.Parameters;

public class PorpoiseTestDataCapturer {

	public final static String EOL = System.getProperty("line.separator");

	public static boolean capture = false;
	public static boolean capturePorpoises = true;
	public static boolean captureTrackedPorpoises = false;
	public static int tickMod = 1000;
	public static long tickStartCapture = 0;
	public static long tickEndCapture = Long.MAX_VALUE;
	public static boolean includePosList = false;
	public static boolean includePopulationSize = true;

	public static File captureFile = new File("testdata_capture" + System.currentTimeMillis() + ".txt");

	private static ObjectMapper jsonMapper;

	public static void capture(Parameters params) {
		if (!capture) {
			return;
		}

		jsonMapper = new ObjectMapper();

		var paramMap = new HashMap<String, Object>();
		params.getSchema().parameterNames().forEach(p -> {
			paramMap.put(p, params.getValue(p));
		});

		SimParams simParams = new SimParams();
		simParams.parameters = paramMap;

		try {
			jsonMapper.writeValue(captureFile, simParams);
		} catch (Exception e) {
			throw new RuntimeException("Error capturing simulation", e);
		}
	}

	public static void capture(RandomPorpoiseReportProxy rp) {
		if (!captureTrackedPorpoises) {
			return;
		}

		capturePorpoise(rp.getPorpoise());
	}

	public static void capture(Porpoise p) {
		if (!capturePorpoises) {
			return;
		}

		capturePorpoise(p);
	}

	private static void capturePorpoise(Porpoise p) {
		if (!shouldCaptureThisTick()) {
			return;
		}
		if (!p.isAlive()) {
			return;
		}

		SimTick simTick = new SimTick();
		simTick.tick = SimulationTime.getTick();
		SimPorpoise simPorp = new SimPorpoise(p, includePosList);
		simTick.porp = simPorp;

		try {
			jsonMapper.writeValue(captureFile, simTick);
		} catch (Exception e) {
			throw new RuntimeException("Error capturing simulation", e);
		}
	}

	public static void captureSimulation(Context<Agent> context) {
		if (!shouldCaptureThisTick()) {
			return;
		}

		if (includePopulationSize) {
			SimTick simTick = new SimTick();
			simTick.tick = SimulationTime.getTick();
			SimState simState = new SimState();
			simState.populationSize = context.getObjectsAsStream(Porpoise.class).count();
			simTick.sim = simState;

			try {
				jsonMapper.writeValue(captureFile, simTick);
			} catch (Exception e) {
				throw new RuntimeException("Error capturing simulation", e);
			}
		}
	}

	private static boolean shouldCaptureThisTick() {
		if (!capture) {
			return false;
		}
		if (SimulationTime.getTick() < tickStartCapture || SimulationTime.getTick() > tickEndCapture) {
			return false;
		}
		if (SimulationTime.getTick() % tickMod != 0) {
			return false;
		}

		return true;
	}

}

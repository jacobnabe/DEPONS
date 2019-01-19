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

package dk.au.bios.porpoise.util.test

import groovy.json.JsonBuilder
import repast.simphony.context.Context
import repast.simphony.parameter.Parameters
import dk.au.bios.porpoise.Agent
import dk.au.bios.porpoise.Porpoise
import dk.au.bios.porpoise.RandomPorpoiseReportProxy
import dk.au.bios.porpoise.util.SimulationTime

class PorpoiseTestDataCapturer {

	public static boolean capture = false;
	public static boolean capturePorpoises = true;
	public static boolean captureTrackedPorpoises = false;
	public static int tickMod = 1000;
	public static long tickStartCapture = 0;
	public static long tickEndCapture = Long.MAX_VALUE;
	public static boolean includePosList = false;
	public static boolean includePopulationSize = true;

	def static EOL = System.getProperty("line.separator")
	def static captureFile = new File("testdata_capture" + System.currentTimeMillis() + ".txt");

	def static capture(Parameters params) {
		if (!capture) {
			return;
		}

		def json = new JsonBuilder()
		def data = json {
			parameters {
				params.getSchema().parameterNames().each {
					"${it}" params.getValue(it)
				}
			}
		}

		captureFile << json.toString() << EOL
	}

	def static capture(RandomPorpoiseReportProxy rp) {
		if (!captureTrackedPorpoises) {
			return;
		}

		capturePorpoise(rp.getPorpoise());
	}

	def static capture(Porpoise p) {
		if (!capturePorpoises) {
			return;
		}

		capturePorpoise(p);
	}

	def private static capturePorpoise(Porpoise p) {
		if (!shouldCaptureThisTick()) {
			return;
		}
		if (!p.alive) {
			return;
		}

		def json = new JsonBuilder()
		if (includePosList) {
			def data = json {
				tick SimulationTime.getTick()
				porp {
					id p.getId()
					x p.getPosition().x
					y p.getPosition().y
					heading p.getHeading()
					prevAngle p.prevAngle
					prevLogMov p.prevLogMov
					dispType p.dispersalBehaviour.dispersalType
					age p.age
					ageOfMaturity p.ageOfMaturity
					pregnancyStatus p.pregnancyStatus
					matingDay p.matingDay
					energyLevel p.energyLevel
					energyLevelSum p.energyLevelSum
					deterStrength p.deterStrength
					VT p.vt
					deterVt p.deterVt
					posList p.posList
				}
			}
		} else {
			def data = json {
				tick SimulationTime.getTick()
				porp {
					id p.getId()
					x p.getPosition().x
					y p.getPosition().y
					heading p.getHeading()
					prevAngle p.prevAngle
					//					prevLogMov p.prevLogMov
					dispType p.dispersalBehaviour.dispersalType
					age p.age
					ageOfMaturity p.ageOfMaturity
					pregnancyStatus p.pregnancyStatus
					matingDay p.matingDay
					energyLevel p.energyLevel
					energyLevelSum p.energyLevelSum
					deterStrength p.deterStrength
					VT p.vt
					deterVt p.deterVt
				}
			}
		}

		captureFile << json.toString() << EOL
	}

	def static captureSimulation(Context<Agent> context) {
		if (!shouldCaptureThisTick()) {
			return;
		}

		if (includePopulationSize) {
			int porpCount = 0;
			context.getObjects(Porpoise.class).each { porpCount++; }

			def json = new JsonBuilder()
			def data = json {
				tick SimulationTime.getTick()
				sim { populationSize porpCount }
			}
			captureFile << json.toString() << EOL
		}
	}

	def private static shouldCaptureThisTick() {
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

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

package dk.au.bios.porpoise.agents.misc;

import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.util.SimulationTime;

/**
 * A special agent used for reporting statistics at the time of death. An
 * instance is created and added to the context at the time of death of a
 * porpoise. The logging is configured to output this to the Reproductive log at
 * every tick. The instances are then removed at the beginning of each tick by
 * the special DeadPorpoisesReportProxyCleanupTask. This is a bit of a special
 * construct, required by the event-based logging requirements, which doesn't
 * fit nicely into the Repast logging framework.
 */
public class DeadPorpoiseReportProxy extends Agent {

	private double tickAtDeath;
	private long id;
	private double ageAtDeath;
	private int calvesBorn;
	private int calvesWeaned;
	private String longDistMovType;

	public DeadPorpoiseReportProxy(Porpoise porp) {
		super(porp.getId());

		this.tickAtDeath = SimulationTime.getTick();
		this.ageAtDeath = porp.getAge();
		this.calvesBorn = porp.getCalvesBorn();
		this.calvesWeaned = porp.getCalvesWeaned();
		this.longDistMovType = porp.getDispersalBehaviour().getDispersalShortName();
	}

	public double getTickAtDeath() {
		return tickAtDeath;
	}

	public long getId() {
		return id;
	}

	public double getAgeAtDeath() {
		return ageAtDeath;
	}

	public int getCalvesBorn() {
		return calvesBorn;
	}

	public int getCalvesWeaned() {
		return calvesWeaned;
	}

	public String getLongDistMovType() {
		return longDistMovType;
	}

}

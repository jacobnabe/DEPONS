package dk.au.bios.porpoise.util.test;

import java.util.HashMap;

import dk.au.bios.porpoise.Porpoise;

public class CapturedSimulation {

	public static class SimParams {
		public HashMap<String, Object> parameters;
	}

	public static class SimTick {
		public double tick;
		public SimState sim;
		public SimPorpoise porp;
	}

	public static class SimState {
		public long populationSize;
	}

	public static class SimPorpoise {
		public long id;
		public double x;
		public double y;
		public double heading;
		public double prevAngle;
		public double prevLogMov;
		public int dispType;
		public double age;
		public double ageOfMaturity;
		public int pregnancyStatus;
		public int matingDay;
		public double energyLevel;
		public double energyLevelSum;
		public double deterStrength;
		public double[] VT;
		public double[] deterVt;
//		public String VT;
//		public String deterVt;
		public String posList;
		
		public SimPorpoise() {
		}

		public SimPorpoise(Porpoise p, boolean includePosList) {
			this.id = p.getId();
			this.x = p.getPosition().getX();
			this.y = p.getPosition().getY();
			this.heading = p.getHeading();
			this.prevAngle = p.getPrevAngle();
			if (includePosList) {
				this.prevLogMov = p.getPrevLogMov();
			}
			this.dispType = p.getDispersalBehaviour().getDispersalType();
			this.age = p.getAge();
			this.ageOfMaturity = p.getAgeOfMaturity();
			this.pregnancyStatus = p.getPregnancyStatus();
			this.matingDay = p.getMatingDay();
			this.energyLevel = p.getEnergyLevel();
			this.energyLevelSum = p.getEnergyLevelSum();
			this.deterStrength = p.getDeterStrength();
			this.VT = p.getVT();
			this.deterVt = p.getDeterVector();
			if (includePosList) {
				this.posList = p.getPosList();
			}
		}
		
	}

}

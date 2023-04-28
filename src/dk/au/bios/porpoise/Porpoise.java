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

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;

import dk.au.bios.porpoise.agents.misc.DeadPorpoiseReportProxy;
import dk.au.bios.porpoise.agents.misc.TrackingDisplayAgent;
import dk.au.bios.porpoise.behavior.Dispersal;
import dk.au.bios.porpoise.behavior.DispersalFactory;
import dk.au.bios.porpoise.behavior.PersistentSpatialMemory;
import dk.au.bios.porpoise.behavior.RefMem;
import dk.au.bios.porpoise.behavior.RefMemTurnCalculator;
import dk.au.bios.porpoise.ships.ShipDeterrence;
import dk.au.bios.porpoise.tasks.YearlyTask;
import dk.au.bios.porpoise.util.CircularBuffer;
import dk.au.bios.porpoise.util.DebugLog;
import dk.au.bios.porpoise.util.PSMVerificationLog;
import dk.au.bios.porpoise.util.ReplayHelper;
import dk.au.bios.porpoise.util.SimulationTime;
import dk.au.bios.porpoise.util.test.PorpoiseTestDataCapturer;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.Dimensions;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.PointTranslator;
import repast.simphony.space.grid.GridPoint;

/**
 * A Porpoise, a primary agent in the simulation.
 */
public class Porpoise extends Agent {

	public static final AtomicLong PORPOISE_ID = new AtomicLong();

	private final Context<Agent> context;

	private double energyConsumedDailyTemp; // The energy spent today by the porpoise - At the end of the day it becomes
	// "energyConsumedDaily"
	private double energyConsumedDaily; // The energy consumed yesterday by the porpoise
	private double foodEatenDailyTemp; // The energy so far today.
	private double foodEatenDaily; // The energy eaten yesterday by the porpoise

	private double age; // Age in years (remember, 360 days per year)
	private final double ageOfMaturity; // Age when becoming receptive, in years
	private byte pregnancyStatus; // 0 (unable to mate, young/low energy); 1 (unable to mate, pregnant); 2 (ready to
	// mate)
	private int matingDay; // Day of year. Most mating occurs in August (Lockyer 2003)
	private int daysSinceMating; // Days since mating. -99 if not pregnant
	private int daysSinceGivingBirth; // Days since giving birth. -99 if not with lactating calf
	private boolean withLactCalf; // true/false, with lactating calf
	private int calvesBorn = 0; // Counter for number of calves born
	private int calvesWeaned = 0; // Counter for number of calves weaned (successfully to completion)
	private double energyLevel; // Porpoises get energy by eating and loose energy by moving.
	private double energyLevelSum; // Sum of energy levels. Reset to 0 every day
	private final CircularBuffer<Double> energyLevelDaily; // List with average energy for last ten days. Latest days
	// first.
	private int dispNumTicks; // The number of ticks the porp has been dispersing for.
	private double prevAngle; // Last turning angle (not the heading!)
	private double presAngle; // Present turning angle
	private double prevLogMov; // Previous Log10 (move length [measured in 100-m steps])
	private double presLogMov; // Present Log10 (move length [measured in 100-m steps])
	private boolean enoughWaterAhead; // Turn to avoid land if false
	private CircularBuffer<NdPoint> posList; // Coordinates of previous positions -- one per 30 min
	private final CircularBuffer<NdPoint> posListDaily; // Coordinates of previous 10 daily positions -- daily,
	// corresponding
	// to energy-level-daily
	private int ignoreDeterrence; // The number of steps to keep ignoring deterrence. While >0 then the deterrence
	// vector is ignored.

	// Remembered feeding success (after memory decay)
	private final CircularBuffer<Double> storedUtilList = new CircularBuffer<Double>(SimulationConstants.MEMORY_MAX);

	private double[] vt = new double[] { 0.0, 0.0 }; // resultant attraction vector, resulting from reference memory of
	// food availability (model >=2)

	private final double[] deterVt = new double[] { 0.0, 0.0 }; // Vector (= list) determining which direction a porp is
	// deterred from wind turbines and ships, and how much
	private double deterStrength; // The strength of the deterrence, is adjusted based on Psi_deter every step while the
	// porpoise is deterred.
	private int deterTimeLeft; // The number of steps remaining while the porpoise is deterred.
	private double veTotal; // Total value of food expected to be found in the future

	private double soundSourceDistance = -1;
	private double soundSourceAngle;
	private double soundSourceImpact = 10.0;
	private final RefMemTurnCalculator refMemTurnCalculator; // TODO Move to global instance

	private final Dispersal dispersalBehaviour;

	/**
	 * Used to adjust the step-length when the porpoise is moved in multiple parts during a tick. Value between 0.0 (no
	 * further movement) and 1.0 (no movement done yet). When the porpoise is moved, the caller is responsible for
	 * adjusting this value.
	 */
	private double tickMoveAdjustMultiplier = 1.0;

	/** Track whether the porpoise is alive. */
	private boolean alive = true;

	private final PersistentSpatialMemory psm; // Always enabled for now.
	private PersistentSpatialMemory calfPsm = null; // If the porpoise is with calf, then this is the PSM it will use.
	private boolean trackVisitedCells = false;
	private boolean writePsmSteps = false;

	private ShipDeterrence shipDeterrence = new ShipDeterrence();
	private double loudestShipSPL = 0.0d;

	/**
	 * Constructor for a newborn porpoised.
	 *
	 * @param parent
	 */
	public Porpoise(final Porpoise parent) {
		this(parent.context, 0, parent.refMemTurnCalculator, parent.getCalfPersistentSpatialMemory());
	}

	/**
	 * Constructor for a porpoise part of the initial population (not born during simulation)
	 *
	 * @param space
	 * @param grid
	 * @param cellData
	 * @param context
	 * @param age
	 * @param hatched
	 * @param refMemTurnCalculator
	 */
	public Porpoise(final Context<Agent> context, final double age, final RefMemTurnCalculator refMemTurnCalculator) {
		this(context, age, refMemTurnCalculator, new PersistentSpatialMemory(Globals.getWorldWidth(),
				Globals.getWorldHeight(), PersistentSpatialMemory.generatedPreferredDistance()));

		if (age > 0) {
			// This is the model setup, there is a probability that the porpoise is with a lactating calf.
			// Notice: The probability is not dependent on the age of the porpoise if it is above the age of 0 .

			this.pregnancyStatus = 2;
			// become pregnanat with prob. taken from Read & Hohn 1995
			if (this.pregnancyStatus == 2
					&& Globals.getRandomSource().nextPregnancyStatusConceive(0, 1) < SimulationParameters
					.getConceiveProb()) {
				this.pregnancyStatus = 1;
				this.daysSinceMating = Globals.getRandomSource().getInitialDaysSinceMating();
			} else {
				this.pregnancyStatus = 0;
			}
		}
	}

	private Porpoise(final Context<Agent> context, final double age, final RefMemTurnCalculator refMemTurnCalculator,
			final PersistentSpatialMemory psm) {
		super(Porpoise.PORPOISE_ID.getAndIncrement());
		this.posList = new CircularBuffer<NdPoint>(SimulationConstants.MEMORY_MAX);
		this.posListDaily = new CircularBuffer<NdPoint>(10);
		for (int i = 0; i < 10; i++) {
			this.posListDaily.add(new NdPoint(0, 0));
		}

		this.energyLevelDaily = new CircularBuffer<Double>(10);
		for (int i = 0; i < 10; i++) {
			this.energyLevelDaily.add(0.0);
		}

		this.refMemTurnCalculator = refMemTurnCalculator;
		this.context = context;
		this.psm = psm;
		this.dispersalBehaviour = DispersalFactory.getPSMDispersal(this);

		// Setup
		this.ageOfMaturity = SimulationParameters.getMaturityAge(); // FIXME This is not really variable per instance
		this.energyLevel = Globals.getRandomSource().nextEnergyNormal();
		this.prevLogMov = 0.8;
		this.prevAngle = 10;
		this.age = age;
	}

	@ScheduledMethod(start = 0, interval = 1, priority = AgentPriority.PORP_MOVE)
	public void move() {
		if (Globals.getRandomReplaySource() != null) {
			final DecimalFormat fmt = new DecimalFormat("0.###");

			final NdPoint p = getPosition();

			final int tick = (int) SimulationTime.getTick();
			System.out.println("pos#" + tick + "#" + fmt.format(p.getX()) + "#" + fmt.format(p.getY()) + "#E"
					+ fmt.format(this.energyLevel) + "#H" + fmt.format(getHeading()) + "#D"
					+ fmt.format(this.deterVt[0]) + ";" + fmt.format(this.deterVt[1]));
			System.out.println("disp#" + tick + "#" + this.dispersalBehaviour.getDispersalType());
			ReplayHelper.print("pos#" + tick + "#" + fmt.format(p.getX()) + "#" + fmt.format(p.getY()) + "#E"
					+ fmt.format(this.energyLevel) + "#H" + fmt.format(getHeading()) + "#D"
					+ fmt.format(this.deterVt[0]) + ";" + fmt.format(this.deterVt[1]));
		}

		if (this.soundSourceDistance != -1) {
			new SoundSource(context, this, this.soundSourceAngle, this.soundSourceDistance, this.soundSourceImpact);
			this.soundSourceDistance = -1;
		}

		this.tickMoveAdjustMultiplier = 1.0;

		// Dispersal step (before actual stdMove())
		if (isAlive()) {
			if (Globals.getCellData().getDepth(ndPointToGridPoint(getPosition())) <= 0) {
				System.out.println("No water : " + Globals.getCellData().getDepth(ndPointToGridPoint(getPosition())));
			}

			// Track the number of ticks dispersed
			if (!this.dispersalBehaviour.isDispersing()) {
				dispNumTicks = 0;
			} else {
				dispNumTicks++;
			}

			dispersalBehaviour.disperse();
		}

		applyShipDeterrence();

		if (isAlive()) {
			// Simple implementation
			if (SimulationParameters.getModel() == 1) {
				final boolean useExpFoodVal = false;
				stdMove(useExpFoodVal);
				// update position list:
				final NdPoint pos = getPosition();
				this.posList.add(pos);
			} else if (SimulationParameters.getModel() == 2) {
				final boolean useExpFoodVal = true;

				// get attracted to places where food was found. Influences direction moved in stdMove() through vector
				// 'VT'
				final double[] temp = this.refMemTurnCalculator.refMemTurn(this, Globals.getCellData(), storedUtilList,
						posList);
				if (temp != null) {
					vt = temp;
				}

				getExpFoodVal(); // determines the tendency to move following CRW behaviour based on foraging success in
				// recent past
				if (!this.dispersalBehaviour.isDispersing()) {
					stdMove(useExpFoodVal); // this is where the porp moves forward
				}
				// update position list:
				final NdPoint pos = getPosition();
				this.posList.add(pos);

				updEnergeticStatus(); // food level increases in 'go' -- affect the landscape and energetic status of
				// the porpoise

			} else if (SimulationParameters.getModel() >= 3) {
				final boolean useExpFoodVal = true;

				// get attracted to places where food was found. Influences direction moved in stdMove() through vector
				// 'VT'
				final double[] temp = this.refMemTurnCalculator.refMemTurn(this, Globals.getCellData(), storedUtilList,
						posList);
				if (temp != null) {
					vt = temp;
				}

				getExpFoodVal(); // determines the tendency to move following CRW behaviour based on foraging success in
				// recent past
				if (!this.dispersalBehaviour.isDispersing()) {
					stdMove(useExpFoodVal); // this is where the porp moves forward and responds to noise by turning
					// away
				}
				// update position list:
				final NdPoint pos = getPosition();
				this.posList.add(pos);

				updEnergeticStatus(); // transform food to energy and spend energy based on step length. Food level in
				// patches increases in 'go'
				// mortality and pregnancy status is set in class DailyTask for models > 4
			}

			PorpoiseTestDataCapturer.capture(this);
		}

		trackCellVisit();
		shipDeterrence.resetSteps();
	}

	/**
	 * Implementation of standard movement.
	 *
	 * @param useExpFoodVal get more attracted to the CRW path if food was found recently
	 */
	private void stdMove(final boolean useExpFoodVal) {
		// int startUtmX = getUtmX(); // PSM Verification
		// int startUtmY = getUtmY(); // PSM Verification
		// double startHeading = getHeading(); // PSM Verification

		final double prevMov = Math.pow(10, this.prevLogMov); // Consider saving prevMov instead of calculating.
		final double presHeading = getHeading();
		final NdPoint presPosition = getPosition();

		this.presAngle = 999;
		int j = 1;

		while (Math.abs(this.presAngle) > 180) {
			final double ran = Globals.getRandomSource().nextCrwAngle();
			ReplayHelper.print("normal-0-38:{0}", ran);

			final double presAngleBase = SimulationParameters.getCorrAngleBase() * this.prevAngle;
			final double presAngleBathy = SimulationParameters.getCorrAngleBathy()
					* Globals.getCellData().getDepth(presPosition);
			final double presAngleSalinity = SimulationParameters.getCorrAngleSalinity()
					* Globals.getCellData().getSalinity(presPosition);

			final double angleTmp = presAngleBase + ran;
			// Autoreg can't be used for estimating parameter as estimated turns are changed if on shallow water.
			this.presAngle = angleTmp * (presAngleBathy + presAngleSalinity + SimulationParameters.getCorrAngleBaseSD());

			j++;
			if (j == 200) {
				this.presAngle = this.presAngle * 90 / Math.abs(this.presAngle);

				if (DebugLog.isEnabledFor(3)) {
					DebugLog.print3("exiting loop 1, ang={}", this.presAngle);
				}
			}
		}

		ReplayHelper.print("std-move-1 pres-angle:{0} pres-heading: {1} prev-angle: {2} heading:{3}", this.presAngle,
				presHeading, this.prevAngle, getHeading());
		ReplayHelper.print("std-move-1 deter-vt:[{0} {1}]", this.deterVt[0], this.deterVt[1]);
		final double sign = this.presAngle < 0 ? -1.0 : 1.0;

		this.presAngle = Math.abs(this.presAngle);
		// Make angle decrease linearly with mov-dist
		boolean goOn = true;

		j = 1;
		double rnd = 0;

		while (goOn) {
			rnd = Globals.getRandomSource().nextCrwAngleWithM(); // draws the number to be added to presAngle
			ReplayHelper.print("normal-96-28:{0}", rnd);
			if (prevMov <= SimulationParameters.getM()) {
				this.presAngle = this.presAngle + rnd - (rnd * prevMov / SimulationParameters.getM());
			}

			// remember that turning angle is unsigned here
			goOn = this.presAngle >= 180; // Continue while presAngle is 180 and above

			j++;
			if (j == 200) {
				presAngle = Globals.getRandomSource().nextStdMove(0, 20) + 90;
				goOn = false;

				if (DebugLog.isEnabledFor(3)) {
					DebugLog.print3("exiting loop 2, ang={}", presAngle);
				}
			}
		}

		ReplayHelper.print("std-move-2 pres-angle:{0} pres-heading: {1} prev-angle: {2} heading:{3}", this.presAngle,
				presHeading, this.prevAngle, getHeading());
		this.presAngle = this.presAngle * sign;

		final double angleBeforeAvoidLand = this.presAngle; // for printing later using debug 2
		incHeading(this.presAngle);

		double angleTurnedRight = this.presAngle; // for updating prevAngle at end of stdMove()
		this.presAngle = 0;
		ReplayHelper.print(
				"std-move-3 pres-angle:{0} pres-heading: {1} prev-angle: {2} heading:{3} angle-turned-right: {4}",
				presAngle, presHeading, this.prevAngle, getHeading(), angleTurnedRight);

		this.presLogMov = 999;
		// double porpMaxDist = Globals.MAX_MOV;

		j = 1;

		double presLogMovMin = Double.MAX_VALUE;
		double presLogMovMax = Double.MIN_VALUE;
		while (this.presLogMov > SimulationParameters.getMaxMov()) {
			final double ran = Globals.getRandomSource().nextCrwStepLength();
			ReplayHelper.print("normal-042-048:{0}", ran);

			final double presLogMovLength = SimulationParameters.getCorrLogmovLength() * this.prevLogMov;
			final double presLogMovBathy = SimulationParameters.getCorrLogmovBathy()
					* Globals.getCellData().getDepth(presPosition);
			final double presLogMovSalinity = SimulationParameters.getCorrLogmovSalinity()
					* Globals.getCellData().getSalinity(presPosition);
			this.presLogMov = presLogMovLength + presLogMovBathy + presLogMovSalinity + ran;

			/*
			 * if (this.getId() == 0 && SimulationTime.getTick() % 500 == 0) {
			 * System.err.printf("#%f    1: %f, 2: %f, 3: %f, ran: %f, =: %f\n", SimulationTime.getTick(),
			 * presLogMovLength, presLogMovBathy, presLogMovSalinity, ran, presLogMov); }
			 */

			if (presLogMov < presLogMovMin) {
				presLogMovMin = presLogMov;
			}
			if (presLogMov > presLogMovMax) {
				presLogMovMax = presLogMov;
			}
			// this.presLogMov = presLogMovLength + presLogMovBathy + ran;
			// this.presLogMov = presLogMovLength + ran;

			j++;
			if (j == 200) { // this has no effect, it will not stop the loop as prevLogMov is not changed.
				if (presAngle == 0) { // presAngle will always be 0 here.
					presAngle += 0.00001;
				}

				this.presAngle = this.presAngle * 90 / Math.abs(presAngle);

				if (DebugLog.isEnabledFor(3)) {
					DebugLog.print3("exiting loop 3, ang={}", presAngle);
					DebugLog.print3("Porpoise {} did not find presLogMov < dmax_mov ({})."
							+ " Found values between {} and {}. Using dmax_mov.", this.getId(),
							SimulationParameters.getMaxMov(), presLogMovMin, presLogMovMax);
				}
				this.presLogMov = SimulationParameters.getMaxMov();
			}
		}

		double presMov = Math.pow(10, this.presLogMov);

		// Turn to avoid swimming on land if necessary:
		this.enoughWaterAhead = false;

		int countI = 0;

		while (!this.enoughWaterAhead) {
			ReplayHelper.print("std-move-4 pres-angle:{0} pres-heading: {1} prev-angle: {2} heading:{3}"
					+ " enoughW: {4} angle-turned-right: {5}", presAngle, presHeading, this.prevAngle, getHeading(),
					enoughWaterAhead, angleTurnedRight);
			checkDepth();
			ReplayHelper.print("std-move-5 enough-water-ahead {0}", enoughWaterAhead);

			if (!this.enoughWaterAhead) {
				avoidLand();
			}
			presMov = Math.pow(10, this.presLogMov); // because presLogMov may have changed in avoidLand()

			incHeading(presAngle); // angle to turn -- presAngle -- is changed in avoidLand()

			angleTurnedRight += presAngle;

			if (angleTurnedRight > 180) {
				angleTurnedRight -= 360;
			}
			if (angleTurnedRight < -180) {
				angleTurnedRight += 360;
			}

			presAngle = 0;
			countI++;

			if (countI == 100) {
				this.enoughWaterAhead = true;

				DebugLog.print1("caught water-ahead loop");
			}
		}

		// test depth again, avoid-beh = 5:
		checkDepth();

		if (!this.enoughWaterAhead) {
			GridPoint max = null;
			for (final GridPoint g : getNeighbors()) {
				if (max == null) {
					max = g;
				} else if (Globals.getCellData().getDepth(max) < Globals.getCellData().getDepth(g)) {
					max = g;
				}
			}

			facePoint(new NdPoint(max.getX(), max.getY()));

			angleTurnedRight += presAngle;
			if (angleTurnedRight > 180) {
				angleTurnedRight -= 360;
			}
			if (angleTurnedRight < -180) {
				angleTurnedRight += 360;
			}

			presMov = 1;

			if (DebugLog.isEnabledFor(1)) {
				DebugLog.print1("beh =  5 ; tck {}", SimulationTime.getTick());
			}
		}

		// Change direction if attracted / deterred by certain areas (model >= 2)
		double totalDX = 0;
		double totalDY = 0;
		NdPoint pos = getPosition();

		if (!useExpFoodVal) {
			totalDX = getDx() * presMov + this.vt[0]; // VT isn't used in stdMove() till here
			totalDY = getDy() * presMov + this.vt[1]; // note that dx is change in x if taking ONE step forward

			facePoint(new NdPoint(pos.getX() + totalDX, pos.getY() + totalDY)); // really not needed, it already points
			// that way
		} else if (SimulationParameters.getModel() < 3) {
			// length of vector pointing in direction predicted by CRW (VE-total and pres-mov are porp variables)
			// Used to use Globals.CRW_CONTRIB instead of this.crwContrib
			final double crwContrib = SimulationParameters.getInertiaConst() + presMov * this.veTotal;
			totalDX = getDx() * crwContrib + this.vt[0];
			totalDY = getDy() * crwContrib + this.vt[1];

			facePoint(new NdPoint(pos.getX() + totalDX, pos.getY() + totalDY));
		} else if (SimulationParameters.getModel() >= 3) {
			// Used to use Globals.CRW_CONTRIB instead of this.crwContrib
			final double crwContrib = SimulationParameters.getInertiaConst() + presMov * this.veTotal;
			ReplayHelper
			.print("std-move-4 pres-angle:{0} presheading: {1} prev-angle: {2} heading:{3} crwcontrib: {4} VT: [{5} {6}]",
					presAngle, presHeading, this.prevAngle, getHeading(), crwContrib, this.vt[0], this.vt[1]);

			// deterrence behaviour -- get scared away from ships and wind turbines
			checkDeterrence();

			if (this.ignoreDeterrence <= 0) {
				totalDX = getDx() * crwContrib + this.vt[0] + this.deterVt[0];
				totalDY = getDy() * crwContrib + this.vt[1] + this.deterVt[1];
			} else {
				// We are ignoring deterrence, don't apply
				totalDX = getDx() * crwContrib + this.vt[0];
				totalDY = getDy() * crwContrib + this.vt[1];

				this.ignoreDeterrence--;
			}
			ReplayHelper.print("facexy:{0}:{1}", (pos.getX() + totalDX), (pos.getY() + totalDY));
			facePoint(new NdPoint(pos.getX() + totalDX, pos.getY() + totalDY));
			ReplayHelper.print("heading after facexy:{0}", getHeading());
		}

		// Store turn for calc of turning angle in next step:

		// total change in heading, including all adjustments till here. 'presHeading' was calc in beginning of
		// stdMove()
		final double totalTurn = substractHeadings(getHeading(), presHeading);

		// Move:
		// In the population model all movement lengths are still calculated in 100 m steps, but the cell size has
		// increased from 100 m to 400 m
		// The step should therefore be divided by 4.
		final double moveDistance = tickMoveAdjustMultiplier * (presMov / 4.0); // movement length isn't affected by
		// presence
		// of food
		tickMoveAdjustMultiplier = 0.0; // using up all the distance allowed left
		ReplayHelper.print("std-move-4 total-turn:{0} pres-mov:{1}", totalTurn, prevMov);
		ReplayHelper.print("std-move-4 posA:{0} heading:{1}", pos, getHeading());
		ReplayHelper.print("std-move-4 forward{0}:", moveDistance);
		forward(moveDistance);
		// REMOVED - PSM distance tracking changed to only track distance in dispersal, excluding standard move
		// psm.addDistanceTravelled(moveDistance);
		ReplayHelper.print("std-move-4 posB:{0} heading:{1}", getPosition(), getHeading());

		if (DebugLog.isEnabledFor(2)) {
			if (SimulationTime.getTick() == 0) {
				// I assume we need to get the pos after the move here
				pos = getPosition();
				DebugLog.print("dist angle-before-avoid-land angle-turned-right x y");

				final StringBuilder sb = new StringBuilder();
				sb.append(Math.round(Math.pow(10, prevLogMov) * 100) / 100); // THIS IS IMPORTANT -- the porp turns
				// before it moves, so turning angle is
				// affected by previous moving dist
				sb.append(" ");
				sb.append(angleBeforeAvoidLand);
				sb.append(" ");
				sb.append(angleTurnedRight);
				sb.append(" ");
				sb.append(pos.getX() * 100 + Globals.getXllCorner());
				sb.append(" ");
				sb.append(pos.getY() * 100 + Globals.getYllCorner());
				DebugLog.print(sb.toString());
			}
		} else if (DebugLog.isEnabledFor(5)) {
			pos = getPosition();
			DebugLog.print("CRW-contrib: [{},{}]", pos.getX()
					* (SimulationParameters.getInertiaConst() + presMov * this.veTotal), pos.getY()
					* (SimulationParameters.getInertiaConst() + presMov * this.veTotal));
			// Not sure how the list function in NETLOGO works..
			DebugLog.print("MR-Contrib: [{},{}]", this.vt[0], this.vt[1]);
			DebugLog.print("dx, dy (after): [{},{}] ", totalDX, totalDY);
			DebugLog.print("heading (after): {}", getHeading());
			DebugLog.print("total-turn: {}", getHeading()); // ??!
		}

		// Remember current moves for the next iteration
		// if attraction to food alters the movement angle (i.e. VT != 0), this isn't remembered for next step
		this.prevAngle = totalTurn; // so the additional turn due to attraction to food DOES influence turning angle in
		// next step
		this.prevLogMov = Math.log10(presMov); // total steplength, resulting from VT + presMov

		// test depth one last time, avoid-beh = 6 - move back on same track:
		if (!(getDepth() > 0)) {
			if (posList.size() > 1) {
				facePoint(this.posList.get(1));
			}

			angleTurnedRight += presAngle; // Is this right?

			if (angleTurnedRight > 180) {
				angleTurnedRight -= 360;
			} else if (angleTurnedRight < -180) {
				angleTurnedRight += 360;
			}

			// move 100 m towards deeper patch
			if (posList.size() > 1) {
				setTurtlePosition(this.posList.get(1));
			}

			if (DebugLog.isEnabledFor(1)) {
				DebugLog.print1("beh =  6; tck {} ; {} degr.", SimulationTime.getTick(), angleTurnedRight);
			}
		}

		if (writePsmSteps) {
			PSMVerificationLog.print("STDMOVE", this, moveDistance);
		}
	}

	private void trackCellVisit() {
		if (trackVisitedCells && isAlive()) {
			final TrackingDisplayAgent tda = (TrackingDisplayAgent) context.getObjects(TrackingDisplayAgent.class).get(
					0);
			final GridPoint gp = this.getGrid().getLocation(this);
			int visitType;
			if (this.dispersalBehaviour.isDispersing()) {
				visitType = 2;
			} else {
				visitType = 1;
			}
			tda.visited(gp.getX(), gp.getY(), visitType);
		}
	}

	/**
	 * Calculate the expected value (VE-total) of the food to be found in the future based on food found in recent
	 * positions x the working memory Uses the values of the patches in "storedUtilList", calculated in
	 * RefMemTurnCalculator.
	 */
	private void getExpFoodVal() {
		int ii = 1;

		this.veTotal = 0;

		final int maxI = Math.min(RefMem.getWorkMemSize(), this.storedUtilList.size());
		while (ii < maxI) {
			this.veTotal += RefMem.getWorkMemStrenth(ii - 1) * this.storedUtilList.get(ii - 1);
			ii++;
		}

		if (DebugLog.isEnabledFor(5)) {
			DebugLog.print5("");
			DebugLog.print5("stored-util-list: {}", this.storedUtilList);
			DebugLog.print5("work-mem-strength-list: {}", RefMem.getWorkMemAsString());
			DebugLog.print5("VE-total for porp {}:{}", this.getId(), this.veTotal);
		}
	}

	/**
	 * 1. Reduce food in the patch that the porp just has left. The amount eaten decreases linearly as the porp's energy
	 * level increases from 10 to 20 (=max e) this does not affect the porpoise's perception of the quality of the area,
	 * and therefore the movement is unaffected.
	 *
	 * 2. Adjust porpoise energy level based on amount of food found and time spent per half hour Increase food level in
	 * cells with food-level > 0 AFTERWARDS in order to calc. stored-util-list correctly.
	 */
	private void updEnergeticStatus() {
		double foodEaten = 0;
		double fractOfFoodToEat = 0;

		if (this.energyLevel < 20) {
			fractOfFoodToEat = (20.0 - energyLevel) / 10.0;
		}
		if (fractOfFoodToEat > 0.99) {
			fractOfFoodToEat = 0.99;
		}

		foodEaten += Globals.getCellData().eatFood(ndPointToGridPoint(this.posList.get(1)), fractOfFoodToEat);

		this.foodEatenDailyTemp += foodEaten;
		ReplayHelper.print("energy before eat food {0} eaten {1}", energyLevel, foodEaten);
		psm.updateMemory(getPosition(), foodEaten);
		if (calfPsm != null && this.getDispersalBehaviour().calfHasPSM()) {
			calfPsm.updateMemory(getPosition(), foodEaten);
		}
		this.energyLevel += foodEaten;

		// Scale e-use depending on season and lactation
		double scalingFactor = 1;

		// Animals have approximately 30% lower energy consumption when the water is cold, Nov-Mar, and approx.
		// 15% lower energy consumption in Oct+Apr (Lockyer et al 2003. Monitoring growth and energy utilization
		// of the harbour porpoise (Phocoena phocoena) in human care. Harbour porpoises in the North Atlantic
		// 5:143-175.)
		if (SimulationTime.getMonthOfYearWithOffset() == 4 || SimulationTime.getMonthOfYearWithOffset() == 10) {
			scalingFactor = 1.15;
		} else if (SimulationTime.getMonthOfYearWithOffset() > 4 && SimulationTime.getMonthOfYearWithOffset() < 10) {
			scalingFactor = SimulationParameters.getEWarm();
		}

		// Food consumption increases approx 40% when lactating, there is apparently no effect of pregnancy. (Magnus
		// Wahlberg <magnus@fjord-baelt.dk>, unpubl. data)
		if (this.withLactCalf) {
			scalingFactor *= SimulationParameters.getELact();
		}

		// Probability of dying increases with decreasing energy level
		final double yearlySurvProb = 1 - (SimulationConstants.M_MORT_PROB_CONST * Math.exp(-this.energyLevel
				* SimulationParameters.getXSurvivalProbConst()));
		double stepSurvProb = 0;

		if (this.energyLevel > 0) {
			stepSurvProb = Math.exp(Math.log(yearlySurvProb) / (360 * 48));
		}

		final double ran = Globals.getRandomSource().nextEnergeticUpdate(0, 1);
		ReplayHelper.print("porp-upd-energetic-status:{0}", ran);
		if (ran > stepSurvProb) {
			if (!this.withLactCalf || this.energyLevel <= 0) {
				Globals.getListOfDeadAge().addLast((int) this.age);
				Globals.getListOfDeadDay().addLast(SimulationTime.getDayOfSimulation());
				die(CauseOfDeath.Starvation);
			}
			// Better abandoning calf than dying
			if (this.withLactCalf) {
				this.withLactCalf = false;
				this.calfPsm = null;
			}
		}

		final double consumed = (0.001 * scalingFactor * SimulationParameters.getEUsePer30Min() + (Math.pow(10,
				this.prevLogMov) * 0.001 * scalingFactor * SimulationConstants.E_USE_PER_KM / 0.4));
		ReplayHelper.print("energy before consume food {0} consumed  {1} prev-logmov {2} scaling-factor {3}"
				+ " month {4} with-lact-calf {5}", energyLevel, consumed, prevLogMov, scalingFactor,
				SimulationTime.getMonthOfYearWithOffset(), withLactCalf);
		consumeEnergy(consumed);

		this.energyLevelSum += this.energyLevel;
	}

	@Override
	public String toString() {
		final DecimalFormat df = new DecimalFormat("#.00");
		return "[" + df.format(getHeading()) + ":" + df.format(this.energyLevel) + "]";
	}

	private void checkDepth() {
		// Check that there is enough water at all steplengths ahead, set enough-water-ahead to false if < min-depth
		this.enoughWaterAhead = true;

		final double presMov = Math.pow(10, this.presLogMov);
		final double dd = Math.ceil(presMov / 0.1);

		final GridPoint pointAhead = Agent.ndPointToGridPoint(getPointAhead(presMov));

		if (Globals.getCellData().getDepth(pointAhead) < 0) {
			// Globals.replayPrint("porp-check-depth enough-water-ahead false . depth-list " depth-list " depth-path "
			// depth-path);
			ReplayHelper.print("porp-check-depth enough-water-ahead false . depth-list [NA NA] depth-path [NA]");
			this.enoughWaterAhead = false;
			return;
		}

		final double[] vector = getVector(0.1);

		final GridPoint[] points = new GridPoint[(int) dd + 1];

		// Globals.replayPrint("porp-check-depth depth-list " depth-list " patch ahead " patch-ahead pres-mov " bath " [
		// bathymetry ] of patch-ahead pres-mov);
		ReplayHelper.print("porp-check-depth depth-list [NA NA] patch ahead (patch NA NA) bath NA");

		for (int i = 0; i < dd; i++) {
			final GridPoint p = Agent.ndPointToGridPoint(getPointFromVector(vector, i + 1));

			points[i] = p;

			if (!isPointGood(p, 0, false)) {
				// Globals.replayPrint("porp-check-depth enough-water-ahead false . depth-list " depth-list
				// " depth-path " depth-path);
				ReplayHelper.print("porp-check-depth enough-water-ahead false . depth-list [NA NA] depth-path [NA]");
				this.enoughWaterAhead = false;
				break;
			}
		}
	}

	public NdPoint getPointAhead(final double dist) {
		return getPointAhead(dist, 0);
	}

	public NdPoint getPointAhead(final double dist, final double angleOffset) {
		return getPointAtHeadingAndDist(angleOffset + getHeading(), dist);
	}

	public NdPoint getPointAtHeadingAndDist(final double heading, final double distance) {
		final double headingNormalized = normHeading(heading);

		final double[] anglesInRadians = { (Math.PI / 2) - getHeadingInRads(headingNormalized), 0.0 };
		final double[] displacement = SpatialMath.getDisplacement(2, 0, distance, anglesInRadians);

		final double[] movedCoords = new double[2];
		getPosition().toDoubleArray(movedCoords);
		final PointTranslator trans = this.getSpace().getPointTranslator();
		trans.translate(movedCoords, displacement);

		return new NdPoint(movedCoords);
	}

	public NdPoint getPointAtHeadingAndDistNoBorder(final double heading, final double distance) {
		final double headingNormalized = normHeading(heading);

		final double[] anglesInRadians = { (Math.PI / 2) - getHeadingInRads(headingNormalized), 0.0 };
		final double[] displacement = SpatialMath.getDisplacement(2, 0, distance, anglesInRadians);

		final double[] movedCoords = new double[2];
		getPosition().toDoubleArray(movedCoords);
		movedCoords[0] += displacement[0];
		movedCoords[1] += displacement[1];

		return new NdPoint(movedCoords);
	}

	private double[] getVector(final double distance) {
		final double[] anglesInRadians = { (Math.PI / 2) - getHeadingInRads(), 0.0 };
		return SpatialMath.getDisplacement(2, 0, distance, anglesInRadians);
	}

	private NdPoint getPointFromVector(final double[] vector, final int scale) {
		final double[] movedCoords = new double[2];
		getPosition().toDoubleArray(movedCoords);
		final PointTranslator trans = this.getSpace().getPointTranslator();
		trans.translate(movedCoords, vector[0] * scale, vector[1] * scale);

		return new NdPoint(movedCoords);
	}

	private void avoidLandTurn(final GridPoint r, final GridPoint l, final double randAng, final int degrees) {
		if (isPointGood(r) && isPointGood(l)) {
			final double bathR = Globals.getCellData().getDepth(r);
			final double bathL = Globals.getCellData().getDepth(l);

			// comparison can be true only if neither bath-r or bath-l are NaN, i.e. if both are > min-depth
			// both points are good, pick the deepest
			if (bathR >= bathL) {
				this.presAngle += degrees + randAng;
			} else {
				this.presAngle -= degrees + randAng;
			}
		} else {
			if (isPointGood(r)) {
				this.presAngle += degrees + randAng;
			} else {
				this.presAngle -= degrees + randAng;
			}
		}
	}

	private boolean isPointGood(final GridPoint p, final double minDepth, final boolean allowEqual) {
		if (allowEqual) {
			return Globals.getCellData().getDepth(p) >= minDepth;
		} else {
			return Globals.getCellData().getDepth(p) > minDepth;
		}
	}

	private boolean isPointGood(final GridPoint p) {
		return isPointGood(p, SimulationParameters.getMinDepth(), true);
	}

	private void avoidLand() {
		/*
		 * If shallow water ahead, turn right or left depending on where water is deeper. Turn as little as possible.
		 * Don't do the turning here, but change angle to be turned in stdMove(). Note that the emergency procedure
		 * "avoid-beh 5" is found in stdMove().
		 */
		final double randAng = Globals.getRandomSource().nextAvoidLand(0, 10);
		ReplayHelper.print("porp-avoid-land:{0}", randAng);
		final NdPoint pos = getPosition();
		ReplayHelper.print("porp-avoid-land#0 pres angle :{0} x:{1} y:{2}", presAngle, pos.getX(), pos.getY());
		int avoidBeh = 0;
		final double presMov = Math.pow(10, this.presLogMov); // ?!

		GridPoint leftPoint = ndPointToGridPoint(getPointLeftAndAhead(40 + randAng, presMov));
		GridPoint rightPoint = ndPointToGridPoint(getPointRightAndAhead(40 + randAng, presMov));

		// alternative kinds of evasive behaviour:
		if (isPointGood(rightPoint) || isPointGood(leftPoint)) {
			avoidBeh = 1; // evasive behaviour type 1

			avoidLandTurn(rightPoint, leftPoint, randAng, 40);
		} else {
			// try turning more aprubtly ( = 70 deg )
			rightPoint = ndPointToGridPoint(getPointRightAndAhead(70 + randAng, presMov));
			leftPoint = ndPointToGridPoint(getPointLeftAndAhead(70 + randAng, presMov));

			if (isPointGood(rightPoint) || isPointGood(leftPoint)) {
				avoidBeh = 2; // evasive behaviour type 2

				if (isPointGood(rightPoint) || isPointGood(leftPoint)) {
					avoidLandTurn(rightPoint, leftPoint, randAng, 70);
				}
			} else {
				rightPoint = ndPointToGridPoint(getPointRightAndAhead(120 + randAng, presMov));
				leftPoint = ndPointToGridPoint(getPointLeftAndAhead(120 + randAng, presMov));

				if (isPointGood(rightPoint) || isPointGood(leftPoint)) {
					avoidBeh = 3;

					if (isPointGood(rightPoint) || isPointGood(leftPoint)) {
						avoidLandTurn(rightPoint, leftPoint, randAng, 120);
					}
				} else {
					// if everything else fails, turn around
					avoidBeh = 4;

					int j = 0;
					checkDepth();
					while (!this.enoughWaterAhead && j < this.posList.size()) {
						final NdPoint prevPoint = this.posList.get(j);
						this.facePoint(prevPoint);
						this.setPosition(prevPoint);
						j++;
						checkDepth();

						if (j == 20) {
							this.enoughWaterAhead = true;
						}
					}
				}
			}
		}

		if (DebugLog.isEnabledFor(1)) {
			DebugLog.print1("beh = {}; tck = {}; {} degr.", avoidBeh, SimulationTime.getTick(),
					Math.round(this.presAngle));
		}
	}

	/**
	 * Computes the difference between the given headings, that is, the number of degrees in the smallest angle by which
	 * heading2 could be rotated to produce heading1. A positive answer means a clockwise rotation, a negative answer
	 * counterclockwise. The result is always in the range -180 to 180, but is never exactly -180. Note that simply
	 * subtracting the two headings using the - (minus) operator wouldn't work. Just subtracting corresponds to always
	 * rotating clockwise from heading2 to heading1; but sometimes the counterclockwise rotation is shorter. For
	 * example, the difference between 5 degrees and 355 degrees is 10 degrees, not -350 degrees.
	 *
	 * @param heading1 The first heading.
	 * @param heading2 The second heading.
	 * @return The difference in the headings.
	 */
	private double substractHeadings(final double heading1, final double heading2) {
		final double diff = heading1 - heading2;

		if (diff <= -180) {
			// It is shorter to turn right
			return diff + 360;
		} else if (diff > 180) {
			return diff - 360;
		} else {
			return diff;
		}
	}

	private NdPoint getPointLeftAndAhead(final double angle, final double distance) {
		return getPointAhead(distance, -angle);
	}

	private NdPoint getPointRightAndAhead(final double angle, final double distance) {
		return getPointAhead(distance, angle);
	}

	private double getDepth() {
		return Globals.getCellData().getDepth(Agent.ndPointToGridPoint(this.getPosition()));
	}

	private void setTurtlePosition(final NdPoint pos) {
		this.setPosition(pos);
	}

	// Reports the x-increment or y-increment (the amount by which the turtle's xcor or ycor would change) if the
	// turtle were to take one step forward in its current heading.
	private double getDx() {
		return Math.sin(getHeadingInRads());
	}

	// Reports the x-increment or y-increment (the amount by which the turtle's xcor or ycor would change) if the
	// turtle were to take one step forward in its current heading.
	private double getDy() {
		return Math.cos(getHeadingInRads());
	}

	private void die(final CauseOfDeath cause) {
		this.alive = false;
		context.remove(this);
		Globals.getMonthlyStats().addDeath(cause);
		YearlyTask.recordDeath((int) Math.floor(this.getAge()));
		
		DeadPorpoiseReportProxy reportProxy = new DeadPorpoiseReportProxy(this);
		context.add(reportProxy);
	}

	public boolean isAlive() {
		return alive;
	}

	// TODO: this should be an initialise function for the entire porpoise object
	public void moveAwayFromLand() {
		final Dimensions dim = this.getSpace().getDimensions();

		// Only the space is initialized at this time..
		while (Globals.getCellData().getDepth(ndPointToGridPoint(getPosition())) <= 0) {
			NdPoint newPos = new NdPoint(RandomHelper.nextDoubleFromTo(0, dim.getWidth() - dim.getOrigin(0)),
					RandomHelper.nextDoubleFromTo(0, dim.getHeight() - dim.getOrigin(1)));
			this.setPosition(newPos);
		}

		// Initialize the posList
		this.posList.add(getPosition());
	}

	/**
	 * The tasks to perform daily. This is called from the DailyTask class. Consider making this a @ScheduledMethod and
	 * removing the DailyTask class.
	 */
	public void performDailyStep() {
		ReplayHelper.print("perform-daily-step");

		this.foodEatenDaily = this.foodEatenDailyTemp;
		this.foodEatenDailyTemp = 0;

		this.energyConsumedDaily = this.energyConsumedDailyTemp;
		this.energyConsumedDailyTemp = 0;

		this.age += 1.0 / 360; // TODO: We can avoid this if we record the born tick.

		final double eMean = this.energyLevelSum / 48.0;
		this.energyLevelDaily.add(Math.round(eMean * 1000.0) / 1000.0);

		this.posListDaily.add(getPosition());

		if (SimulationParameters.getModel() >= 3 && !DispersalFactory.isOff()) {
			if (!this.dispersalBehaviour.isDispersing()) {
				ReplayHelper.print("daily-step energy-level-daily:{0}", this.energyLevelDaily);
				boolean decreasingEnergy = true;
				for (int i = 0; i < SimulationParameters.getTDisp(); i++) {
					if (this.energyLevelDaily.get(i) >= this.energyLevelDaily.get(i + 1)) {
						decreasingEnergy = false;
						break;
					}
				}
				if (decreasingEnergy) {
					// decreasing energy for three days
					this.dispersalBehaviour.activate();
				}
			}

			// Energy level higher than any of the previous seven days, stop dispersing;
			if (this.dispersalBehaviour.isDispersing()) {
				double min = this.energyLevelDaily.get(1);
				for (int i = 2; i < 8; i++) {
					min = Math.min(min, this.energyLevelDaily.get(i));
				}

				if (this.energyLevelDaily.get(0) > min) {
					this.dispersalBehaviour.deactivate();

					if (DebugLog.isEnabledFor(7) && (this.getId() == 0 || this.getId() == 1)) {
						DebugLog.print("Food found, stop disp., (porp {})", this.getId());
					}
				}
			}
		} // End model >= 3 tasks.

		this.energyLevelSum = 0; // reset daily

		if (SimulationParameters.getModel() >= 4 && SimulationConstants.MORTALITY_ENABLED) {
			if (updMortality()) {
				updPregnancyStatus();
			}
		}
	}

	/**
	 * Updates the mortality state of the porpoise. If the porpoise is still alive true is returned. Otherwise false is
	 * returned.
	 *
	 * @return Whether not the porpoise is still alive.
	 */
	private boolean updMortality() {
		// Ok that only divided by 360, called once per day
		final double dailySurvivalProb = Math.exp(Math.log(1 - SimulationParameters.getBycatchProb()) / 360);

		final double ran = Globals.getRandomSource().nextMortality(0, 1);
		ReplayHelper.print("porp-upd-mortality:{0}", ran);
		// Introducing maximum age and Mortality due to by-catch
		if (ran > dailySurvivalProb || this.age > SimulationParameters.getMaxAge()) {
			Globals.getListOfDeadAge().addLast((int) this.age);
			Globals.getListOfDeadDay().addLast(SimulationTime.getDayOfSimulation());
			die(this.age > SimulationParameters.getMaxAge() ? CauseOfDeath.OldAge : CauseOfDeath.ByCatch);
			return false;
		} else {
			return true;
		}

	}

	private void updPregnancyStatus() {
		// 0 (unable to mate, young/low energy); 1 (unable to mate, pregnant); 2 (ready to mate)
		// Become ready to mate:
		if (this.pregnancyStatus == 0 && this.age >= this.ageOfMaturity) {
			this.pregnancyStatus = 2;
		}

		// Mate:
		if (this.pregnancyStatus == 2 && SimulationTime.getDayOfYear() == this.matingDay) {
			// become pregnanat with prob. taken from Read & Hohn 1995
			if (Globals.getRandomSource().nextPregnancyStatusConceive(0, 1) < SimulationParameters.getConceiveProb()) {
				this.pregnancyStatus = 1;
				if (DebugLog.isEnabledFor(9)) {
					DebugLog.print9("{} pregnant", this.getId());
				}
				this.daysSinceMating = 0;
			}
		}

		// Give birth:
		// give birth. Gestation time = approx 10 mo (Lockyer 2003)
		if (this.pregnancyStatus == 1 && this.daysSinceMating == SimulationParameters.getGestationTime()) {
			this.pregnancyStatus = 2; // so it is ready to mate even though it has a very young calf
			this.withLactCalf = true;
			this.calvesBorn++;
			double calfPsmPrefDistance;
			if (this.getDispersalBehaviour().calfInheritsPsmDist()) {
				calfPsmPrefDistance = this.getPersistentSpatialMemory().getPreferredDistance();
			} else {
				calfPsmPrefDistance = PersistentSpatialMemory.generatedPreferredDistance();
			}
			this.calfPsm = new PersistentSpatialMemory(Globals.getWorldWidth(), Globals.getWorldHeight(),
					calfPsmPrefDistance);

			this.daysSinceMating = -99;
			this.daysSinceGivingBirth = 0;

			if (DebugLog.isEnabledFor(9)) {
				DebugLog.print9("{} with lact calf", this.getId());
			}
		}

		// nursing for 8 months
		if (this.withLactCalf && this.daysSinceGivingBirth == SimulationParameters.getNursingTime()) {
			int nOffspr = 0;

			if (Globals.getRandomSource().nextPregnancyStatusBoyGirl(0, 1) > 0.5) { // assuming 50 % males and no
				// abortions
				nOffspr = 1;
			}

			if (DebugLog.isEnabledFor(9)) {
				DebugLog.print("{} hatching {}", this.getId(), nOffspr);
			}

			if (nOffspr > 0) {
				final Porpoise calf = new Porpoise(this);
				this.context.add(calf);
				calf.setPosition(this.getPosition());
				calf.moveAwayFromLand(); // Initializes the pos list. TODO: not nice to do here, should be done
				// elsewhere
				Globals.getMonthlyStats().addBirth();
			}
			this.withLactCalf = false;
			this.calvesWeaned++;
			this.calfPsm = null;
			this.daysSinceGivingBirth = -99;
		}

		if (this.pregnancyStatus == 1) {
			this.daysSinceMating++;
		}

		if (this.withLactCalf) {
			this.daysSinceGivingBirth++;
		}
	}

	public int getMatingDay() {
		return this.matingDay;
	}

	public void setRandomMatingDay() {
		final double ran = Globals.getRandomSource().nextMatingDayNormal();
		ReplayHelper.print("mating-day:{0}", ran);
		this.matingDay = (int) Math.round(ran);
	}
	
	public void applyShipDeterrence() {
		var deterrenceStrength = shipDeterrence.deterrenceStrength();
		if (deterrenceStrength > 0) {
			this.loudestShipSPL = shipDeterrence.getLoudestShipSPL();
	
			if (this.deterStrength < deterrenceStrength) {
				this.deterStrength = deterrenceStrength;
				// vector pointing away from turbine
				this.deterVt[0] = shipDeterrence.deterrenceVtX();
				this.deterVt[1] = shipDeterrence.deterrenceVtY();
	
				this.deterTimeLeft = SimulationParameters.getDeterTime(); // how long to remain affected
			}

			// Porpoises nearby stop dispersing (which could force them to cross over disturbing agents very fast)
			dispersalBehaviour.deactivate();
		}
	}

	public void deterShipStep(int step, Ship ship, double deterX, double deterY, double mag, double receivedLevelVHF) {
		shipDeterrence.recordStep(step, ship, deterX, deterY, mag, receivedLevelVHF);
	}

	public synchronized void deter(final double currentDeterenceStrength, final SoundSource s) {
		final NdPoint shipPosition = s.getPosition();
		final NdPoint porpPosition = getPosition();

		// become deterred if not already more scared of other sound source
		if (this.deterStrength < currentDeterenceStrength) {
			this.deterStrength = currentDeterenceStrength;
			// vector pointing away from turbine
			this.deterVt[0] = currentDeterenceStrength * ((porpPosition.getX() - shipPosition.getX())) * SimulationParameters.getDeterrenceCoeff();
			this.deterVt[1] = currentDeterenceStrength * ((porpPosition.getY() - shipPosition.getY())) * SimulationParameters.getDeterrenceCoeff();

			this.deterTimeLeft = SimulationParameters.getDeterTime(); // how long to remain affected
		}

		// Porpoises nearby stop dispersing (which could force them to cross over disturbing agents very fast)
		dispersalBehaviour.deactivate();
	}

	public synchronized void deter(final double currentDeterenceStrength, final Turbine t) {
		// Deterrence by wind farms
		final NdPoint turbPosition = t.getPosition();
		final NdPoint porpPosition = getPosition();

		// become deterred if not already more scared of other wind turbine
		if (this.deterStrength < currentDeterenceStrength) {
			this.deterStrength = currentDeterenceStrength;
			// vector pointing away from turbine
			this.deterVt[0] = currentDeterenceStrength * ((porpPosition.getX() - turbPosition.getX())) * SimulationParameters.getDeterrenceCoeff();
			this.deterVt[1] = currentDeterenceStrength * ((porpPosition.getY() - turbPosition.getY())) * SimulationParameters.getDeterrenceCoeff();

			this.deterTimeLeft = SimulationParameters.getDeterTime(); // how long to remain affected
		}

		// Porpoises nearby stop dispersing (which could force them to cross over disturbing agents very fast)
		dispersalBehaviour.deactivate();
	}

	public void updateDeterence() {
		if (this.deterTimeLeft <= 0) {
			this.deterStrength = 0;
			this.deterVt[0] = 0;
			this.deterVt[1] = 0;
		} else {
			this.deterTimeLeft--;
			this.deterStrength *= (100 - SimulationParameters.getDeterDecay()) * 0.01;
			this.deterVt[0] /= 2.0;
			this.deterVt[1] /= 2.0;
		}
	}

	public void consumeEnergy(final double energyAmount) {
		this.energyLevel -= energyAmount;
		this.energyConsumedDailyTemp += energyAmount;
	}

	public double getEnergyLevel() {
		return this.energyLevel;
	}

	public double getEnergyLevelSum() {
		return energyLevelSum;
	}
	
	public CircularBuffer<Double> getEnergyLevelDaily() {
		return energyLevelDaily;
	}

	public double getSoundSourceDistance() {
		return soundSourceDistance;
	}

	public void setSoundSourceDistance(final double soundSourceDistance) {
		this.soundSourceDistance = soundSourceDistance;
	}

	public double getSoundSourceAngle() {
		return soundSourceAngle;
	}

	public void setSoundSourceAngle(final double soundSourceAngle) {
		this.soundSourceAngle = soundSourceAngle;
	}

	public void setSoundSourceImpact(final double impact) {
		this.soundSourceImpact = impact;
	}

	public double getSoundSourceImpact() {
		return soundSourceImpact;
	}

	public double getLoudestShipSPL() {
		return loudestShipSPL;
	}

	public double getDeterStrength() {
		return this.deterStrength;
	}

	public int getDeterTimeLeft() {
		return this.deterTimeLeft;
	}

	public double[] getDeterVector() {
//		return "[" + this.deterVt[0] + "," + this.deterVt[1] + "]";
		return this.deterVt;
	}

	public double[] getVT() {
//		return "[" + this.vt[0] + "," + this.vt[1] + "]";
		return this.vt;
	}

	public int getBlock() {
		return Globals.getCellData().getBlock(getPosition());
	}

	public PersistentSpatialMemory getPersistentSpatialMemory() {
		return psm;
	}

	public PersistentSpatialMemory getCalfPersistentSpatialMemory() {
		return calfPsm;
	}

	public double getUtmX() {
		return Globals.convertGridXToUtm(getPosition().getX());
	}

	public double getUtmY() {
		return Globals.convertGridYToUtm(getPosition().getY());
	}

	private void setPrevAngle(final double prevAngle) {
		this.prevAngle = prevAngle;
	}

	/**
	 * Checks whether the porpoise is stuck and should go into deterrence ignore mode.
	 */
	private void checkDeterrence() {
		// Only check whether we should ignore deterrence if we are not already ignoring it.
		// If the porpoise has moved less than IGNORE_DETER_STUCK_TIME, e.g. right after start, then we ignore it for
		// now.
		if (ignoreDeterrence == 0 && this.posList.size() > SimulationConstants.IGNORE_DETER_STUCK_TIME
				&& this.deterStrength > SimulationConstants.IGNORE_DETER_MIN_IMPACT) {
			double totalDistance = 0; //

			if (this.posList.size() >= 2) {
				// Max 15 last pos to check
				for (int i = 1; i < posList.size() && i < SimulationConstants.IGNORE_DETER_STUCK_TIME; i++) {
					final NdPoint p1 = posList.get(i);
					final NdPoint p0 = posList.get(i - 1);
					totalDistance += this.getSpace().getDistance(p0, p1);
				}
			}

			if (totalDistance <= SimulationConstants.IGNORE_DETER_MIN_DISTANCE) {
				ignoreDeterrence = SimulationConstants.IGNORE_DETER_NUMBER_OF_STEPS_IGNORE;
			}
		}
	}

	public int getIgnoreDeterrence() {
		// Used for the UI so the ignore deterrence state can be observed.
		return this.ignoreDeterrence;
	}

	/**
	 * Gets the age of the porpoise. The unit is years.
	 *
	 * @return The age of the porpoise in years.
	 */
	public double getAge() {
		return age;
	}

	public double getAgeOfMaturity() {
		return ageOfMaturity;
	}

	public byte getPregnancyStatus() {
		return pregnancyStatus;
	}

	/**
	 * Gets the position if the agent moved distance forward without taking border wrap or bounce into account. This
	 * position can therefore be of the edge of the map.
	 *
	 * @param distance The distance to move forward.
	 */
	protected NdPoint getPositionForward(final double distance) {
		// TODO: Copy paste from forward, find a way to have only one implementation.
		final Dimensions d = this.getSpace().getDimensions();
		final double[] anglesInRadians = { (Math.PI / 2) - getHeadingInRads(), 0.0 };
		final double[] displacement = SpatialMath.getDisplacement(d.size(), 0, distance, anglesInRadians);

		final double[] newPos = new double[2];
		final NdPoint pos = getPosition();
		pos.toDoubleArray(newPos);

		newPos[0] += displacement[0];
		newPos[1] += displacement[1];

		return new NdPoint(newPos);
	}

	/**
	 * Moves the agent forward and bounces the agent on the edge. The agent will
	 *
	 * @param distance The distance to move forward.
	 */
	@Override
	public void forward(final double distance) {
		if (SimulationParameters.isHomogenous() || SimulationParameters.getModel() < 5) {
			super.forward(distance);
			return;
		}

		final Dimensions d = this.getSpace().getDimensions();
		final double[] anglesInRadians = { (Math.PI / 2) - getHeadingInRads(), 0.0 };
		final double[] displacement = SpatialMath.getDisplacement(d.size(), 0, distance, anglesInRadians);

		final double[] newPos = new double[2];
		final NdPoint pos = getPosition();
		pos.toDoubleArray(newPos);

		newPos[0] += displacement[0];
		newPos[1] += displacement[1];

		boolean angleChanged = false;

		if (newPos[0] >= d.getWidth() || newPos[0] < 0) {
			// we are moving off the right most edge or the left most edge
			// new angle will be {-dx,dy}
			angleChanged = true;
			// newPos[0] = -2*displacement[0]; // This is not a bounce, this is a turn before impact.
			displacement[0] = -displacement[0];
		}

		if (newPos[1] >= d.getHeight() || newPos[1] < 0) {
			// we are moving off the top or the bottom of the map
			// new angle will be {dx,-dy}
			angleChanged = true;
			// newPos[1] = -2*displacement[1];
			displacement[1] = -displacement[1];
		}

		if (angleChanged) {
			final double angle = Math.atan(displacement[1] / displacement[0]) - (Math.PI / 2);
			final double newHeading = Math.toDegrees(angle);
			setHeading(newHeading);
			setPrevAngle(0);
		}

		moveByVector(distance, anglesInRadians);
	}

	public int getLactatingCalf() {
		if (this.withLactCalf) {
			return 1;
		} else {
			return 0;
		}
	}

	public int getCalvesBorn() {
		return this.calvesBorn;
	}

	public int getCalvesWeaned() {
		return this.calvesWeaned;
	}

	public double getEnergyConsumedDaily() {
		return this.energyConsumedDaily;
	}

	public double getFoodEaten() {
		return this.foodEatenDaily;
	}

	public double getPrevLogMov() {
		return this.prevLogMov;
	}

	public double getPresLogMov() {
		return this.presLogMov;
	}
	
	public double getPrevAngle() {
		return prevAngle;
	}

	public double getPresAngle() {
		return presAngle;
	}
	
	public double getTickMoveAdjustMultiplier() {
		return tickMoveAdjustMultiplier;
	}

	public int getInFoodPatch() {
		final GridPoint p = Agent.ndPointToGridPoint(getPosition());
		if (Globals.getCellData().getFoodProb(p) > 0 && Globals.getCellData().getMaxEnt(p) > 0) {
			return 1;
		} else {
			return 0;
		}
	}

	/***
	 * Gets the distance moved by the porpoise in the last day.
	 *
	 * @return The distance moved (cells) in the last 48 steps.
	 */
	public double getDailyMovement() {
		double distance = 0;
		NdPoint lastPos = getPosition();

		final int steps = Math.min(48, this.posList.size());

		for (int i = 0; i < steps; i++) {
			final NdPoint currPos = this.posList.get(i);
			distance += this.getSpace().getDistance(lastPos, currPos);
			lastPos = currPos;
		}

		if (steps < 48) {
			distance = (distance / steps) * 48.0;
		}

		return distance;
	}

	/**
	 * Returns 1 if the porpoise is in dispersal 2 mode. This allows us to sum the porpoises in dispersal 2 mode easily.
	 *
	 * @return
	 */
	public int getIsInDisp1Mode()
	{
		return this.dispersalBehaviour.getDispersalType() == 1 ? 1 : 0;
	}

	/***
	 * @see Porpoise.getIsInDisp1Mode
	 *
	 * @return
	 */
	public int getIsInDisp2Mode()
	{
		return this.dispersalBehaviour.getDispersalType() == 2 ? 1 : 0;
	}
	
	/***
	 * Returns 1 if the porpoise is in dispersal 3 mode. This allows us to sum the porpoises in dispersal 2 mode easily.
	 *
	 * @return
	 */
	public int getIsInDisp3Mode() {
		switch (this.dispersalBehaviour.getDispersalType()) {
		case 3:
		case 4:
			return 1;
		default:
			return 0;
		}
	}

	/**
	 * Returns the number of ticks the porpoise has been dispersing. Zero (0) will be returned if the porpoise is not
	 * dispersing.
	 *
	 * @return The number of ticks the porpoise has been dispersing
	 */
	public int getDispNumTicks() {
		return this.dispNumTicks;
	}

	public int getDispersalMode() {
		return this.dispersalBehaviour.getDispersalType(); // this.dispType;
	}

	public Dispersal getDispersalBehaviour() {
		return this.dispersalBehaviour;
	}

	public void reinitializePoslist() {
		this.posList = new CircularBuffer<NdPoint>(SimulationConstants.MEMORY_MAX);
		this.posList.add(getPosition());
	}

	public void setTrackVisitedCells(final boolean track) {
		// We only enable this in the UI
		if (!RunEnvironment.getInstance().isBatch()) {
			this.trackVisitedCells = track;
		}
	}

	public void setWritePsmSteps(final boolean writePsmSteps) {
		this.writePsmSteps = writePsmSteps;
	}

	public void setPosList(final String s) {
		return; // does nothing, only here to enable edit box on UI
	}

	public String getPosList() {
		final StringBuffer sb = new StringBuffer("[");
		for (int i = 0; i < this.posList.size(); i++) {
			if (i != 0) {
				sb.append(" "); // match NetLogo formatting
			}
			final NdPoint pos = this.posList.get(i);
			sb.append("[").append(pos.getX()).append(" ").append(pos.getY()).append("]");
		}
		sb.append("]");

		return sb.toString();
	}

	public CircularBuffer<NdPoint> getPosListDaily() {
		return posListDaily;
	}

	public boolean isEnoughWaterAhead() {
		return enoughWaterAhead;
	}

	public boolean isWritePsmSteps() {
		return writePsmSteps;
	}

}

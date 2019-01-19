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

package dk.au.bios.porpoise;

import repast.simphony.engine.environment.RunEnvironmentBuilder;
import repast.simphony.scenario.ModelInitializer;
import repast.simphony.scenario.Scenario;

/**
 * Model initializer. This is not currently doing anything but was used for testing some custom Data Sinks. The file is
 * retained in case this will be pursued further in the future.
 */
public class PorpoiseInitializer implements ModelInitializer {

	// private static final String DS_RANDOM_PORPOISE = "Random Porpoise";
	// private static final String DS_STATS = "Sim stats";
	// private static final int TICKS_PER_DAY = 48;

	@Override
	public void initialize(final Scenario scenario, final RunEnvironmentBuilder builder) {

		/*
		 * TODO: Enable. Disabled due to batch problem.
		 * RSApplication.getRSApplicationInstance().addCustomUserPanel(Globals.USER_PANEL);
		 *
		 * scenario.addMasterControllerAction(new NullAbstractControllerAction<Object>() {
		 *
		 * // private CSVFileDataSink randomPorpoiseDataSink; // private CSVFileDataSink statisticsDataSink; private
		 * DataSetBuilder<?> statisticsBuilder;
		 *
		 * // private List<BatchParamMapFileWriter> writers = new ArrayList<BatchParamMapFileWriter>();
		 *
		 * @Override public void runInitialize(RunState runState, Context<? extends Object> context, Parameters
		 * runParams) { super.runInitialize(runState, context, runParams);
		 *
		 * // Boolean logStats = runParams.getBoolean("logStatistics"); // if (logStats != null && logStats) { // int
		 * statsOffset = runParams.getInteger("logStatisticsOffset") * TICKS_PER_DAY; // int statsInterval =
		 * runParams.getInteger("logStatisticsInterval") * TICKS_PER_DAY;
		 *
		 * int statsOffset = runParams.getInteger("logStatisticsOffset"); int statsInterval =
		 * runParams.getInteger("logStatisticsInterval"); ScheduleParameters sp =
		 * ScheduleParameters.createRepeating(statsOffset, statsInterval);
		 * statisticsBuilder.defineScheduleParameters(sp, false);
		 *
		 * // statisticsDataSink.setEnabled(true); // }
		 *
		 * // randomPorpoiseDataSink.setEnabled(runParams.getBoolean("logRandomPorpoise")); // // for
		 * (BatchParamMapFileWriter writer : writers) { // writer.runStarted(); // } }
		 */

		/**
		 * Called during initialization. Despite the name, this also happens during a "normal" run.
		 */
		/*
		 * TODO: Enable. Disabled due to batch problem.
		 *
		 * @Override public void batchInitialize(RunState runState, Object contextId) {
		 *
		 * /* TODO: Enable. Disabled due to batch problem. DataSetRegistry registry = (DataSetRegistry) runState
		 * .getFromRegistry(DataConstants.REGISTRY_KEY); DataSetManager manager = registry.getDataSetManager(contextId);
		 *
		 * // SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd-HHmmss"); // String timeStamp = fmt.format(new
		 * Date());
		 *
		 * // FileNameFormatter fnFormatter = new FileNameFormatter("Statistics.csv", true);
		 *
		 * statisticsBuilder = manager.getDataSetBuilder(DS_STATS); // statisticsDataSink = new
		 * CSVFileDataSink(fnFormatter.getFilename()); // statisticsBuilder.addDataSink(statisticsDataSink); // //
		 * DataSetBuilder<?> porpoiseBuilder = manager.getDataSetBuilder(DS_RANDOM_PORPOISE); // randomPorpoiseDataSink
		 * = new CSVFileDataSink("RandomPorpoise" + "_" + timeStamp + ".csv"); //
		 * porpoiseBuilder.addDataSink(randomPorpoiseDataSink); // // if (runState.getRunInfo().isBatch()) { //
		 * BatchParamMapFileWriter writer = new BatchParamMapFileWriter( // manager.getBatchRunDataSource(),
		 * fnFormatter, // ";", FormatType.TABULAR); // statisticsBuilder.addDataSink(writer); // writers.add(writer);
		 * // }
		 *
		 * }
		 *
		 * @Override public String toString() { return "DataSink initializer"; }
		 *
		 * // @Override // public void batchCleanup(RunState runState, Object contextId) { // writers.clear(); // }
		 *
		 *
		 * });
		 */
	}

}

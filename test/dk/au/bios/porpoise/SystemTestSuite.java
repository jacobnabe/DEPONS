package dk.au.bios.porpoise;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import dk.au.bios.porpoise.behaviour.DispersalPSMType2Test;
import dk.au.bios.porpoise.behaviour.DispersalPSMType3Test;
import dk.au.bios.porpoise.behaviour.PersistenSpatialMemoryTest;
import dk.au.bios.porpoise.ships.ShipsDataTest;
import dk.au.bios.porpoise.util.CircularBufferTest;
import dk.au.bios.porpoise.util.DebugLogTest;

@RunWith(Suite.class)

@SuiteClasses({ 
	PorpoiseDeterrenceTest.class,
	PorpoiseMoveTest.class,
	PorpoiseMoveUnrollTest.class,
	PorpoiseTest.class,
	DispersalPSMType2Test.class,
	DispersalPSMType3Test.class,
	PersistenSpatialMemoryTest.class,
	ShipsDataTest.class,
	CircularBufferTest.class,
	DebugLogTest.class,
	ReplayedSimulationKattegatTest.class,
	ReplayedSimulationDanTyskTest.class,
	ReplayedSimulationNorthSeaTest.class 
})
public class SystemTestSuite {

}

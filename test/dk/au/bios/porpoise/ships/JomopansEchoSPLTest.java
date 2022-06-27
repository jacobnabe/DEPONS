/*
 * Copyright (C) 2022 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import static org.assertj.core.api.Assertions.within;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class JomopansEchoSPLTest {


	@ParameterizedTest
	@CsvSource({
		"Bulker, -11, 12.0, 300.00, 172.70323",
		"Bulker, 0, 12.0, 300.00, 161.38133",
		"Bulker, 2, 12.0, 300.00, 159.29780",
		"Bulker, 11, 12.0, 300.00, 150.14937",
		"Bulker, 12, 12.0, 300.00, 149.14457",
		"Containership, -11, 20.0, 366.00, 179.98581",
		"Containership, 0, 20.0, 366.00, 169.63399",
		"Containership, 2, 20.0, 366.00, 167.56442",
		"Containership, 11, 20.0, 366.00, 158.44671",
		"Containership, 12, 20.0, 366.00, 157.44298",
		"Cruise, -11, 19.0, 360.00, 176.45360",
		"Cruise, 0, 19.0, 360.00, 169.47462",
		"Cruise, 2, 19.0, 360.00, 167.41758",
		"Cruise, 11, 19.0, 360.00, 158.30394",
		"Cruise, 12, 19.0, 360.00, 157.30007",
		"Dredger, -11, 2.5, 44.15, 118.75708",
		"Dredger, 0, 2.5, 44.15, 113.86604",
		"Dredger, 2, 2.5, 44.15, 111.76423",
		"Dredger, 11, 2.5, 44.15, 102.55929",
		"Dredger, 12, 2.5, 44.15, 101.55233",
		"Fishing, -11, 10.0, 24.00, 156.60149",
		"Fishing, 0, 10.0, 24.00, 155.07434",
		"Fishing, 2, 10.0, 24.00, 152.97251",
		"Fishing, 11, 10.0, 24.00, 143.69732",
		"Fishing, 12, 10.0, 24.00, 142.68714",
		"Government/Research, -11, 11.0, 82.90, 165.92687",
		"Government/Research, 0, 11.0, 82.90, 162.46369",
		"Government/Research, 2, 11.0, 82.90, 160.35776",
		"Government/Research, 11, 11.0, 82.90, 151.12325",
		"Government/Research, 12, 11.0, 82.90, 150.11503",
		"Naval, -11, 30.0, 183.00, 192.98406",
		"Naval, 0, 30.0, 183.00, 186.87516",
		"Naval, 2, 30.0, 183.00, 184.77995",
		"Naval, 11, 30.0, 183.00, 175.59974",
		"Naval, 12, 30.0, 183.00, 174.59375",
		"Other, -11, 18.0, 75.00, 179.27018",
		"Other, 0, 18.0, 75.00, 176.47548",
		"Other, 2, 18.0, 75.00, 174.36943",
		"Other, 11, 18.0, 75.00, 165.12089",
		"Other, 12, 18.0, 75.00, 164.11203",
		"Passenger, -11, 30.0, 43.50, 182.99909",
		"Passenger, 0, 30.0, 43.50, 177.94030",
		"Passenger, 2, 30.0, 43.50, 175.83926",
		"Passenger, 11, 30.0, 43.50, 166.63775",
		"Passenger, 12, 30.0, 43.50, 165.63093",
		"Tanker, -11, 14.0, 315.00, 180.27272",
		"Tanker, 0, 14.0, 315.00, 168.82197",
		"Tanker, 2, 14.0, 315.00, 166.73234",
		"Tanker, 11, 14.0, 315.00, 157.56836",
		"Tanker, 12, 14.0, 315.00, 156.56299",
		"Tug, -11, 5.0, 26.00, 148.68381",
		"Tug, 0, 5.0, 26.00, 151.97409",
		"Tug, 2, 5.0, 26.00, 150.00067",
		"Tug, 11, 5.0, 26.00, 140.64469",
		"Tug, 12, 5.0, 26.00, 139.62769",
		"Vehicle Carrier, -11, 14.95, 192.00, 170.52697",
		"Vehicle Carrier, 0, 14.95, 192.00, 159.86823",
		"Vehicle Carrier, 2, 14.95, 192.00, 157.79167",
		"Vehicle Carrier, 11, 14.95, 192.00, 148.65922",
		"Vehicle Carrier, 12, 14.95, 192.00, 147.65498"
		})
	public void calculateSpl(String vc, int band, double speed, double length, double spl) {
		var jespl = new JomopansEchoSPL();
		Assertions.assertThat(jespl.calculateSourceLevel(VesselClass.forValue(vc), speed, length, band)).isCloseTo(spl, within(0.00001));
	}

}

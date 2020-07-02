package dk.au.bios.porpoise.ships

import com.fasterxml.jackson.databind.ObjectMapper

import spock.lang.Specification

class ShipsDataTest extends Specification {

	def "Load ship data from json file"() {
		given:
		ObjectMapper objMapper = new ObjectMapper();
		ShipsData shipsData = objMapper.readValue(ShipsDataTest.class.getResourceAsStream("ships.json"), ShipsData.class);
		
		expect:
		shipsData.routes.size() == 2
		shipsData.ships.size() == 5
	}
}

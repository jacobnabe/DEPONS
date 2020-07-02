package dk.au.bios.porpoise.ships;

import java.util.List;

public class Route {

	private String name;
	private List<Buoy> route;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Buoy> getRoute() {
		return route;
	}

	public void setRoute(List<Buoy> route) {
		this.route = route;
	}

}

package dk.au.bios.porpoise.ships;

public class Ship {

	private String name;
	private double speed;
	private double impact;
	private int start;
	private String route;
	private SurveyShipData survey;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getImpact() {
		return impact;
	}

	public void setImpact(double impact) {
		this.impact = impact;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public SurveyShipData getSurvey() {
		return survey;
	}

	public void setSurvey(SurveyShipData survey) {
		this.survey = survey;
	}

}

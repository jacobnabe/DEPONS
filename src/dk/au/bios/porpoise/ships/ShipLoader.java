package dk.au.bios.porpoise.ships;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;

public class ShipLoader {

	public void load(final Context<Agent> context, final ContinuousSpace<Agent> space, final Grid<Agent> grid,
			final String landscape) throws Exception {

		if (Files.exists(Paths.get("data", landscape, "ships.json"))) {
			try (InputStream dataIS = new FileInputStream(Paths.get("data", landscape, "ships.json").toFile())) {
				loadFromStream(context, space, grid, dataIS);
			}
		} else if (Files.exists(Paths.get("data", landscape + ".zip"))) {
			try (ZipFile zf = new ZipFile(Paths.get("data", landscape + ".zip").toFile());
					InputStream dataIS = zf.getInputStream(zf.getEntry("ships.json"))) {
				loadFromStream(context, space, grid, dataIS);
			}
		} else {
			throw new Exception("File ships.json does not exist for landscape " + landscape);
		}
	}

	private void loadFromStream(final Context<Agent> context, final ContinuousSpace<Agent> space,
			final Grid<Agent> grid, final InputStream source)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objMapper = new ObjectMapper();

		ShipsData shipsData = objMapper.readValue(source, ShipsData.class);

		// Read routes
		Map<String, NdPoint[]> routes = new HashMap<>();
		for (Route r : shipsData.getRoutes()) {
			String name = r.getName();
			final Function<? super Buoy, ? extends NdPoint> mapper;
			mapper = buoy -> new NdPoint(convertUtmXToGrid(buoy.getX()), convertUtmYToGrid(buoy.getY()));
			List<NdPoint> route = r.getRoute().stream().map(mapper).collect(Collectors.toList());
			NdPoint[] routePoints = route.toArray(new NdPoint[route.size()]);
			routes.put(name, routePoints);
		}

		for (Ship s : shipsData.getShips()) {
			NdPoint[] route = routes.get(s.getRoute());
			dk.au.bios.porpoise.Ship agent = new dk.au.bios.porpoise.Ship(space, grid, route, s);
			context.add(agent);
			agent.initialize();
		}
	}

	private double convertUtmXToGrid(final double utmX) {
		return (utmX - Globals.getXllCorner()) / 400.0d;
	}

	private double convertUtmYToGrid(final double utmY) {
		return (utmY - Globals.getYllCorner()) / 400.0d;
	}
}

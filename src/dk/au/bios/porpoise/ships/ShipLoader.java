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
import repast.simphony.space.continuous.NdPoint;

public class ShipLoader {

	public void load(final Context<Agent> context, final String landscape) throws Exception {

		if (Files.exists(Paths.get("data", landscape, "ships.json"))) {
			try (InputStream dataIS = new FileInputStream(Paths.get("data", landscape, "ships.json").toFile())) {
				loadFromStream(context, dataIS);
			}
		} else if (Files.exists(Paths.get("data", landscape + ".zip"))) {
			try (ZipFile zf = new ZipFile(Paths.get("data", landscape + ".zip").toFile());
					InputStream dataIS = zf.getInputStream(zf.getEntry("ships.json"))) {
				loadFromStream(context, dataIS);
			}
		} else {
			throw new Exception("File ships.json does not exist for landscape " + landscape);
		}
	}

	private void loadFromStream(final Context<Agent> context, final InputStream source)
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
			dk.au.bios.porpoise.Ship agent = new dk.au.bios.porpoise.Ship(route, s);
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

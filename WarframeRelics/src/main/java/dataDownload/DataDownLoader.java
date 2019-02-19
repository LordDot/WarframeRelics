package dataDownload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dataBase.IDataBase;
import dataBase.SQLLiteDataBase;
import gui.Util;

public class DataDownLoader {
	private static final String RELIC_URL = "https://drops.warframestat.us/data/relics.json";
	private static final String MISSION_URL = "https://drops.warframestat.us/data/missionRewards.json";

	private IDataBase database;

	public DataDownLoader(IDataBase database) {
		this.database = database;
	}

	public void downLoadPartData() throws IOException, SQLException {
		URLConnection url = new URL(RELIC_URL).openConnection();
		url.addRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		java.io.InputStream ips = url.getInputStream();

		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(ips));
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JsonParser parser = new JsonParser();
		JsonArray list = parser.parse(builder.toString()).getAsJsonObject().get("relics").getAsJsonArray();
		for (Iterator<JsonElement> i = list.iterator(); i.hasNext();) {
			JsonObject relic = (JsonObject) i.next();
			if (relic.get("state").getAsString().equals("Intact")) {
				addRelic(relic);
			}
		}
	}

	private void addRelic(JsonObject relic) throws IOException, SQLException {
		String tierName = relic.get("tier").getAsString();
		int tier = Util.convertFissureNameToInt(tierName);

		String relicName = relic.get("relicName").getAsString();
		database.addRelic(tier, relicName);

		System.out.println(tierName + " " + relicName);

		JsonArray rewards = relic.get("rewards").getAsJsonArray();
		for (Iterator<JsonElement> i = rewards.iterator(); i.hasNext();) {
			JsonObject reward = i.next().getAsJsonObject();
			String itemName = reward.get("itemName").getAsString();
			try {
				database.addItem(itemName);
			} catch (SQLException e) {

			}

			int rarity;
			double dropChance = reward.get("chance").getAsDouble();
			if (dropChance == 25.33) {
				rarity = SQLLiteDataBase.BRONZE;
			} else if (dropChance == 11) {
				rarity = SQLLiteDataBase.SILVER;
			} else if (dropChance == 2) {
				rarity = SQLLiteDataBase.GOLD;
			} else {
				throw new IOException("Unknow rarity");
			}
			database.addDrop(itemName, tier, relicName, rarity);
		}
	}

	public void downloadMissionData() throws IOException, SQLException {
		URLConnection url = new URL(MISSION_URL).openConnection();
		url.addRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		java.io.InputStream ips = url.getInputStream();

		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(ips));
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JsonParser parser = new JsonParser();
		JsonObject missions = parser.parse(builder.toString()).getAsJsonObject().get("missionRewards")
				.getAsJsonObject();
		for (String planetName : missions.keySet()) {
			JsonObject planet = missions.get(planetName).getAsJsonObject();
			for (String missionName : planet.keySet()) {
				System.out.println(planetName + "/" + missionName);
				if (!missionName.contains("The Index")) {
					handleMission(planet.get(missionName).getAsJsonObject());
				}
			}
		}

		database.updateItemData();
	}

	private void handleMission(JsonObject mission) throws IOException, SQLException {
		if (mission.get("isEvent").getAsBoolean()) {
			return;
		}

		String missionType = mission.get("gameMode").getAsString();
		if (missionType.equals("Survival") || missionType.equals("Defense") || missionType.equals("Rescue")
				|| missionType.equals("Caches") || missionType.equals("Interception") || missionType.equals("Spy")
				|| missionType.equals("Excavation") || missionType.equals("Conclave") || missionType.equals("Defection")
				|| missionType.equals("Infested Salvage") || missionType.equals("Rush")
				|| missionType.equals("Sanctuary Onslaught")) {
			handleABCMission(mission);
		} else if (missionType.equals("Capture") || missionType.equals("Exterminate")
				|| missionType.equals("Assassination") || missionType.equals("Mobile Defense")
				|| missionType.equals("Sabotage") || missionType.equals("Pursuit") || missionType.equals("Arena")) {
			handleAMission(mission);
		} else {
			throw new IOException("Unknown Missiontype: " + missionType);
		}
	}

	private void handleABCMission(JsonObject mission) throws IOException, SQLException {
		JsonObject rewards = mission.get("rewards").getAsJsonObject();
		for (String rotationName : rewards.keySet()) {
			JsonArray rotation = rewards.get(rotationName).getAsJsonArray();
			for (JsonElement e : rotation) {
				String name = e.getAsJsonObject().get("itemName").getAsString();
				if (name.contains("Relic")) {
					setRelicUnvaulted(name);
				}
			}
		}
	}

	private void handleAMission(JsonObject mission) throws IOException, SQLException {
		JsonArray rotation = mission.get("rewards").getAsJsonArray();
		for (JsonElement e : rotation) {
			String name = e.getAsJsonObject().get("itemName").getAsString();
			if (name.contains("Relic")) {
				setRelicUnvaulted(name);
			}
		}
	}

	private void setRelicUnvaulted(String name) throws IOException, SQLException {
		String[] split = name.split(" ");
		int tier = Util.convertFissureNameToInt(split[0]);
		database.setRelicVaulted(tier, split[1], false);
	}
}

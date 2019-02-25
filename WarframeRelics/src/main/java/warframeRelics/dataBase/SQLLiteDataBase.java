package warframeRelics.dataBase;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import warframeRelics.gui.Util;

public class SQLLiteDataBase implements IDataBase, AutoCloseable {

	private static Logger log = Logger.getLogger(SQLLiteDataBase.class.getName());
	
	public static int BRONZE = 0;
	public static int SILVER = 1;
	public static int GOLD = 3;

	public static int LITH = 0;
	public static int MESO = 1;
	public static int NEO = 2;
	public static int AXI = 3;

	private static String ADD_ITEM = "insert into items (id,name,vaulted) values(?,?,1);";
	private static String GET_ITEM_BY_NAME = "select * from items where name = ?;";
	private static String ADD_RELIC = "insert into relics (id,name,type,vaulted) values (?,?,?,1);";
	private static String GET_RELIC_BY_NAME = "select * from relics where name = ? and type = ?;";
	private static String ADD_DROP = "insert into drops (itemId,relicId,rarity)values(?,?,?);";
	private static String GET_ALL_ITEM_NAMES = "select name from items;";
	private static String SET_RELIC_VAULTED = "update relics set vaulted = ? where name = ? and type = ?";
	private static String SET_ITEM_VAULTED = "update items set vaulted = ? where name = ?";
	private static String GET_UNVAULTED_RELICS = "select * from relics where vaulted = 0";
	private static String GET_ITEMS_BY_RELIC = "select id, name from items, drops where items.id = drops.itemId and drops.relicId = ?;";
	private static String EMPTY_ITEMS_TABLE = "delete from items;";
	private static String EMPTY_DROPS_TABLE = "delete from drops;";
	private static String EMPTY_RELIC_TABLE = "delete from relics;";

	private Connection connection;

	private PreparedStatement addItemByName;
	private PreparedStatement getItemByName;
	private PreparedStatement addRelicByName;
	private PreparedStatement getRelicByName;
	private PreparedStatement addDrop;
	private PreparedStatement getAllItemNames;
	private PreparedStatement setRelicVaulted;
	private PreparedStatement setItemVaulted;
	private PreparedStatement getUnvaultedRelics;
	private PreparedStatement getItemsByRelic;

	private int itemPrimaryKeyCounter;
	private int relicPrimarykeyCounter;

	public SQLLiteDataBase(String path) throws SQLException {
		String url = "jdbc:sqlite:" + path;
		boolean newDB = !new File(path).exists();

		connection = DriverManager.getConnection(url);

		itemPrimaryKeyCounter = 0;
		relicPrimarykeyCounter = 0;

		if (newDB) {

			try (Statement stmt = connection.createStatement();) {
				stmt.execute("Create table items(Id int primary_key, name varchar2(50) unique, vaulted bit);");
				stmt.execute(
						"Create table relics(Id int primary_key, name varchar2(2),type int, vaulted bit, unique (name, type));");
				stmt.execute("create table drops(itemId int, relicId int, rarity int);");
			}
		}

		addItemByName = connection.prepareStatement(ADD_ITEM);
		getItemByName = connection.prepareStatement(GET_ITEM_BY_NAME);
		addRelicByName = connection.prepareStatement(ADD_RELIC);
		getRelicByName = connection.prepareStatement(GET_RELIC_BY_NAME);
		addDrop = connection.prepareStatement(ADD_DROP);
		getAllItemNames = connection.prepareStatement(GET_ALL_ITEM_NAMES);
		setRelicVaulted = connection.prepareStatement(SET_RELIC_VAULTED);
		setItemVaulted = connection.prepareStatement(SET_ITEM_VAULTED);
		getUnvaultedRelics = connection.prepareStatement(GET_UNVAULTED_RELICS);
		getItemsByRelic = connection.prepareStatement(GET_ITEMS_BY_RELIC);
		getAllItemNames.executeQuery();
		getAllItemNames.executeQuery();
	}

	@Override
	public String getNearestItemName(String name) throws RuntimeException {
		String bestName = name;
		int bestDistance = Integer.MAX_VALUE;
		try (ResultSet results = getAllItemNames.executeQuery();) {
			while (results.next()) {
				String currentName = results.getString(1);
				int distance = Util.stringDifference(name.toLowerCase(), currentName.toLowerCase());
				if (distance == 0) {
					log.info("Found Name " + currentName + " in database");
					return currentName;
				}
				if (distance < bestDistance) {
					bestDistance = distance;
					bestName = currentName;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		log.info("Fixed name " + name + " to " + bestName);
		return bestName;
	}

	public List<String> getAllItems(){
		List<String> ret = new ArrayList<>();
		try (ResultSet results = getAllItemNames.executeQuery();) {
			while (results.next()) {
				ret.add(results.getString(1));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}
	
	public void addItem(String name) throws SQLException {
		addItemByName.setInt(1, itemPrimaryKeyCounter);
		addItemByName.setString(2, name);
		addItemByName.execute();
		itemPrimaryKeyCounter++;
	}

	public void addDrop(String item, int relicType, String relic, int rarity) throws SQLException {
		int itemId, relicId;

		getItemByName.setString(1, item);
		try (ResultSet set = getItemByName.executeQuery();) {
			if (!set.next()) {
				throw new IllegalStateException("item not yet added");
			}
			itemId = set.getInt("Id");
		}

		getRelicByName.setString(1, relic);
		getRelicByName.setInt(2, relicType);
		try (ResultSet set = getRelicByName.executeQuery();) {
			if (!set.next()) {
				throw new IllegalStateException("relic not yet added");
			}
			relicId = set.getInt("Id");
		}

		addDrop.setInt(1, itemId);
		addDrop.setInt(2, relicId);
		addDrop.setInt(3, rarity);
		addDrop.execute();
	}

	public void addRelic(int type, String name) throws SQLException {
		addRelicByName.setInt(1, relicPrimarykeyCounter++);
		addRelicByName.setString(2, name);
		addRelicByName.setInt(3, type);
		addRelicByName.execute();
	}

	@Override
	public void setRelicVaulted(int type, String name, boolean vaulted) throws SQLException {
		setRelicVaulted.setInt(3, type);
		setRelicVaulted.setString(2, name);
		setRelicVaulted.setInt(1, vaulted ? 1 : 0);
		setRelicVaulted.execute();
	}

	@Override
	public void setItemVaulted(String name, boolean vaulted) throws SQLException {
		setItemVaulted.setString(2, name);
		setItemVaulted.setInt(1, vaulted ? 1 : 0);
		setItemVaulted.execute();
	}

	@Override
	public void updateItemData() throws SQLException {
		try (ResultSet relics = getUnvaultedRelics.executeQuery();) {
			while (relics.next()) {
				getItemsByRelic.setInt(1, relics.getInt("Id"));
				ResultSet items = getItemsByRelic.executeQuery();
				while (items.next()) {
					setItemVaulted(items.getString("name"), false);
				}
			}
		}
	}

	@Override
	public boolean getItemVaulted(String name) throws SQLException {
		getItemByName.setString(1, name);
		try (ResultSet res = getItemByName.executeQuery();) {
			if (res.next()) {
				return res.getBoolean("vaulted");
			}
		}
		return true;
	}

	public void emptyTables() throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute(EMPTY_DROPS_TABLE);
			stmt.execute(EMPTY_ITEMS_TABLE);
			stmt.execute(EMPTY_RELIC_TABLE);
		}
		itemPrimaryKeyCounter = 0;
		relicPrimarykeyCounter = 0;
	}

	@Override
	public void close() throws SQLException {
		addItemByName.close();
		getItemByName.close();
		addRelicByName.close();
		getRelicByName.close();
		addDrop.close();
		getAllItemNames.close();
		setRelicVaulted.close();
		setItemVaulted.close();
		getUnvaultedRelics.close();
		getItemsByRelic.close();
		connection.close();
	}
	
	@Override
	public void setFastMode(boolean fastMode) throws SQLException {
		if(!fastMode) {
			connection.commit();
		}
		connection.setAutoCommit(!fastMode);
	}
}

package warframeRelics.dataBase;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import warframeRelics.beans.PrimeItem;
import warframeRelics.gui.Util;
import warframeRelics.gui.WarframeRelics;

public class SQLLiteDataBase implements IDataBase, AutoCloseable {

	private static Logger log = Logger.getLogger(SQLLiteDataBase.class.getName());

	private static final String getAllItemNamesSql = "select display_name from items;";
	private PreparedStatement getAllItemNames;
	private static final String getItemByDisplayNameSql = "select * from items where display_name = ?;";
	private PreparedStatement getItemByDisplayName;
	private static final String addItemSql = "insert into items values (?,?,?,?);";
	private PreparedStatement addItem;

	private Connection connection;
	private int relicPrimaryKey;

	public SQLLiteDataBase(String path) throws SQLException {
		relicPrimaryKey = 0;

		String url = "jdbc:sqlite:" + path;
		boolean newDB = !new File(path).exists();

		connection = DriverManager.getConnection(url);

		if (newDB) {
			try (Statement stmt = connection.createStatement();) {
				stmt.execute("Create table items(id int primary_key, unique_name varchar2(100) unique, display_name varchar2(50) unique, vaulted bit);");
				stmt.execute("Create table info (id int primary key, key varchar(20),value varchar(20));");
				stmt.execute("insert into info values(0, 'version','" + WarframeRelics.VERSION + "');");
			}
		}

		getAllItemNames = connection.prepareStatement(getAllItemNamesSql);
		getItemByDisplayName = connection.prepareStatement(getItemByDisplayNameSql);
		addItem = connection.prepareStatement(addItemSql);
	}

	@Override
	public synchronized PrimeItem getNearestItemName(String name) throws RuntimeException {
		String bestName = name;
		int bestDistance = Integer.MAX_VALUE;
		try (ResultSet results = getAllItemNames.executeQuery();) {
			while (results.next()) {
				String currentName = results.getString(1);
				int distance = Util.stringDifference(name.toLowerCase(), currentName.toLowerCase());
				if (distance == 0) {
					bestName = currentName;
					break;
				}
				if (distance < bestDistance) {
					bestDistance = distance;
					bestName = currentName;
				}
				log.finest(bestName);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		log.info("searched for " + name + " found " + bestName);
		try {
			return selectItemByDisplayName(bestName);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private PrimeItem selectItemByDisplayName(String name) throws SQLException {
		getItemByDisplayName.setString(1, name);
		PrimeItem ret = null;
		try(ResultSet results = getItemByDisplayName.executeQuery()){
			if(results.next()){
				String uniqueName = results.getString("unique_name");
				String displayName = results.getString("display_name");
				boolean vaulted = results.getBoolean("vaulted");
				ret = new PrimeItem(uniqueName,displayName,vaulted);
			}
		}
		return ret;
	}


	@Override
	public void emptyTables() throws SQLException {
		try(Statement stmt = connection.createStatement()){
			stmt.execute("delete from items;");
		}
	}

	@Override
	public void close() throws SQLException {
		addItem.close();
		getItemByDisplayName.close();
		getAllItemNames.close();
		connection.close();
	}

	@Override
	public void addItem(String uniqueName, String displayName, boolean vaulted) throws SQLException {
		addItem.setInt(1,relicPrimaryKey++);
		addItem.setString(2,uniqueName);
		addItem.setString(3,displayName);
		addItem.setBoolean(4,vaulted);
		addItem.execute();
	}

	@Override
	public void setFastMode(boolean fastMode) throws SQLException {
		if(!fastMode) {
			connection.commit();
		}
		connection.setAutoCommit(!fastMode);
	}
}

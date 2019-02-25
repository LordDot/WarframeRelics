package warframeRelics.dataBase;

import java.sql.SQLException;

public interface IDataBase extends INameFixer{
	public void addItem(String name) throws SQLException;
	public void addDrop(String item, int relicType, String relic, int rarity) throws SQLException;
	public void addRelic(int type, String name) throws SQLException;
	public void setRelicVaulted(int type, String name, boolean vaulted) throws SQLException;
	public void setItemVaulted(String name, boolean vaulted) throws SQLException;
	public void updateItemData() throws SQLException;
	public boolean getItemVaulted(String name) throws SQLException;
	void setFastMode(boolean fastMode) throws SQLException;
}

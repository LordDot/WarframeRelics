package warframeRelics.dataBase;

import java.sql.SQLException;

public interface IDataBase extends INameFixer{
	void addItem(String uniqueName, String displayName, boolean vaulted) throws SQLException;
	void setFastMode(boolean fastMode) throws SQLException;
	void emptyTables() throws SQLException;
}

package warframeRelics.gui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.XMLFormatter;

public class WarframeRelicsMain {
	public static String version = "1.1.0.0";

	public static void main(String args[]) throws SecurityException, IOException {

		Logger rootLogger = Logger.getLogger("warframeRelics");
		
		//rootLogger.setLevel(Level.FINEST);
		
		
        FileHandler handler = new FileHandler("log.txt");
        handler.setFormatter(new SimpleFormatter());
		rootLogger.addHandler(handler);

		new WarframeRelics();
	}
}

package warframeRelics.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JProgressBar;
import javax.swing.plaf.ProgressBarUI;

import org.apache.log4j.Logger;

import net.sourceforge.tess4j.TesseractException;
import warframeRelics.dataBase.SQLLiteDataBase;
import warframeRelics.dataDownload.DataDownLoader;
import warframeRelics.pricing.Pricer;
import warframeRelics.pricing.WarframeMarket;
import warframeRelics.screenCapture.RelicReader;
import warframeRelics.screenCapture.ScreenBufferedImageProvider;

public class WarframeRelics {
	private static final Logger log = Logger.getLogger(WarframeRelics.class.getName());

	private Frame frame;
	private Button readButton;
	private Label[] labels;
	private PriceDisplayer[] prices;
	private Button updateButton;
	private JProgressBar progressBar;
	private Panel mainPanel;
	private Button debugButton;

	private RelicReader relicReader;
	private SQLLiteDataBase database;
	private Pricer pricer;
	private int debugImageCounter;

	public WarframeRelics() {
		frame = new Frame();
		frame.setSize(500, 350);
		frame.setTitle("Warframe Relics " + WarframeRelicsMain.version);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				e.getWindow().dispose();
				if (database != null) {
					try {
						database.close();
					} catch (SQLException e1) {
						e1.printStackTrace();
						log.error(e1);
					}
				}
			}
		});
		frame.setLayout(new BorderLayout());

		mainPanel = new Panel(new GridLayout(6, 2));

		labels = new Label[4];
		prices = new PriceDisplayer[4];
		for (int i = 0; i < 4; i++) {
			labels[i] = new Label();
			labels[i].setAlignment(Label.CENTER);
			mainPanel.add(labels[i]);
			prices[i] = new PriceDisplayer();
			mainPanel.add(prices[i]);
		}

		updateButton = new Button("Update Data");
		updateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(() -> {
						try {
							progressBar.setIndeterminate(true);
							database.emptyTables();
							DataDownLoader dl = new DataDownLoader(database);
							Set<String> wordList = dl.downLoadPartData();
							File f = new File("tessdata/eng.user-words");
							if (f.exists()) {
								f.delete();
							}
							f.createNewFile();
							try (FileWriter out = new FileWriter(f);) {
								for (String s : wordList) {
									out.append(s);
									out.append("\n");
								}
							}
							dl.downloadMissionData();
							relicReader = new RelicReader(database, new ScreenBufferedImageProvider());
						} catch (SQLException e1) {
							e1.printStackTrace();
							log.error(e1);
						} catch (IOException e2) {
							e2.printStackTrace();
							log.error(e2);
						} catch (AWTException e3) {
							e3.printStackTrace();
							log.error(e3);
						} finally {
							progressBar.setIndeterminate(false);
						}
					}
				).start();
			}
		});
		mainPanel.add(updateButton);

		readButton = new Button();
		readButton.setLabel("Read Rewards");
		readButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						progressBar.setIndeterminate(true);
						try {
							String[] rewards = relicReader.readRelics();
							for (int i = 0; i < 4; i++) {
								String labelText = rewards[i];
								if (database.getItemVaulted(rewards[i])) {
									labelText += " (v)";
								}
								labels[i].setText(labelText);
								if (rewards[i].equals("Forma Blueprint")) {
									prices[i].setPrices(null);
								} else {
									prices[i].setPrices(pricer.getPlat(rewards[i]));
								}
							}
						} catch (TesseractException e1) {
							e1.printStackTrace();
							log.error(e1);
						} catch (SQLException e1) {
							e1.printStackTrace();
							log.error(e1);
						} catch (Exception e) {
							e.printStackTrace();
							log.error(e);
						} finally {
							progressBar.setIndeterminate(false);
						}
					}
				}).start();
			}
		});
		mainPanel.add(readButton);

		debugButton = new Button();
		debugButton.setLabel("Debug info");
		debugButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						progressBar.setIndeterminate(true);
						try {
							File f = new File("./debug/image" + debugImageCounter++ + ".png");
							f.mkdirs();
							f.createNewFile();
							log.info("writing debug image number" + debugImageCounter);
							ImageIO.write(new Robot().createScreenCapture(new Rectangle(0, 0, 1920, 1080)), "png", f);
						} catch (IOException e) {
							e.printStackTrace();
							log.error(e);
						} catch (AWTException e) {
							e.printStackTrace();
							log.error(e);
						} finally {
							progressBar.setIndeterminate(false);
						}
					}
				}).start();
			}
		});
		mainPanel.add(debugButton);

		frame.add(mainPanel, BorderLayout.CENTER);

		progressBar = new JProgressBar();
		frame.add(progressBar, BorderLayout.PAGE_END);

		frame.setVisible(true);

		try {
			database = new SQLLiteDataBase("./db.db");
		} catch (SQLException e) {
			e.printStackTrace();
			log.error(e);
		}

		try {
			relicReader = new RelicReader(database, new ScreenBufferedImageProvider());
		} catch (AWTException e1) {
			e1.printStackTrace();
			log.error(e1);
		}
		pricer = new WarframeMarket();
	}
}

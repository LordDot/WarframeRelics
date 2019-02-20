package warframeRelics.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

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

	private RelicReader relicReader;
	private SQLLiteDataBase database;
	private Pricer pricer;

	public WarframeRelics() {
		frame = new Frame();
		frame.setSize(400, 300);
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

		mainPanel = new Panel(new GridLayout(5,2));

		

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
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							progressBar.setIndeterminate(true);
							database.emptyTables();
							DataDownLoader dl = new DataDownLoader(database);
							 dl.downLoadPartData();
							 dl.downloadMissionData();
						} catch (SQLException e1) {
							e1.printStackTrace();
							log.error(e1);
						} catch (IOException e) {
							e.printStackTrace();
							log.error(e);
						} finally {
							progressBar.setIndeterminate(false);
						}
					}
				}).start();
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
								if(rewards[i].equals("Forma Blueprint")) {
									prices[i].setPrices(null);
								}else {
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

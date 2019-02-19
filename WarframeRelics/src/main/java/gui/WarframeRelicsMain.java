package gui;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.imageio.ImageIO;

import ch.qos.logback.core.net.SyslogOutputStream;
import dataBase.IDataBase;
import dataBase.SQLLiteDataBase;
import dataDownload.DataDownLoader;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import pricing.WarframeMarket;
import screenCapture.BufferedImageProvider;
import screenCapture.ImageExtractor;
import screenCapture.RelicReader;

public class WarframeRelicsMain {
	public static void main(String args[]) throws TesseractException, AWTException, IOException {
		new WarframeRelics();
	}
}

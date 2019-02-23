package warframeRelics.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class FileExtractor {

	private String dataFile;

	public FileExtractor(String dataFile) {
		this.dataFile = dataFile;
	}

	public void extractFiles() throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(getClass().getClassLoader().getResourceAsStream(dataFile)))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] split = line.split(";");
				File target = new File(split[1].trim());
				if (!target.exists()) {
					try (InputStream in = getClass().getClassLoader().getResourceAsStream(split[0].trim())) {
						Files.copy(in, target.toPath());
					}
				}
			}
		}
	}
}

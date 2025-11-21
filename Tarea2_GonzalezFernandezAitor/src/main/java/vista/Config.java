package vista;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//creo esta clase para cargar las properties
public class Config {
	private static final Properties props = new Properties();

	static {

		try (InputStream in = Config.class.getClassLoader().getResourceAsStream("application.properties")) {
			if (in != null) {
				props.load(in);
			} else {

				try (FileInputStream fis = new FileInputStream("application.properties")) {
					props.load(fis);
				} catch (IOException e) {
					throw new RuntimeException("No se ha encontrado application.properties", e);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Error cargando application.properties: " + e.getMessage(), e);
		}
	}

	public static String get(String key) {
		return props.getProperty(key);
	}

	public static String getOrDefault(String key, String def) {
		return props.getProperty(key, def);
	}
}

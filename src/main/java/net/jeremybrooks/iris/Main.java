package net.jeremybrooks.iris;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * @author Jeremy Brooks
 */
public class Main {
  public static final String PROPERTY_SOURCE_DIRECTORY = "source.directory";
  public static final String PROPERTY_WINDOW_X = "window.x";
  public static final String PROPERTY_WINDOW_Y = "window.y";
  public static final String PROPERTY_WINDOW_HEIGHT = "window.height";
  public static final String PROPERTY_WINDOW_WIDTH = "window.width";

  private static Properties properties = new Properties();
  private static File propertiesFile;
  private static Logger logger = LogManager.getLogger();

  /**
   * Program entry point.
   *
   * @param args no command line arguments are supported.
   */
  public static void main(String... args) {
    logger.info(String.format("%s version %s starting", Main.class.getPackage().getImplementationTitle(),
        Main.class.getPackage().getImplementationVersion()));
    // set up preferences
    File configDir = new File(System.getProperty("user.home") + "/.iris");
    if (!configDir.exists()) {
      if (!configDir.mkdirs()) {
        error("Could not create directory " + configDir.getAbsolutePath(), null);
      }
    }
    propertiesFile = new File(configDir, "iris.properties");
    InputStream in = null;
    try {
      if (propertiesFile.exists()) {
        in = new FileInputStream(propertiesFile);
        properties.load(in);
      } else {
        properties.setProperty(PROPERTY_SOURCE_DIRECTORY, "");
        properties.setProperty(PROPERTY_WINDOW_WIDTH, "700");
        properties.setProperty(PROPERTY_WINDOW_HEIGHT, "400");
        properties.setProperty(PROPERTY_WINDOW_X, "50");
        properties.setProperty(PROPERTY_WINDOW_Y, "50");

        saveProperties();
      }
    } catch (Exception e) {
      error("Error loading properties file " + propertiesFile.getAbsolutePath() +
          "\n\nTry deleting the iris configuration directory and trying again.", e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (Exception e) {
          logger.warn("Error closing input stream.", e);
        }
      }
    }

    MainWindow mainWindow = new MainWindow();
    try {
      int height = Integer.parseInt(getProperty(PROPERTY_WINDOW_HEIGHT));
      int width = Integer.parseInt(getProperty(PROPERTY_WINDOW_WIDTH));
      int x = Integer.parseInt(getProperty(PROPERTY_WINDOW_X));
      int y = Integer.parseInt(getProperty(PROPERTY_WINDOW_Y));
      mainWindow.setSize(width, height);
      mainWindow.setLocation(x, y);
    } catch (Exception e) {
      mainWindow.setSize(700, 400);
      mainWindow.setLocation(50, 50);
    }
    mainWindow.setVisible(true);
    mainWindow.loadPlaylist();
    Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(mainWindow)));
  }


  private static void error(String errorMessage, Throwable cause) {
    logger.error("Exiting due to error " + errorMessage, cause);
    JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    System.exit(1);
  }

  /**
   * Set the key/value pair in properties.
   *
   * @param key   the key to set.
   * @param value the value for the key.
   */
  public static void setProperty(String key, String value) {
    properties.setProperty(key, value);
  }

  /**
   * Get the value for the specified key.
   *
   * @param key key to get value for.
   * @return current value for the key.
   */
  public static String getProperty(String key) {
    return properties.getProperty(key);
  }

  /**
   * Save the current configuration.
   */
  public static void saveProperties() {
    OutputStream out = null;
    try {
      logger.info("Saving properties " + properties);
      out = new FileOutputStream(propertiesFile);
      properties.store(out, "iris properties");
      out.flush();
      out.close();
    } catch (Exception e) {
      logger.error("Error saving properties.", e);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (Exception e) {
          logger.warn("Error closing output stream.", e);
        }
      }
    }
  }
}

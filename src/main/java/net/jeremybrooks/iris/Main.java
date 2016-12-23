package net.jeremybrooks.iris;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JOptionPane;

/**
 * @author Jeremy Brooks
 */
public class Main {
  public static final String PROPERTY_SOURCE_DIRECTORY = "source.directory";

  private static Properties properties = new Properties();
  private static File propertiesFile;

  /**
   * Program entry point.
   * @param args no command line arguments are supported.
   */
  public static void main(String... args) {
    // set up preferences
    File prefs = new File(System.getProperty("user.home") + "/.iris");
    if (!prefs.exists()) {
       if (!prefs.mkdirs()) {
         error("Could not create directory " + prefs.getAbsolutePath());
       }
       propertiesFile = new File(prefs, "iris.properties");
       properties.setProperty(PROPERTY_SOURCE_DIRECTORY, "");
       saveProperties();
    } else {
      InputStream in = null;
      try {
        propertiesFile = new File(prefs, "iris.properties");
        in = new FileInputStream(propertiesFile);
        properties.load(in);
      } catch (Exception e) {
        error("Error loading properties file " + propertiesFile.getAbsolutePath() +
        "\n\nTry deleting the iris configuration directory and trying again.");
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (Exception e) {
            //ignore
          }
        }
      }
    }
    new MainWindow().setVisible(true);
  }


  private static void error(String errorMessage) {
    JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    System.exit(1);
  }

  /**
   * Set the key/value pair in properties.
   * @param key the key to set.
   * @param value the value for the key.
   */
  public static void setProperty(String key, String value) {
    properties.setProperty(key, value);
  }

  /**
   * Get the value for the specified key.
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
      out = new FileOutputStream(propertiesFile);
      properties.store(out, "iris properties");
      out.flush();
      out.close();
    } catch (Exception e) {
      System.out.println("Error saving properties.");
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
  }
}

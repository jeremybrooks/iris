package net.jeremybrooks.iris;

public class ShutdownHook implements Runnable {
  private MainWindow mainWindow;

  /**
   * Create a new Shutdown Hook instance.
   * @param mainWindow the main window.
   */
  public ShutdownHook(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
  }

  /**
   * Save the window position and size at exit.
   */
  public void run() {
    Main.setProperty(Main.PROPERTY_WINDOW_WIDTH, Integer.toString(mainWindow.getWidth()));
    Main.setProperty(Main.PROPERTY_WINDOW_HEIGHT, Integer.toString(mainWindow.getHeight()));
    Main.setProperty(Main.PROPERTY_WINDOW_X, Integer.toString(mainWindow.getX()));
    Main.setProperty(Main.PROPERTY_WINDOW_Y, Integer.toString(mainWindow.getY()));
    Main.saveProperties();
  }
}

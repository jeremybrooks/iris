package net.jeremybrooks.iris;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jeremy Brooks
 */
public class MainWindow extends JFrame {
  private GraphicsDevice[] devices;
  private JWindow imageDisplayWindow;
  private Logger logger = LogManager.getLogger();

  /**
   * Create the main window and fire off the image load.
   */
  public MainWindow() {
    devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    logger.info("Found " + devices.length + " graphics devices.");
    int i = 0;
    for (GraphicsDevice device : devices) {
      StringBuilder sb = new StringBuilder(MessageFormat.format("Device {0} ID: ", i)).append(device.getIDstring());
      int j = 0;
      for (GraphicsConfiguration configuration : device.getConfigurations()) {
        sb.append("; Configuration ").append(j).append(" bounds: ").append(configuration.getBounds());
        j++;
      }
      logger.info(sb);
    }
    initComponents();
    List<Image> images = new ArrayList<>();
    try {
      images.add(ImageIO.read(getClass().getResource("/icon_16.png")));
      images.add(ImageIO.read(getClass().getResource("/icon_32.png")));
      images.add(ImageIO.read(getClass().getResource("/icon_48.png")));
      images.add(ImageIO.read(getClass().getResource("/icon_128.png")));
    } catch (Exception e) {
      logger.warn("Error loading images for icon.", e);
    }
    this.setIconImages(images);
    this.setTitle(MainWindow.class.getPackage().getImplementationTitle() + " : " +
        MainWindow.class.getPackage().getImplementationVersion());
    this.imageList.setCellRenderer(new LabelListCellRenderer());
  }

  private void menuItemQuitActionPerformed() {
    System.exit(0);
  }

  private void menuItemSourceDirectoryActionPerformed() {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setCurrentDirectory(new File(Main.getProperty("source.directory")));
    chooser.setMultiSelectionEnabled(false);
    int result = chooser.showDialog(this, "OK");
    if (result == JFileChooser.APPROVE_OPTION) {
      Main.setProperty("source.directory", chooser.getSelectedFile().getAbsolutePath());
      Main.saveProperties();
      this.loadPlaylist();
    }
  }

  /**
   * Load the images from the source directory.
   * Warn the user if there is no source directory or if no files were found in the directory.
   */
  void loadPlaylist() {
    ImageCache.getInstance().clearCache();
    String source = Main.getProperty(Main.PROPERTY_SOURCE_DIRECTORY);
    if (source.trim().length() == 0) {
      this.btnHide.setEnabled(false);
      this.btnShow.setEnabled(false);
      this.statusBar.setText("No source directory.");
      JOptionPane.showMessageDialog(this,
          "No image source directory is selected.\n" +
              "Go to File -> Image Source Directory to select where your images will come from.",
          "No Files Loaded",
          JOptionPane.ERROR_MESSAGE);
    } else {
      btnHide.setEnabled(false);
      btnShow.setEnabled(false);
      imageList.setModel(new DefaultListModel<>());
      new ImageProcessor(source).execute();
    }
  }


  private void btnPlayActionPerformed() {
    this.displaySelectedImage();
  }

  /*
   * Display the currently selected image on the monitor that is NOT displaying this window.
   * If there is only one available monitor, the image will be displayed on that monitor.
   */
  private void displaySelectedImage() {
    if (this.imageList.getModel().getSize() > 0) {
      GraphicsDevice currentDevice = this.getGraphicsConfiguration().getDevice();
      GraphicsDevice targetDevice = currentDevice;
      for (GraphicsDevice device : this.devices) {
        if (!device.equals(currentDevice)) {
          targetDevice = device;
          break;
        }
      }
      this.logger.info(String.format("Current device: %s; Target device: %s",
          currentDevice.getIDstring(), targetDevice.getIDstring()));
      File f = this.imageList.getSelectedValue();

      if (this.imageDisplayWindow != null) {
        this.imageDisplayWindow.setVisible(false);
        this.imageDisplayWindow.dispose();
      }

      this.imageDisplayWindow = new JWindow(targetDevice.getDefaultConfiguration());
      for (GraphicsConfiguration configuration : targetDevice.getConfigurations()) {
        Rectangle gcBounds = configuration.getBounds();
        int xoffs = gcBounds.x;
        int yoffs = gcBounds.y;

        try {
          BufferedImage img = ImageIO.read(f);
          int size;
          Scalr.Mode mode;

          this.logger.info(String.format("Target display size is %d x %d",
              gcBounds.width, gcBounds.height));
          this.logger.info(String.format("Image size is %d x %d",
              img.getWidth(), img.getHeight()));

          if (img.getWidth() > img.getHeight()) {
            // landscape
            size = gcBounds.width;
            mode = Scalr.Mode.FIT_TO_WIDTH;
            // calculate final image height
            int finalHeight = (int)(img.getHeight() * ((float)gcBounds.width / img.getWidth()));
            this.logger.info(String.format("Landscape: Resize to WIDTH %d would produce image %dx%d",
                size, size, finalHeight));
            if (finalHeight > gcBounds.height) {
              this.logger.info("Too tall; will size to height instead.");
              size = gcBounds.height;
              mode = Scalr.Mode.FIT_TO_HEIGHT;
            }
          } else {
            // portrait
            size = gcBounds.height;
            mode = Scalr.Mode.FIT_TO_HEIGHT;
            // calculate final image width
            int finalWidth = (int)(img.getWidth() * ((float)gcBounds.height / img.getHeight()));
            this.logger.info(String.format("Portrait: Resize to HEIGHT %d would produce image %dx%d",
                size, size, finalWidth));
            if (finalWidth > gcBounds.width) {
              this.logger.info("Too wide; will size to width instead.");
              size = gcBounds.width;
              mode = Scalr.Mode.FIT_TO_WIDTH;
            }
          }
          this.logger.info(String.format("Scaling to %d pixels for mode %s",
              size, mode == Scalr.Mode.FIT_TO_HEIGHT ? "FIT_TO_HEIGHT" : "FIT_TO_WIDTH"));
          Image resized = Scalr.resize(img, mode, size);
          this.logger.info(String.format("New size is %d x %d",
              resized.getWidth(null), resized.getHeight(null)));

          img.flush();

          // center based on target display size, add the label, and display the window
          int xPosition = (gcBounds.width - resized.getWidth(null)) / 2;
          int yPosition = (gcBounds.height - resized.getHeight(null)) / 2;
          this.imageDisplayWindow.setLocation(xoffs + xPosition, yoffs + yPosition);
          this.imageDisplayWindow.add(new JLabel(new ImageIcon(resized)));
          this.imageDisplayWindow.pack();
          this.imageDisplayWindow.setVisible(true);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(this,
              "Error displaying image " + f.getAbsolutePath() + "\n\n" + e,
              "Error Displaying Image", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  private void btnHideActionPerformed() {
    if (this.imageDisplayWindow != null) {
      this.imageDisplayWindow.setVisible(false);
      this.imageDisplayWindow.dispose();
      this.imageDisplayWindow = null;
    }
  }

  private void menuItemRefreshActionPerformed() {
    this.loadPlaylist();
  }

  /**
   * Look at all the files in the source directory. If a file is a supported image type,
   * create a cached thumbnail version and put the file in the list model.
   */
  class ImageProcessor extends SwingWorker<Void, File> {
    private String source;

    ImageProcessor(String source) {
      this.source = source;
    }

    @Override
    protected Void doInBackground() throws Exception {
      statusBar.setIcon(new ImageIcon(getClass().getResource("/spinner.gif")));
      // list files and sort them
      File[] files = new File(source).listFiles();
      if (files == null) {
        files = new File[0];
      }
      Arrays.sort(files, new FilenameComparator());
      for (File f : files) {
        logger.info("Got file " + f.getAbsolutePath());
        String name = f.getName();
        if (name.toLowerCase().endsWith(".jpg") ||
            name.toLowerCase().endsWith(".jpeg") ||
            name.toLowerCase().endsWith(".png")) {
          logger.info("Creating thumbnail for " + name);
          SwingUtilities.invokeLater(() -> statusBar.setText("Processing " + name + "..."));
          File file = new File(source, name);
          BufferedImage bufferedImage = ImageIO.read(file);
          Thumbnail thumbnail = new Thumbnail(Scalr.resize(bufferedImage, Scalr.Mode.FIT_TO_WIDTH, 100),
              bufferedImage.getWidth(), bufferedImage.getHeight());
          ImageCache.getInstance().addImage(thumbnail, name);
          bufferedImage.flush();
          publish(file);
        } else {
          logger.info("Unsupported file type; ignoring.");
        }
      }
      return null;
    }

    @Override
    protected void process(List<File> chunks) {
      for (File file : chunks) {
        ((DefaultListModel<File>)imageList.getModel()).addElement(file);
      }
    }

    @Override
    protected void done() {
      statusBar.setIcon(null);
      int size = ((DefaultListModel<File>)imageList.getModel()).size();
      statusBar.setText(source + ": " + size + " files");
      imageList.setSelectedIndex(0);
      if (size == 0) {
        btnHide.setEnabled(false);
        btnShow.setEnabled(false);
        JOptionPane.showMessageDialog(MainWindow.this,
            "No valid image files were found in directory " + source +
                ".\nAdd some files and go to File -> Refresh to reload the list.",
            "No Files Found",
            JOptionPane.ERROR_MESSAGE);
      } else {
        btnHide.setEnabled(true);
        btnShow.setEnabled(true);
      }
    }
  }

  private void initComponents() {
    // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
    menuBar1 = new JMenuBar();
    menu1 = new JMenu();
    menuItemSourceDirectory = new JMenuItem();
    menuItemRefresh = new JMenuItem();
    menuItemQuit = new JMenuItem();
    scrollPane1 = new JScrollPane();
    imageList = new JList<>();
    panel1 = new JPanel();
    btnShow = new JButton();
    btnHide = new JButton();
    statusBar = new JLabel();

    //======== this ========
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    Container contentPane = getContentPane();
    contentPane.setLayout(new GridBagLayout());
    ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0};
    ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
    ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
    ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};

    //======== menuBar1 ========
    {

      //======== menu1 ========
      {
        menu1.setText("File");

        //---- menuItemSourceDirectory ----
        menuItemSourceDirectory.setText("Image Source Directory");
        menuItemSourceDirectory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItemSourceDirectory.addActionListener(e -> menuItemSourceDirectoryActionPerformed());
        menu1.add(menuItemSourceDirectory);

        //---- menuItemRefresh ----
        menuItemRefresh.setText("Refresh");
        menuItemRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        menuItemRefresh.addActionListener(e -> menuItemRefreshActionPerformed());
        menu1.add(menuItemRefresh);
        menu1.addSeparator();

        //---- menuItemQuit ----
        menuItemQuit.setText("Quit");
        menuItemQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItemQuit.addActionListener(e -> menuItemQuitActionPerformed());
        menu1.add(menuItemQuit);
      }
      menuBar1.add(menu1);
    }
    setJMenuBar(menuBar1);

    //======== scrollPane1 ========
    {

      //---- imageList ----
      imageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      scrollPane1.setViewportView(imageList);
    }
    contentPane.add(scrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.BOTH,
      new Insets(0, 0, 0, 0), 0, 0));

    //======== panel1 ========
    {
      panel1.setLayout(new FlowLayout());

      //---- btnShow ----
      btnShow.setText("Show");
      btnShow.setEnabled(false);
      btnShow.addActionListener(e -> btnPlayActionPerformed());
      panel1.add(btnShow);

      //---- btnHide ----
      btnHide.setText("Hide");
      btnHide.setEnabled(false);
      btnHide.addActionListener(e -> btnHideActionPerformed());
      panel1.add(btnHide);
    }
    contentPane.add(panel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.BOTH,
      new Insets(0, 0, 0, 0), 0, 0));

    //---- statusBar ----
    statusBar.setText("Loading...");
    statusBar.setForeground(Color.black);
    statusBar.setFont(statusBar.getFont().deriveFont(10f));
    contentPane.add(statusBar, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.BOTH,
      new Insets(0, 0, 0, 0), 0, 0));
    setSize(700, 400);
    setLocationRelativeTo(getOwner());
    // JFormDesigner - End of component initialization  //GEN-END:initComponents
  }

  // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
  private JMenuBar menuBar1;
  private JMenu menu1;
  private JMenuItem menuItemSourceDirectory;
  private JMenuItem menuItemRefresh;
  private JMenuItem menuItemQuit;
  private JScrollPane scrollPane1;
  private JList<File> imageList;
  private JPanel panel1;
  private JButton btnShow;
  private JButton btnHide;
  private JLabel statusBar;
  // JFormDesigner - End of variables declaration  //GEN-END:variables
}

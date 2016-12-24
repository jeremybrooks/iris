/*
 * Created by JFormDesigner on Wed Dec 21 14:55:57 PST 2016
 */

package net.jeremybrooks.iris;

import org.imgscalr.Scalr;

import java.awt.Canvas;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

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
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

/**
 * @author Jeremy Brooks
 */
public class MainWindow extends JFrame {
  private GraphicsDevice[] devices;
  private JWindow window;

  /**
   * Create the main window and fire off the image load.
   */
  public MainWindow() {
    devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    initComponents();
    this.setTitle(MainWindow.class.getPackage().getImplementationTitle() + " : " +
        MainWindow.class.getPackage().getImplementationVersion());
    this.imageList.setCellRenderer(new LabelListCellRenderer());
    loadPlaylist();
  }

  private void menuItemQuitActionPerformed(ActionEvent e) {
    System.exit(0);
  }

  private void menuItemSourceDirectoryActionPerformed(ActionEvent e) {
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

  /*
   * Load the images from the source directory.
   * Warn the user if there is no source directory or if no files were found in the directory.
   */
  private void loadPlaylist() {
    String source = Main.getProperty(Main.PROPERTY_SOURCE_DIRECTORY);
    if (source.trim().length() == 0) {
      this.statusBar.setText("No source directory.");
      JOptionPane.showMessageDialog(this,
          "No image source directory is selected.\n" +
              "Go to File -> Image Source Directory to select where your images will come from.",
          "No Files Loaded",
          JOptionPane.ERROR_MESSAGE);
    } else {
      // list files and sort them
      File[] files = new File(source).listFiles();
      if (files == null) {
        files = new File[0];
      }
      Arrays.sort(files, new Comparator<File>() {
        public int compare(File o1, File o2) {
          int n1 = extractNumber(o1.getName());
          int n2 = extractNumber(o2.getName());
          return n1 - n2;
        }

        private int extractNumber(String name) {
          int i = 0;
          try {
            int s = name.indexOf('_') + 1;
            int e = name.lastIndexOf('.');
            String number = name.substring(s, e);
            i = Integer.parseInt(number);
          } catch (Exception e) {
            i = 0; // if filename does not match the format
            // then default to 0
          }
          return i;
        }
      });

      // add files to the model if they are .jpg, .jpeg, or .png
      DefaultListModel<File> model = new DefaultListModel<File>();
      for (File f : files) {
        String name = f.getName();
        if (name.toLowerCase().endsWith(".jpg") ||
            name.toLowerCase().endsWith(".jpeg") ||
            name.toLowerCase().endsWith(".png")) {
          File file = new File(Main.getProperty(Main.PROPERTY_SOURCE_DIRECTORY), name);
          model.addElement(file);
        }
      }

      // update the status bar and put the model on the list
      this.statusBar.setText(source + ": " + model.size() + " files");
      this.imageList.setModel(model);
      this.imageList.setSelectedIndex(0);
      if (model.size() == 0) {
        JOptionPane.showMessageDialog(this,
            "No valid image files were found in directory " + source +
                ".\nAdd some files and go to File -> Refresh to reload the list.",
            "No Files Found",
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void btnPlayActionPerformed(ActionEvent e) {
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
      File f = (File) this.imageList.getSelectedValue();

      if (window != null) {
        window.setVisible(false);
        window.dispose();
      }

      window = new JWindow(targetDevice.getDefaultConfiguration());
      for (GraphicsConfiguration configuration : targetDevice.getConfigurations()) {
        Canvas c = new Canvas(configuration);
        Rectangle gcBounds = configuration.getBounds();
        int xoffs = gcBounds.x;
        int yoffs = gcBounds.y;
        window.getContentPane().add(c);

        try {
          BufferedImage img = ImageIO.read(f);
          int size;
          Scalr.Mode mode;

          // scale to fit the shortest of width/height
          if (gcBounds.width > gcBounds.height) {
            size = gcBounds.height;
            mode = Scalr.Mode.FIT_TO_HEIGHT;
          } else {
            size = gcBounds.width;
            mode = Scalr.Mode.FIT_TO_WIDTH;
          }
          Image resized = Scalr.resize(img, mode, size);
          img.flush();

          JLabel lbl = new JLabel();
          lbl.setIcon(new ImageIcon(resized));

          int xPosition = (gcBounds.width - resized.getWidth(null)) / 2;
          int yPosition = (gcBounds.height - resized.getHeight(null)) / 2;
          window.setLocation(xoffs + xPosition, yoffs + yPosition);
          window.add(lbl);
          window.pack();
          window.setVisible(true);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(this,
              "Error displaying image " + f.getAbsolutePath() + "\n\n" + e,
              "Error Displaying Image", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  private void btnHideActionPerformed(ActionEvent e) {
    if (this.window != null) {
      this.window.setVisible(false);
      this.window.dispose();
      this.window = null;
    }
  }

  private void menuItemRefreshActionPerformed(ActionEvent e) {
    this.loadPlaylist();
  }

  private void initComponents() {
    // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
    menuBar1 = new JMenuBar();
    menu1 = new JMenu();
    menuItemSourceDirectory = new JMenuItem();
    menuItemRefresh = new JMenuItem();
    menuItemQuit = new JMenuItem();
    scrollPane1 = new JScrollPane();
    imageList = new JList();
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
        menuItemSourceDirectory.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            menuItemSourceDirectoryActionPerformed(e);
          }
        });
        menu1.add(menuItemSourceDirectory);

        //---- menuItemRefresh ----
        menuItemRefresh.setText("Refresh");
        menuItemRefresh.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            menuItemRefreshActionPerformed(e);
          }
        });
        menu1.add(menuItemRefresh);
        menu1.addSeparator();

        //---- menuItemQuit ----
        menuItemQuit.setText("Quit");
        menuItemQuit.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            menuItemQuitActionPerformed(e);
          }
        });
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
      btnShow.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          btnPlayActionPerformed(e);
        }
      });
      panel1.add(btnShow);

      //---- btnHide ----
      btnHide.setText("Hide");
      btnHide.setEnabled(false);
      btnHide.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          btnHideActionPerformed(e);
        }
      });
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
  private JList imageList;
  private JPanel panel1;
  private JButton btnShow;
  private JButton btnHide;
  private JLabel statusBar;
  // JFormDesigner - End of variables declaration  //GEN-END:variables
}

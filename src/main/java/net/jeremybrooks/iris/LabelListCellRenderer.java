package net.jeremybrooks.iris;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;

public class LabelListCellRenderer extends JLabel implements ListCellRenderer<File> {
  private Logger logger = LogManager.getLogger();

  public Component getListCellRendererComponent(JList<? extends File> list, File value, int index, boolean isSelected,
                                                boolean cellHasFocus) {
    String name = value.getName().substring(0, value.getName().lastIndexOf('.'));
    this.setText(name);
    this.setOpaque(true);
    if (isSelected) {
      setBackground(Color.blue);
      setForeground(Color.white);
    } else {
      setBackground(Color.white);
      setForeground(Color.black);
    }

    try {
      Thumbnail thumbnail = ImageCache.getInstance().getImage(name);
      if (thumbnail == null) {
        BufferedImage bufferedImage = ImageIO.read(value);
        thumbnail = new Thumbnail(Scalr.resize(bufferedImage, Scalr.Mode.FIT_TO_WIDTH, 100),
            bufferedImage.getWidth(), bufferedImage.getHeight());
        ImageCache.getInstance().addImage(thumbnail, name);
        bufferedImage.flush();
      }
      this.setIcon(thumbnail);
      this.setText(name + "   [" + thumbnail.getOriginalWidth() + "x" + thumbnail.getOriginalHeight() + "]");
    } catch (Exception e) {
      logger.error("Error loading file " + value.getName(), e);
      this.setIcon(null);
      this.setBackground(Color.white);
      this.setForeground(Color.red);
      this.setText("Cannot load file " + value.getName());
    }
    return this;
  }
}

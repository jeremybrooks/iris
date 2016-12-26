package net.jeremybrooks.iris;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Color;
import java.awt.Component;
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

    Thumbnail thumbnail = ImageCache.getInstance().getImage(value.getName());
    if (thumbnail == null) {
      // NOTE: this should never happen. The thumbnail is cached before the file is added to the model.
      logger.error("Missing thumbnail " + value.getName());
      this.setIcon(null);
      this.setText(name);
    } else {
      this.setIcon(thumbnail);
      this.setText(name + "   [" + thumbnail.getOriginalWidth() + "x" + thumbnail.getOriginalHeight() + "]");
    }
    return this;
  }
}

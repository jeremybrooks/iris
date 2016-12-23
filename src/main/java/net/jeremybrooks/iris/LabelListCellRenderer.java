package net.jeremybrooks.iris;

import org.imgscalr.Scalr;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class LabelListCellRenderer extends JLabel implements ListCellRenderer<File> {

  public Component getListCellRendererComponent(JList<? extends File> list, File value, int index, boolean isSelected, boolean cellHasFocus) {
    this.setText(value.getName().substring(0, value.getName().lastIndexOf('.')));
    this.setOpaque(true);
    if (isSelected) {
      setBackground(Color.blue);
      setForeground(Color.white);
    } else {
      setBackground(Color.white);
      setForeground(Color.black);
    }

    try {
      BufferedImage img = ImageIO.read(value);
      this.setIcon(new ImageIcon(Scalr.resize(img, Scalr.Mode.FIT_TO_WIDTH, 100)));
      this.setText(this.getText() + "   [" + img.getWidth() + "x" + img.getHeight() + "]");
      img.flush();
    } catch (Exception e) {
      this.setIcon(null);
      this.setBackground(Color.white);
      this.setForeground(Color.red);
      this.setText("Cannot load file " + value.getName());
    }
    return this;
  }
}

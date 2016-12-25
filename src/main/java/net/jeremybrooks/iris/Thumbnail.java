package net.jeremybrooks.iris;

import java.awt.Image;
import javax.swing.ImageIcon;

/**
 * An ImageIcon that knows the original size of the image it represents.
 */
public class Thumbnail extends ImageIcon {
  private int originalWidth;
  private int originalHeight;

  public Thumbnail(Image image, int originalWidth, int originalHeight) {
    super(image);
    this.originalHeight = originalHeight;
    this.originalWidth = originalWidth;
  }

  /**
   * The original width of the image this thumbnail represents.
   * @return the original width.
   */
  public int getOriginalWidth() { return this.originalWidth;}

  /**
   * The original height of the image this thumbnail represents.
   * @return the original height.
   */
  public int getOriginalHeight() {return this.originalHeight;}
}

package net.jeremybrooks.iris;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple image cache.
 * Implemented as a singleton.
 */
public class ImageCache {
  private Logger logger = LogManager.getLogger();
  private Map<String, Thumbnail> cache;
  private static ImageCache instance;

  /**
   * Get the image cache instance.
   * @return image cache instance.
   */
  public static ImageCache getInstance() {
    if (instance == null) {
      instance = new ImageCache();
    }
    return instance;
  }

  private ImageCache() {
    this.cache = new HashMap<>();
  }

  /**
   * Add an image to the cache.
   * Duplicate images will be ignored.
   * @param thumbnail thumbnail to add.
   * @param name name of the image.
   */
  public void addImage(Thumbnail thumbnail, String name) {
    if (this.cache.containsKey(name)) {
      logger.info("Image cache already contains " + name + "; not adding.");
    } else {
      logger.info("Adding " + name + " to image cache.");
      this.cache.put(name, thumbnail);
    }
  }

  /**
   * Get an image from the cache.
   * @param name name of the image to get.
   * @return thumbnail image matching the name, or null if the image doesn't exist.
   */
  public Thumbnail getImage(String name) {
    return this.cache.get(name);
  }

  /**
   * Remove all thumbnails from cache.
   */
  public void clearCache() {
    this.cache.clear();
    this.logger.info("Cache cleared.");
  }
}

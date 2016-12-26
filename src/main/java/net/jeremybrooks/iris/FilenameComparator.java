package net.jeremybrooks.iris;

import java.io.File;
import java.util.Comparator;

public class FilenameComparator implements Comparator<File> {
  public int compare(File o1, File o2) {
    int n1 = extractNumber(o1.getName());
    int n2 = extractNumber(o2.getName());
    return n1 - n2;
  }

  private int extractNumber(String name) {
    int i;
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
}

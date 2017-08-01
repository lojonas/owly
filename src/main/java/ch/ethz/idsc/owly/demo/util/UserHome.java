// code by jph
package ch.ethz.idsc.owly.demo.util;

import java.io.File;

public enum UserHome {
  ;
  /** @param filename
   * @return */
  public static File file(String filename) {
    return new File(System.getProperty("user.home"), filename);
  }

  /** @param filename
   * @return */
  public static File Pictures(String filename) {
    File directory = file("Pictures");
    directory.mkdir();
    return new File(directory, filename);
  }
}

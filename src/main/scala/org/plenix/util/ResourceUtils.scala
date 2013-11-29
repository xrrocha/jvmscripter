package org.plenix.util

object ResourceUtils {
  def loadResource(resourceName: String, classLoader: ClassLoader = getClass.getClassLoader): Option[String] = {
    val is = classLoader.getResourceAsStream(resourceName)
    Option(is).map { is =>
      val buffer = new Array[Byte](4096)
      val baos = new java.io.ByteArrayOutputStream
      Iterator.
        continually(is.read(buffer)).
        takeWhile(_ != -1).
        filter(_ > 0).
        foreach(baos.write(buffer, 0, _))
      baos.toString
    }
  }
}
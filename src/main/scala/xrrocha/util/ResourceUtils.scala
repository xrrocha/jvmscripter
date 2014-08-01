package xrrocha.util

import java.io.InputStream

object ResourceUtils {
  def getResources(resourceName: String, classLoader: ClassLoader = getClass.getClassLoader): Seq[InputStream] = {
    import collection.JavaConversions._
    classLoader.getResources(resourceName).map(_.openStream).toSeq
  }

  def loadResource(resourceName: String, classLoader: ClassLoader = getClass.getClassLoader): Option[String] = {
    val is = classLoader.getResourceAsStream(resourceName)
    Option(is).map(inputStream2String)
  }

  def inputStream2String(is: InputStream) = {
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
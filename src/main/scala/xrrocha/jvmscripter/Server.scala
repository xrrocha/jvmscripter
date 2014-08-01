package xrrocha.jvmscripter

import akka.actor._
import akka.io.IO
import spray.can.Http

/**
 * Provides a RESTful remote scripting server for a given set of
 * scripting bindings on a given port number.
 * 
 * Upon starting an actor service is bound to an HTTP service on
 * the local host.
 * 
 * Upon stopping the actor service is shutdown.
 */
class Server(serverBindings: Map[String, Any] = Map(), portNumber: Int = 4269) {
  /**
   * The named remote scripting actor system
   */
  implicit val system = ActorSystem("jvmscripter")

  /**
   * The actor properties for the remote scripting service, including the provided
   * script bindings.
   */
  val props = Props(classOf[JVMScripter], serverBindings)
  /**
   * The actor system
   */
  val service = system.actorOf(props, "jvmscripter-service")

  /**
   * Start the actor system as an HTTP service on the local host and the given
   * port number
   */
  def start() {
    IO(Http) ! Http.Bind(service, "localhost", port = portNumber)
  }

  /**
   * Shutdown the HTTP service by shutting down the actor system
   */
  def stop() {
    system.shutdown()
  }
}

/**
 * The `Server` companion object. In addition to providing a constructor method, this object
 * is also an `App` that runs the service with bare bindings containing only the tool's
 * version number.
 */
object Server extends App {
  def apply(serverBindings: Map[String, Any], portNumber: Int) = new Server(serverBindings, portNumber)

  // Test server
  new Server(Map("jvmscripterVersion" -> "1.0-SNAPSHOT")).start()
}

class JVMSServerStarter extends java.util.Properties with java.lang.Runnable with java.io.Closeable {
  import scala.collection.JavaConversions._

  var server: Server = _

  def run() {
    val bindings = {
      for {
        key <- keySet
        if key.isInstanceOf[String]
        propertyName = key.toString
        if !propertyName.startsWith("jvms.")
        propertyValue = get(propertyName)
      } yield propertyName -> propertyValue
    }.toMap

    val portNumber = getProperty("jvms.portNumber", "4269").toInt

    server = new Server(bindings, portNumber)
    server.start()
  }

  def close() {
    server.stop()
  }
}

/**
 * Remote scripting actor implementation accepting a set of script bindings.
 */
class JVMScripter(serverBindings: Map[String, Any]) extends JVMScripterServiceActor {
  override lazy val scriptManager: ScriptManager = new ScriptManager {
    override lazy val bindings: Map[String, Any] = serverBindings
  }
}

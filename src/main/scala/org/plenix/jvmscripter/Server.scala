package org.plenix.jvmscripter

import akka.actor.{ ActorSystem, Props }
import akka.io.IO
import spray.can.Http
import akka.actor._

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
  def apply(serverBindings: Map[String, Any] = Map(), portNumber: Int = 4269) = new Server(serverBindings, portNumber)

  // Test server
  new Server(Map("jvmscripterVersion" -> "1.0-SNAPSHOT")).start()
}

/**
 * Remote scripting actor implementation accepting a set of script bindings.
 */
class JVMScripter(serverBindings: Map[String, Any]) extends JVMScripterServiceActor {
  override lazy val scriptManager: ScriptManager = new ScriptManager {
    override lazy val bindings: Map[String, Any] = serverBindings
  }
}

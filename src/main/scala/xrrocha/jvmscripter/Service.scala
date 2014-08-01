package xrrocha.jvmscripter

import akka.actor.Actor
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.json.DefaultJsonProtocol
import spray.routing.Directive.pimpApply
import spray.routing.HttpService

/**
 * The JSON protocol for case class formats
 */
object JsonProtocol extends DefaultJsonProtocol {
  implicit val languageInfoFormat = jsonFormat5(LanguageInfo)
  implicit val scripterInfoFormat = jsonFormat2(ScripterInfo)
  implicit val errorResultFormat = jsonFormat3(ScriptError)
  implicit val scriptResultFormat = jsonFormat3(ScriptResult)
}

/**
 * The trait mixing up Akka actor and remote scripting REST service
 */
trait JVMScripterServiceActor extends Actor with JVMScripterService {
  def actorRefFactory = context
  def receive = runRoute(jvmScripterRoute)
}

/**
 * The remote scripting REST Spray service. This trait simply combines the
 * REST services as such with the minimal static resource service
 */
trait JVMScripterService extends HttpService with RestResources with StaticResources {
  val jvmScripterRoute = restResources ~ staticResources
}

/**
 * The REST scripting services.
 */
trait RestResources extends HttpService {
  import JsonProtocol._
  
  def scriptManager: ScriptManager

  val restResources =
    rejectEmptyResponse {
      pathPrefix("api") {
        pathPrefix("languages") {
          path(Segment) { name =>
            get {
              complete(Languages.languageInfoFor(name))
            }
          } ~
            get {
              complete(Languages.languageInfos)
            }
        } ~
          pathPrefix("scripters") {
            path(Segment) { id =>
              get {
                complete(scriptManager.scripterInfoFor(id))
              }
            } ~
              get {
                complete(scriptManager.scripterInfos)
              } ~
              put {
                entity(as[String]) { languageName =>
                  complete(scriptManager.newScripter(languageName))
                }
              } ~
              path(Segment) { id =>
                post {
                  entity(as[String]) { script =>
                    complete(scriptManager.executeScript(id, script))
                  }
                } ~
                  delete {
                    complete(scriptManager.removeScripter(id))
                  }
              }
          }
      }
    }
}

/**
 * Static resources: AngularJS application files
 */
trait StaticResources extends HttpService {
  val baseDirectory = "www"

  val staticResources =
    get {
      path(Rest) { path =>
        val resourceName =
          if (path == "" || path == "/") "index.html"
          else path
        getFromResource(s"${baseDirectory}/${resourceName}")
      }
    }
}

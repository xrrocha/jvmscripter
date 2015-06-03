package xrrocha.jvmscripter

import java.io.{ByteArrayOutputStream, IOException, OutputStreamWriter}
import javax.script.{ScriptContext, ScriptEngine, ScriptEngineManager, ScriptException, SimpleBindings, SimpleScriptContext}

import com.typesafe.scalalogging.LazyLogging

trait ScriptEnginePreprocessor {
  def preprocessEngine(engine: ScriptEngine): Unit
  def preprocessScript(engine: ScriptEngine, script: String): Unit
}
case class Language(
  name: String,
  description: String,
  syntax: String,
  extension: String,
  version: String,
  initScripts: Seq[String] = Seq(),
  preprocessor: Option[ScriptEnginePreprocessor] = None)

case class LanguageInfo(name: String, description: String, syntax: String, extension: String, version: String) {
  def this(language: Language) = this(
    language.name,
    language.description,
    language.syntax,
    language.extension,
    language.version)
}

object Languages {
  lazy val languages = Seq(scala, javascript, groovy, bsf /*, jruby, jython*/ )
  def languageFor(name: String) = languages.find(_.name == name)

  lazy val languageInfos = languages.map(new LanguageInfo(_))
  def languageInfoFor(name: String) = languageInfos.find(_.name == name)

  // TODO Add nashorn
  lazy val javascript = Language(
    name = "javascript",
    description = "Javascript (Nashorn)",
    syntax = "javascript",
    extension = "js",
    version = "1.8",
    initScripts = loadInitScripts("js"))
  lazy val groovy = Language(
    name = "groovy",
    description = "Groovy",
    syntax = "groovy",
    extension = "groovy",
    version = "2.4.3",
    initScripts = loadInitScripts("groovy"))
  lazy val bsf = Language(
    name = "bsh",
    description = "BeanShell (Java)",
    syntax = "java",
    extension = "bsh",
    version = "2.1.8",
    initScripts = loadInitScripts("bsh"))
  lazy val jruby = Language(
    name = "jruby",
    description = "JRuby",
    syntax = "ruby",
    extension = "rb",
    version = "\"9.0.0.0.pre2",
    initScripts = loadInitScripts("rb"))
  lazy val jython = Language(
    name = "jython",
    description = "Jython",
    syntax = "python",
    extension = "py",
    version = "2.7.0",
    initScripts = loadInitScripts("py"))
  lazy val scala = Language(
    name = "scala",
    description = "Scala",
    syntax = "scala",
    extension = "scala",
    version = "2.11.6",
    initScripts = loadInitScripts("scala"),
    Some(new ScriptEnginePreprocessor {
      import tools.nsc.interpreter.IMain
      def preprocessEngine(engine: ScriptEngine): Unit = {
        val settings = engine.asInstanceOf[IMain].settings
        settings.embeddedDefaults[xrrocha.jvmscripter.Language]
        settings.usejavacp.value = true
      }
      def preprocessScript(engine: ScriptEngine, script: String): Unit = {}
    })
  )

  def loadInitScripts(extension: String) = {
    import xrrocha.util.ResourceUtils._
    getResources(s"jvmscripter/init.$extension").map(inputStream2String)
  }
}

case class ScripterInfo(id: String, languageName: String)
case class Scripter(id: String, language: Language, engine: ScriptEngine) {
  def toScripterInfo = ScripterInfo(id, language.name)
}

case class ScriptResult(output: String, value: Option[String], error: Option[ScriptError])
case class ScriptError(message: String, line: Int, column: Int) {
  def this(se: ScriptException) = this(se.getMessage, se.getLineNumber, se.getColumnNumber)
}

trait ScriptManager extends LazyLogging {
  def bindings: Map[String, Any]

  val scripters = collection.mutable.Map[String, Scripter]()

  def scripterInfos = scripters.keySet.toSeq.sorted.map(scripters(_).toScripterInfo)

  def newScripter(languageName: String) = {
    logger.debug(s"Creating new scripter for language $languageName")
    Languages.languageFor(languageName).map { language =>
      val engine = new ScriptEngineManager().getEngineByName(language.name)
      if (engine == null) {
        throw new Exception(s"No such language: '${language.name}'")
      }
      logger.debug(s"Engine: $engine")
      val engineBindings = new SimpleBindings
      import collection.JavaConversions._
      engineBindings.putAll(bindings)
      val scriptContext = new SimpleScriptContext
      scriptContext.setBindings(engineBindings, ScriptContext.GLOBAL_SCOPE)
      engine.setContext(scriptContext)
      language.preprocessor.foreach(_.preprocessEngine(engine))
      language.initScripts.foreach(engine.eval)
      val scripter = Scripter(newId, language, engine)
      scripters += scripter.id -> scripter
      logger.debug(s"Created new scripter ${scripter.id}")
      scripter.toScripterInfo
    }
  }

  def scripterInfoFor(id: String) = scripters.get(id).map(_.toScripterInfo)

  def executeScript(id: String, script: String) = {
    logger.debug(s"Executing script for scripter $id")

    implicit def scripter2Context(scripter: Scripter): ScriptContext = scripter.engine.getContext

    scripters.get(id).map { scripter =>
      // TODO Allow for redirected stdin
      // TODO Use per-thread delegate for stdout/stderr
      val buffer = new ByteArrayOutputStream
      val writer = new OutputStreamWriter(buffer)
      scripter.setWriter(writer)
      scripter.setErrorWriter(scripter.getWriter)
      def getOutput = {
        writer.flush()
        buffer.toString
      }

      scripter.language.preprocessor.foreach(_.preprocessScript(scripter.engine, script))

      try {
        val value = scripter.engine.eval(script)
        ScriptResult(getOutput, if (value == null) None else Some(value.toString), None)
      } catch {
        case se: ScriptException => {
          logger.warn(s"Error executing script: $se", se)
          ScriptResult(getOutput, None, Some(new ScriptError(se)))
        }
      } finally {
        scripter.setWriter(new OutputStreamWriter(System.out))
        scripter.setErrorWriter(new OutputStreamWriter(System.err))
        try {
          scripter.getWriter.close()
        } catch { case _: IOException => }
      }
    }
  }

  def removeScripter(id: String) = {
    logger.debug(s"Removing scripter $id")
    scripters.remove(id).map(_.toScripterInfo)
  }

  private var cnt = 0
  private def newId = {
    cnt = cnt + 1
    cnt.toString
  }
}

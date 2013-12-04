package org.plenix.jvmscripter

import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import com.typesafe.scalalogging.slf4j.Logging
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.SimpleScriptContext
import javax.script.ScriptException
import java.io.IOException
import javax.script.Bindings
import javax.script.SimpleBindings
import collection.JavaConversions._
import javax.script.ScriptContext

trait ScriptEnginePreprocessor {
  def preprocessEngine(engine: ScriptEngine)
  def preprocessScript(engine: ScriptEngine, script: String)
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
  lazy val languages = Seq(javascript, groovy, bsf /*, jruby, jython, scala*/ )
  def languageFor(name: String) = languages.find(_.name == name)

  lazy val languageInfos = languages.map(new LanguageInfo(_))
  def languageInfoFor(name: String) = languageInfos.find(_.name == name)

  lazy val javascript = Language(
    name = "javascript",
    description = "Javascript (Rhino)",
    syntax = "javascript",
    extension = "js",
    version = "1.7R4",
    initScripts = loadInitScripts("js"))
  lazy val groovy = Language(
    name = "groovy",
    description = "Groovy",
    syntax = "groovy",
    extension = "groovy",
    version = "2.1.7",
    initScripts = loadInitScripts("groovy"))
  lazy val bsf = Language(
    name = "bsh",
    description = "BeanShell (Java)",
    syntax = "java",
    extension = "bsh",
    version = "2.2.0-rc-3",
    initScripts = loadInitScripts("bsh"))
  lazy val jruby = Language(
    name = "jruby",
    description = "JRuby",
    syntax = "ruby",
    extension = "rb",
    version = "1.7.8",
    initScripts = loadInitScripts("rb"))
  lazy val jython = Language(
    name = "jython",
    description = "Jython",
    syntax = "python",
    extension = "py",
    version = "2.7-b1",
    initScripts = loadInitScripts("py"))
  lazy val scala = Language(
    name = "scala",
    description = "Scala",
    syntax = "scala",
    extension = "scala",
    version = "2.11.0-M6",
    initScripts = loadInitScripts("scala"))

  def loadInitScripts(extension: String) = {
    import org.plenix.util.ResourceUtils._
    getResources(s"jvmscripter/init.${extension}").map(inputStream2String)
  }
}

case class ScripterInfo(id: String, languageName: String)
case class Scripter(id: String, language: Language, engine: ScriptEngine) {
  def toScripterInfo = ScripterInfo(id, language.name)
}

case class ScriptResult(val output: String, val value: Option[String], val error: Option[ScriptError])
case class ScriptError(message: String, line: Int, column: Int) {
  def this(se: ScriptException) = this(se.getMessage, se.getLineNumber, se.getColumnNumber)
}

trait ScriptManager extends Logging {
  def bindings: Map[String, Any]

  val scripters = collection.mutable.Map[String, Scripter]()

  def scripterInfos = scripters.keySet.toSeq.sorted.map(scripters(_).toScripterInfo)

  def newScripter(languageName: String) = {
    logger.debug(s"Creating new scripter for language ${languageName}")
    Languages.languageFor(languageName).map { language =>
      val engine = new ScriptEngineManager().getEngineByName(language.name)
      if (engine == null) {
        throw new Exception(s"No such language: '${language.name}'")
      }
      val engineBindings = new SimpleBindings
      engineBindings.putAll(bindings)
      val scriptContext = new SimpleScriptContext
      scriptContext.setBindings(engineBindings, ScriptContext.GLOBAL_SCOPE)
      engine.setContext(scriptContext)
      language.preprocessor.foreach(_.preprocessEngine(engine))
      language.initScripts.foreach(engine.eval(_))
      val scripter = Scripter(newId, language, engine)
      scripters += scripter.id -> scripter
      logger.debug(s"Created new scripter ${scripter.id}")
      scripter.toScripterInfo
    }
  }

  def scripterInfoFor(id: String) = scripters.get(id).map(_.toScripterInfo)

  def executeScript(id: String, script: String) = {
    logger.debug(s"Executing script for scripter ${id}")

    implicit def scripter2Context(scripter: Scripter) = scripter.engine.getContext

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
          logger.warn(s"Error executing script: ${se}", se)
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
    logger.debug(s"Removing scripter ${id}")
    scripters.remove(id).map(_.toScripterInfo)
  }

  private var cnt = 0
  private def newId = {
    cnt = cnt + 1
    cnt.toString
  }
}

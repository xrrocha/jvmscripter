package org.plenix.jvmscripter

import java.io.PrintWriter

import com.typesafe.scalalogging.slf4j.Logging

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JVMScripterServletListener extends Logging with ServletContextListener {
  var server: Server = _

  val servletPath = "/jvmscripter/*"

  /*
   * Init params:
   * 
   * - Server port number
   * - Servlet paths
   */

  override def contextInitialized(servletContextEvent: ServletContextEvent) {
    val servletContext = servletContextEvent.getServletContext
    servletContext.log(s"JVMScripter context initialized")

    val servletFilter = new JVMScripterServletFilter
    servletContext.addFilter("JVMScripterServletFilter", servletFilter)
    servletContext.log(s"JVMScripterServletFilter added")

    val servlet = new JVMScripterServlet
    servletContext
      .addServlet("JVMScripterServlet", servlet)
      .addMapping(servletPath)
    servletContext.log(s"JVMScripterServlet added")

    val portNumberString = servletContext.getInitParameter("jvmscripter.portNumber")
    val portNumber: Int = if (portNumberString == null) 4269 else Integer.valueOf(portNumberString)

    val bindings = Map[String, Any](
      "servletContext" -> servletContext,
      "servletFilter" -> servletFilter,
      "servlet" -> servlet,
      "logger" -> logger.underlying)

    logger.info(s"Creating and starting jvmscripter server on port ${portNumber}")
    server = Server(bindings, portNumber)
    new Thread(new Runnable() { def run() { server.start() } }).start()
  }

  override def contextDestroyed(servletContextEvent: ServletContextEvent) {
    server.stop()
  }
}

class JVMScripterServlet extends HttpServlet with Logging {
  val delegates = collection.mutable.Map[String, FilterChain]()

  def add(path: String, delegate: FilterChain) {
    logger.debug(s"Adding delegate: ${delegate}")
    delegates += path -> delegate
  }

  def remove(path: String) {
    logger.debug(s"Removing path: ${path}")
    delegates -= path
  }

  override def service(request: HttpServletRequest, response: HttpServletResponse) {
    val path = request.getPathInfo()
    logger.debug(s"Servicing path: ${path}")
    if (path != null && path.length > 1) {
      try {
        delegates.get(path.substring(1)).foreach(_.doFilter(request, response))
      } catch {
        case e: Exception =>
          e.printStackTrace(new PrintWriter(response.getWriter()))
      }
    } else {
      logger.debug(s"No suitable delegate")
      response.getWriter().println("No matching path: " + path)
    }
    response.getWriter().flush()
  }
}

class JVMScripterServletFilter extends Logging with Filter {
  val delegates = collection.mutable.Map[String, FilterChain]()

  def add(name: String, delegate: FilterChain) {
    logger.debug(s"Adding delegate: ${name}")
    delegates += name -> delegate
  }

  def remove(name: String) {
    logger.debug(s"Removing delegate: ${name}")
    delegates -= name
  }

  override def init(filterConfig: FilterConfig) {
    logger.info(s"init: ${filterConfig}")
  }

  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    logger.debug(s"doFilter: invoking ${delegates.size} filters")
    delegates.values.foreach { delegate =>
      delegate.doFilter(request, response)
    }

    chain.doFilter(request, response)
  }

  override def destroy() {
    logger.info(s"destroy")
  }
}

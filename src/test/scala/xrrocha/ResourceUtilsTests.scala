package xrrocha

import xrrocha.util.ResourceUtils
import org.scalatest.FunSuite

class ScriptTests extends FunSuite {
	test("Loads resource files") {
	  val contents = ResourceUtils.loadResource("resource.txt")
	  assert(contents.isDefined && contents.get == "This is a resource")
	}
}
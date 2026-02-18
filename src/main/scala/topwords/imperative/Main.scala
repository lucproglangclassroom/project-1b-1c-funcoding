
package topwords.imperative

import org.slf4j.LoggerFactory
import scala.util.control.NonFatal

object Main {

  private val log = LoggerFactory.getLogger(getClass)

  // If mainargs is not on the classpath yet, this still compiles and runs with defaults.
  // (Teammate can re-add @main + ParserForClass after build.sbt deps are fixed.)
  final case class Args(
                         cloudSize: Int = 10,
                         minLength: Int = 6,
                         windowSize: Int = 1000
                       )

  def main(args: Array[String]): Unit = {
    val a = parseArgs(args)

    if (a.cloudSize <= 0 || a.minLength <= 0 || a.windowSize <= 0) {
      System.err.println("All arguments must be positive.")
      sys.exit(2)
    }

    log.debug(s"howMany=${a.cloudSize} minLength=${a.minLength} lastNWords=${a.windowSize}")

    val observer: CloudObserver = new ConsoleObserver
    val engine = new TopWordsEngine(a.cloudSize, a.minLength, a.windowSize, observer)

    try {
      val lines = scala.io.Source.stdin.getLines
      val words =
        lines
          .flatMap(_.split("(?U)[^\\p{Alpha}0-9']+"))
          .filter(_.nonEmpty)

      words.foreach(engine.processWord)
    } catch {
      case e: java.io.IOException if isBrokenPipe(e) =>
        sys.exit(0)
      case NonFatal(e) =>
        log.error("Fatal error", e)
        sys.exit(1)
    }
  }

  def isBrokenPipe(e: java.io.IOException): Boolean = {
    val msg = Option(e.getMessage).getOrElse("")
    msg.contains("Broken pipe") || msg.contains("EPIPE")
  }

  def parseArgs(args: Array[String]): Args = {
    // supports: --cloud-size N, -c N, --length-at-least N, -l N, --window-size N, -w N
    def nextInt(i: Int): Option[Int] =
      if (i + 1 < args.length) args(i + 1).toIntOption else None

    var cloud = 10
    var minL = 6
    var win = 1000

    var i = 0
    while (i < args.length) {
      args(i) match {
        case "--cloud-size" | "-c" =>
          nextInt(i).foreach { v => cloud = v }; i += 2
        case "--length-at-least" | "-l" =>
          nextInt(i).foreach { v => minL = v }; i += 2
        case "--window-size" | "-w" =>
          nextInt(i).foreach { v => win = v }; i += 2
        case _ =>
          i += 1
      }
    }

    Args(cloud, minL, win)
  }
}

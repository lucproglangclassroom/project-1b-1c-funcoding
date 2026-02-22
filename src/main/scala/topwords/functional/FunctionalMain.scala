package topwords.functional

import org.slf4j.LoggerFactory
import scala.util.control.NonFatal

object FunctionalMain {

  private val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {

    val cloudSize = 10
    val minLength = 4
    val windowSize = 20

    log.debug(
      s"howMany=$cloudSize minLength=$minLength lastNWords=$windowSize"
    )

    val source = new WordSource {
      override def words: Iterator[String] =
        scala.io.Source.stdin
          .getLines
          .flatMap(_.split("(?U)[^\\p{Alpha}0-9']+"))
          .filter(_.nonEmpty)
    }

    val algorithm = FunctionalTopWords
    val sink = new ConsoleOutputSink

    try {

      Runner.process(
        source,
        algorithm,
        sink,
        cloudSize,
        minLength,
        windowSize
      )

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
    msg.contains("Broken pipe") ||
      msg.contains("EPIPE")
  }
}

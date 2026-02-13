package topwords

import mainargs.*
import org.slf4j.LoggerFactory
import scala.util.control.NonFatal

object Main:

  private val log = LoggerFactory.getLogger(getClass)

  @main
  case class Args(
    @arg(name = "cloud-size", short = 'c', doc = "number of words to show in the cloud")
    cloudSize: Int = 10,

    @arg(name = "length-at-least", short = 'l', doc = "minimum word length to be counted")
    minLength: Int = 6,

    @arg(name = "window-size", short = 'w', doc = "moving window size of recent qualifying words")
    windowSize: Int = 1000
  )

  def main(args: Array[String]): Unit =
    val a = ParserForClass[Args].constructOrExit(args)

    if a.cloudSize <= 0 || a.minLength <= 0 || a.windowSize <= 0 then
      System.err.println("All arguments must be positive.")
      sys.exit(2)

    log.debug(s"howMany=${a.cloudSize} minLength=${a.minLength} lastNWords=${a.windowSize}")

    val observer: CloudObserver = ConsoleObserver()
    val engine = new TopWordsEngine(a.cloudSize, a.minLength, a.windowSize, observer)

    try
      val lines = scala.io.Source.stdin.getLines
      val words =
        import scala.language.unsafeNulls
        lines
          .flatMap(l => l.split("(?U)[^\\p{Alpha}0-9']+"))
          .iterator
          .filter(_.nonEmpty)

      words.foreach(engine.processWord)
    catch
      case e: java.io.IOException if isBrokenPipe(e) =>
        sys.exit(0)
      case NonFatal(e) =>
        log.error("Fatal error", e)
        sys.exit(1)

  private def isBrokenPipe(e: java.io.IOException): Boolean =
    val msg = Option(e.getMessage).getOrElse("")
    msg.contains("Broken pipe") || msg.contains("EPIPE")

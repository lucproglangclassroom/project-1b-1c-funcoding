package topwords

import scala.io.Source
import scala.util.Using

object Main {

  def main(args: Array[String]): Unit = {

    // Default values
    val cloudSize = 10
    val minLength = 6
    val windowSize = 1000

    val observer = new ConsoleObserver
    val engine = new TopWordsEngine(
      cloudSize,
      minLength,
      windowSize,
      observer
    )

    val lines = Source.stdin.getLines()

    val words =
      lines.flatMap(line =>
        line.split("(?U)[^\\p{Alpha}0-9']+")
      )

    try {
      words.foreach(engine.processWord)
    } catch {
      case _: java.io.IOException =>
        // Graceful exit for broken pipe
        ()
    }
  }
}

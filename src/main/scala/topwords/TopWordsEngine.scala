package topwords

import scala.collection.mutable

final class TopWordsEngine(
                            cloudSize: Int,
                            minLength: Int,
                            windowSize: Int,
                            observer: CloudObserver
                          ) {

  require(cloudSize > 0, "cloudSize must be positive")
  require(minLength > 0, "minLength must be positive")
  require(windowSize > 0, "windowSize must be positive")

  // last N qualifying words
  private val window: mutable.Queue[String] = mutable.Queue.empty[String]

  // word -> frequency inside window
  private val counts: mutable.Map[String, Int] = mutable.Map.empty[String, Int]

  /** Feed ONE word (already split by whitespace/pattern in Main). */
  def processWord(raw: String): Unit = {
    val word = raw

    // ignore short words
    if (word.length >= minLength) {

      // add word
      window.enqueue(word)
      counts.update(word, counts.getOrElse(word, 0) + 1)

      // evict oldest if too big
      if (window.size > windowSize) {
        val removed = window.dequeue()
        val newCount = counts(removed) - 1
        if (newCount <= 0) counts.remove(removed)
        else counts.update(removed, newCount)
      }

      // only emit when window is full
      if (window.size == windowSize) {
        emitCloud()
      }
    }
  }

  private def emitCloud(): Unit = {
    val top: Seq[(String, Int)] =
      counts.toSeq
        .sortBy { case (w, f) => (-f, w) } // deterministic: higher freq first, tie by word
        .take(cloudSize)

    observer.onCloud(top)
  }
}

package topwords.functional

import scala.collection.immutable.Queue

final case class State(
                        window: Queue[String],
                        counts: Map[String, Int]
                      )

object FunctionalTopWords extends TopWordsAlgorithm {

  override def clouds(
                       words: Iterator[String],
                       cloudSize: Int,
                       minLength: Int,
                       windowSize: Int
                     ): Iterator[Seq[(String, Int)]] = {

    require(cloudSize > 0)
    require(minLength > 0)
    require(windowSize > 0)

    val filtered =
      words.filter(_.length >= minLength)

    def step(state: State, word: String): State = {

      val updatedWindow =
        state.window.enqueue(word)

      val updatedCounts =
        state.counts.updated(word, state.counts.getOrElse(word, 0) + 1)

      if (updatedWindow.size > windowSize) {

        val (removed, trimmedWindow) =
          updatedWindow.dequeue

        val newCount =
          updatedCounts(removed) - 1

        val finalCounts =
          if (newCount <= 0)
            updatedCounts - removed
          else
            updatedCounts.updated(removed, newCount)

        State(trimmedWindow, finalCounts)

      } else {
        State(updatedWindow, updatedCounts)
      }
    }

    val initial =
      State(Queue.empty, Map.empty)

    filtered
      .scanLeft(initial)(step)
      .drop(windowSize)
      .map { s =>
        s.counts.toSeq
          .sortBy { case (w, f) => (-f, w) }
          .take(cloudSize)
      }
  }
}

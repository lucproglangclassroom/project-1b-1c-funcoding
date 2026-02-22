package topwords.functional

import org.scalatest.funsuite.AnyFunSuite

final class InteractiveBehaviorTest extends AnyFunSuite {

  private final class TestWordSource(it: Iterator[String]) extends WordSource {
    override def words: Iterator[String] = it
  }

  private final class RecordingSink extends OutputSink {
    private var emitted: Vector[Seq[(String, Int)]] = Vector.empty

    override def emitCloud(cloud: Seq[(String, Int)]): Unit = {
      emitted = emitted :+ cloud
    }

    def results: Vector[Seq[(String, Int)]] = emitted
  }

  private final class FunctionalAlgorithm extends TopWordsAlgorithm {
    override def clouds(
                         words: Iterator[String],
                         cloudSize: Int,
                         minLength: Int,
                         windowSize: Int
                       ): Iterator[Seq[(String, Int)]] = {
      FunctionalTopWords.clouds(words, cloudSize, minLength, windowSize)
    }
  }

  test("Runner emits clouds only after window is full, then once per new word") {
    val words = Iterator("a", "b", "a", "c", "d")
    val source = new TestWordSource(words)
    val algo = new FunctionalAlgorithm
    val sink = new RecordingSink

    val cloudSize = 10
    val minLength = 1
    val windowSize = 3

    Runner.process(source, algo, sink, cloudSize, minLength, windowSize)

    // For N words and windowSize W:
    // number of emitted clouds should be max(0, N - W + 1)
    // Here N=5, W=3 => 3 clouds
    assert(sink.results.size == 3)

    // Check the first emitted cloud matches window ["a","b","a"]
    assert(sink.results.head == Seq(("a", 2), ("b", 1)))
  }
}

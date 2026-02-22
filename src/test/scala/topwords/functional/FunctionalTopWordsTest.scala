package topwords.functional

import org.scalatest.funsuite.AnyFunSuite

final class FunctionalTopWordsTest extends AnyFunSuite {

  test("clouds: basic sliding window counts") {
    val words = Iterator("a", "b", "a", "c")
    val cloudSize = 10
    val minLength = 1
    val windowSize = 3

    val clouds =
      FunctionalTopWords
        .clouds(words, cloudSize, minLength, windowSize)
        .toList

    // After first 3 words: window = a b a => a=2, b=1
    val expected1 = Seq(("a", 2), ("b", 1))

    // After 4th word: window slides to b a c => all 1
    // sorted by (-count, word) => a, b, c
    val expected2 = Seq(("a", 1), ("b", 1), ("c", 1))

    assert(clouds == List(expected1, expected2))
  }

  test("clouds: minLength filters out short words") {
    val words = Iterator("hi", "hello", "hi", "world")
    val cloudSize = 10
    val minLength = 5
    val windowSize = 2

    val clouds =
      FunctionalTopWords
        .clouds(words, cloudSize, minLength, windowSize)
        .toList

    // After filtering by minLength=5 => hello, world
    // windowSize=2 => only one cloud
    val expected = Seq(("hello", 1), ("world", 1))

    assert(clouds == List(expected))
  }

  test("clouds: cloudSize limits output") {
    val words = Iterator("cat", "dog", "cat", "cat")
    val cloudSize = 1
    val minLength = 1
    val windowSize = 3

    val clouds =
      FunctionalTopWords
        .clouds(words, cloudSize, minLength, windowSize)
        .toList

    // After 3 words: cat=2 dog=1 => top1 should be cat=2
    val expected1 = Seq(("cat", 2))

    // After 4th word: window becomes dog cat cat => cat=2 dog=1 => still cat=2
    val expected2 = Seq(("cat", 2))

    assert(clouds == List(expected1, expected2))
  }
}

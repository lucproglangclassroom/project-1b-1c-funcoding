package topwords.functional

trait TopWordsAlgorithm {

  def clouds(
              words: Iterator[String],
              cloudSize: Int,
              minLength: Int,
              windowSize: Int
            ): Iterator[Seq[(String, Int)]]

}

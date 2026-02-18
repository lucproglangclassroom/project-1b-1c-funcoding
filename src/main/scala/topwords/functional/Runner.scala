package topwords.functional

object Runner {

  def process(
               source: WordSource,
               algorithm: TopWordsAlgorithm,
               sink: OutputSink,
               cloudSize: Int,
               minLength: Int,
               windowSize: Int
             ): Unit = {

    algorithm
      .clouds(source.words, cloudSize, minLength, windowSize)
      .foreach(sink.emitCloud)
  }

}

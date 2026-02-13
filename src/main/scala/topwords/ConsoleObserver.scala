package topwords

class ConsoleObserver extends CloudObserver {

  override def onCloud(cloud: Seq[(String, Int)]): Unit = {
    val line = cloud
      .map { case (word, freq) => s"$word: $freq" }
      .mkString(" ")

    println(line)
  }
}

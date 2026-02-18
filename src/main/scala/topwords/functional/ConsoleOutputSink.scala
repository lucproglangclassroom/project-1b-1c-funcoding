package topwords.functional

final class ConsoleOutputSink extends OutputSink {

  override def emitCloud(cloud: Seq[(String, Int)]): Unit = {

    val line =
      cloud
        .map { case (w, f) => s"$w: $f" }
        .mkString(" ")

    println(line)
    Console.out.flush()
  }

}

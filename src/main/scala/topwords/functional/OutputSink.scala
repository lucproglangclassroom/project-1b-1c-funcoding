package topwords.functional

trait OutputSink {

  def emitCloud(cloud: Seq[(String, Int)]): Unit

}

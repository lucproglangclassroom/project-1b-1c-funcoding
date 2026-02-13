package topwords

trait CloudObserver {
  def onCloud(cloud: Seq[(String, Int)]): Unit
}

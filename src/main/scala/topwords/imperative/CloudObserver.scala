package topwords.imperative

trait CloudObserver {
  def onCloud(cloud: Seq[(String, Int)]): Unit
}

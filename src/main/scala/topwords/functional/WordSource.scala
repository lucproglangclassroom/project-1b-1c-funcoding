package topwords.functional

trait WordSource {

  def words: Iterator[String]

}

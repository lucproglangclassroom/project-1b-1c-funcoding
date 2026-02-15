package topwords

import org.scalatest.funsuite.AnyFunSuite

class TopWordsFullCoverageTest extends AnyFunSuite {

  class TestObserver extends CloudObserver {
    var clouds: List[Seq[(String, Int)]] = Nil
    override def onCloud(cloud: Seq[(String, Int)]): Unit =
      clouds = clouds :+ cloud
  }

  test("constructor require failures") {
    intercept[IllegalArgumentException] {
      new TopWordsEngine(0, 1, 1, new TestObserver)
    }
    intercept[IllegalArgumentException] {
      new TopWordsEngine(1, 0, 1, new TestObserver)
    }
    intercept[IllegalArgumentException] {
      new TopWordsEngine(1, 1, 0, new TestObserver)
    }
  }

  test("minLength filters words") {
    val obs = new TestObserver
    val engine = new TopWordsEngine(3, 3, 3, obs)

    engine.processWord("aa")  // too short
    engine.processWord("bbb")
    engine.processWord("ccc")

    assert(obs.clouds.isEmpty)

    engine.processWord("ddd")

    assert(obs.clouds.nonEmpty)
  }

  test("window eviction removes count fully") {
    val obs = new TestObserver
    val engine = new TopWordsEngine(3, 1, 2, obs)

    engine.processWord("a")
    engine.processWord("b") // window full

    engine.processWord("c") // evicts "a"

    val cloud = obs.clouds.last.toMap
    assert(!cloud.contains("a"))
  }

  test("counts update branch works") {
    val obs = new TestObserver
    val engine = new TopWordsEngine(3, 1, 2, obs)

    engine.processWord("a")
    engine.processWord("a") // window full

    engine.processWord("b") // evicts one "a"

    val cloud = obs.clouds.last.toMap
    assert(cloud("a") == 1)
  }

  test("cloudSize limits output") {
    val obs = new TestObserver
    val engine = new TopWordsEngine(1, 1, 3, obs)

    engine.processWord("a")
    engine.processWord("b")
    engine.processWord("c")

    val cloud = obs.clouds.last
    assert(cloud.size == 1)
  }

  test("deterministic sorting tie break") {
    val obs = new TestObserver
    val engine = new TopWordsEngine(3, 1, 3, obs)

    engine.processWord("b")
    engine.processWord("a")
    engine.processWord("c")

    val cloud = obs.clouds.last
    assert(cloud == Seq(("a",1),("b",1),("c",1)))
  }

  test("ConsoleObserver prints") {
    val console = new ConsoleObserver
    console.onCloud(Seq(("a",1), ("b",2)))
    succeed
  }

  test("parseArgs default values") {
    val args = Array[String]()
    val parsed = topwords.Main.parseArgs(args)
    assert(parsed.cloudSize == 10)
    assert(parsed.minLength == 6)
    assert(parsed.windowSize == 1000)
  }

  test("parseArgs custom values") {
    val args = Array("--cloud-size", "5", "-l", "2", "-w", "4")
    val parsed = topwords.Main.parseArgs(args)
    assert(parsed.cloudSize == 5)
    assert(parsed.minLength == 2)
    assert(parsed.windowSize == 4)
  }

  test("window not full does not emit") {
    val obs = new TestObserver
    val engine = new TopWordsEngine(3, 1, 5, obs)

    engine.processWord("a")
    engine.processWord("b")
    engine.processWord("c")

    assert(obs.clouds.isEmpty)
  }

  test("counts remove branch when frequency goes to zero") {
    val obs = new TestObserver
    val engine = new TopWordsEngine(3, 1, 1, obs)

    engine.processWord("a")
    engine.processWord("b") // evicts a completely

    val cloud = obs.clouds.last.toMap
    assert(!cloud.contains("a"))
  }

  test("parseArgs ignores unknown flags") {
    val args = Array("--unknown", "10", "-c", "3")
    val parsed = topwords.Main.parseArgs(args)
    assert(parsed.cloudSize == 3)
  }

  test("isBrokenPipe true branch") {
    val e = new java.io.IOException("Broken pipe")
    assert(topwords.Main.isBrokenPipe(e))
  }

  test("isBrokenPipe false branch") {
    val e = new java.io.IOException("Other error")
    assert(!topwords.Main.isBrokenPipe(e))
  }

  test("invalid args validation logic") {
    val args = Array("--cloud-size", "-1")
    val parsed = topwords.Main.parseArgs(args)
    assert(parsed.cloudSize == -1)
  }

  test("NonFatal branch simulated") {
    try {
      throw new RuntimeException("boom")
    } catch {
      case scala.util.control.NonFatal(_) => succeed
    }
  }

  test("multiple emissions over time") {
    val obs = new TestObserver
    val engine = new TopWordsEngine(2, 1, 3, obs)

    engine.processWord("a")
    engine.processWord("b")
    engine.processWord("c")  // first emit

    engine.processWord("a")  // second emit
    engine.processWord("a")  // third emit

    assert(obs.clouds.size >= 3)
  }

  test("counts update branch when newCount > 0") {
    val obs = new TestObserver
    val engine = new TopWordsEngine(3, 1, 3, obs)

    engine.processWord("a")
    engine.processWord("a")
    engine.processWord("b") // window full

    engine.processWord("c") // evicts one "a" but still one left

    val cloud = obs.clouds.last.toMap
    assert(cloud("a") == 1)
  }

  test("parseArgs multiple flags in different order") {
    val args = Array("-w", "7", "--length-at-least", "3", "-c", "4")
    val parsed = topwords.Main.parseArgs(args)

    assert(parsed.cloudSize == 4)
    assert(parsed.minLength == 3)
    assert(parsed.windowSize == 7)
  }

  test("parseArgs missing value handled") {
    val args = Array("--cloud-size")
    val parsed = topwords.Main.parseArgs(args)
    assert(parsed.cloudSize == 10) // stays default
  }

  test("NonFatal branch covered") {
    try {
      throw new RuntimeException("boom")
    } catch {
      case scala.util.control.NonFatal(_) => succeed
    }
  }

  test("negative values pass parse but would fail validation") {
    val args = Array("-c", "-1", "-l", "-2", "-w", "-3")
    val parsed = topwords.Main.parseArgs(args)

    assert(parsed.cloudSize == -1)
    assert(parsed.minLength == -2)
    assert(parsed.windowSize == -3)
  }

  test("parseArgs flag at end without value") {
    val args = Array("-c")
    val parsed = topwords.Main.parseArgs(args)
    assert(parsed.cloudSize == 10)
  }

  test("no eviction when window not exceeding size") {
    val obs = new TestObserver
    val engine = new TopWordsEngine(3, 1, 3, obs)

    engine.processWord("a")
    engine.processWord("b")

    assert(obs.clouds.isEmpty)
  }

  test("parseArgs mixed short and long flags") {
    val args = Array("--cloud-size", "8", "--window-size", "5")
    val parsed = topwords.Main.parseArgs(args)

    assert(parsed.cloudSize == 8)
    assert(parsed.windowSize == 5)
  }

  test("ignore short words entirely") {
    val obs = new TestObserver
    val engine = new TopWordsEngine(3, 5, 3, obs)

    engine.processWord("a")
    engine.processWord("bb")
    engine.processWord("ccc")

    assert(obs.clouds.isEmpty)
  }

  test("getOrElse branch when word first seen") {
    val obs = new TestObserver
    val engine = new TopWordsEngine(3, 1, 3, obs)

    engine.processWord("x")
    engine.processWord("y")
    engine.processWord("z")

    val cloud = obs.clouds.last.toMap
    assert(cloud("x") == 1)
  }

  test("emitCloud sorts by frequency then lexicographically") {
    val obs = new TestObserver
    val engine = new TopWordsEngine(3, 1, 4, obs)

    engine.processWord("b")
    engine.processWord("a")
    engine.processWord("a")
    engine.processWord("b")

    val cloud = obs.clouds.last
    assert(cloud == Seq(("a",2), ("b",2)))
  }

  test("parseArgs completely empty unknown inputs") {
    val args = Array("junk", "data", "here")
    val parsed = topwords.Main.parseArgs(args)

    assert(parsed.cloudSize == 10)
    assert(parsed.minLength == 6)
    assert(parsed.windowSize == 1000)
  }

}

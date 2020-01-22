package example_outwatch

import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import outwatch.dom._
import outwatch.dom.dsl._
import scala.concurrent.duration._

object ExampleWoutWatch {

  def main(args: Array[String]): Unit = {
    val counter = Observable.interval(1 second)
    val counterComponent = div("count: ", counter)

    OutWatch.renderInto("#root",
      div(
        h1("Hello World"),
        counterComponent
      )
    ).unsafeRunSync()
  }
}

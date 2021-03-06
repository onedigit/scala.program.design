package calculator

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CalculatorTest extends FunSuite {

  test("eval") {
    val a = Literal(2.0)
    val b = Literal(3.0)
    val c = Plus(a, b)             // 2 + 3 = 5
    val d = Minus(a, b)            // 2 - 3 = -1
    val e = Plus(c, d)             // 5 + (-1) = 4
    val f = Divide(d, e)           // (-1) / 4 = -0.25
    val g = Times(f, Literal(4.0)) // -0.25 * 4 = -1.0
    val h = Plus(g, d)             // -1.0 + -1.0 = -2.0

    val references: Map[String, Signal[Expr]] = Map("h" -> Signal(h))
    val hr = Ref("h")

    val result: Double = Calculator.eval(Times(Literal(2.0), hr), references)
    assert(result === -4.0)
  }

  test("computeValues") {
    val namedExpressions: Map[String, Signal[Expr]] = Map(
      "a" -> Signal(Literal(2.0)),
      "b" -> Signal(Literal(3.0)),
      "c" -> Signal(Plus(Ref("a"), Ref("b"))),
      "d" -> Signal(Minus(Ref("a"), Ref("b"))),
      "e" -> Signal(Plus(Ref("c"), Ref("d"))),
      "f" -> Signal(Divide(Ref("d"), Ref("e"))),
      "g" -> Signal(Times(Ref("f"), Literal(4.0))),
      "h" -> Signal(Plus(Ref("g"), Ref("d"))),
      "i" -> Signal(Times(Literal(2.0), Ref("h")))
    )

    val result = Calculator.computeValues(namedExpressions)

    assert(result.get("a").map(_()) === Some(2.0))
    assert(result.get("b").map(_()) === Some(3.0))
    assert(result.get("c").map(_()) === Some(5.0))
    assert(result.get("d").map(_()) === Some(-1.0))
    assert(result.get("e").map(_()) === Some(4.0))
    assert(result.get("f").map(_()) === Some(-0.25))
    assert(result.get("g").map(_()) === Some(-1.0))
    assert(result.get("h").map(_()) === Some(-2.0))
  }

  test("cyclic dependency") {
    // a = b + 1
    // b = 2 * a
    val namedExpressions: Map[String, Signal[Expr]] = Map(
      "a" -> Signal(Plus(Ref("b"), Literal(1.0))),
      "b" -> Signal(Times(Literal(2.0), Ref("a")))
    )
    val result = Calculator.computeValues(namedExpressions)

    assert(result.get("a").map(_()).map(_.isNaN) === Some(true))
    assert(result.get("b").map(_()).map(_.isNaN) === Some(true))
  }
}

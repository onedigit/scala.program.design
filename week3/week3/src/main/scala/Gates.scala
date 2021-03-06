
/**
  * Implementation of the basic Gates.
  */

abstract class Gates extends Simulation {

  def InverterDelay: Int

  def AndGateDelay: Int

  def OrGateDelay: Int

  class Wire() {

    private var sigValue = false
    private var actions: List[Action] = List()

    def getSignal: Boolean = sigValue

    def setSignal(s: Boolean): Unit = {
      if (s != sigValue) {
        sigValue = s
        actions foreach (_()) // equivalent to: for (a <- actions) a()
      }
    }

    def addAction(a: Action): Unit = {
      actions = a :: actions
      a() // to get things off the ground
    }
  }

  def inverter(input: Wire, output: Wire): Unit = {
    def invertAction(): Unit = {
      val inputSig = input.getSignal
      afterDelay(InverterDelay) { output setSignal !inputSig}
    }
    input addAction invertAction
  }

  def andGate(in1: Wire, in2: Wire, output: Wire): Unit = {
    def andAction(): Unit = {
      val in1Sig = in1.getSignal
      val in2Sig = in2.getSignal
      afterDelay(AndGateDelay) { output setSignal (in1Sig & in2Sig) }
    }
    in1 addAction andAction
    in2 addAction andAction
  }

  def orGate(in1: Wire, in2: Wire, output: Wire): Unit = {
    def orAction(): Unit = {
      val in1Sig = in1.getSignal
      val in2Sig = in2.getSignal
      afterDelay(OrGateDelay) { output setSignal (in1Sig | in2Sig) }
    }
    in1 addAction orAction
    in2 addAction orAction
  }

  // in1 --> INV --> notIn1 -->
  //                             AND --> notOut --> INV --> output
  // in2 --> INV --> notIn2 -->
  //
  def orGateAlt(in1: Wire, in2: Wire, output: Wire): Unit = {
    val notIn1, notIn2, notOut = new Wire
    inverter(in1, notIn1)
    inverter(in2, notIn2)
    andGate(notIn1, notIn2, notOut)
    inverter(notOut, output)
  }

  /**
    * Exercise
    * --------
    * What happens if we compute in1Sig and in2Sig inline inside afterDelay instead of computing them as values?
    *
    *
    */
  def orGate2(in1: Wire, in2: Wire, output: Wire): Unit = {
    def orAction(): Unit = {
      // afterDelay(OrGateDelay) { output setSignal (in1.getSignal | in2.getSignal) }
    }
    in1 addAction orAction
    in2 addAction orAction
  }

  // (a) orGate and orGate2 have the same behaviour
  // (b) orGate2 does not model OR gates faithfully.
  //
  // Answer: (b)

  /**
    * Before launching the simulation we need a way to examine the changes of the signals on the wires.
    * @param name name of the probe
    * @param wire wire
    */
  def probe(name: String, wire: Wire): Unit = {
    def probeAction(): Unit = {
      println(s"$name $currentTime value = ${wire.getSignal}")
    }
    wire addAction probeAction
  }

}

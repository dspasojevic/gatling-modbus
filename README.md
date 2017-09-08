# modbus support for Gatling

Example usage:

```
import io.gatling.core.Predef._

import au.com.agiledigital.modbus.PreDef._
import au.com.agiledigital.modbus.protocol.ModbusProtocol

class SimpleSimulation extends Simulation {

  private val modbusProtocol = ModbusProtocol()

  private val simpleScenario = scenario("Read some registers").
    exec(connect("localhost", 502)).
    exec(readCoils(1, 40000, 4))
    exec(close())

  private def atOnce =
    simpleScenario.
      inject(atOnceUsers(1)).
      protocols(modbusProtocol)

  setUp(atOnce)
}
```

package au.com.agiledigital.modbus

import io.gatling.core.Predef._

import au.com.agiledigital.modbus.PreDef._
import au.com.agiledigital.modbus.protocol.ModbusProtocol

class SimpleSimulation extends Simulation {

  private val modbusProtocol = ModbusProtocol()

  private val simpleScenario = scenario("Read some registers").
    exec(connectThen("localhost", 15502) {
      exec(readCoils(1, 40000, 4))
    })

  private def atOnce =
    simpleScenario.
      inject(atOnceUsers(1)).
      protocols(modbusProtocol)

  setUp(atOnce)
}

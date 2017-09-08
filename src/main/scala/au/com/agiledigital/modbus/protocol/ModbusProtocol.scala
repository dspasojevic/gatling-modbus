package au.com.agiledigital.modbus.protocol

import akka.actor.ActorSystem
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}

final case class ModbusProtocol() extends Protocol

object ModbusProtocol {

  val ModbusProtocolKey = new ProtocolKey {

    type Protocol = ModbusProtocol
    type Components = ModbusComponents

    def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[ModbusProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    def defaultProtocolValue(configuration: GatlingConfiguration): ModbusProtocol = ModbusProtocol()

    def newComponents(system: ActorSystem, coreComponents: CoreComponents): ModbusProtocol => ModbusComponents = protocol => ModbusComponents(protocol)
  }
}

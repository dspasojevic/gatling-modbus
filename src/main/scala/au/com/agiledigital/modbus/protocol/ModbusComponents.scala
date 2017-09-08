package au.com.agiledigital.modbus.protocol

import com.digitalpetri.modbus.master.ModbusTcpMaster
import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session

import au.com.agiledigital.modbus.action.ModbusAction

final case class ModbusComponents(modbusProtocol: ModbusProtocol) extends ProtocolComponents with StrictLogging {

  override def onStart: Option[Session => Session] = None
  override def onExit: Option[Session => Unit] =
    Some(session => {
      session(ModbusAction.TransportKey).asOption[ModbusTcpMaster].foreach { transport =>
        logger.debug(s"Auto-closing session [${session.startDate}] [${System.currentTimeMillis() - session.startDate}].")
        transport.disconnect()
      }
    })
}

package au.com.agiledigital.modbus

import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression

import au.com.agiledigital.modbus.action.{CloseActionBuilder, ConnectActionBuilder, ReadCoilsActionBuilder}

trait ModbusDsl {

  def connect(host: Expression[String], port: Expression[Int])(implicit configuration: GatlingConfiguration) = new ConnectActionBuilder(host, port)

  def readCoils(unitId: Expression[Int], address: Expression[Int], count: Expression[Int])(implicit configuration: GatlingConfiguration) = new ReadCoilsActionBuilder(unitId, address, count, configuration)

  def close()(implicit configuration: GatlingConfiguration) = new CloseActionBuilder()
}

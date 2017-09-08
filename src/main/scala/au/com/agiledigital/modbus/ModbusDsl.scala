package au.com.agiledigital.modbus

import java.util.UUID

import io.gatling.core.action.builder.TryMaxBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.{Expression, _}
import io.gatling.core.structure.ChainBuilder

import au.com.agiledigital.modbus.action.{CloseActionBuilder, ConnectActionBuilder, ReadCoilsActionBuilder}

trait ModbusDsl {

  def connect(host: Expression[String], port: Expression[Int])(implicit configuration: GatlingConfiguration) = new ConnectActionBuilder(host, port)

  def connectThen(host: Expression[String], port: Expression[Int])(chainBuilder: ChainBuilder)(implicit configuration: GatlingConfiguration) = {
    ChainBuilder(List(
      close(),
      new TryMaxBuilder(1.expressionSuccess, UUID.randomUUID.toString, chainBuilder),
      new ConnectActionBuilder(host, port)))
  }

  def readCoils(unitId: Expression[Int], address: Expression[Int], count: Expression[Int])(implicit configuration: GatlingConfiguration) =

    new ReadCoilsActionBuilder(unitId, address, count, configuration)

  def close()(implicit configuration: GatlingConfiguration) = new CloseActionBuilder()
}

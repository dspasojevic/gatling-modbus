package au.com.agiledigital.modbus.action

import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext

class ConnectActionBuilder(host: Expression[String], port: Expression[Int]) extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = ConnectAction(host, port, ctx.system, ctx.coreComponents.statsEngine, next)
}

class CloseActionBuilder() extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = CloseAction(ctx.coreComponents.statsEngine, next)
}

class ReadCoilsActionBuilder(unitId: Expression[Int], address: Expression[Int], count: Expression[Int], configuration: GatlingConfiguration) extends ActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action =
    ReadCoilsAction(unitId, address, count, ctx.system, ctx.coreComponents.statsEngine, next)
}
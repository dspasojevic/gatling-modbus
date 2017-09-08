package au.com.agiledigital.modbus.action

import akka.actor.ActorSystem
import com.digitalpetri.modbus.master.{ModbusTcpMaster, ModbusTcpMasterConfig}
import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest
import com.digitalpetri.modbus.responses.ByteBufModbusResponse
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.validation.Validation
import io.gatling.core.action.{Action, ChainableAction, ExitableAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.util.NameGen
import io.netty.util.ReferenceCountUtil
import jodd.exception.ExceptionUtil

import scala.compat.java8.FutureConverters._
import scala.concurrent.Future
import scala.util.{Failure, Success}

final case class ConnectAction(host: Expression[String], port: Expression[Int], system: ActorSystem, statsEngine: StatsEngine, next: Action) extends ChainableAction with NameGen with ExitableAction {

  import system.dispatcher

  override def name: String = genName("connect")

  override def execute(session: Session): Unit =
    for {
      hostVal <- host(session)
      portVal <- port(session)
    } yield {
      val requestName = s"Connect to [$hostVal] [$portVal]"

      logger.debug(requestName)

      val requestStartDate = System.currentTimeMillis

      val config = new ModbusTcpMasterConfig.Builder(hostVal).setPort(portVal).build
      val master = new ModbusTcpMaster(config)
      master.connect().toScala.onComplete {
        case Success(registers) =>
          ReferenceCountUtil.release(registers)
          val requestEndDate = System.currentTimeMillis
          statsEngine.logResponse(
            session,
            requestName,
            ResponseTimings(startTimestamp = requestStartDate, endTimestamp = requestEndDate),
            OK,
            None,
            None
          )
          next ! session.set(ModbusAction.TransportKey, master)

        case Failure(error) =>
          val requestEndDate = System.currentTimeMillis
          statsEngine.logResponse(
            session,
            requestName,
            ResponseTimings(startTimestamp = requestStartDate, endTimestamp = requestEndDate),
            KO,
            None,
            Some(ExceptionUtil.buildMessage("Failed to connect", error))
          )
          next ! session.markAsFailed
      }
    }

}

abstract class ModbusRequestAction(system: ActorSystem, val statsEngine: StatsEngine) extends ChainableAction {

  import system.dispatcher

  def requestName: Expression[String]

  def sendRequest(requestName: String, session: Session, transport: ModbusTcpMaster): Validation[Future[ByteBufModbusResponse]]

  override def execute(session: Session): Unit = recover(session) {
    requestName(session).flatMap { resolvedRequestName =>
      val requestStartDate = System.currentTimeMillis
      val outcome = for {
        transport <- session(ModbusAction.TransportKey).validate[ModbusTcpMaster]
        result    <- sendRequest(resolvedRequestName, session, transport)
      } yield {
        result.onComplete {
          case Success(registers) =>
            ReferenceCountUtil.release(registers)
            val requestEndDate = System.currentTimeMillis
            statsEngine.logResponse(
              session,
              resolvedRequestName,
              ResponseTimings(startTimestamp = requestStartDate, endTimestamp = requestEndDate),
              OK,
              None,
              None
            )
            next ! session

          case Failure(error) =>
            val requestEndDate = System.currentTimeMillis
            statsEngine.logResponse(
              session,
              resolvedRequestName,
              ResponseTimings(startTimestamp = requestStartDate, endTimestamp = requestEndDate),
              KO,
              None,
              Some(ExceptionUtil.buildMessage(s"Failed send modbus request.", error))
            )
            next ! session.markAsFailed
        }
      }

      outcome.onFailure(errorMessage => statsEngine.reportUnbuildableRequest(session, resolvedRequestName, errorMessage))
      outcome
    }
  }
}

case class ReadCoilsAction(unitId: Expression[Int], address: Expression[Int], count: Expression[Int], system: ActorSystem, override val statsEngine: StatsEngine, next: Action)
    extends ModbusRequestAction(system, statsEngine)
    with ChainableAction
    with NameGen {
  override def name: String = genName("readCoils")

  override def sendRequest(requestName: String, session: Session, transport: ModbusTcpMaster): Validation[Future[ByteBufModbusResponse]] =
    for {
      unitIdVal  <- unitId(session)
      addressVal <- address(session)
      countVal   <- count(session)
    } yield transport.sendRequest(new ReadHoldingRegistersRequest(addressVal, countVal), unitIdVal).toScala

  override def requestName: Expression[String] = { session =>
    for {
      unitIdVal  <- unitId(session)
      addressVal <- address(session)
      countVal   <- count(session)
      transport  <- session(ModbusAction.TransportKey).validate[ModbusTcpMaster]
    } yield s"Read Coils [${transport.getConfig.getAddress}:${transport.getConfig.getPort}] [$unitIdVal] [$addressVal] [$countVal]"
  }
}

final case class CloseAction(statsEngine: StatsEngine, next: Action) extends ChainableAction with NameGen {

  override def name: String = genName("close")

  override def execute(session: Session): Unit = {
    logger.debug(s"Closing session [${session.startDate}], session life was [${System.currentTimeMillis() - session.startDate}] ms")
    session(ModbusAction.TransportKey).asOption[ModbusTcpMaster].foreach(_.disconnect())
    next ! session.remove(ModbusAction.TransportKey)
  }
}

object ModbusAction {
  final val TransportKey = "transport"
}
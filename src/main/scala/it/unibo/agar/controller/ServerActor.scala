package it.unibo.agar.controller

import akka.actor.typed.*
import akka.actor.typed.scaladsl.*
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import it.unibo.agar.Message.*
import it.unibo.agar.model.MockGameStateManager

import scala.concurrent.duration.*

object ServerActor:

  // La dio di chiave per il receptionist
  val ServerKey: ServiceKey[ServerCommand] = ServiceKey[ServerCommand]("ServerService")

  def apply(manager: MockGameStateManager): Behavior[ServerCommand] =
    Behaviors.setup { ctx =>
      ctx.system.receptionist ! Receptionist.Register(ServerKey, ctx.self)
      ctx.log.info("Server registrato nel Receptionist")

      Behaviors.withTimers { timers =>
        timers.startTimerAtFixedRate(Tick(manager.getWorld), 3.seconds)
        running(manager, Set.empty)
      }
    }

  private def running(manager: MockGameStateManager, clients: Set[ActorRef[ClientCommand]]): Behavior[ServerCommand] =
    Behaviors.receive { (ctx, msg) =>
      msg match
        case RegisterClient(client) =>
          ctx.log.info(s"Client registrato: $client")
          val newClients = clients + client
          //todo modifica il mondo aggiungendo il player
          newClients.foreach(_ ! UpdateClient(manager.getWorld))
          running(manager, newClients)


        case tick: Tick =>
          clients.foreach(_ ! UpdateClient(tick.world))
          ctx.log.info(s"Inviato update a ${clients.size} client")
          Behaviors.same

        case updateDirection: UpdatePlayerDirection =>
          ctx.log.info(s"Inviato messaggio a ${clients.size} client")
          manager.movePlayerDirection(updateDirection.id, updateDirection.dx, updateDirection.dy)
          Behaviors.same

    }





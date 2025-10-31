package it.unibo.agar.controller

import akka.actor.typed.*
import akka.actor.typed.scaladsl.*
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import it.unibo.agar.Message.*
import it.unibo.agar.model.MockGameStateManager
import it.unibo.agar.view.GlobalView

import scala.concurrent.duration.*

object ServerActor:

  // La dio di chiave per il receptionist
  val ServerKey: ServiceKey[ServerCommand] = ServiceKey[ServerCommand]("ServerService")

  def apply(view: GlobalView): Behavior[ServerCommand] =
    Behaviors.setup { ctx =>
      ctx.system.receptionist ! Receptionist.Register(ServerKey, ctx.self)
      ctx.log.info("Server registrato nel Receptionist")

      Behaviors.withTimers { timers =>
        timers.startTimerAtFixedRate(Tick(view.manager.getWorld), 3.seconds)
        running(view.manager, Set.empty, view)
      }
    }

  private def running(manager: MockGameStateManager, clients: Set[ActorRef[ClientCommand]], view: GlobalView): Behavior[ServerCommand] =
    Behaviors.receive { (ctx, msg) =>
      msg match
        case RegisterClient(client) =>
          ctx.log.info(s"Client registrato: $client")
          val newClients = clients + client
          //todo modifica il mondo aggiungendo il player
          manager.world = manager.world.addPlayer(clients.size.toString)
          view.manager = manager.copy(manager.world)
          client ! ThisIsYourId(clients.size.toString)

          newClients.foreach(_ ! UpdateClient(manager.getWorld))
          running(manager, newClients, view)


        case tick: Tick =>
          clients.foreach(_ ! UpdateClient(tick.world))
          ctx.log.info(s"Inviato update a ${clients.size} client")
          manager.world = manager.world.copy(foods = Seq.empty)
          val man = manager.copy(manager.world)
          view.manager = man
          view.repaint()
          Behaviors.same

        case updateDirection: UpdatePlayerDirection =>
          ctx.log.info(s"Inviato messaggio a ${clients.size} client")
          manager.movePlayerDirection(updateDirection.id, updateDirection.dx, updateDirection.dy)
          Behaviors.same

    }





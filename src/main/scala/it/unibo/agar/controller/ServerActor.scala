package it.unibo.agar.controller

import akka.actor.typed.*
import akka.actor.typed.scaladsl.*
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import it.unibo.agar.Message.*
import it.unibo.agar.model.{FoodManager, ServerGameStateManager}
import it.unibo.agar.view.GlobalView

import scala.concurrent.duration.*

object ServerActor:

  // La dio di chiave per il receptionist
  val ServerKey: ServiceKey[ServerCommand] = ServiceKey[ServerCommand]("ServerService")

  def apply(manager: ServerGameStateManager, view: GlobalView): Behavior[ServerCommand] =
    Behaviors.setup { ctx =>
      ctx.system.receptionist ! Receptionist.Register(ServerKey, ctx.self)
      ctx.log.info("Server registrato nel Receptionist")

      val fm = ctx.spawn(FoodManager(), "foodManager")

      Behaviors.withTimers { timers =>
        timers.startTimerAtFixedRate(Tick(), 30.millis)
        timers.startTimerAtFixedRate(GenerateFood(), 1.seconds)
        running(manager, Set.empty, view, fm)
      }
    }

  private def running(manager: ServerGameStateManager,
                      clients: Set[ActorRef[ClientCommand]],
                      view: GlobalView,
                      foodManager: ActorRef[FoodManagerCommand]): Behavior[ServerCommand] =
    Behaviors.receive { (ctx, msg) =>
      msg match
        case RegisterClient(client) =>
          ctx.log.info(s"Client registrato: $client")
          val newClients = clients + client
          manager.world = manager.world.addPlayer(clients.size.toString)
          view.manager = manager.copy(manager.world)
          client ! Init(clients.size.toString, manager.world)
          newClients.foreach(_ ! UpdateClient(manager.getWorld))
          running(manager, newClients, view, foodManager)

        case DisconnectClient(client, id) =>
          val player = manager.world.playerById(id).get
          manager.world = manager.world.removePlayers(Seq(player))
          view.repaint()
          running(manager, clients.filterNot(_ == client), view, foodManager)

        case Tick() =>
          manager.tick()
          val winner = manager.winningPlayer
          if winner.isDefined then
            ctx.log.info(s"Il giocatore ${winner.get.id} ha raggiunto la massa per vincere")
            clients.foreach(_ ! EndGame(winner.get.id))
          clients.foreach(_ ! UpdateClient(manager.world))
          ctx.log.info(s"Inviato update a ${clients.size} client")
          view.repaint()
          Behaviors.same

        case GenerateFood() =>
          foodManager ! AddFood(ctx.self)
          ctx.log.info(s"Inviato update a ${clients.size} client")
          view.repaint()
          Behaviors.same

        case updateDirection: UpdatePlayerDirection =>
          manager.movePlayerDirection(updateDirection.id, updateDirection.dx, updateDirection.dy)
          Behaviors.same

        case ServerAddFood(food) =>
          manager.world = manager.world.copy(foods = manager.world.foods :+ food)
          val man = manager.copy(manager.world)
          view.manager = man
          view.repaint()
          Behaviors.same
    }





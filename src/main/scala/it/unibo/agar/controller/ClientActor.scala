package it.unibo.agar.controller

import akka.actor.typed.{ActorRef, Behavior, PostStop}
import akka.actor.typed.scaladsl.*
import it.unibo.agar.Message.{ClientCommand, DisconnectClient, Init, RegisterClient, ServerCommand, UpdateClient}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import ServerActor.ServerKey
import it.unibo.agar.view.LocalView

object ClientActor:
  var servers: Set[ActorRef[ServerCommand]] = Set.empty
  
  def apply(view: LocalView): Behavior[ClientCommand | Receptionist.Listing] = {
    Behaviors.setup { ctx =>
      val adapter = ctx.messageAdapter[Receptionist.Listing](listing => listing)
      ctx.system.receptionist ! Receptionist.Subscribe(ServerKey, adapter)

      ctx.log.info("Client avviato e in attesa del server...")
      Behaviors.receiveMessage[ClientCommand | Receptionist.Listing] {
        case listing: Receptionist.Listing =>
          servers = listing.serviceInstances(ServerKey)
          servers.foreach { serverRef =>
            ctx.log.info(s"Trovato server $serverRef — invio RegisterClient")
            serverRef ! RegisterClient(ctx.self.narrow[ClientCommand])
          }
          Behaviors.same

        case Init(id, w) =>
          ctx.log.info(s"Ricevuto id $id")
          view.playerId = id
          view.manager.world = w
          view.title = s"Agar.io - Local View ($id)"
          view.open()
          Behaviors.same

        case world: UpdateClient =>
          ctx.log.info(s"Ricevuto stato del mondo ${world.world.players}")
          view.manager.world = world.world
          view.repaint()
          Behaviors.same

        case _ =>
          ctx.log.info("Unknown message type received")
          Behaviors.same
      }.receiveSignal{
        case (ctx, PostStop) =>
          servers.foreach(_ ! DisconnectClient(ctx.self.narrow[ClientCommand], view.playerId))
          Behaviors.same
      }
    }
  }

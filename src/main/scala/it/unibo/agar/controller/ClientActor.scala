package it.unibo.agar.controller

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.*
import it.unibo.agar.Message.{ClientCommand, RegisterClient, ThisIsYourId, UpdateClient}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import ServerActor.ServerKey

object ClientActor:
  def apply(): Behavior[ClientCommand | Receptionist.Listing] =
    Behaviors.setup { ctx =>      
      val adapter = ctx.messageAdapter[Receptionist.Listing](listing => listing)
      ctx.system.receptionist ! Receptionist.Subscribe(ServerKey, adapter)

      ctx.log.info("Client avviato e in attesa del server...")
      Behaviors.receiveMessage {
        case listing: Receptionist.Listing =>
          val servers = listing.serviceInstances(ServerKey)
          servers.foreach { serverRef =>
            ctx.log.info(s"Trovato server $serverRef — invio RegisterClient")
            serverRef ! RegisterClient(ctx.self.narrow[ClientCommand])
          }
          Behaviors.same

        case id: ThisIsYourId =>
          ctx.log.info(s"Ricevuto id $id")
          Behaviors.same

        case world: UpdateClient =>
          ctx.log.info(s"Ricevuto stato del mondo")
          Behaviors.same
        case _ =>
          ctx.log.info("Unknown message type received")
          Behaviors.same
      }
    }

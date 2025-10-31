package it.unibo.agar

import akka.actor.typed.ActorRef
import it.unibo.agar.model.{Food, MockGameStateManager, World}

object Message:
  /** Tag interface for all messages sends by actors */
  trait Message
  
  // Messaggini per il server
  trait ServerCommand extends Message
  final case class RegisterClient(client: ActorRef[ClientCommand]) extends ServerCommand
  final case class UpdatePlayerDirection(id: String, dx: Double, dy: Double) extends ServerCommand
  final case class Tick(world: World) extends ServerCommand
  final case class WorldUpdated(world: World) extends ServerCommand
  final case class DisconnectClient() extends Message
  final case class ServerAddFood(food: Food) extends ServerCommand


  // Messaggini verso il client
  trait ClientCommand extends Message
  final case class UpdateClient(world: World) extends ClientCommand
  case class ThisIsYourId(id: String) extends ClientCommand
  
  trait FoodManagerCommand extends Message
  final case class AddFood(replyTo: ActorRef[ServerCommand]) extends FoodManagerCommand
  final case class RemoveFood(world: World, food: Food, replyTo: ActorRef[World]) extends FoodManagerCommand

  enum Mess extends Message:
    case Boh
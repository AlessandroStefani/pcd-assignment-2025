package it.unibo.agar

import akka.actor.typed.ActorRef
import it.unibo.agar.model.{Food, World}

object Message:
  /** Tag interface for all messages sends by actors */
  trait Message
  
  // Messaggini per il server
  trait ServerCommand extends Message
  final case class RegisterClient(client: ActorRef[ClientCommand]) extends ServerCommand
  final case class UpdatePlayerDirection(id: String, dx: Double, dy: Double) extends ServerCommand
  final case class Tick() extends ServerCommand
  final case class GenerateFood() extends ServerCommand
  final case class WorldUpdated(world: World) extends ServerCommand
  final case class DisconnectClient(client: ActorRef[ClientCommand], id: String) extends ServerCommand
  final case class ServerAddFood(food: Food) extends ServerCommand

  // Messaggini verso il client
  trait ClientCommand extends Message
  final case class UpdateClient(world: World) extends ClientCommand
  final case class Init(id: String, world: World) extends ClientCommand
  final case class EndGame(winnerId: String) extends ClientCommand

  trait FoodManagerCommand extends Message
  final case class AddFood(replyTo: ActorRef[ServerCommand]) extends FoodManagerCommand
  final case class RemoveFood(world: World, food: Food, replyTo: ActorRef[World]) extends FoodManagerCommand

  enum Mess extends Message:
    case Boh
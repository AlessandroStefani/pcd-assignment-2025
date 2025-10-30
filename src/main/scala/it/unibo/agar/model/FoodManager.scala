package it.unibo.agar.model

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.Message
import it.unibo.agar.Message.FoodManagerCommand
import it.unibo.agar.model.{Food, World}

import scala.util.Random

object FoodManager {

  def apply(manager: MockGameStateManager): Behavior[FoodManagerCommand] =
    Behaviors.receive { (context, message) =>
      message match {
        case Message.AddFood(replyTo) =>
          val newFood = Food(
            "food" + manager.world.foods.size,
            (500).toInt,
            (500).toInt,
            500
          )
          val updatedFoodList = manager.world.foods :+ newFood
          val updatedWorld = manager.world.copy(foods = updatedFoodList)
          
          context.log.info(s"Added food at (${newFood.x}, ${newFood.y})")
          //replyTo ! updatedWorld
          val newmanager = manager.copy(world = updatedWorld)
          FoodManager(newmanager)

        case Message.RemoveFood(food, replyTo) =>
          val updatedFoodList = manager.world.foods.filterNot(_ == food)
          val updatedWorld = manager.world.copy(foods = updatedFoodList)
          context.log.info(s"Removed food at (${food.x}, ${food.y})")
          //replyTo ! updatedWorld
          val newmanager = manager.copy(world = updatedWorld)
          FoodManager(newmanager)
      }
    }
}

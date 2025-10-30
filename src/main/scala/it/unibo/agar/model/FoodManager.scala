package it.unibo.agar.model

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.Message
import it.unibo.agar.Message.FoodManagerCommand
import it.unibo.agar.model.{Food, World}

import scala.util.Random

object FoodManager {

  def apply(world: World): Behavior[FoodManagerCommand] =
    Behaviors.receive { (context, message) =>
      message match {
        case Message.AddFood(world, replyTo) =>
          val newFood = Food(
            "food" + world.foods.size,
            (500).toInt,
            (500).toInt,
            500
          )
          val updatedFoodList = world.foods :+ newFood
          val updatedWorld = world.copy(foods = updatedFoodList)
          
          context.log.info(s"Added food at (${newFood.x}, ${newFood.y})")
          replyTo ! updatedWorld
          FoodManager(updatedWorld)

        case Message.RemoveFood(world, food, replyTo) =>
          val updatedFoodList = world.foods.filterNot(_ == food)
          val updatedWorld = world.copy(foods = updatedFoodList)
          context.log.info(s"Removed food at (${food.x}, ${food.y})")
          replyTo ! updatedWorld
          FoodManager(updatedWorld)
      }
    }
}

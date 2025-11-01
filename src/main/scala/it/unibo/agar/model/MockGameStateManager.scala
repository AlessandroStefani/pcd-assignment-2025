package it.unibo.agar.model

trait GameStateManager:
  def getWorld: World
  def movePlayerDirection(id: String, dx: Double, dy: Double): Unit

case class ClientGameStateManager(var world: World = World(0, 0, Seq.empty, Seq.empty)) extends GameStateManager:
  
  override def getWorld: World = world
  override def movePlayerDirection(id: String, dx: Double, dy: Double): Unit = ???

case class ServerGameStateManager(
    var world: World,
    massForWin: Double,
    speed: Double = 10.0,
) extends GameStateManager:

  private var directions: Map[String, (Double, Double)] = Map.empty
  def getWorld: World = world

  // Move a player in a given direction (dx, dy)
  def movePlayerDirection(id: String, dx: Double, dy: Double): Unit =
    directions = directions.updated(id, (dx, dy))
    
  def addNewPlayer(id: String): Unit =
    world = world.addPlayer(id)

  def tick(): Unit =
    directions.foreach:
      case (id, (dx, dy)) =>
        world.playerById(id) match
          case Some(player) =>
            world = updateWorldAfterMovement(updatePlayerPosition(player, dx, dy))
          case None =>
          // Player not found, ignore movement

  def winningPlayer: Option[Player] =
    world.players.find(_.mass >= massForWin)

  private def updatePlayerPosition(player: Player, dx: Double, dy: Double): Player =
    val newX = (player.x + dx * speed).max(0).min(world.width)
    val newY = (player.y + dy * speed).max(0).min(world.height)
    player.copy(x = newX, y = newY)

  private def updateWorldAfterMovement(player: Player): World =
    val foodEaten = world.foods.filter(food => EatingManager.canEatFood(player, food))
    val playerEatsFood = foodEaten.foldLeft(player)((p, food) => p.grow(food))
    val playersEaten = world
      .playersExcludingSelf(player)
      .filter(player => EatingManager.canEatPlayer(playerEatsFood, player))
    val playerEatPlayers = playersEaten.foldLeft(playerEatsFood)((p, other) => p.grow(other))
    world
      .updatePlayer(playerEatPlayers)
      .removePlayers(playersEaten)
      .removeFoods(foodEaten)

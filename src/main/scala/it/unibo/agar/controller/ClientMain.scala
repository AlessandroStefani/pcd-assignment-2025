package it.unibo.agar.controller

import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory
import it.unibo.agar.model.{ClientGameStateManager, World}
import it.unibo.agar.view.LocalView

import scala.swing.{Frame, SimpleSwingApplication}

//@main def ClientMain(): Unit =
//  val manager = MockGameStateManager(World(0, 0, Seq.empty, Seq.empty))
//  val view = LocalView(manager, "N/A")
//  val system = ActorSystem(ClientActor(view), "ClusterSystem", ConfigFactory.load("client.conf"))

object ClientMain extends SimpleSwingApplication:
  val manager: ClientGameStateManager = ClientGameStateManager()
  var view = LocalView(manager, "N/A")
  val system = ActorSystem(ClientActor(manager, view), "ClusterSystem", ConfigFactory.load("client.conf"))

  override def top: Frame =
    new Frame { visible = false }


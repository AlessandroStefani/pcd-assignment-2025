package it.unibo.agar.controller

import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory

@main def ClientMain(): Unit =
  val system = ActorSystem(ClientActor(), "ClusterSystem", ConfigFactory.load("client.conf"))

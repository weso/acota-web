package models

import akka.actor.Actor
import play.Logger

class HolderActor extends Actor {
  var sessions: Map[String, Int] = Map.empty

  override def receive = {
    case CheckSession(sessionId, id) =>
      sender ! (this.sessions.getOrElse(sessionId, -1) == id)
    case SetSession(sessionId) =>
      val newId = this.sessions.getOrElse(sessionId, 0) + 1
      this.sessions += sessionId -> newId
      sender ! newId
  }
}

case class CheckSession(sessionId: String, id: Int)
case class SetSession(sessionId: String)


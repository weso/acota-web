package controllers

import play.api.mvc.Controller
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee._
import models._
import akka.actor._
import scala.concurrent.duration._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Akka
import play.api.Play.current
import models.EchoActor
import models.Acota._

object WebSocket extends Controller {
  /**
   * Handles the chat websocket.
   */
  def recommend() = Acota.join

}
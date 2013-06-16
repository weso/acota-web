package models

import akka.actor._
import scala.concurrent.duration._
import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import akka.util.Timeout
import akka.pattern.ask
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api._
import play.api.mvc._
import akka.pattern.ask
import es.weso.acota.core.CoreConfiguration
import es.weso.acota.core.business.enhancer.LuceneEnhancer
import es.weso.acota.core.business.enhancer.OpenNLPEnhancer
import es.weso.acota.core.business.enhancer.TokenizerEnhancer
import es.weso.acota.core.business.enhancer.WordnetEnhancer
import es.weso.acota.core.business.enhancer.GoogleEnhancer
import es.weso.acota.core.entity.ResourceTO
import es.weso.acota.core.entity.RequestSuggestionTO
import es.weso.acota.core.utils.AcotaUtil
import scala.collection.JavaConversions._
import akka.pattern._

import akka.pattern.ask

object Acota {

  implicit val timeout = akka.util.Timeout(1 second)

  def join = WebSocket.async[JsValue] {
    requestHeader =>
      val actor = Akka.system.actorOf(Props[EchoActor])
      (actor ? Start()) map {
        case Connected(event) =>
          val in = Iteratee.foreach[JsValue] {
            event => actor ! Recommend((event \ "uri").as[String], (event \ "label").as[String], (event \ "description").as[String])
          }
          (in, event)
      }
  }
}

// Actor messages
case class Message(msg: JsValue)
case class Start()
case class Connected(out: PushEnumerator[JsValue])
case class Recommend(uri: String, label: String, description: String)

class EchoActor extends Actor {
  var out: PushEnumerator[JsValue] = _
  val coreConf = new CoreConfiguration
  override def receive = {
    case Start() =>
      this.out = Enumerator.imperative[JsValue]()
      sender ! Connected(out)
    case Recommend(uri, label, description) => {
      val resource = new ResourceTO()
      resource.setUri(uri)
      resource.setLabel(label)
      resource.setDescription(description)

      val request = new RequestSuggestionTO()
      request.setResource(resource)

      val luceneE = new LuceneEnhancer(coreConf)
      val openNLPE = new OpenNLPEnhancer(coreConf)
      val tokenizerE = new TokenizerEnhancer(coreConf)
      val wordnetE = new WordnetEnhancer(coreConf)
      val googleE = new GoogleEnhancer(coreConf)
      var suggestions = luceneE.enhance(request)
      Logger.info("Listo" + suggestions.getTags().size())
      this.out.push(Json.obj("recommendations" -> generateJson(request)))
      suggestions = openNLPE.enhance(request)
      Logger.info("Listo" + suggestions.getTags().size())
      this.out.push(Json.obj("recommendations" -> generateJson(request)))
      suggestions = tokenizerE.enhance(request)
      Logger.info("Listo" + suggestions.getTags().size())
      this.out.push(Json.obj("recommendations" -> generateJson(request)))
      suggestions = wordnetE.enhance(request)
      Logger.info("Listo" + suggestions.getTags().size())
      this.out.push(Json.obj("recommendations" -> generateJson(request)))
      suggestions = googleE.enhance(request)
      Logger.info("Listo" + suggestions.getTags().size())
      this.out.push(Json.obj("recommendations" -> generateJson(request)))
    }
  }
  def generateJson(request: RequestSuggestionTO) = {
    val tags = AcotaUtil.sortTags(request.getSuggestions.getTags())
    val limit = if (tags.size() > 12) 12 else tags.size()
    tags.subList(0, limit).map(a => Json.obj("label" -> a.getValue().getLabel(), "lang" -> a.getValue().getLang(), "value" -> a.getValue().getValue()))
  }
}
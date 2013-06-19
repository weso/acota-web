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
import es.weso.acota.core.entity.SuggestionTO
import es.weso.acota.core.utils.AcotaUtil
import scala.collection.JavaConversions._
import akka.pattern._
import java.security.SecureRandom
import es.weso.acota.core.business.enhancer.LabelRecommenderEnhancer
import es.weso.acota.core.FeedbackConfiguration

object Acota {

  implicit val timeout = akka.util.Timeout(1 second)

  val holder = Akka.system.actorOf(Props[HolderActor])
  val coreConf = new CoreConfiguration
  coreConf.setMemcachedEnabled(true)
  val feedbackConf = new FeedbackConfiguration

  def join = WebSocket.async[JsValue] {
    requestHeader =>
      val actor = Akka.system.actorOf(Props[AcotaActor])
      (actor ? Start()) map {
        case Connected(event) =>
          val in = Iteratee.foreach[JsValue] {
            event =>
              Logger.info(event.toString)
              val id = (event \ "id").as[String]
              if (id equals "?") {
                actor ! Connect()
              } else if (id==null){
                
              }else{
                actor ! Recommend(id, (event \ "uri").as[String], (event \ "label").as[String], (event \ "description").as[String])
              }
          }
          (in, event)
      }
  }
}

// Actor messages
case class Start()
case class Connected(out: PushEnumerator[JsValue])


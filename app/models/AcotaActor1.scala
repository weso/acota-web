package models

import play.api.libs.iteratee.PushEnumerator
import es.weso.acota.core.CoreConfiguration
import es.weso.acota.core.FeedbackConfiguration
import es.weso.acota.core.business.enhancer.LuceneEnhancer
import es.weso.acota.core.business.enhancer.OpenNLPEnhancer
import es.weso.acota.core.business.enhancer.TokenizerEnhancer
import es.weso.acota.core.business.enhancer.WordnetEnhancer
import es.weso.acota.core.business.enhancer.GoogleEnhancer
import es.weso.acota.core.business.enhancer.LabelRecommenderEnhancer
import es.weso.acota.core.entity.ResourceTO
import es.weso.acota.core.entity.RequestSuggestionTO
import es.weso.acota.core.entity.SuggestionTO
import es.weso.acota.core.utils.AcotaUtil
import scala.collection.JavaConversions._
import akka.actor._
import akka.pattern._
import akka.pattern.ask
import scala.concurrent.duration._
import java.security.SecureRandom
import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import models._

case class Connect()
case class Recommend(id: String, uri: String, label: String, description: String)

class AcotaActor extends Actor {
  implicit val timeout = akka.util.Timeout(1 second)
  var out: PushEnumerator[JsValue] = _
  val random = new SecureRandom
  override def receive = {
    case Start() =>
      this.out = Enumerator.imperative[JsValue]()
      sender ! Connected(out)
    case Connect() => {
      val session = BigInt(130, random).toString(32)
      this.out.push(Json.obj("id" -> BigInt(130, random).toString(32)))
      Logger.info("Connected: " + session)
    }
    case Recommend(id, uri, label, description) => {
      val resource = new ResourceTO()
      resource.setUri(uri)
      resource.setLabel(label)
      resource.setDescription(description)

      val request = new RequestSuggestionTO()
      request.setResource(resource)

      (Acota.holder ? SetSession(id)) map {
        case n: Int =>
          perform(n)

      }
      def perform(n: Int) {
        def runA1() {
          val luceneE = new LuceneEnhancer(Acota.coreConf)
          val suggestionsOut = luceneE.enhance(request)
          (Acota.holder ? CheckSession(id, n)) map {
            case true => runA2(suggestionsOut)
          }
        }

        def runA2(suggestions: SuggestionTO) {
          Logger.info("A.1 Listo" + suggestions.getTags().size())
          this.out.push(Json.obj("recommendations" -> generateJson(request)))
          val openNLPE = new OpenNLPEnhancer(Acota.coreConf)
          val suggestionsOut = openNLPE.enhance(request)
          (Acota.holder ? CheckSession(id, n)) map {
            case true => runA3(suggestionsOut)
          }
        }

        def runA3(suggestions: SuggestionTO) {
          Logger.info("A.2 Listo" + suggestions.getTags().size())
          this.out.push(Json.obj("recommendations" -> generateJson(request)))
          val tokenizerE = new TokenizerEnhancer(Acota.coreConf)
          val suggestionsOut = tokenizerE.enhance(request)
          (Acota.holder ? CheckSession(id, n)) map {
            case true => runB1(suggestionsOut)
          }
        }

        def runB1(suggestions: SuggestionTO) {
          Logger.info("A.3 Listo" + suggestions.getTags().size())
          this.out.push(Json.obj("recommendations" -> generateJson(request)))
          val wordnetE = new WordnetEnhancer(Acota.coreConf)
          val suggestionsOut = wordnetE.enhance(request)
          (Acota.holder ? CheckSession(id, n)) map {
            case true => runB2(suggestionsOut)
          }
        }

        def runB2(suggestions: SuggestionTO) {
          Logger.info("B.1 Listo" + suggestions.getTags().size())
          this.out.push(Json.obj("recommendations" -> generateJson(request)))
          val googleE = new GoogleEnhancer(Acota.coreConf)
          val init = System.nanoTime()
          val suggestionsOut = googleE.enhance(request)
          Logger.info("MS: "+(init-System.nanoTime))
          (Acota.holder ? CheckSession(id, n)) map {
            case true => runC1(suggestionsOut)
          }
        }

        def runC1(suggestions: SuggestionTO) {
          Logger.info("B.2 Listo" + suggestions.getTags().size())
          this.out.push(Json.obj("recommendations" -> generateJson(request)))
          val mahoutE = new LabelRecommenderEnhancer(Acota.feedbackConf)
          val suggestionsOut = mahoutE.enhance(request)
          (Acota.holder ? CheckSession(id, n)) map {
            case true =>
              Logger.info("C.1 Listo" + suggestions.getTags().size())
              this.out.push(Json.obj("recommendations" -> generateJson(request)))
          }
        }
        runA1()
      }

    }
  }

  def generateJson(request: RequestSuggestionTO) = {
    val tags = AcotaUtil.sortTags(request.getSuggestions.getTags())
    val limit = if (tags.size() > 12) 12 else tags.size()
    tags.subList(0, limit).map(a => Json.obj("label" -> a.getValue().getLabel(), "lang" -> a.getValue().getLang(), "value" -> a.getValue().getValue()))
  }
}
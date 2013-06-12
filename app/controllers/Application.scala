package controllers

import play.api._
import play.api.mvc._
import models.WResource
import play.api.libs.functional.syntax._
import play.api.libs.json._
import formatters.WResourceFormatter._
import utils.JsonNotFound
import utils.JsonBadRequest
import play.api.http.Status._
import scala.collection.JavaConversions._
import es.weso.acota.core.entity.ResourceTO
import es.weso.acota.core.CoreConfiguration
import es.weso.acota.core.FeedbackConfiguration
import es.weso.acota.core.business.enhancer.LuceneEnhancer
import es.weso.acota.core.business.enhancer.OpenNLPEnhancer
import es.weso.acota.core.business.enhancer.TokenizerEnhancer
import es.weso.acota.core.business.enhancer.GoogleEnhancer
import es.weso.acota.core.business.enhancer.WordnetEnhancer
import es.weso.acota.core.utils.AcotaUtil
import es.weso.acota.core.business.enhancer.EnhancerAdapter
import es.weso.acota.core.entity.RequestSuggestionTO

object Application extends Controller {

  val coreConf = new CoreConfiguration
  val feedbackConf = new FeedbackConfiguration
  val luceneE = new LuceneEnhancer(coreConf)
  val openNLPE = new OpenNLPEnhancer(coreConf)
  val tokenizerE = new TokenizerEnhancer(coreConf)
  val wordnetE = new WordnetEnhancer(coreConf)
  val googleE = new GoogleEnhancer(coreConf)

  AcotaUtil.enchain(Array[EnhancerAdapter](luceneE, openNLPE,
    tokenizerE, wordnetE, googleE))
    
  def index = Action {
    Ok(views.html.index("Demo."))
  }

  def recommend = Action { implicit request =>
    println("ENTRA")
    println("Request :"+request.body)
    request.body.asJson.map { json =>
      json.asOpt[WResource].map { wResource =>
        val resource = new ResourceTO()
        resource.setUri(wResource.uri)
        resource.setLabel(wResource.label)
        resource.setDescription(wResource.description)

        val request = new RequestSuggestionTO()
        request.setResource(resource)
        Logger.info("Llamada")
        val suggestions = luceneE.enhance(request)
        Logger.info("Listo")
        val result = AcotaUtil.sortTags(suggestions.getTags()).subList(0, 12).map(a => Json.obj("label" ->a.getValue().getLabel(), "lang" -> a.getValue().getLang(), "value" -> a.getValue().getValue()))
        Logger.info("Done")
        Ok(Json.obj("status" -> "OK", "recommendations" -> result))
      }.getOrElse(JsonBadRequest("Bad Request"))
    }.getOrElse(JsonBadRequest("Empty Request"))
  }

}
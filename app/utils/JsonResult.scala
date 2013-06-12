package utils

import play.api.http.Status
import play.api.libs.json.Json._
import play.api.mvc.Results
import models.Error

import formatters.ErrorFormatter._

object JsonBadRequest {
  def apply(message: String = "Bad request error", status: Int = Status.BAD_REQUEST, errorCode: Int = 10000) = {
    Results.BadRequest(toJson(Error(status, errorCode, message)))
  }
  def apply(errors: List[Error]) = {
    Results.BadRequest(toJson(errors))
  }
  def apply(error: Error) = {
    Results.BadRequest(toJson(error))
  }
}

object JsonNotFound {
  def apply(message: String = "Not found", developerMessage: String = "Not found",
    status: Int = Status.NOT_FOUND, errorCode: Int = 0) = {
    Results.NotFound(toJson(Error(status, errorCode, message)))
  }
}
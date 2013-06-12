package models

import play.api.http.Status

case class Error(
  val status: Int = Status.INTERNAL_SERVER_ERROR,
  val errorCode: Int = 10000,
  val message: String = "")

object ValidationError {
  def apply(message: String) = {
    Error(status = Status.BAD_REQUEST, message = message)
  }
}
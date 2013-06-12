package models

import play.api.libs.json._
import play.api.libs.json.Format._
import play.api.libs.functional.syntax._

case class WResource(val uri: String, val label: String, val description: String)

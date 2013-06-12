package formatters

import models.WResource
import play.api.libs.functional.syntax._
import play.api.libs.json._

object WResourceFormatter {

  implicit val wResourceFormat: Format[WResource] = (
    (__ \ "uri").format[String] and
    (__ \ "label").format[String] and
    (__ \ "description").format[String])(WResource.apply, unlift(WResource.unapply))
}
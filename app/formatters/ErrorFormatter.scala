package formatters

import models.Error
import play.api.libs.functional.syntax._
import play.api.libs.json._

object ErrorFormatter {
  implicit val wResourceFormat: Format[Error] = (
    (__ \ " ").format[Int] and
    (__ \ " ").format[Int] and
    (__ \ " ").format[String])(Error.apply, unlift(Error.unapply))
}
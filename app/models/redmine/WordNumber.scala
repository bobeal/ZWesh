package models.redmine

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class WordNumber(text: String, size: Long)

object WordNumberHelper {
  implicit val WordNumberFormat = Json.format[WordNumber]
}


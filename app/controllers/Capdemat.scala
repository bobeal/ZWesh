package controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.ws._
import play.api.cache.Cache
import play.api.Play.current
import play.api.http._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
object Capdemat extends Controller {

  def index = TODO

  def dashboard = Action {
    import play.api.libs.json._
    Ok(views.html.widgets.capdemat.capdematList(Json.obj(
        "widget-title" -> "CapDemat",
        "cities" -> JsArray(Seq(
            Json.obj("name" -> "Pessac",
                    "userCount" -> 8948,
                    "askCount" -> 30929,
                    "paymentCount" -> 3727),
            Json.obj("name" -> "Beaucouzé",
                    "userCount" -> 609,
                    "askCount" -> 2861,
                    "paymentCount" -> 2156),
            Json.obj("name" -> "Roubaix",
                    "userCount" -> 10649,
                    "askCount" -> 31196,
                    "paymentCount" -> 0)
        ))
    )))
  }

  def rawData = Action {
    Async {
      Cache
        .getOrElse("capdemat-json", 60*5)(WS.url("http://dev.wenria.com/api/concours/search/").get().map(_.body))
        .map(data => Ok(data).withHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON))
    }
  }

}
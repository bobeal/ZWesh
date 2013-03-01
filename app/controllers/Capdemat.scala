package controllers

import play.api._
import play.api.mvc._

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
}
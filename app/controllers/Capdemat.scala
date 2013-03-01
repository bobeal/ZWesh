package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._

import play.api.libs.json._
import play.api.libs.ws._
import play.api.cache.Cache
import play.api.Play.current
import play.api.http._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

object Capdemat extends Controller {
  lazy val LOGGER = play.api.Logger

  def index = TODO

  def dashboard = Action {
    Async {
        LOGGER.info("Sent request to mairie24")
        // WS.url("http://zwesh.loc:9000/capdemat/raw").withTimeout(5 * 60000).get.map { response =>
        //     LOGGER.info("Received response from mairie24")
        //     val json = response.json
        //     Ok(views.html.widgets.capdemat.capdematList(mapDashboard(json)))
        // }


        // MOCK
        LOGGER.info("Received response from mairie24")
        val json = Json.obj(
            "Pessac" -> Json.obj(
                "nbOfIndividuals" -> 8948,
                "nbOfRequests" -> 30929,
                "nbOfPayments" -> 3727
            ),
            "Beaucouzé" -> Json.obj(
                "nbOfIndividuals" -> 609,
                "nbOfRequests" -> 2861,
                "nbOfPayments" -> 2156
            ),
            "Roubaix" -> Json.obj(
                "nbOfIndividuals" -> 10649,
                "nbOfRequests" -> 31196,
                "nbOfPayments" -> 0
            ),
            "Total" -> Json.obj(
                "nbOfIndividuals" -> 20206,
                "nbOfRequests" -> 64986,
                "nbOfPayments" -> 5883
            )
        )
        Future.successful(Ok(views.html.widgets.capdemat.capdematList(mapDashboard(json))))
    }
  }

  private def mapDashboard(json: JsValue): JsValue = {
    import play.api.libs.json._

    val cities: List[JsValue] = json.asOpt[JsObject].map(_.fieldSet.toList.map { case (cityName, cityData) =>
        Json.obj("name" -> cityName,
            "userCount" -> (cityData \ "nbOfIndividuals").as[Long],
            "askCount" -> (cityData \ "nbOfRequests").as[Long],
            "paymentCount" -> (cityData \ "nbOfPayments").as[Long])
    }).getOrElse(List())
    Json.obj(
        "widget-title" -> "CapDemat",
        "cities" -> JsArray(cities.toSeq)
    )
  }

  def rawData = Action {
    Async {
      Cache
        .getOrElse("capdemat-json", 60 * 60)(
          WS.url("https://demarches-plaisirenligne.mairie24.fr/service/statistics").withTimeout(5 * 60000).get.map(_.body)
      ).map(data =>
        Ok(data).withHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON))
    }
  }

}
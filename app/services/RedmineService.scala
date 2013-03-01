package services

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

object RedmineService {

  def getIssues = {
      WS.url("https://code.zenexity.com/issues.json?limit=100&key=" + Play.current.configuration.getString("api.key").getOrElse("")).get()
  }
}

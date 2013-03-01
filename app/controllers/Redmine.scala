package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import services._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.redmine.WordNumberHelper._

object Redmine extends Controller {

  def issues = Action {
      Async {
        RedmineService.getIssues.map{ response =>
          Ok(response.json)
        }
      }
  }

  def revisions = Action {
    Async {
      RedmineService.getRevisions.map{ response =>
        Ok(response.xml)
      }
    }
  }

  def cloud = Action {
    Ok(views.html.redmine.cloud())
  }


  def getDescWordNumber = Action {
    Async {
       RedmineService.getIssuesDescriptionWordNumber.map( response =>
       Ok(Json.toJson(response))
      )
    }
  }
}

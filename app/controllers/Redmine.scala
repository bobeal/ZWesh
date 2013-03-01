package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import services._

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
}

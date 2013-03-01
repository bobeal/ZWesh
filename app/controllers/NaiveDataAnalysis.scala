package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import services._
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

object NaiveDataAnalysis extends Controller {

  implicit val tupleStringIntWrites = (
    (__ \ "ticketId").write[String] and
    (__ \ "nbCommits").write[Int]
  )({t: (String, Int) => t})

  def commitsByTickets = Action {
    Async {
      RedmineService.getIssues.flatMap{ res =>
        res.status match {
          case 200 =>
            RedmineService.getRevisions.map{ revisions =>
              revisions.status match {
                case 200 =>
                  val CommitTitleRegex = """<.*?>.*? ([a-z0-9]{8}): (.*?)</.*?>""".r
                  val issuesIds = (res.json \ "issues").as[List[JsValue]].map( issue =>
                    (issue \ "id").toString
                  )
                  val commits:List[(String, String)] = (revisions.xml \\ "title").map{ title =>
                    for(CommitTitleRegex(hash, message) <- CommitTitleRegex findFirstIn title.toString)
                    yield ((hash, message))
                  }.toList.flatten
                  issuesIds.map{ id =>
                    val c = commits.filter(_._2.contains(id))
                    (id, c.size)
                  }
                case _ => Nil
              }
            }
          case _ => Future(Nil)
        }
      }.map{ value =>
        Ok(Json.toJson(value))
      }
    }
  }

  def commitsWithoutTicketNumber = TODO

  def commitsWithMoreThanOneTicketNumber = TODO

}


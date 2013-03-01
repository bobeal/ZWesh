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

  implicit val tupleStringStringWrites = (
    (__ \ "commit").write[String] and
    (__ \ "message").write[String]
  )({t: (String, String) => t})

  def commitsByTickets = Action {
    Async {
      RedmineService.getIssues.flatMap{ res =>
        res.status match {
          case 200 =>
            val issuesIds = (res.json \ "issues").as[List[JsValue]].map( issue =>
              (issue \ "id").toString
            )
            findCommits.map{ commits =>
              issuesIds.map{ id =>
                val c = commits.filter(_._2.contains(id))
                (id, c.size)
              }
            }
          case _ => Future(Nil)
        }
      }.map{ value =>
        Ok(Json.toJson(value))
      }
    }
  }

  def commitsWithoutTicketNumber = Action {
    Async {
      findCommits.map( commits =>
        Ok(Json.toJson(commits.filterNot(_._2.contains("#"))))
      )
    }
  }

  def commitsWithMoreThanOneTicketNumber = Action {
    Async {
      findCommits.map( commits =>
        Ok(Json.toJson(commits.filter(_._2.count(_ == '#') > 1)))
      )
    }
  }

  private def findCommits: Future[List[(String, String)]] = {
    RedmineService.getRevisions.map{ revisions =>
      revisions.status match {
        case 200 =>
          val CommitTitleRegex = """<.*?>.*? ([a-z0-9]{8}): (.*?)</.*?>""".r
          (revisions.xml \\ "title").map{ title =>
            for(CommitTitleRegex(hash, message) <- CommitTitleRegex findFirstIn title.toString)
              yield ((hash, message))
          }.toList.flatten
        case _ => Nil
      }
    }
  }

}


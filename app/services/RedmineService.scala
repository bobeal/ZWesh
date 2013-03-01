package services

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

import play.api.libs.json._
import play.api.libs.functional.syntax._

import models.redmine.{WordNumber}

object RedmineService {

  def getIssues = {
      WS.url("https://code.zenexity.com/issues.json?limit=100&key=" + Play.current.configuration.getString("api.key").getOrElse("")).get()
  }

  def getRevisions = {
    WS.url("https://code.zenexity.com/projects/capdemat/repository/revisions.atom?key=" + Play.current.configuration.getString("rss.key").getOrElse("")).get()
  }

  def getIssuesDescriptionWordNumber : Future[List[WordNumber]] = {
    getIssues.map{ response =>
      (response.json \ "issues").as[List[JsValue]].map{ v =>
        val desc = (v \ "description").as[String]
        val title = (v \ "subject").as[String]
        (desc ++ title).split(" ").toList.map( t=>
          WordNumber(t,1L)
        )
      }.flatten.foldLeft[List[WordNumber]](Nil){ (acc, wn) =>
        val index = acc.indexWhere( w => w.text.equals(wn.text))
        if(!index.equals(-1) && wn.text.size > 4){
          val newElem = acc(index).copy(size = acc(index).size + 1)
          acc.filterNot(w => w.text.equals(wn.text)) :+ newElem
        }
        else{
          if(wn.text.size > 4) {
            acc :+ wn
          }
          else{
            acc
          }
        }
      }.foldLeft[List[WordNumber]](Nil){ (acc, wn) =>
        if(wn.size > 3) { acc :+ wn}
        else { acc }
      }


    }
  }
}

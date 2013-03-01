package controllers

import play.api._
import libs.ws.WS.WSRequestHolder
import play.api.mvc._

import play.api.libs.ws._
import play.api.libs.oauth._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Locale

object Twitter extends Controller {

  val hash = "%23"

  def list(owner: String, list: String) =
    countTweetsByUrl(s"https://api.twitter.com/1.1/lists/statuses.json?owner_screen_name=$owner&slug=$list&count=1000")

  def hashtag(hashtag: String) =
    countTweetsByUrl(s"https://api.twitter.com/1.1/search/tweets.json?q=$hash$hashtag&count=1000", _ \ "statuses")

  def hashtags(hashtags: String) = {
    val tags = hashtags.split(",").map(hash+_).mkString("%20OR%20")
    countTweetsByUrl(s"https://api.twitter.com/1.1/search/tweets.json?q=$tags&count=1000", _ \ "statuses")
  }

  private def countTweetsByUrl(url: String, extractData: JsValue => JsValue = {json => json}) = Action { implicit request =>
    TwitterOAuth.checkToken.map(implicit token =>
      Async {
        call(url).get.map(result =>
          Ok(Json.toJson(Map("count" -> countTweetsToday(extractData(result.json))))))
      }
    ).getOrElse(Forbidden)
  }

  private def countTweetsToday(json: JsValue) = {
    val limit = DateTime.now().withHourOfDay(7)
    json.as[List[Tweet]].count(t => t.date.isAfter(limit))
  }

  // -- Twitter Oauth WS

  private def call(url: String)(implicit token: RequestToken): WSRequestHolder = {
    WS.url(url).sign(OAuthCalculator(TwitterOAuth.KEY, token))
  }

  // --- Tweet parsing

  case class Tweet(date: DateTime, id: Int)

  val twitterDateReader = new Reads[org.joda.time.DateTime] {
    def reads(json: JsValue): JsResult[DateTime] = {
      json match {
        case JsNumber(d) => JsSuccess(new DateTime(d.toLong))
        case JsString(s) => {
          val sf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH)
          sf.setLenient(true)
          JsSuccess(new DateTime(sf.parse(s)))
        }
        case _ => JsError("Error while parsing date")
      }
    }
  }

  implicit val tweetReader = (
    (__ \ "created_at").read[DateTime](twitterDateReader) and
    (__ \ "id").read[Int]
  )(Tweet.apply _)

}
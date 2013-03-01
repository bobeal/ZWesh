package controllers

import play.api._
import libs.ws.WS.WSRequestHolder
import play.api.mvc._

import play.api.libs.ws._
import play.api.libs.oauth._
import play.api.Play.current
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
    sessionTokenPair.map(implicit token =>
      Async {
        println(url)
        call(url).get.map(result =>
          Ok(Json.toJson(Map("count" -> countTweetsToday(extractData(result.json))))))
      }
    ).getOrElse(Redirect(routes.Twitter.authenticate()))
  }

  private def countTweetsToday(json: JsValue) = {
    val limit = DateTime.now().withHourOfDay(7)
    json.as[List[Tweet]].count(t => t.date.isAfter(limit))
  }


  // -- Twitter Oauth

  lazy val conf = Play.configuration.getConfig("twitter").getOrElse(throw new RuntimeException("Missing config for Twitter"))
  lazy val KEY = ConsumerKey(conf.getString("clientId").get, conf.getString("clientSecret").get)
  lazy val TWITTER = OAuth(ServiceInfo(
    "https://api.twitter.com/oauth/request_token",
    "https://api.twitter.com/oauth/access_token",
    "https://api.twitter.com/oauth/authorize", KEY))

  def authenticate = Action { implicit request =>
    request.queryString.get("oauth_verifier").flatMap(_.headOption).map { verifier =>
      val tokenPair = sessionTokenPair(request).get
      TWITTER.retrieveAccessToken(tokenPair, verifier) match {
        case Right(t) => Redirect(request.uri).withSession("token" -> t.token, "secret" -> t.secret)
        case Left(e) => throw e
      }
    }.getOrElse(
      TWITTER.retrieveRequestToken(routes.Twitter.authenticate.absoluteURL()) match {
        case Right(t) => Redirect(TWITTER.redirectUrl(t.token)).withSession("token" -> t.token, "secret" -> t.secret)
        case Left(e) => throw e
      })
  }

  private def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield {
      RequestToken(token, secret)
    }
  }

  private def call(url: String)(implicit token: RequestToken): WSRequestHolder = {
    WS.url(url).sign(OAuthCalculator(Twitter.KEY, token))
  }


  // --- Tweet parsing

  case class Tweet(date: DateTime, id: Int, text: String, name: String, screenName: String)

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
      (__ \ "id").read[Int] and
      (__ \ "text").read[String] and
      (__ \ "user" \ "name").read[String] and
      (__ \ "user" \ "screen_name").read[String]
    )(Tweet.apply _)

}
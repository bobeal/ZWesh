package controllers

import play.api._
import libs.oauth.{RequestToken, ServiceInfo, OAuth, ConsumerKey}
import play.api.mvc._

object Application extends Controller with TwitterOAuthController {
  def index = Action { implicit request =>
    TwitterOAuth.checkToken
      .map(_ => Ok(views.html.index("Your new application is ready.")))
      .getOrElse(Redirect(routes.Application.authenticate))
  }
}

trait TwitterOAuthController extends Controller {
  import TwitterOAuth._

  def authenticate = Action { implicit request =>
    request.queryString.get("oauth_verifier").flatMap(_.headOption).map { verifier =>
      val tokenPair = checkToken(request).get
      TWITTER.retrieveAccessToken(tokenPair, verifier) match {
        case Right(t) => Redirect(routes.Application.index).withSession("token" -> t.token, "secret" -> t.secret)
        case Left(e) => throw e
      }
    }.getOrElse(
      TWITTER.retrieveRequestToken(routes.Application.authenticate().absoluteURL()) match {
        case Right(t) => Redirect(TWITTER.redirectUrl(t.token)).withSession("token" -> t.token, "secret" -> t.secret)
        case Left(e) => throw e
      })
  }
}

object TwitterOAuth {
  import play.api.Play.current

  def checkToken(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield {
      RequestToken(token, secret)
    }
  }

  lazy val conf = Play.configuration.getConfig("twitter").getOrElse(throw new RuntimeException("Missing config for Twitter"))
  lazy val KEY = ConsumerKey(conf.getString("clientId").get, conf.getString("clientSecret").get)
  lazy val TWITTER = OAuth(ServiceInfo(
    "https://api.twitter.com/oauth/request_token",
    "https://api.twitter.com/oauth/access_token",
    "https://api.twitter.com/oauth/authorize", KEY))

}

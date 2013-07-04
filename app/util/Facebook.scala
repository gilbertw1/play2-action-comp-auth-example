package util

import scala.util.matching.Regex
import play.api.libs.json.{Json,Format}
import play.api.mvc.{Session,Request,AnyContent}
import play.api.Play.current
import scala.concurrent.{ExecutionContext,Await,duration}

import duration._
import ExecutionContext.Implicits.global
import dispatch._
import Json._

object Facebook {

    implicit val fbUserFormat = format[FacebookUser]

    val appId = current.configuration.getString("facebook.playId").get
    val scope = current.configuration.getString("facebook.scope").get
    val appSecret = current.configuration.getString("facebook.playSecret").get
    val redirectUrl = current.configuration.getString("domain.urlBase").get + "/fbOAuthReturn"
    val sessionSecret = current.configuration.getString("facebook.state").get

    def generateOAuthUrl(): String = {
        s"https://www.facebook.com/dialog/oauth?client_id=${appId}&scope=${scope}&redirect_uri=${redirectUrl}&state=${sessionSecret}"
    }

    def isAuthenticated(session: Session): Boolean = {
        session.get("fbAccessToken") match {
            case Some(token) =>
                val expirationTS = (session.get("fbTokenExpires").getOrElse("0").toLong * 1000)
                System.currentTimeMillis < expirationTS
            case None =>
                false
        }
    }

    def serializeUser(fbUser: FacebookUser): String = {
        stringify(toJson(fbUser))
    }

    def deserializeUser(fbUserJson: String): FacebookUser = {
        parse(fbUserJson).as[FacebookUser]
    }

    def retrieveAccessTokenFromSession(session: Session): Option[String] = {
        if(isAuthenticated(session))
            session.get("fbAccessToken")
        else
            None
    }

    def retrieveFacebookUserFromSession(session: Session): Option[FacebookUser] = {
        if(isAuthenticated(session)) {
            session.get("fbUser") match {
                case Some(fbUserJson) => Some(deserializeUser(fbUserJson))
                case None => None
            }
        } else {
            None
        }
    }

    def retrieveAccessToken(state: String, code: String): Option[(String,Long)] = {
        val fbAccessRequest = url("https://graph.facebook.com/oauth/access_token") <<? Map (
            "client_id" -> appId, "redirect_uri" -> redirectUrl, "client_secret" -> appSecret, "code" -> code
        )
        
        val fbAccessResponse = Http(fbAccessRequest OK as.String)
        Await.result(fbAccessResponse.map(tryExtractTokenFromResponse), 5 seconds)
    }

    def tryExtractTokenFromResponse(resp: String): Option[(String,Long)] = {
        try {
            val tokenExtractor = new Regex("access_token=(.*?)&expires=(.*)")
            val tokenExtractor(token: String, expires: String) = resp
            val expireTimestamp = (expires.toLong * 1000) + System.currentTimeMillis
            Some((token, expireTimestamp))
        } catch {
            case e: MatchError => None
        }
    }

    def retrieveFacebookUser(accessToken: String): FacebookUser = {
        val fbDataRequest = url("https://graph.facebook.com/me") <<? Map("access_token" -> accessToken)
        val fbDataResponse = Http(fbDataRequest OK as.String)
        Await.result(fbDataResponse.map(deserializeUser), 5 seconds)
    }
}

case class FacebookUser (
    id: String,
    name: String,
    first_name: String,
    last_name: String,
    link: String,
    username: String,
    gender: String,
    email: String
) {
    val imgUrl = "https://graph.facebook.com/" + username + "/picture"
}
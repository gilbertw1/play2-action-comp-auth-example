package controllers

import play.api._
import play.api.mvc._
import models._
import util._

object Application extends BaseController {
  
    def index = UserAuthenticated { implicit request =>
        val memberCompanies = Company.findByMemberUserId(request.user.id.get) 
        val adminCompanies = Company.findByAdminUserId(request.user.id.get)
        Ok(views.html.index(memberCompanies, adminCompanies))
    }

    def fbOAuth = Action { request =>
        val oAuthUrl = Facebook.generateOAuthUrl()
        Redirect( oAuthUrl )
    }

    def fbOAuthReturn(state: String, code: String) = Action { request =>
        Facebook.retrieveAccessToken(state, code) match {
            case Some((token,expires)) =>
                val fbUserInfo = Facebook.retrieveFacebookUser(token)
                val newSession = request.session +
                    ("fbAccessToken" -> token) +
                    ("fbTokenExpires" -> expires.toString) +
                    ("fbUserInfo" -> Facebook.serializeUser(fbUserInfo)) -
                    "PostOAuthUrl"
                request.session.get("PostOAuthUrl") match {
                    case Some(url) => Redirect(url).withSession(newSession)
                    case None => Redirect(routes.Application.index()).withSession(newSession)
                }
            case None => 
                Redirect(routes.Application.fbOAuth())
        }
    } 

}
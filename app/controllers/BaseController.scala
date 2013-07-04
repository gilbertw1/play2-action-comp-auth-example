package controllers

import anorm.Id
import play.api._
import play.api.mvc._
import play.api.Play.current
import models._
import util._

trait BaseController extends Controller {

    def FacebookAuthenticated(f: FacebookAuthenticatedRequest => Result) = {
        Action { request =>
            val session = request.session
            Facebook.retrieveFacebookUserFromSession(session) match {
                case Some(fbUserInfo: FacebookUser) => 
                    f(FacebookAuthenticatedRequest(fbUserInfo, request))
                case None => 
                    val newSession = session + ("PostOAuthUrl" -> request.uri)
                    Redirect(routes.Application.fbOAuth()).withSession(newSession)
            }
        }
    }
  
    def UserAuthenticated(f: UserAuthenticatedRequest => Result) = {
        FacebookAuthenticated { request =>
            request.session.get("authenticatedUser") match {
                case Some(userJson) =>
                    f(UserAuthenticatedRequest(User.deserialize(userJson), request))
                case None =>
                    val fbId = request.fbUserInfo.id
                    val user = User.findByFacebookId(fbId).getOrElse(createUserFromFacebookUser(request.fbUserInfo))
                    f(UserAuthenticatedRequest(user, request)).asInstanceOf[PlainResult].withSession (
                        request.session + ("authenticatedUser" -> User.serialize(user))
                    )
            }
        }
    }

    def CompanyAdminAuthenticated(companyId: Long)(f: CompanyAdminAuthenticatedRequest => Result) = {
        UserAuthenticated { request =>
            val companyOpt = Company.findById(companyId)
            val user = request.user
            companyOpt match {
                case Some(company) if User.isCompanyAdmin(user, company.id.get) => f(CompanyAdminAuthenticatedRequest(user, company, request))
                case _ => Unauthorized
            }
        }
    }

    def CompanyAuthenticated(companyId: Long)(f: CompanyAuthenticatedRequest => Result) = {
        UserAuthenticated { request =>
            val companyOpt = Company.findById(companyId)
            val user = request.user
            companyOpt match {
                case Some(company) if User.isCompanyMember(user, company.id.get) => f(CompanyAuthenticatedRequest(user, company, request))
                case _ => Unauthorized
            }
        }
    }

    def SurveyAuthenticated(companyId: Long, surveyId: Long)(f: SurveyAuthenticatedRequest => Result) = {
        CompanyAuthenticated(companyId) { request =>
            val surveyOpt = Survey.findById(surveyId)
            val user = request.user
            surveyOpt match {
                case Some(survey) if User.canAccessSurvey(user, survey.id.get) => f(SurveyAuthenticatedRequest(user, request.company, survey, request))
                case _ => Unauthorized
            }
        }
    }

    def createUserFromFacebookUser(fbUser: FacebookUser): User = {
        val user = User.createUserObjectFromFacebookUser(fbUser)
        val id = User.create(user).get
        user.copy(id = Id(id))
    }

    def getSessionFromPlainResult(result: PlainResult): Session = {
        val session = Cookies(result.header.headers.get("Set-Cookie"))
            .get(Session.COOKIE_NAME).map(_.value).map(Session.decode)
            .getOrElse(Map.empty)
        Session.deserialize(session)
    }

}

case class SurveyAuthenticatedRequest (val user: User, val company: Company, val survey: Survey, request: Request[AnyContent]) extends WrappedRequest(request)
case class CompanyAuthenticatedRequest (val user: User, val company: Company, request: Request[AnyContent]) extends WrappedRequest(request)
case class CompanyAdminAuthenticatedRequest (val user: User, val company: Company, request: Request[AnyContent]) extends WrappedRequest(request)
case class UserAuthenticatedRequest (val user: User, request: Request[AnyContent]) extends WrappedRequest(request)
case class FacebookAuthenticatedRequest (val fbUserInfo: FacebookUser, request: Request[AnyContent]) extends WrappedRequest(request)
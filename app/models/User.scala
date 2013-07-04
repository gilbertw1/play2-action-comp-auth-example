package models

import anorm._
import anorm.SqlParser._
import util.FacebookUser
import play.api.db._
import play.api.Play.current
import play.api.libs.json.{Json,Format,JsNumber,JsValue,JsSuccess,JsNull,JsResult}
import Json._

case class User (
    id: Pk[Long],
    email: String,
    firstname: String,
    lastname: String,
    facebookId: String,
    facebookUsername: String
) {
    val fbImgUrl = "https://graph.facebook.com/" + facebookUsername + "/picture?type=large"
}

object User {

    def all(): List[User] = ???

    def findById(id: Long): Option[User] = ???

    def findByCompanyId(companyId: Long): List[User] = ???

    def findByFacebookId(id: String): Option[User] = ???

    def isCompanyMember(user: User, companyId: Long): Boolean = ???

    def isCompanyAdmin(user: User, companyId: Long): Boolean = ???

    def canAccessSurvey(user: User, surveyId: Long): Boolean = ???

    def joinCompany(user: User, companyId: Long) = ???

    def create(user: User): Option[Long] = ???

    def update(id: Long, user: User) = ???

    def serialize(user: User): String = ???

    def deserialize(userJson: String): User = ???

    def createUserObjectFromFacebookUser(fbUser: FacebookUser): User = ???
}
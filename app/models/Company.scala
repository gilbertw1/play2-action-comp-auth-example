package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Company (
    id: Pk[Long] = NotAssigned,
    name: String,
    description: String
)

object Company {

    def all(): List[Company] = ???

    def findById(id: Long): Option[Company] = ???

    def findByMemberUserId(userId: Long): List[Company] = ???

    def findByAdminUserId(userId: Long): List[Company] = ???

    def create(company: Company): Option[Long] = ???

    def update(id: Long, company: Company) = ???
    
}
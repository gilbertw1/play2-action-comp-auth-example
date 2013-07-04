package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current

case class Survey (
    id: Pk[Long] = NotAssigned,
    name: String,
    questions: List[String]
)

object Survey {

    def all(): List[Survey] = ???

    def findById(id: Long): Option[Survey] = ???

    def findByCompanyId(id: Long): List[Survey] = ???

    def create(survey: Survey): Option[Long] = ???

    def update(id: Long, survey: Survey) = ???
}
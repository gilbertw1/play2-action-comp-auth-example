package controllers

import play.api._
import play.api.mvc._
import models._

object Surveys extends BaseController {

    def list(companyId: Long) = CompanyAuthenticated(companyId) { implicit request =>
        Ok(views.html.survey.list(Survey.findByCompanyId(companyId)))
    }

    def create(companyId: Long) = CompanyAdminAuthenticated(companyId) { implicit request =>
        Ok(views.html.survey.create())
    }

    def view(companyId: Long, surveyId: Long) = SurveyAuthenticated(companyId, surveyId) { implicit request =>
        Ok(views.html.survey.view())
    }

    def update(companyId: Long, surveyId: Long) = CompanyAdminAuthenticated(companyId) { implicit request =>
        Survey.findById(surveyId).map(s => Ok(views.html.survey.update(s))).getOrElse(NotFound)
    }

    def fillOut(companyId: Long, surveyId: Long) = SurveyAuthenticated(companyId, surveyId) { implicit request =>
        Ok(views.html.survey.fillOut())
    }
}
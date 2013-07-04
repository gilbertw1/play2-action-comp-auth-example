package controllers

import play.api._
import play.api.mvc._
import models._

object Companies extends BaseController {

    def list = UserAuthenticated { implicit request =>
        Ok(views.html.company.list(Company.all()))
    }

    def create = UserAuthenticated { implicit request =>
        Ok(views.html.company.create())
    }

    def view(id: Long) = CompanyAuthenticated(id) { implicit request =>
        Ok(views.html.company.view())
    }

    def update(id: Long) = CompanyAdminAuthenticated(id) { implicit request =>
        Ok(views.html.company.update())
    }

    def admin(id: Long) = CompanyAdminAuthenticated(id) { implicit request =>
        Ok(views.html.company.admin())
    }
}
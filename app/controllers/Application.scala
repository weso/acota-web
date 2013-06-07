package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Demo"))
  }
  
  def recommend = Action {
    Ok(views.html.index("Demo"))
  }
  
  def feedback = Action {
    Ok(views.html.index("Demo"))
  }
}
package models

import play.api.libs.json.{Json, Format}

trait SocialProfile {
  def id: String

  def provider: String

  def email: String
}

case class FacebookProfile(id: String, email: String, first_name: String, gender: String, last_name: String, link: String, locale: String) extends SocialProfile {
  implicit val jsonFormat = Json.format[FacebookProfile]
  val provider = "facebook"
}

object FacebookProfile {
  implicit val jsonFormat = Json.format[FacebookProfile]
}
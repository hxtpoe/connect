package models

case class User(id: Option[Int],
                emails: Set[String],
                registeredAccount: Option[RegisteredAccount],
                socialAccounts: SocialAccounts)

case class RegisteredAccount(email: String, password: String)

case class SocialAccounts(
                           facebook: Option[FacebookProfile]
                           )

object User {

}
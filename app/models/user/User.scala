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
//  implicit val registeredAccountBsonFormat = Macros.handler[RegisteredAccount]
//  implicit val registeredAccountJsonFormat = Json.format[RegisteredAccount]
//
//  implicit val socialAccountsBsonFormat = Macros.handler[SocialAccounts]
//  implicit val socialAccountsJsonFormat = Json.format[SocialAccounts]
//
//  implicit val bsonFormat = Macros.handler[User]
//  implicit val jsonFormat = Json.format[User]
}
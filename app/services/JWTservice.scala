package services

import akka.actor.{Props, Actor}
import models.User
import com.nimbusds.jose._
import com.nimbusds.jose.crypto._
import com.nimbusds.jwt._

class JWTService extends Actor {
  import JWTService._

  def receive = {
    case Generate(u) => sender() ! u.id.toString // TODO: Real implementation!
  }
}

object JWTService {
  case class Generate(user: User)

  def generate(subject: String): String = {
    // Create HMAC signer
    val signer = new MACSigner("xx")

    // Prepare JWT with claims set
    val claimsSet = new JWTClaimsSet()
    claimsSet.setSubject(subject)
    val signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet)
    signedJWT.sign(signer)
    signedJWT.serialize()
  }

  def props = Props(new JWTService)
}
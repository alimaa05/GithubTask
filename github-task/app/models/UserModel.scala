package models

import play.api.libs.json.{Json, OFormat}

case class UserModel(login: String,
                     created_at: String,
                     location: Option[String],
                     followers: Int,
                     following: Int
                    )

//companion object - object with same name as case class - will have access to the companion object from that case class
object UserModel{
  implicit val formats: OFormat[UserModel] = Json.format[UserModel]}



//OFormat allows you to read and write to json
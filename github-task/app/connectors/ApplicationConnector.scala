package connectors

import cats.data.EitherT
import models.{APIError, UserModel}
import play.api.libs.json.OFormat
import play.api.libs.ws.WSClient
import play.api.libs.json.{JsError, JsSuccess, Json, OFormat}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApplicationConnector @Inject()(ws: WSClient){
  // connects to the API on the internet
  // wsClient is middle man - talking to the client (API) and connecting to the application


  def get[Response](url: String)(implicit reads : OFormat[Response], ec: ExecutionContext) : Future[Either[APIError, UserModel]] = {
    val requestResponse = ws.url(url).get()
    requestResponse.map {
      result => result.json.validate[UserModel]match {
        case JsSuccess(validatedModel,_) => Right(validatedModel)
        case JsError(error) => Left(APIError.BadAPIResponse(404, "bad error"))
      }
    }
  }
}

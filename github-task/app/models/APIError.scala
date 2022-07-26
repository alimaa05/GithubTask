package models

import play.api.http.Status

//cant access it unless accessing it through the class APIError
sealed abstract class APIError(
                                val httpResponseStatus: Int,
                                val reason: String
                              )

object APIError {

  final case class BadAPIResponse(upstreamStatus: Int, upstreamMessage: String)
    extends APIError(
      Status.INTERNAL_SERVER_ERROR, // a 500 error
      s"Bad response from upstream; got status: $upstreamStatus, and got reason $upstreamMessage"
    )

  final case class BdError(message: String, status: Int)
  extends APIError(500,"Bad error")

}
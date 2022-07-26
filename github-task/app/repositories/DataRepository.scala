package repositories
import com.google.common.base.Objects.equal
import com.google.inject.ImplementedBy
import com.mongodb.client.result.DeleteResult
import models.{APIError, UserModel}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{empty, equal}
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.model._
import play.api.libs.json.JsValue
//import play.api.mvc.Filters
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import views.html.helper.options

import javax.inject.{Inject, Singleton}
import scala.annotation.elidable.byName
import scala.concurrent.{ExecutionContext, Future}
import scala.util

//@ImplementedBy(classOf[DataRepository])
//trait MockRepositoryTrait {
//  def create(user: UserModel): Future[Either[APIError, UserModel]]
//  def read(id: String): Future[Either[APIError, UserModel]]
//  def readByName(name: String): Future[Either[APIError, UserModel]]
//  def update(id: String): Future[Either[APIError, UserModel]]
//  def updateField(id: String, field: String, updatedValue: String): Future[Option[UserModel]]
//  def delete(id: String): Future[Either[APIError, String]]
//}

// gets data from database
@Singleton
class DataRepository @Inject() (
                                 mongoComponent: MongoComponent
                               )(implicit ec: ExecutionContext) extends PlayMongoRepository[UserModel](
  collectionName = "users",
  mongoComponent = mongoComponent,
  domainFormat = UserModel.formats,
  indexes = Seq(IndexModel(
    Indexes.ascending("login"),IndexOptions().unique(true)
  )),
  replaceIndexes = false
) {

  def create(user: UserModel): Future[Either[APIError, UserModel]] =
    collection
      .insertOne(user)
      .toFutureOption()
      .map{
        case Some(value) if value.wasAcknowledged() => Right(user)
        case _ => Left(APIError.BadAPIResponse(400, "user could not be created"))
      }

  private def byLogin(login: String): Bson =
    Filters.and(
      Filters.equal("login", login)
    )

//  private def byName(name: String): Bson =
//    Filters.and(
//      Filters.equal("name", name)
//    )
//
//  def readByName(name: String): Future[Either[APIError, UserModel]] = {
//    collection.find(byName(name)).headOption() flatMap{
//      case Some(data) =>
//        Future(Right(data))
//      case _ =>
//        Future(Left(APIError.BadAPIResponse(400, "could not find user name")))
//    }
//  }

  def read(id: String): Future[Either[APIError, UserModel]] = {
    collection.find(byLogin(id)).headOption flatMap {
      case Some(data) =>
        Future(Right(data))
      case _ =>
        Future(Left(APIError.BadAPIResponse(400, "Could not read user")))
    }
  }

  def update(id: String): Future[Either[APIError, UserModel]] =
    collection.replaceOne(
      filter = byLogin(id),
      replacement = user,
      options = new ReplaceOptions().upsert(true)
    ).toFutureOption().map {
      case Some(value) if value.wasAcknowledged() => Right(user)
      case _ => Left(APIError.BadAPIResponse(400, "user could not be updated"))
    }

  def updateField(id: String, field: String, updatedValue: String): Future[Option[UserModel]] = {
    collection.findOneAndUpdate(
      equal("_id", id),
      set(field, updatedValue),
      options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
    ).toFutureOption()
  }

  def delete(id: String): Future[Either[APIError, String]] =
    collection.deleteOne(
      filter = byLogin(id)
    ).toFutureOption().map{
      case Some(value) if value.getDeletedCount == 1 => Right("user successfully deleted")
      case _ => Left(APIError.BadAPIResponse(400, "Could not delete user"))
    }

  def deleteAll(): Future[Unit] = collection.deleteMany(empty()).toFuture().map(_ => ())


  val user: UserModel = UserModel("abcd", "1234", Option["London"], 20, 4)
}



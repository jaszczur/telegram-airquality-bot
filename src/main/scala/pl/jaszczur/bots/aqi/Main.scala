package pl.jaszczur.bots.aqi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, ResponseEntity}
import akka.http.scaladsl.unmarshalling.{PredefinedFromEntityUnmarshallers, Unmarshaller}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.util.parsing.json.{JSON, JSONObject}
import scala.util.{Failure, Success}

object Main {

  def handle(entity: ResponseEntity): Unit = {
    entity.
  }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val um: Unmarshaller[HttpEntity, Option[JSONObject]] = {
      Unmarshaller.byteStringUnmarshaller.mapWithCharset { (data, charset) =>
        JSON.parseFull(data.toString()).map(_.asInstanceOf[JSONObject])
      }
    }

    val stationId = 117
    val apiUrl = s"http://powietrze.gios.gov.pl/pjp/current/getAQIDetails?id=$stationId&param=AQI"
    Source.single((HttpRequest(uri = apiUrl), 117))
      .via(Http().superPool())
      .runForeach {
        case (Success(r), stationId) => handle(r.entity )
        case (Failure(ex), stationId) => ex.printStackTrace()
      }


  }
}

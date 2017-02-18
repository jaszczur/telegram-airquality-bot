package pl.jaszczur.bots.aqi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val stationId = 117
    val apiUrl = s"http://powietrze.gios.gov.pl/pjp/current/getAQIDetails?id=$stationId&param=AQI"
     Source.single((HttpRequest(uri = apiUrl), 117))
      .via(Http().superPool())
      .map {
        case (Success(r), stationId) => println(r.entity)
        case (Failure(ex), stationId) => ex.printStackTrace()
      }
        .runForeach { case _ => system.shutdown()}


  }
}

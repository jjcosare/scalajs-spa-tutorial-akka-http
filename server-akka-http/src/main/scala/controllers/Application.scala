package controllers

import java.nio.ByteBuffer

import com.typesafe.config.ConfigFactory
import services.ApiService
import spatutorial.shared.Api

import twirl.Implicits._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext.Implicits.global

import boopickle.Default._
import services.ApiService
import spatutorial.shared.Api

object Router extends autowire.Server[ByteBuffer, Pickler, Pickler] {
  override def read[R: Pickler](p: ByteBuffer) = Unpickle[R].fromBytes(p)
  override def write[R: Pickler](r: R) = Pickle.intoBytes(r)
}

object Application {
  val apiService = new ApiService()

  def index =  pathSingleSlash {
    get {
      complete {
        views.html.index.render("SPA tutorial")
      }
    }
  }

  def assetsFonts = pathPrefix("assets" / "fonts" / Remaining) { file =>
    encodeResponse {
      getFromResource("public/lib/fontawesome/fonts/" + file)
    }
  }

  def assetsAny = pathPrefix("assets" / Remaining) { file =>
    encodeResponse {
      getFromResource("public/" + file)
    }
  }

  def autowireApi =  path("api" / Segments) { s =>
    post {
      entity(as[String]) { e =>
        println(s"Request path: ${e.mkString}")

        // get the request body as ByteBuffer
        val b = ByteBuffer.wrap(e.getBytes())

        // call Autowire route
        complete {
          Router.route[Api](apiService)(
            autowire.Core.Request(
              s,
              Unpickle[Map[String, ByteBuffer]].fromBytes(b)
            )
          ).map(buffer => {
            val data = Array.ofDim[Byte](buffer.remaining())
            buffer.get(data)
            data
          })
        }
      }
    }
  }

  def logging = pathPrefix("logging") {
    pathEndOrSingleSlash {
      post {
        entity(as[String]) { e =>
          println(s"CLIENT - $e")
            complete("")
//          complete(HttpEntity(ContentTypes.`application/json`, entity.dataBytes))
        }
      }
    }
  }

  def main(args: Array[String]) {
    implicit val system = ActorSystem("server-system")
    implicit val materializer = ActorMaterializer()

    val config = ConfigFactory.load()
    val interface = config.getString("http.interface")
    val port = config.getInt("http.port")
    val route = {index ~ assetsFonts ~ assetsAny ~ autowireApi ~ logging}

    Http().bindAndHandle(route, interface, port)

    println(s"Server online at http://$interface:$port")
  }
}

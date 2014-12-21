package scalawebsocket

import org.eclipse.jetty.server.handler.HandlerWrapper
import org.eclipse.jetty.websocket.WebSocketFactory
import org.eclipse.jetty.server.{ Handler, Server, Request}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eclipse.jetty.websocket
import scala.concurrent.Promise
import scala.util.Try
import org.eclipse.jetty.server.nio.SelectChannelConnector

case class Error(ex: Throwable)
case class Closed(closeCode: Int, message: String)
case class Message(message: String)
case class BinaryMessage(data: Array[Byte], offset: Int, length: Int)


class WebSocketServer(val port: Int) extends Server {
   private[scalawebsocket] val onConnectionPublisher = new EventPublisher[ConnectionWrapper]
   val onConnection = onConnectionPublisher.source

   private var _connector: SelectChannelConnector = _

   def setupAndStart() : Unit = setupAndStart(new DefaultWebSocketHandler(this))

   def setupAndStart(handler: Handler) : Unit = {
      _connector = new SelectChannelConnector()
      _connector.setPort(port)
      addConnector(_connector)
      setHandler(handler)
      start()
   }
}

class ConnectionWrapper(val connection: websocket.WebSocket.Connection) {

   private[scalawebsocket] val onMessagePublisher = new EventPublisher[Message]
   val onMessage = onMessagePublisher.source

   private[scalawebsocket] val onBinaryMessagePublisher = new EventPublisher[BinaryMessage]
   val onBinaryMessage = onBinaryMessagePublisher.source

   private val closedPromise = Promise[Closed]()
   val closed = closedPromise.future

   private val errorPromise = Promise[Error]()
   val error = errorPromise.future

   //connection.setMaxTextMessageSize(1000)

   val fail: PartialFunction[Throwable, Unit] = (value:Throwable) => value match {
      case ex =>
         connection.close()
         errorPromise.success(Error(ex))
   }

   def sendMessage(message: String): Unit = {
      Try(connection.sendMessage(message))
        .recover(fail)
   }
   def sendMessage(data: Array[Byte], offset: Int, length: Int): Unit = {
      Try(connection.sendMessage(data, offset, length))
        .recover(fail)
   }

   private[scalawebsocket] def onClose(closeCode: Int, message: String): Unit = {
      closedPromise.success(Closed(closeCode, message))
   }

   def close() = connection.close()
}

class ServerWebSocket(server: WebSocketServer) extends websocket.WebSocket
   with websocket.WebSocket.OnTextMessage
   with websocket.WebSocket.OnBinaryMessage {

   private var wrapper: ConnectionWrapper = null

   def onOpen(connection: websocket.WebSocket.Connection) {
      this.wrapper = new ConnectionWrapper(connection)
      server.onConnectionPublisher.publish(wrapper)
   }

   def onClose(i: Int, s: String) {
      wrapper.onClose(i, s)
   }

   def onMessage(s: String) {
      wrapper.onMessagePublisher.publish(Message(s))
   }

   def onMessage(data: Array[Byte], offset: Int, length: Int) {
      wrapper.onBinaryMessagePublisher.publish(BinaryMessage(data, offset, length))
   }
}

class DefaultWebSocketHandler(server: WebSocketServer) extends WebSocketHandler {
   def doWebSocketConnect(request: HttpServletRequest, protocol: String) = new ServerWebSocket(server)
}

abstract class WebSocketHandler extends HandlerWrapper with WebSocketFactory.Acceptor {

   def getWebSocketFactory: WebSocketFactory = {
      _webSocketFactory
   }

   override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) {
      if (_webSocketFactory.acceptWebSocket(request, response) || response.isCommitted) return
      super.handle(target, baseRequest, request, response)
   }

   def checkOrigin(request: HttpServletRequest, origin: String): Boolean = {
      true
   }

   private final val _webSocketFactory: WebSocketFactory = new WebSocketFactory(this, 32 * 1024)
}
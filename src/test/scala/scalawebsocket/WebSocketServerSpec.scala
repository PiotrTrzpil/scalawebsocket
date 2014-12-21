/*
* Copyright 2012 Jeanfrancois Arcand
* Copyright 2013 Piotr Buda
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License. You may obtain a copy of
* the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations under
* the License.
*
* Changes are made to the test code to use different API of the WebSocket.
*/

package scalawebsocket

import java.io.IOException
import javax.servlet.http.HttpServletRequest
import java.util.concurrent.{TimeUnit, CountDownLatch}
import com.ning.http.client.AsyncHttpClient
import org.scalatest.matchers.{MatchResult, BeMatcher}
import com.typesafe.scalalogging.StrictLogging


class WebSocketServerSpec extends BaseTest with StrictLogging {


   it should "call all onConnection handlers" in {

      val server = new WebSocketServer(findFreePort)
      server.setupAndStart()
      server.onConnection += (connection => {
         connection.onMessage += (m => {
            connection.sendMessage(m.message)
         })
      })
      server.onConnection += (connection => {
         connection.onMessage += (m => {
            connection.sendMessage(m.message)
         })
      })

      val latch = new CountDownLatch(2)
      val socket = WebSocket()
        .onTextMessage(m => latch.countDown())
        .open("ws://127.0.0.1:" + server.port)

      socket.sendText("should return 2 times")

      waitForHandlersToExecute(latch)
   }

   it should "allow multiple connections" in {

      val server = new WebSocketServer(findFreePort)
      server.setupAndStart()
      server.onConnection += (connection => {
         connection.onMessage += (m => {
            connection.sendMessage(m.message)
         })
      })

      val latch = new CountDownLatch(2)
      val socket = WebSocket()
        .onTextMessage(m => latch.countDown())
        .open("ws://127.0.0.1:" + server.port)

      val socket2 = WebSocket()
        .onTextMessage(m => latch.countDown())
        .open("ws://127.0.0.1:" + server.port)

      socket.sendText("should return 2 times")
      socket2.sendText("should return 2 times")

      waitForHandlersToExecute(latch)
   }


   it should "call onClose when closed by client" in {

      import scala.concurrent.ExecutionContext.Implicits.global
      val server = new WebSocketServer(findFreePort)
      val latch = new CountDownLatch(2)

      server.onConnection += (connection => {
         connection.closed.onSuccess {
            case Closed(code, message) =>
               latch.countDown()
         }
      })
      server.setupAndStart()

      val socket = WebSocket()
        .onTextMessage(m => latch.countDown())
        .open("ws://127.0.0.1:" + server.port)
      socket.onClose(w => {
         latch.countDown()
      })

      socket.close()

      waitForHandlersToExecute(latch)
   }

   it should "call onClose on both sides when closed by the server" in {

      import scala.concurrent.ExecutionContext.Implicits.global
      val server = new WebSocketServer(findFreePort)
      val latch = new CountDownLatch(2)

      server.onConnection += (connection => {

         connection.onMessage += (m => {
            connection.close()
         })
         connection.closed.onSuccess {
            case Closed(code, message) =>
               latch.countDown()
         }
      })
      server.setupAndStart()

      val socket = WebSocket()
        .onTextMessage(m => latch.countDown())
        .open("ws://127.0.0.1:" + server.port)

      socket.sendText("close cause")
      socket.onClose(w => {
         latch.countDown()
      })
      waitForHandlersToExecute(latch)
   }

}

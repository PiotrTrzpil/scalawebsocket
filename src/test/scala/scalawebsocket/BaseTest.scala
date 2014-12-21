/*
* Copyright 2012 Jeanfrancois Arcand
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
*/

package scalawebsocket

import org.scalatest.{FlatSpecLike, Matchers, BeforeAndAfterAll}
import java.net.ServerSocket
import com.typesafe.scalalogging.StrictLogging
import java.util.concurrent.{TimeUnit, CountDownLatch}
import org.scalatest.matchers.{MatchResult, BeMatcher}

abstract class BaseTest extends FlatSpecLike with BeforeAndAfterAll with Matchers with StrictLogging {
  protected var port1: Int = 0
  var server: WebSocketServer = null

  override def beforeAll() {
    setUpGlobal()
  }

  override def afterAll() {
    tearDownGlobal()
  }

  def setUpGlobal() {
    port1 = findFreePort
    server = new WebSocketServer(port1)
    server.onConnection += onConnection
    server.setupAndStart()
    logger.info("Local HTTP server started successfully")
  }

  def tearDownGlobal() {
     server.stop()
  }

   def waitForHandlersToExecute(latch: CountDownLatch) {
      class CountDownLatchMatcher extends BeMatcher[Boolean] {
         def apply(left: Boolean): MatchResult = {
            MatchResult(left, "Not all handlers were executed", "All handlers were executed")
         }
      }
      val completed = new CountDownLatchMatcher
      latch.await(10, TimeUnit.SECONDS) should be(completed)
   }


  protected def findFreePort: Int = {
    var socket: ServerSocket = null
    try {
      socket = new ServerSocket(0)
      socket.getLocalPort
    } finally {
      if (socket != null) {
        socket.close()
      }
    }
  }

  protected def getTargetUrl: String = {
    "ws://127.0.0.1:" + port1
  }
   def onConnection(connection: ConnectionWrapper) = {}
}

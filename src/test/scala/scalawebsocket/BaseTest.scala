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

import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.scalatest.{FlatSpecLike, Matchers, BeforeAndAfterAll}
import org.eclipse.jetty.server.Server
import java.net.ServerSocket
import com.typesafe.scalalogging.StrictLogging

abstract class BaseTest extends Server with FlatSpecLike with BeforeAndAfterAll with Matchers with StrictLogging {
  protected var port1: Int = 0
  private var _connector: SelectChannelConnector = null

  override def beforeAll() {
    setUpGlobal()
  }

  override def afterAll() {
    tearDownGlobal()
  }

  def setUpGlobal() {
    port1 = findFreePort
    _connector = new SelectChannelConnector
    _connector.setPort(port1)
    addConnector(_connector)
    val _wsHandler: WebSocketHandler = getWebSocketHandler
    setHandler(_wsHandler)
    start()
    logger.info("Local HTTP server started successfully")
  }

  def tearDownGlobal() {
    stop()
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

  def getWebSocketHandler: WebSocketHandler
}

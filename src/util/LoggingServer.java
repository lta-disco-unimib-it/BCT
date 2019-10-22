/*******************************************************************************
 *    Copyright 2019 Fabrizio Pastore, Leonardo Mariani, and other authors indicated in the source code below.
 *   
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class LoggingServer {
  private ServerSocket serverSocket = null;

  

  public LoggingServer(int port) {
    try {
      serverSocket = new ServerSocket(port);
      
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void acceptMessage() {
	  while ( true ) {
		  try {
			  Socket socket = serverSocket.accept();
			  InputStream inStream = socket.getInputStream();
			  BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
			  String str = null;
			  while ((str = reader.readLine()) != null) {
				  System.out.println(str);
			  }
			  socket.close();
		  } catch (IOException ioe) {
			  ioe.printStackTrace();
		  }
	  }
  }

  public static void main(String args[]) {
    LoggingServer server = new LoggingServer(2020);
    server.acceptMessage();
  }
}


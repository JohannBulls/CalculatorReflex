package co.edu.escuelaing;

import java.net.*;
import java.io.*;

public class CalReflexFacade {
   public static void main(String[] args) throws IOException, URISyntaxException {
      ServerSocket serverSocket = null;
      try {
          serverSocket = new ServerSocket(36000);
      } catch (IOException e) {
          System.err.println("Could not listen on port: 36000.");
          System.exit(1);
      }
      boolean running = true;
      while (running) {
          Socket clientSocket = null;
          try {
              System.out.println("Listo para recibir ...");
              clientSocket = serverSocket.accept();
          } catch (IOException e) {
              System.err.println("Accept failed.");
              System.exit(1);
          }
          PrintWriter out = new PrintWriter(
                  clientSocket.getOutputStream(), true);
          BufferedReader in = new BufferedReader(
                  new InputStreamReader(clientSocket.getInputStream()));
          String inputLine;
          String firstLine = "";
          boolean isFirstLine = true;

          while ((inputLine = in.readLine()) != null) {
              System.out.println("Recibí: " + inputLine);
              if (isFirstLine) {
                  firstLine = inputLine;
                  isFirstLine = false;
              }
              if (!in.ready()) {
                  break;
              }
          }

          URI reqURL = getRequestURI(firstLine);
          String outputLine;
          if (reqURL.getPath().startsWith("/computar")) {
              outputLine = HttpConnection.getResponse("/computar?" + reqURL.getQuery());
          } else {
              outputLine = getHttpClient();
          }

          out.println(outputLine);
          out.close();
          in.close();
          clientSocket.close();
      }
      serverSocket.close();
  }

  public static String getHttpClient() {
      return "HTTP/1.1 200 OK\r\n" +
              "Content-Type: text/html\r\n" +
              "\r\n" +
              "<!DOCTYPE html>\n" +
              "<html>\n" +
              "    <head>\n" +
              "        <title>Form Example</title>\n" +
              "        <meta charset=\"UTF-8\">\n" +
              "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
              "    </head>\n" +
              "    <body>\n" +
              "        <h1>Method Not Found</h1>\n" +
              "    </body>\n" +
              "</html>";
  }

  public static URI getRequestURI(String firstLine) throws URISyntaxException {
      String rurl = firstLine.split(" ")[1];
      return new URI(rurl);
  }
}

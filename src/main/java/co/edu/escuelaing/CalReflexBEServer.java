package co.edu.escuelaing;

import java.net.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CalReflexBEServer {
   public static void main(String[] args) throws IOException {
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
         String command = "";
         while ((inputLine = in.readLine()) != null) {
            System.out.println("Recibí: " + inputLine);
            if (!in.ready()) {
               command = inputLine.split("=")[1]; 
               break;
            }
         }

         String result = "";
         try {
            result = computeMathCommand(command); 
         } catch (Exception e) {
            result = "Error al ejecutar el comando: " + e.getMessage();
         }

         String outputLine = "HTTP/1.1 200 OK\r\n"
               + "Content-Type: text/html\r\n"
               + "\r\n"
               + "<!DOCTYPE html>\n"
               + "<html>\n"
               + "<head>\n"
               + "<meta charset=\"UTF-8\">\n"
               + "<title>Resultado</title>\n"
               + "</head>\n"
               + "<body>\n"
               + "<h1>Resultado: " + result + "</h1>\n"
               + "</body>\n"
               + "</html>\n";
         out.println(outputLine);
         out.close();
         in.close();
         clientSocket.close();
      }
      serverSocket.close();
   }

   public static String computeMathCommand(String command)
         throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
      Class<?> c = Math.class;
      Method method;
      double result = 0;

      switch (command) {
         case "abs":
            method = c.getDeclaredMethod("abs", double.class);
            result = (double) method.invoke(null, -2.0);
            break;
         case "sqrt":
            method = c.getDeclaredMethod("sqrt", double.class);
            result = (double) method.invoke(null, 16.0);
            break;
         default:
            return "Comando no soportado";
      }
      return String.valueOf(result);
   }
}

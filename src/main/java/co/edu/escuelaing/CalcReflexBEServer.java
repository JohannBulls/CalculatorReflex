package co.edu.escuelaing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class CalcReflexBEServer {
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
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;
            boolean isFirstLine = true;
            String firstLine = "";
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

            if (firstLine == null || firstLine.isEmpty()) {
                out.println("HTTP/1.1 400 Bad Request\r\n\r\nInvalid request");
                continue;
            }

            URI requestURI = getRequestURI(firstLine);

            if (requestURI.getPath().startsWith("/compreflex")) {
                // Decodificar el comando
                String query = requestURI.getQuery();
                String comandoCodificado = query.split("=")[1];
                String comandoDecodificado = URLDecoder.decode(comandoCodificado, StandardCharsets.UTF_8.name());

                String result = "";
                try {
                    result = computeMaxCommand(comandoDecodificado);
                } catch (Exception e) {
                    result = "Error: " + e.getMessage();
                }
                outputLine = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: application/json\r\n"
                        + "\r\n"
                        + "{\"resultado\":\"" + result + "\"}";
            } else {
                outputLine = getDefaultResponse();
            }

            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    private static URI getRequestURI(String firstLine) throws URISyntaxException {
        String ruri = firstLine.split(" ")[1];
        return new URI(ruri);
    }

    public static String computeMaxCommand(String command) throws Exception {
        String methodName = command.substring(0, command.indexOf("("));
        System.out.println("Método: " + methodName);
        int openParenIndex = command.indexOf("(");
        int closeParenIndex = command.indexOf(")");

        if (openParenIndex == -1 || closeParenIndex == -1 || openParenIndex >= closeParenIndex) {
            throw new IllegalArgumentException("Comando inválido: falta o malformación de los paréntesis");
        }

        String paramsString = command.substring(openParenIndex + 1, closeParenIndex);
        System.out.println("Parámetros: " + paramsString);

        if (!methodName.equals("max")) {
            throw new IllegalArgumentException("Método no soportado: " + methodName);
        }

        String[] paramValues = paramsString.split(",");
        if (paramValues.length != 2) {
            throw new IllegalArgumentException("El método max requiere exactamente dos parámetros");
        }

        Object[] params = new Object[2];
        Class<?>[] paramTypes = new Class<?>[2];

        for (int i = 0; i < paramValues.length; i++) {
            String param = paramValues[i].trim();
            try {
                params[i] = Double.parseDouble(param);
                paramTypes[i] = double.class;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Parámetro inválido: " + param);
            }
        }

        Class<?> c = Math.class;
        Method method = c.getMethod(methodName, paramTypes);
        Object result = method.invoke(null, params);
        return result.toString();
    }

    public static String getDefaultResponse() {
        String htmlCode = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<title>Calculadora</title>\n"
                + "</head>\n"
                + "<body>\n"
                + "<h1>Calculadora</h1>\n"
                + "<form>\n"
                + "<label for=\"comando\">Comando:</label><br>\n"
                + "<input type=\"text\" id=\"comando\" name=\"comando\" placeholder=\"pow(2,3)\"><br><br>\n"
                + "<input type=\"button\" value=\"Calcular\" onclick=\"loadGetMsg()\">\n"
                + "</form>\n"
                + "<div id=\"getrespmsg\"></div>\n"
                + "<script>\n"
                + "function loadGetMsg() {\n"
                + "let comando = document.getElementById(\"comando\").value;\n"
                + "const xhttp = new XMLHttpRequest();\n"
                + "xhttp.onload = function() {\n"
                + "document.getElementById(\"getrespmsg\").innerHTML = this.responseText;\n"
                + "}\n"
                + "xhttp.open(\"GET\", \"/compreflex?comando=\" + encodeURIComponent(comando));\n"
                + "xhttp.send();\n"
                + "}\n"
                + "</script>\n"
                + "</body>\n"
                + "</html>";
        return htmlCode;
    }
}
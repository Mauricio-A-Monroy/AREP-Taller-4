package edu.escuelaing.arep.microspring.http;

import edu.escuelaing.arep.microspring.annotation.RequestParam;

import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.lang.reflect.Parameter;

public class HttpServer {
    //private static Map<String,BiFunction<HttpRequest, HttpResponse, String>> servicios = new HashMap();
    private static Map<String,ServiceHandler> servicios = new HashMap<>();
    private static String staticFilePath = "src/main/resources/static";;

    public static void start() throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;
        int idCounter = 0;
        Map<Integer, String> dataStore = new HashMap<>();

        while (running) {

            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            OutputStream outputStream = clientSocket.getOutputStream();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            BufferedOutputStream dataOut = new BufferedOutputStream(clientSocket.getOutputStream());

            String inputLine;
            HttpResponse response;

            boolean isFirstLine = true;
            String filePath = "";
            String HTTPRequest = "";
            while ((inputLine = in.readLine()) != null) {
                if (isFirstLine){
                    HTTPRequest = inputLine.split(" ")[0];
                    filePath = inputLine.split(" ")[1];
                    isFirstLine = false;
                }

                if (!in.ready()) {
                    break;
                }
            }

            System.out.println("Request: " + HTTPRequest);
            System.out.println("FilePath: " + filePath);

            URI resourceURI = new URI(filePath);
            System.out.println("URI: " + resourceURI);

            if (resourceURI.getPath().startsWith("/app/")) {
                if(resourceURI.getPath().endsWith("rest-service")){
                    handleRestRequest(HTTPRequest, filePath, resourceURI,out, dataStore, idCounter);
                    if (HTTPRequest.equals("POST")) {
                        idCounter += 1;
                    }
                    outputStream.close();
                }
                else {
                    HttpRequest req = new HttpRequest(resourceURI.getPath(), resourceURI.getQuery());
                    response = processRequest(req);
                    outputStream.write(response.buildResponse().getBytes());
                    outputStream.flush();
                }
            } else {
                getDefaultResponse(filePath, out, dataOut);
            }

            outputStream.close();
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }
    public static void get(String route, Object object, Method method ){
        System.out.println("route: " + route);
        servicios.put("/app" + route, new ServiceHandler(object, method));
    }


    private static HttpResponse processRequest(HttpRequest req) {
        HttpResponse response = new HttpResponse(200, "OK");
        System.out.println("Query: " + req.getQuery());
        System.out.println("Path: " + req.getPath());

        ServiceHandler handler = servicios.get(req.getPath());

        if (handler != null) {
            try {
                String responseBody = invokeControllerMethod(handler.getInstance(), handler.getMethod(), req);
                response.setBody("{\"response\":\"" + responseBody + "\"}", "application/json");
            } catch (Exception e) {
                e.printStackTrace();
                response = new HttpResponse(500, "Internal Server Error");
                response.setBody("{\"error\":\"Internal Server Error\"}", "application/json");
            }
        } else {
            response = new HttpResponse(404, "Not Found");
            response.setBody("<h1>404 Not Found</h1>", "text/html");
        }

        return response;
    }


    private static String invokeControllerMethod(Object instance, Method method, HttpRequest req) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = param.getAnnotation(RequestParam.class);
                String paramName = requestParam.value();
                String paramValue = req.getQueryParam(paramName);

                if (paramValue == null || paramValue.isEmpty()) {
                    paramValue = requestParam.defaultValue(); // Usa el valor por defecto
                }
                args[i] = paramValue;
            }
        }

        return (String) method.invoke(instance, args);
    }

    public static void staticFiles(String path){
        staticFilePath = path;
    }

    public static void getDefaultResponse(String filePath, PrintWriter out, BufferedOutputStream dataOut) throws IOException {
        if (filePath.equals("/")){
            filePath = "/index.html";
        }

        File file = new File(staticFilePath + filePath);

        if (file.exists() && !file.isDirectory()) {
            String contentType = getContentType(filePath);

            // Leer el archivo
            byte[] fileData = readFileData(file);

            // Enviar respuesta HTTP
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + contentType);
            out.println("Content-Length: " + fileData.length);
            out.println();
            out.flush();

            dataOut.write(fileData, 0, fileData.length);
            dataOut.flush();
        } else {
            // Archivo no encontrado
            String errorMessage = "HTTP/1.1 404 Not Found\r\n"
                    + "Content-Type: text/html\r\n"
                    + "\r\n"
                    + "<h1>404 File Not Found</h1>";
            out.println(errorMessage);
        }
    }

    private static String getContentType(String filePath) {
        if (filePath.endsWith(".html") || filePath.endsWith(".htm")) {
            return "text/html";
        } else if (filePath.endsWith(".css")) {
            return "text/css";
        } else if (filePath.endsWith(".js")) {
            return "application/javascript";
        } else if (filePath.endsWith(".png")) {
            return "image/png";
        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else {
            return "application/octet-stream"; // Tipo genérico
        }
    }

    private static byte[] readFileData(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileData = new byte[(int) file.length()];
        fileInputStream.read(fileData);
        fileInputStream.close();
        return fileData;
    }

    private static void handleRestRequest(String method, String filePath, URI resourceURI, PrintWriter out, Map<Integer, String> dataStore, int idCounter) {
        String response = "";
        String idParam = filePath.replace("/app/rest-service/", "").trim();

        if (method.equals("GET")) {
            // Convertir los valores a una lista de strings con comillas
            StringBuilder namesJson = new StringBuilder("[ \n ");
            for (Integer key : dataStore.keySet()) { // Iteramos sobre las claves
                namesJson.append("\n {\"id\":")
                        .append(key)  // Usamos directamente la clave
                        .append(", \"name\": \"")
                        .append(dataStore.get(key)) // Obtenemos el valor con la clave
                        .append("\" },");
            }

            // Eliminar la última coma y cerrar el arreglo
            if (namesJson.length() > 1) {
                namesJson.deleteCharAt(namesJson.length() - 1);
            }
            namesJson.append("]");

            for (Integer i = 0; i < dataStore.size(); i++){
                System.out.println("id: " + i + ", value: " + dataStore.get(i));
            }

            // Crear la respuesta JSON
            response = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: application/json\r\n"
                    + "\r\n"
                    + "{ \"names\": " + namesJson.toString() + " }";
        } else if (method.equals("POST")) {
            String requestQuery = resourceURI.getQuery();
            String newName = requestQuery.replace("name=", "").trim();
            dataStore.put(idCounter, newName);

            for (Integer i = 0; i < dataStore.size(); i++){
                System.out.println("id: " + i + ", value: " + dataStore.get(i));
            }

            response = "HTTP/1.1 201 Created\r\n"
                    + "Content-Type: application/json\r\n"
                    + "\r\n"
                    + "{\"id\":" + idCounter + ", \"name\":\"" + newName + "\"}";
        } else if (method.equals("PUT")) {
            String requestQuery = resourceURI.getQuery();
            String [] params = requestQuery.split("&");
            String newName = "";
            Integer id = -1;

            for (String param : params) {
                String[] splitParam = param.split("=");
                if (splitParam[0].equals("id")){
                    id = Integer.parseInt(splitParam[1]);
                }
                if (splitParam[0].equals("newName")){
                    newName = splitParam[1];
                }
            }

            dataStore.put(id, newName);

            response = "HTTP/1.1 200 Ok\r\n"
                    + "Content-Type: application/json\r\n"
                    + "\r\n"
                    + "{\"id\":" + id + ", \"name\":\"" + newName + "\"}";
        }else if (method.equals("DELETE")) {
            String requestQuery = resourceURI.getQuery();
            String stringId = requestQuery.replace("id=", "").trim();
            Integer id = Integer.parseInt(stringId);

            dataStore.remove(id);

            response = "HTTP/1.1 200 Ok\r\n"
                    + "Content-Type: application/json\r\n"
                    + "\r\n";
        } else {
            response = "HTTP/1.1 405 Method Not Allowed\r\n"
                    + "Content-Type: application/json\r\n"
                    + "\r\n"
                    + "{\"error\":\"Method not allowed\"}";
        }

        out.println(response);
    }
}


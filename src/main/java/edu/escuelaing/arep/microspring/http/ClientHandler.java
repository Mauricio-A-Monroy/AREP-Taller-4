package edu.escuelaing.arep.microspring.http;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Map<Integer, String> dataStore;
    private int idCounter;

    public ClientHandler(Socket clientSocket, Map<Integer, String> dataStore, int idCounter) {
        this.clientSocket = clientSocket;
        this.dataStore = dataStore;
        this.idCounter = idCounter;
    }

    @Override
    public void run() {
        try {
            OutputStream outputStream = clientSocket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedOutputStream dataOut = new BufferedOutputStream(clientSocket.getOutputStream());

            String inputLine;
            boolean isFirstLine = true;
            String filePath = "";
            String HTTPRequest = "";

            while ((inputLine = in.readLine()) != null) {
                if (isFirstLine) {
                    HTTPRequest = inputLine.split(" ")[0];
                    filePath = inputLine.split(" ")[1];
                    isFirstLine = false;
                }
                if (!in.ready()) {
                    break;
                }
            }

            URI resourceURI = new URI(filePath);

            if (resourceURI.getPath().startsWith("/app/")) {
                if (resourceURI.getPath().endsWith("rest-service")) {
                    HttpServer.handleRestRequest(HTTPRequest, filePath, resourceURI, out, dataStore, idCounter);
                    if (HTTPRequest.equals("POST")) {
                        idCounter += 1;
                    }
                    outputStream.close();
                } else {
                    HttpRequest req = new HttpRequest(resourceURI.getPath(), resourceURI.getQuery());
                    HttpResponse response = HttpServer.processRequest(req);
                    outputStream.write(response.buildResponse().getBytes());
                    outputStream.flush();
                }
            } else {
                HttpServer.getDefaultResponse(filePath, out, dataOut);
            }

            outputStream.close();
            out.close();
            in.close();
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


package edu.escuelaing.arep.app;

import static org.junit.jupiter.api.Assertions.*;

import edu.escuelaing.arep.microspring.MicroServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private static Thread serverThread;

    @BeforeAll
    static void startServer() {
        serverThread = new Thread(() -> {
            try {
                MicroServer.main(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        // Esperar unos segundos para que el servidor inicie
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }

    @AfterAll
    static void stopServer() {
        serverThread.interrupt();
    }

    @Test
    void testGetIndexHtml() throws Exception {
        URL url = new URL("http://localhost:35000/");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String contentType = conn.getHeaderField("Content-Type");
        assertEquals("text/html", contentType);
    }

    @Test
    void testGetNotFound() throws Exception {
        URL url = new URL("http://localhost:35000/noexiste.html");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(404, conn.getResponseCode());
    }

    @Test
    void testRestServiceGet() throws Exception {
        URL url = new URL("http://localhost:35000/app/rest-service");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String responseBody = readResponse(conn);
        assertTrue(responseBody.contains("\"names\""));
    }

    @Test
    void testRestServicePost() throws Exception {
        URL url = new URL("http://localhost:35000/app/rest-service?name=TestUser");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        assertEquals(201, conn.getResponseCode());
        String responseBody = readResponse(conn);
        assertTrue(responseBody.contains("\"name\":\"TestUser\""));
    }

    @Test
    void testLambdaFunctionGetHelloWorld() throws Exception {
        URL url = new URL("http://localhost:35000/app/greeting");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String responseBody = readResponse(conn);
        assertTrue(responseBody.contains("\"response\":\"Hello World\""));
    }

    @Test
    void testLambdaFunctionGetHello() throws Exception {
        String name = "Mauricio";
        URL url = new URL("http://localhost:35000/app/greeting?name=" + name);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String responseBody = readResponse(conn);
        assertTrue(responseBody.contains("\"response\":\"Hello " + name +"\""));
    }

    @Test
    void testLambdaFunctionGetPi() throws Exception {
        URL url = new URL("http://localhost:35000/app/pi");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String responseBody = readResponse(conn);
        assertTrue(responseBody.contains("\"response\":\"" + Math.PI + "\""));
    }

    @Test
    void testLambdaFunctionGetEuler() throws Exception {
        URL url = new URL("http://localhost:35000/app/e");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String responseBody = readResponse(conn);
        assertTrue(responseBody.contains("\"response\":\"" + Math.E + "\""));
    }

    @Test
    void testRequestCounter() throws Exception {
        final int requestCount = 10;
        CountDownLatch latch = new CountDownLatch(requestCount);

        for (int i = 0; i < requestCount; i++) {
            new Thread(() -> {
                try {
                    sendRequest();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        int finalCount = getRequestCount();
        assertEquals(requestCount + 1, finalCount);
    }

    private void sendRequest() throws Exception {
        URL url = new URL("http://localhost:35000/app/count");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.getResponseCode();
        conn.disconnect();
    }

    private int getRequestCount() throws Exception {
        URL url = new URL("http://localhost:35000/app/count");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        assertEquals(200, conn.getResponseCode());
        String responseBody = readResponse(conn);
        conn.disconnect();

        return Integer.parseInt(responseBody.replaceAll("[^0-9]", ""));
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}


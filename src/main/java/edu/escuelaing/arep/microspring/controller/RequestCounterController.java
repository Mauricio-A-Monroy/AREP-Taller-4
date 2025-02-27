package edu.escuelaing.arep.microspring.controller;


import edu.escuelaing.arep.microspring.annotation.GetMapping;
import edu.escuelaing.arep.microspring.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class RequestCounterController {

    private static final AtomicInteger requestCount = new AtomicInteger(0);

    @GetMapping("/count")
    public String getRequestCount() {
        int count = requestCount.incrementAndGet(); // Incrementa y obtiene el nuevo valor
        return "{ \"requests\": " + count + " }";
    }
}
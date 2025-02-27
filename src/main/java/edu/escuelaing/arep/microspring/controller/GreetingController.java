package edu.escuelaing.arep.microspring.controller;

import edu.escuelaing.arep.microspring.annotation.GetMapping;
import edu.escuelaing.arep.microspring.annotation.RequestParam;
import edu.escuelaing.arep.microspring.annotation.RestController;

@RestController
public class GreetingController {
    @GetMapping("/greeting")
    public static String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hello " + name;
    }
}
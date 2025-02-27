package edu.escuelaing.arep.microspring.controller;

import edu.escuelaing.arep.microspring.annotation.GetMapping;
import edu.escuelaing.arep.microspring.annotation.RequestParam;
import edu.escuelaing.arep.microspring.annotation.RestController;

@RestController
public class MathController {

    @GetMapping("/e")
    public static String e(String notUse){
        return Double.toString(Math.E);
    }

    @GetMapping("/pi")
    public static String pi(String name) {
        return Double.toString(Math.PI);
    }
}

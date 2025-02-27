package edu.escuelaing.arep.microspring;

import edu.escuelaing.arep.microspring.annotation.GetMapping;
import edu.escuelaing.arep.microspring.annotation.RestController;
import edu.escuelaing.arep.microspring.http.HttpServer;

import static edu.escuelaing.arep.microspring.http.HttpServer.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicroServer {

    public static Map<String, Method> services = new HashMap<>();

    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, URISyntaxException, NoSuchMethodException, InstantiationException, IOException {
        List<String> classNames = getClassesInPackage("edu.escuelaing.arep.microspring.controller");
        loadComponents(classNames.toArray(new String[0]));

        staticFiles("src/main/resources/static");
        start();

    }

    private static void loadComponents(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        for(String arg: args){
            Class c = Class.forName(arg);
            Object instance = c.getDeclaredConstructor().newInstance();
            if (!c.isAnnotationPresent(RestController.class)) {
                System.exit(0);
            }
            for (Method mtd : c.getDeclaredMethods()) {
                if (mtd.isAnnotationPresent(GetMapping.class)) {
                    GetMapping getMapping = mtd.getAnnotation(GetMapping.class);
                    String route = getMapping.value();
                    // Registrar la función en HttpServer
                    get(route, instance, mtd);
                }
            }
        }
    }

    public static List<String> getClassesInPackage(String packageName) throws URISyntaxException {
        List<String> classNames = new ArrayList<>();
        String path = packageName.replace('.', '/');

        ClassLoader classLoader = MicroServer.class.getClassLoader();
        URL packageURL = classLoader.getResource(path);

        if (packageURL != null) {
            File directory = new File(new URI(packageURL.toString()));
            if (directory.exists()) {
                for (String fileName : directory.list()) {
                    if (fileName.endsWith(".class")) {
                        String className = packageName + "." + fileName.substring(0, fileName.length() - 6);
                        classNames.add(className);
                    }
                }
            }
        }

        return classNames;
    }

}

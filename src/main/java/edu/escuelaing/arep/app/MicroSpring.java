package edu.escuelaing.arep.app;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static java.lang.System.out;

public class MicroSpring {

    public static void main(String[] args) {
        out.println("Reflexion");
        Class c = String.class;
        Class igualQueC = "Hola".getClass(); // Hacen lo mismo
        printMembers(c.getDeclaredConstructors(), "Constructors");
        out.println("\n");
        printMembers(c.getConstructors(), "Constructors");
    }

    public static void printMembers(Member[] mbrs, String s) {
        out.format("%s:%n", s);
        for (Member mbr : mbrs) {
            if (mbr instanceof Field)
                out.format("  %s%n", ((Field) mbr).toGenericString());
            else if (mbr instanceof Constructor)
                out.format("  %s%n", ((Constructor) mbr).toGenericString());
            else if (mbr instanceof Method)
                out.format("  %s%n", ((Method) mbr).toGenericString());
        }
        if (mbrs.length == 0)
            out.format("  -- No %s --%n", s);
        out.format("%n");

    }
}

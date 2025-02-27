package edu.escuelaing.arep.app;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;

public class InvokePrintMembers {
    public static void main(String... args) throws InvocationTargetException, IllegalAccessException {
        try {
            Class<?> c = Class.forName(args[0]);
            Class[] argTypes = new Class[]{Member[].class, String.class};
            Method m = c.getDeclaredMethod("printMembers", argTypes);

            Class otraClase = LinkedList.class;

            String[] mainArgs = Arrays.copyOfRange(args, 1, args.length);
            System.out.format("invoking %s.printMembers()%n", otraClase.getName());
            m.invoke(null, otraClase.getDeclaredFields(),  "Campos");
            // production code should handle these exceptions more gracefully
        } catch (ClassNotFoundException x) {
            x.printStackTrace();
        } catch (NoSuchMethodException x) {
            x.printStackTrace();
        }
    }
}

package edu.escuelaing.arep.microspring.http;

import java.lang.reflect.Method;

class ServiceHandler {
    private final Object instance;
    private final Method method;

    public ServiceHandler(Object instance, Method method) {
        this.instance = instance;
        this.method = method;
    }

    public Object getInstance() {
        return instance;
    }

    public Method getMethod() {
        return method;
    }
}


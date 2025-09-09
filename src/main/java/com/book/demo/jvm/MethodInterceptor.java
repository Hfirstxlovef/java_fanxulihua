package com.book.demo.jvm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class MethodInterceptor implements InvocationHandler {
    
    private final List<MethodCallFrame> methodCalls;
    
    public MethodInterceptor(List<MethodCallFrame> methodCalls) {
        this.methodCalls = methodCalls;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        long startTime = System.nanoTime();
        
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        
        MethodCallFrame callFrame = new MethodCallFrame(
            className, 
            methodName, 
            formatArgs(args), 
            startTime
        );
        
        try {
            Object result = method.invoke(proxy, args);
            callFrame.setResult(result);
            callFrame.setEndTime(System.nanoTime());
            methodCalls.add(callFrame);
            return result;
        } catch (Exception e) {
            callFrame.setException(e);
            callFrame.setEndTime(System.nanoTime());
            methodCalls.add(callFrame);
            throw e;
        }
    }
    
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "()";
        }
        
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            if (args[i] == null) {
                sb.append("null");
            } else {
                sb.append(args[i].getClass().getSimpleName());
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
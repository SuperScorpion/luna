package com.jy.luna.protocol;

public class RpcRequest {

    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] parameterClassTypes;
    private Object[] parameters;


    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterClassTypes() {
        return parameterClassTypes;
    }

    public void setParameterClassTypes(Class<?>[] parameterClassTypes) {
        this.parameterClassTypes = parameterClassTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}

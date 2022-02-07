package top.dreamlike.loomspirng.proxy;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.springframework.web.context.request.async.DeferredResult;

import static top.dreamlike.loomspirng.util.Util.isAsyncType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoomInterceptor {
    private Map<String, List<Method>> proxyMapping;
    private Object target;

    public LoomInterceptor(Object target) {
        this.target = target;
        proxyMapping = new HashMap<>();
        for (Method method : target.getClass().getMethods()) {
            proxyMapping.computeIfAbsent(method.getName(),name -> new ArrayList<>())
                    .add(method);
        }
    }


    @RuntimeType
    public Object loomEnhance(@Origin Method method, @AllArguments Object[] args) throws InvocationTargetException, IllegalAccessException {
        Method actualMethod = getActualMethod(method);
        if (!isAsyncType(method.getReturnType())) {
            return actualMethod.invoke(target,args);
        }
        DeferredResult<Object> result = new DeferredResult<>();

        Thread.ofVirtual().allowSetThreadLocals(true)
                .start(()->{
                    try {
                        result.setResult(actualMethod.invoke(target, args));
                    } catch (Throwable e) {
                        result.setErrorResult(e);
                    }
                });
        return result;
    }

    private Method getActualMethod(Method proxy){
        List<Method> list = proxyMapping.get(proxy.getName());
        if (list.size() == 1) return list.get(0);
        else {
            Class<?>[] proxyParameters = proxy.getParameterTypes();
            for (Method method : list) {
                if (equalParamTypes(proxyParameters, method.getParameterTypes())){
                    return method;
                }
            }
            return null;
        }
    }
    private boolean equalParamTypes(Class<?>[] params1, Class<?>[] params2) {
        if (params1.length == params2.length) {
            for (int i = 0; i < params1.length; i++) {
                if (params1[i] != params2[i])
                    return false;
            }
            return true;
        }
        return false;
    }
}

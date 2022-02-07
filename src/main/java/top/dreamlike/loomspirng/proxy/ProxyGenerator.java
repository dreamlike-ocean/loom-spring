package top.dreamlike.loomspirng.proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static top.dreamlike.loomspirng.util.Util.isAsyncType;

public class ProxyGenerator {

    public static Object generate(Object controller){
        return generate(controller, Thread.currentThread().getContextClassLoader(), null);
    }

    public static Object generate(Object controller, ClassLoader classLoader){
        return generate(controller, classLoader, null);
    }


    public static Object generate(Object controller, ClassLoader classLoader, File target) {
        Class<?> controllerClass = controller.getClass();
        LoomInterceptor loomInterceptor = new LoomInterceptor(controller);
        DynamicType.Builder<Object> builder = new ByteBuddy()
                .subclass(Object.class)
                .name(controllerClass.getName()+"$$loomEnhance");
        builder = copyClassAnnotation(controllerClass,builder);
        for (Method method : filterMethod(controllerClass)) {
            builder = copyMethod(builder, method, loomInterceptor);
        }

        DynamicType.Unloaded<Object> unloaded = builder.make();
        if (target != null) {
            try {
                unloaded.saveIn(target);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            return unloaded.load(classLoader)
                    .getLoaded().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static DynamicType.Builder<Object> copyClassAnnotation(Class<?> controller, DynamicType.Builder<Object> builder){
        return builder.annotateType(controller.getAnnotations());
    }

    private static List<Method> filterMethod(Class<?> controller){
        return Arrays.stream(controller.getDeclaredMethods())
                .filter(m ->  Modifier.isPublic(m.getModifiers()) && AnnotationUtils.findAnnotation(m,RequestMapping.class)!=null)
                .collect(Collectors.toList());
    }

    private static DynamicType.Builder<Object> copyMethod(DynamicType.Builder<Object> builder,Method origin,LoomInterceptor loomInterceptor){
        TypeDefinition returnType;
        if (isAsyncType(origin.getReturnType())){
            returnType = TypeDefinition.Sort.describe(origin.getGenericReturnType());
        }else {
            returnType = TypeDescription.Generic.Builder.parameterizedType(DeferredResult.class, origin.getGenericReturnType()).build();
        }
        DynamicType.Builder.MethodDefinition.ParameterDefinition<Object> paramBuilder = builder.defineMethod(origin.getName(), returnType, origin.getModifiers());
        for (Parameter parameter : origin.getParameters()) {
            paramBuilder = paramBuilder.withParameter(parameter.getParameterizedType(), parameter.getName())
                    .annotateParameter(parameter.getAnnotations());
        }
        return paramBuilder.intercept(MethodDelegation.to(loomInterceptor))
                .annotateMethod(origin.getAnnotations());
    }



}

package top.dreamlike.loomspirng.util;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletionStage;

public class Util {
    public static boolean isAsyncType(Class type){
        return type.isAssignableFrom(DeferredResult.class) || type.isAssignableFrom(CompletionStage.class) || type.isAssignableFrom(ListenableFuture.class);
    }
}

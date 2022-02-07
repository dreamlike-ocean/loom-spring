package top.dreamlike.loomspirng.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RestController;
import top.dreamlike.loomspirng.proxy.ProxyGenerator;

import java.io.File;

public class LoomEnhanceBeanPostProcessor implements BeanPostProcessor, Ordered {

    private File file;

    public LoomEnhanceBeanPostProcessor(@Value("${loom_enhance.save_path:#{null}}")String value) {
        file = value == null ? null : new File(value);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (AnnotationUtils.findAnnotation(bean.getClass(), RestController.class)==null) return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
        return ProxyGenerator.generate(bean, Thread.currentThread().getContextClassLoader(), file);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

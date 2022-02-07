package top.dreamlike.loomspirng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import top.dreamlike.loomspirng.spring.EnableLoomEnhance;

import javax.servlet.http.HttpServletRequest;

@SpringBootApplication
@EnableLoomEnhance
public class LoomSpirngApplication {



    public static void main(String[] args) throws NoSuchMethodException {
        SpringApplication.run(LoomSpirngApplication.class, args);
//        SpringApplication application = new SpringApplication(LoomSpirngApplication.class);
//        application
//                .setWebApplicationType(WebApplicationType.NONE);
//        ConfigurableApplicationContext context = application.run(args);
//        var value = context.getBean(LoomSpirngApplication.class).value;
//        System.out.println(value == null);

    }


}

@RestController
class Controller{
    @GetMapping("/hello/{path}")
    public String hello(@PathVariable String path){
        return "hello spring and loom,Im run in "+Thread.currentThread();
    }
    @GetMapping("/async")
    public DeferredResult<String> asyncHello(HttpServletRequest httpServletRequest){
        DeferredResult<String> result = new DeferredResult<>();
        Thread.ofVirtual().allowSetThreadLocals(true).start(()->{
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result.setResult("hello spring and loom,Im run in "+Thread.currentThread());
        });
        return result;
    }

    public void a(){}
}

#### 使用参考：

1，java运行环境必须高于openjdk-17-loom+7-342

2，直接在在任意bean上加上@EnableLoomEnhance注解即可

```java
@SpringBootApplication
@EnableLoomEnhance
public class LoomSpirngApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoomSpirngApplication.class, args);
    }
}
```

会在运行时自动增强被@RestController注解的类

被增强的方法拥有如下特点：

1，方法体运行在虚拟线程中，可以使用任何的阻塞client

2，自动开启servlet3.1异步

3，无感迁移，继承One thread per request模型，ThreadLocal仍然可用

4，底层载体线程为fork-join pool

#### 限制

1，增强的方法的条件：

- 返回值不为SpringMVC支持的异步类型——`org.springframework.web.context.request.async.DeferredResult`,`org.springframework.util.concurrent.ListenableFuture`,`java.util.concurrent.CompletionStage`,若原方法返回值为以上的则保持原有，不增强
- 必须是`public`方法
- 其必须拥有`@RequestMapping`及其衍生注解（比如`@GetMapping`）

2，由于使用运行时字节码生成，所以无法修改原有字节码，故需要类加载器加载新生成的字节码，默认类加载器是线程上下文加载器

#### 杂项：

你可以在application.properties中

```properties
loom_enhance.save_path = proxy
```

指定一个文件地址，若指定则向这个地址输出增强后的方法
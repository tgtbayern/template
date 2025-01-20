package com.example.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.AllowedListDeserializingMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration  // 表示该类是一个配置类，Spring会在启动时加载它
public class RabbitConfiguration {
    /**
     * 给@Bean取名字的主要作用是在Spring容器中唯一标识该bean，从而可以通过名字进行精确注入或查找。主要体现在以下几个方面：
     *
     * 避免冲突，区分相同类型的bean
     * 如果应用中存在多个相同类型的bean（例如多个Queue），不指定名字可能导致NoUniqueBeanDefinitionException异常。通过给@Bean取名，可以明确指定注入哪个bean
     *
     * @Configuration
     * public class RabbitConfiguration {
     *
     *     @Bean("emailQueue")
     *     public Queue emailQueue() {
     *         return QueueBuilder.durable().build();
     *     }
     *
     *     @Bean("orderQueue")
     *     public Queue orderQueue() {
     *         return QueueBuilder.durable().build();
     *     }
     * }
     * 在注入时可以指定需要的队列：
     *
     * @Autowired
     * @Qualifier("emailQueue")
     * private Queue queue;  // 注入emailQueue
     * @return
     */
    @Bean("emailQueue")  // 定义一个名为 "emailQueue" 的队列bean，供Spring管理
    public Queue emailQueue() {
        return QueueBuilder
                .durable("emailQueue")  // 将队列声明为持久化队列，重启RabbitMQ后队列不会丢失，
                // 且将这个queue命名，这个queue的名字应该和@see MailQueueListener中@RabbitListener注释相符
                .build();   // 构建队列实例并返回
    }

    /**
     * 在你使用 RabbitMQ 发送和接收消息时，Jackson2JsonMessageConverter 会被应用。
     * 比如，当你用 RabbitTemplate 发送消息时，如果配置了该 Jackson2JsonMessageConverter，
     * RabbitTemplate 会自动使用它来进行消息的序列化（Java 对象 -> JSON）。
     * 同样，当你通过 @RabbitListener 或其他方式接收 RabbitMQ 消息时，
     * Spring 会自动使用 Jackson2JsonMessageConverter 来反序列化消息内容（JSON -> Java 对象）。
     * 如果不配置，消息队列会无法解析java传递给它的内容，因为消息队列不知道传给他的消息的格式
     * @return
     */

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}


package com.example.listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 整个邮件业务的逻辑是，当前端在/ask-code接口处请求时，我们会读取请求的内容，然后调用AccountServiceImpl类中的registerEmailVerifyCode方法
 * 这个方法会首先对ip加锁，保证同一时间同一个ip只能请求一个，然后在redis中检查是否有这个ip，如果有说明这个ip在设定时间内已经请求过这个接口了，所以请求频繁
 * 如果通过了上述检测，生成一个随机6位数验证码，将验证码，需要发送到的邮箱，邮件的类型 存储到一个map中，上述数据分别是同一个map中的一个元素，整个map是一个大元素
 * 然后向指定的mq队列中发送这个map，在MailQueueListener中，我们监听了这个队列，一旦队列中有消息，就会从队列中读取数据，解析出上面存储的内容，调用spring mail，发送邮件
 * 在AccountServiceImpl类中的registerEmailVerifyCode方法将内容发送到消息队列后，这个方法还会将生成的验证码暂存入redis
 * 当用户输入验证码的时候，就可以直接从redis中比对
 */

@Component  // 将MailQueueListener标记为Spring组件，交由Spring容器管理
@RabbitListener(queues = "emailQueue")  // 监听名为"emailQueue"的RabbitMQ队列，接收邮件发送任务
public class MailQueueListener {

    @Resource  // 注入JavaMailSender，用于发送邮件
    JavaMailSender sender;

    @Value("${spring.mail.username}")  // 从配置文件中读取邮件发送方的用户名
    String username;

    @RabbitHandler  // 指定该方法处理从队列中接收的消息
    public void sendMailMessage(Map<String, Object> data) {
        // 从消息中提取email、code和type参数，一个map中的三个键值对才对应一个元素
        String email = (String) data.get("email");
        Integer code = (Integer) data.get("code");
        String type = (String) data.get("type");

        // 根据邮件类型创建不同的邮件内容
        SimpleMailMessage message = switch (type) {
            case "register" -> createMessage(
                    "欢迎注册我们的网站",  // 邮件标题
                    "您的邮件注册验证码为：" + code +
                            "，有效时间3分钟，为了保障您的安全，请勿向他人泄露验证码信息。",  // 邮件内容
                    email  // 收件人邮箱
            );
            case "reset" -> createMessage(
                    "你的密码重置邮件",  // 邮件标题
                    "您好，您正在进行重置密码操作，验证码：" + code +
                            "，有效时间3分钟，如非本人操作，请无视。",  // 邮件内容
                    email  // 收件人邮箱
            );
            default -> null;  // 如果type不匹配，返回null
        };

        // 如果消息为空（未匹配类型），直接返回，不发送邮件
        if(message == null) {
            // todo debug
            // System.out.println("message==null");
            return;
        }
        // 发送邮件
        sender.send(message);
    }

    // 创建邮件消息的辅助方法
    private SimpleMailMessage createMessage(String title, String content, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);  // 设置邮件标题
        message.setText(content);  // 设置邮件正文内容
        message.setTo(email);  // 设置收件人
        message.setFrom(username);  // 设置发件人（配置中读取的邮箱地址）
        System.out.println("creat message");
        return message;
    }
}

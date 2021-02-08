package com.example.monitoragent.config;


import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    //交换机名称
    public static final String ITEM_TOPIC_EXCHANGE_CPU = "item_topic_cpu";
    public static final String ITEM_TOPIC_EXCHANGE_MEM = "item_topic_mem";
    public static final String ITEM_TOPIC_EXCHANGE_NET = "item_topic_net";
    public static final String ITEM_TOPIC_EXCHANGE_DISK = "item_topic_disk";
    //队列名称
    public static final String ITEM_QUEUE_CPU = "item_queue_cpu";
    public static final String ITEM_QUEUE_MEM = "item_queue_mem";
    public static final String ITEM_QUEUE_DISK = "item_queue_disk";
    public static final String ITEM_QUEUE_NET = "item_queue_net";

    //声明交换机
    @Bean("itemTopicExchangeCpu")
    public Exchange topicExchangeCpu(){
        return ExchangeBuilder.topicExchange(ITEM_TOPIC_EXCHANGE_CPU).durable(true).build();
    }
    @Bean("itemTopicExchangeMem")
    public Exchange topicExchangeMem(){
        return ExchangeBuilder.topicExchange(ITEM_TOPIC_EXCHANGE_MEM).durable(true).build();
    }
    @Bean("itemTopicExchangeNet")
    public Exchange topicExchangeNet(){
        return ExchangeBuilder.topicExchange(ITEM_TOPIC_EXCHANGE_NET).durable(true).build();
    }
    @Bean("itemTopicExchangeDisk")
    public Exchange topicExchangeDisk(){
        return ExchangeBuilder.topicExchange(ITEM_TOPIC_EXCHANGE_DISK).durable(true).build();
    }



    //声明队列
    @Bean("itemQueueCPU")
    public Queue itemQueueCpu(){
        return QueueBuilder.durable(ITEM_QUEUE_CPU).build();
    }

    @Bean("itemQueueMem")
    public Queue itemQueueMem(){
        return QueueBuilder.durable(ITEM_QUEUE_MEM).build();
    }

    @Bean("itemQueueNet")
    public Queue itemQueueNet(){
        return QueueBuilder.durable(ITEM_QUEUE_NET).build();
    }
    @Bean("itemQueueDisk")
    public Queue itemQueueDisk(){
        return QueueBuilder.durable(ITEM_QUEUE_DISK).build();
    }


    //绑定队列和交换机
    @Bean
    public Binding itemQueueExchange(@Qualifier("itemQueueCPU") Queue queue,
                                     @Qualifier("itemTopicExchangeCpu") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("cpu_metrics").noargs();
    }
    @Bean
    public Binding itemQueueExchangeMem(@Qualifier("itemQueueMem") Queue queue,
                                     @Qualifier("itemTopicExchangeMem") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("mem_metrics").noargs();
    }
    @Bean
    public Binding itemQueueExchangeNet(@Qualifier("itemQueueNet") Queue queue,
                                        @Qualifier("itemTopicExchangeNet") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("net_metrics").noargs();
    }

    @Bean
    public Binding itemQueueExchangeDisk(@Qualifier("itemQueueDisk") Queue queue,
                                        @Qualifier("itemTopicExchangeDisk") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("disk_metrics").noargs();
    }

}
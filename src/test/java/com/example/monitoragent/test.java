package com.example.monitoragent;

import com.example.monitoragent.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class test {

    @Autowired
    RabbitMQConfig rabbitMQConfig;
    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Test
    public void t(){

        rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_TOPIC_EXCHANGE_CPU ,"中断数" ,122);
        
    }

}

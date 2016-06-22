package com.abin.lee.message.service.impl;

import com.abin.lee.message.service.KafkaProducerService;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: abin
 * Date: 16-6-22
 * Time: 下午10:57
 * To change this template use File | Settings | File Templates.
 */
@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {
    @Resource
    MessageChannel inputToKafka;

    @Override
    public void produceMessage(String messageId) {
        inputToKafka.send(MessageBuilder.withPayload("Message-" + messageId).setHeader("messageKey", messageId).setHeader("topic", "test").build());
    }
}

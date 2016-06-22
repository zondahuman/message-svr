package com.abin.lee.message.service.impl;

import com.abin.lee.message.service.KafkaConsumerService;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: abin
 * Date: 16-6-22
 * Time: 下午10:58
 * To change this template use File | Settings | File Templates.
 */
@Service(value = "kafkaConsumerService")
public class KafkaConsumerServiceImpl implements KafkaConsumerService {
    @Resource
    QueueChannel inputFromKafka;

    @Override
    public void consumeMessage() {
        Message msg;
        while((msg = inputFromKafka.receive()) != null) {
            HashMap map = (HashMap)msg.getPayload();
            Set<Map.Entry> set = map.entrySet();
            for (Map.Entry entry : set) {
                String topic = (String)entry.getKey();
                System.out.println("Topic:" + topic);
                ConcurrentHashMap<Integer,List<byte[]>> messages = (ConcurrentHashMap<Integer,List<byte[]>>)entry.getValue();
                Collection<List<byte[]>> values = messages.values();
                for (Iterator<List<byte[]>> iterator = values.iterator(); iterator.hasNext();) {
                    List<byte[]> list = iterator.next();
                    for (byte[] object : list) {
                        String message = new String(object);
                        System.out.println("\tMessage: " + message);
                    }

                }

            }

        }
    }
}

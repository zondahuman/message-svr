package com.abin.lee.message.main.controller;

import com.abin.lee.message.service.KafkaProducerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: abin
 * Date: 16-6-22
 * Time: 下午9:08
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/kafka")
public class KafkaController {

    @Resource
    KafkaProducerService kafkaProducerService;

    @ResponseBody
    @RequestMapping(value = "/producer")
    public String producer(String messageId) throws Exception {
        String result = "SUCCESS";
        this.kafkaProducerService.produceMessage(messageId);
        return result;
    }









}

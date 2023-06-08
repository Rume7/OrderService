package io.turntabl.orderservice.pubSub;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;

    public RedisPublisher(RedisTemplate<String, Object> redisTemplate,ChannelTopic channelTopic) {
        this.redisTemplate = redisTemplate;
        this.channelTopic = channelTopic;
    }

    public void publishMessage(Object message) {
        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
    }




}
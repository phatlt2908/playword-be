package choichu.vn.playword.configuration;

import choichu.vn.playword.dto.multiwordlink.RoomDTO;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {
  @Bean
  public RedisTemplate<String, RoomDTO> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, RoomDTO> template = new RedisTemplate<>();

    template.setConnectionFactory(connectionFactory);
    // Add some specific configuration here. Key serializers, etc.
    return template;
  }
}

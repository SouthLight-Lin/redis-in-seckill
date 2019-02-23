package config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class JedisFactory {
    JedisPool jedisPool;

    public JedisFactory() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(100);
        poolConfig.setMinIdle(1);
        poolConfig.setMaxTotal(1000);
        poolConfig.setMaxWaitMillis(5000);
        this.jedisPool = new JedisPool(poolConfig, "127.0.0.1");
    }

    public Jedis getJedis() {
        Jedis jedis = jedisPool().getResource();
        return jedis;
    }

    public JedisPool jedisPool() {
        if (this.jedisPool != null) {
            return this.jedisPool;
        }
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(100);
        poolConfig.setMinIdle(1);
        poolConfig.setMaxTotal(1000);
        JedisPool jedisPool = new JedisPool(poolConfig, "127.0.0.1");
        this.jedisPool = jedisPool;
        return this.jedisPool;
    }
}

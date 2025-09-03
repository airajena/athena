package com.webserver.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisManager {
    private JedisPool jedisPool;
    private boolean connected = false;

    public RedisManager(String host, int port) {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(10);
            config.setMaxIdle(5);
            config.setMinIdle(1);
            config.setTestOnBorrow(true);

            jedisPool = new JedisPool(config, host, port, 2000);

            // Test connection
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
                connected = true;
                System.out.println("🔴 Redis connected: " + host + ":" + port);
            }
        } catch (Exception e) {
            System.out.println("⚠️  Redis not available: " + e.getMessage());
            System.out.println("💡 Continuing without Redis caching...");
        }
    }

    public void set(String key, String value, int ttlSeconds) {
        if (!connected) return;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, ttlSeconds, value);
        } catch (Exception e) {
            System.err.println("❌ Redis SET error: " + e.getMessage());
        }
    }

    public String get(String key) {
        if (!connected) return null;

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        } catch (Exception e) {
            System.err.println("❌ Redis GET error: " + e.getMessage());
            return null;
        }
    }

    public void delete(String key) {
        if (!connected) return;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        } catch (Exception e) {
            System.err.println("❌ Redis DELETE error: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
            System.out.println("🔴 Redis connection closed");
        }
    }
}

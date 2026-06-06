package com.spms.common.redis;

import com.spms.common.exception.AppException;
import com.spms.common.exception.CommonError;
import com.spms.common.result.Json;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHelper {
    private static final String GLOBAL_LOCK_KEY = "GLOBAL_LOCK";
    private static final long LOCK_RETRY_STEP_MILLIS = 50;

    private final StringRedisTemplate redisTemplate;
    private final RedisProperties redisProperties;

    public void runWithLock(Runnable task) {
        runWithLock(GLOBAL_LOCK_KEY, task);
    }

    public void runWithLock(String key, Runnable task) {
        Lock lock = lock(key);
        try {
            task.run();
        } finally {
            releaseLock(lock);
        }
    }

    public Lock lock(String key) {
        return lock(key, redisProperties.getLockTimeoutMillis());
    }

    public Lock lock(String key, long timeoutMillis) {
        String value = UUID.randomUUID().toString();
        long waitedMillis = 0;
        while (waitedMillis <= timeoutMillis) {
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(getKey(key), value, timeoutMillis, TimeUnit.MILLISECONDS);
            if (Boolean.TRUE.equals(locked)) {
                return new Lock().setKey(key).setValue(value);
            }
            sleep();
            waitedMillis += LOCK_RETRY_STEP_MILLIS;
        }
        throw new AppException(CommonError.REDIS_ERROR, "系统繁忙，请稍后重试");
    }

    public void releaseLock(Lock lock) {
        if (lock == null || lock.getKey() == null || lock.getValue() == null) {
            throw new AppException(CommonError.REDIS_ERROR, "释放锁失败");
        }
        String key = getKey(lock.getKey());
        String value = redisTemplate.opsForValue().get(key);
        if (Objects.equals(value, lock.getValue())) {
            redisTemplate.delete(key);
        }
    }

    public Long increment(String key) {
        return increment(key, 1);
    }

    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(getKey(key), delta);
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(getKey(key));
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(getKey(key));
    }

    public <T> T get(String key, Class<T> type) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return Json.parse(value, type);
    }

    public void set(String key, Object value) {
        set(key, value, redisProperties.getCacheExpireSecond());
    }

    public void set(String key, Object value, long seconds) {
        String redisKey = getKey(key);
        String redisValue = value instanceof String stringValue ? stringValue : Json.toString(value);
        if (seconds > 0) {
            redisTemplate.opsForValue().set(redisKey, redisValue, seconds, TimeUnit.SECONDS);
            return;
        }
        redisTemplate.opsForValue().set(redisKey, redisValue);
    }

    public void delete(String key) {
        redisTemplate.delete(getKey(key));
    }

    public Boolean expire(String key, long seconds) {
        return redisTemplate.expire(getKey(key), seconds, TimeUnit.SECONDS);
    }

    public Long getExpireSecond(String key) {
        return redisTemplate.getExpire(getKey(key), TimeUnit.SECONDS);
    }

    public void publish(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }

    private String getKey(String key) {
        return redisProperties.getPrefix() + key;
    }

    private void sleep() {
        try {
            Thread.sleep(LOCK_RETRY_STEP_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AppException(CommonError.REDIS_ERROR, "获取锁被中断");
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Lock {
        private String key;
        private String value;
    }
}

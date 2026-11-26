package br.com.user.config;

import com.br.azevedo.infra.cache.CacheProperties;
import com.br.azevedo.infra.cache.MultipleCacheConfig;
import com.br.azevedo.infra.cache.redis.RedisCacheFactory;
import com.br.azevedo.infra.cache.redis.RedisProperties;
import com.br.azevedo.infra.cache.redis.repository.impl.CacheRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import({
        MultipleCacheConfig.class,
        RedisCacheFactory.class,
        CacheProperties.class,
        RedisProperties.class,
        CacheRepositoryImpl.class
})
class RedisConfig {
}

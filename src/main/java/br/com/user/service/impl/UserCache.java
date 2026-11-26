package br.com.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
class UserCache {

    static final String BY_ID = "user_services_cliente_por_id";
    static final String BY_EMAIL = "user_services_cliente_por_email";
    static final String LOGIN = "user_services_cliente_login_email";

    private final CacheManager cacheManager;

    @TransactionalEventListener
    public void invalidate(Changed user) {
        evict(BY_ID, user.id());
        evict(BY_EMAIL, user.email());
        evict(LOGIN, user.email());
    }

    private void evict(String name, Object key) {
        try {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.evictIfPresent(key);
            }
        } catch (RuntimeException exception) {
            log.warn("Falha ao invalidar cache {}: {}", name, exception.getClass().getSimpleName());
        }
    }

    record Changed(Long id, String email) {}
}

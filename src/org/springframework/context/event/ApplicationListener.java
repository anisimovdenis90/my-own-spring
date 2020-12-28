package org.springframework.context.event;

public interface ApplicationListener<T> {
    void onApplicationEvent(T event);
}

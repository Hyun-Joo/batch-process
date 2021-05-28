package com.batch.demo.part3;

import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DuplicateValidationProcessor<T> implements ItemProcessor<T, T> {

    private final Map<String, Object> keyPool = new ConcurrentHashMap<>();
    private final Function<T, String> keyExtractor;
    private final boolean allowDuplicate;

    public DuplicateValidationProcessor(Function<T, String> keyExtractor, boolean allowDuplicate) {
        this.keyExtractor = keyExtractor;
        this.allowDuplicate = allowDuplicate;
    }

    @Override
    public T process(T item) throws Exception {
        if(allowDuplicate){ // 중복을 허용하면
            return item;
        }

        String key = keyExtractor.apply(item);  // 해당 아이템에서 키를 추출
        if(keyPool.containsKey(key)){   // 중복된 key라는 의미
            return null;
        }
        // 중복이 아니면...
        keyPool.put(key, key);
        return item;
    }
}

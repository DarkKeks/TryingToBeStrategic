package raic.strategy;

import java.util.HashMap;
import java.util.function.Supplier;

public class DefaultHashMap<K,V> extends HashMap<K,V> {
    protected Supplier<V> defaultValue;
    public DefaultHashMap(Supplier<V> supplier) {
        this.defaultValue = supplier;
    }
    
    @Override
    public V get(Object k) {
        if(!containsKey(k))
        	put((K)k, defaultValue.get());
      	return super.get(k);
    }
}


public class DefaultHashMap<K,V> extends HashMap<K,V> {
    protected Supplier<V> defaultValue;
    public DefaultHashMap() {
        this.defaultValue = V::new;
    }
    
    @Override
    public V get(Object k) {
        if(!qcontainsKey(k))
        	put(k, defaultValue.get());
      	return get(k);
    }
}
package com.brimworks.databind;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ListIterator;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.brimworks.databind.impl.PrimitiveAdapter;
import com.brimworks.databind.impl.TypeVisitorImpl;

/**
 * DataBind instances must be created with a {@link DataBind.Builder}. The
 * DataBind object is immutable and thus thread-safe, but the builder is not
 * thread safe.
 * 
 */
public class DataBind implements TypeRegistry {
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<Type, TypeFactory<?>> factories = new HashMap<>();
    private ArrayList<TypeFactoryRegistry> factoryRegistries = new ArrayList<>();
    private Map<Type, VisitType<?>> visits = new HashMap<>();
    private ArrayList<VisitTypeRegistry> visitRegistries = new ArrayList<>();
    private BooleanFactory booleanFactory = null;
    private LongFactory longFactory = null;
    private IntFactory intFactory = null;
    private ShortFactory shortFactory = null;
    private ByteFactory byteFactory = null;
    private CharFactory charFactory = null;
    private DoubleFactory doubleFactory = null;
    private FloatFactory floatFactory = null;

    /**
     * Obtain a builder instance for the same state as the this DataBind.
     * 
     * @return a new builder.
     */
    public Builder builder() {
        return new Builder(new DataBind(this));
    }

    public static class Builder implements TypeRegistry.Builder {
        private DataBind instance;

        /**
         * Create a new builder instance, do this first.
         */
        public Builder() {
            this.instance = new DataBind();
            for (PrimitiveAdapter adapter : PrimitiveAdapter.values()) {
                adapter.apply(this);
            }
        }

        private Builder(DataBind instance) {
            this.instance = instance;
        }

        /**
         * Add a registered TypeAdapter implementation.
         * 
         * @param <T>     the type to adapt
         * @param adapter the implementation of the type adapter.
         * @throws NullPointerException if adapter is null.
         */
        public <T> Builder put(TypeAdapter<T> adapter) {
            if (null == adapter)
                throw new NullPointerException("adapter must be non-null");
            Type type = adapter.getRawType();
            putTypeFactory(type, adapter);
            putVisitType(type, adapter);
            return this;
        }

        /**
         * Set the int adapter implementation.
         * 
         * @param adapter the adapter to use.
         * @throws NullPointerException if adapter is null.
         */
        @Override
        public Builder put(IntFactory adapter) {
            if (null == adapter)
                throw new NullPointerException("adapter must be non-null");
            instance.intFactory = adapter;
            return this;
        }

        /**
         * Set the long adapter implementation.
         * 
         * @param adapter the adapter to use.
         * @throws NullPointerException if adapter is null.
         */
        @Override
        public Builder put(LongFactory adapter) {
            if (null == adapter)
                throw new NullPointerException("adapter must be non-null");
            instance.longFactory = adapter;
            return this;
        }

        @Override
        public Builder put(BooleanFactory adapter) {
            if (null == adapter)
                throw new NullPointerException("adapter must be non-null");
            instance.booleanFactory = adapter;
            return this;
        }

        @Override
        public Builder put(ShortFactory adapter) {
            if (null == adapter)
                throw new NullPointerException("adapter must be non-null");
            instance.shortFactory = adapter;
            return this;
        }

        @Override
        public Builder put(ByteFactory adapter) {
            if (null == adapter)
                throw new NullPointerException("adapter must be non-null");
            instance.byteFactory = adapter;
            return this;
        }

        @Override
        public Builder put(CharFactory adapter) {
            if (null == adapter)
                throw new NullPointerException("adapter must be non-null");
            instance.charFactory = adapter;
            return this;
        }

        @Override
        public Builder put(DoubleFactory adapter) {
            if (null == adapter)
                throw new NullPointerException("adapter must be non-null");
            instance.doubleFactory = adapter;
            return this;
        }

        @Override
        public Builder put(FloatFactory adapter) {
            if (null == adapter)
                throw new NullPointerException("adapter must be non-null");
            instance.floatFactory = adapter;
            return this;
        }

        /**
         * Register a TypeFactory.
         * 
         * @param type        the reflection type
         * @param typeFactory the factory to use when that reflection type is found
         * @throws NullPointerException if typeFactory is null
         */
        public <T> Builder putTypeFactory(Type type, TypeFactory<T> typeFactory) {
            if (null == typeFactory)
                throw new NullPointerException("Expected non-null typeFactory");
            instance.factories.put(type, typeFactory);
            return this;
        }

        /**
         * Register a VisitType.
         * 
         * @param type      the reflection type
         * @param visitType the visit function to use when that reflection type is
         *                  found.
         * @throws NullPointerException if visitType is null
         */
        public <T> Builder putVisitType(Type type, VisitType<T> visitType) {
            if (null == visitType)
                throw new NullPointerException("Expected non-null visitType");
            instance.visits.put(type, visitType);
            return this;
        }

        /**
         * Append to the list of delegate type adapters.
         * 
         * @param registry the registry to add to the list of registries to use.
         * @return this
         */
        public Builder add(TypeAdapterRegistry registry) {
            if (null == registry)
                throw new NullPointerException("registry must be non-null");
            addTypeFactoryRegistry(registry);
            addVisitTypeRegistry(registry);
            return this;
        }

        /**
         * Append to the list of delegate type factory registries. The delegate type
         * registries MUST NOT delegate back to this registry otherwise a
         * StackOverflowException will occur when trying to find a type adapter.
         * 
         * @param delegate the registry to add to the list of registries to use.
         */
        public Builder addTypeFactoryRegistry(TypeFactoryRegistry delegate) {
            if (null == delegate)
                throw new NullPointerException("Expected non-null delegate registry");
            if (this == delegate)
                throw new IllegalArgumentException("No circular type registries are allowed");
            instance.factoryRegistries.add(delegate);
            return this;
        }

        /**
         * Append to the list of delegate type factory registries. The delegate type
         * registries MUST NOT delegate back to this registry otherwise a
         * StackOverflowException will occur when trying to find a type adapter.
         * 
         * @param delegate the registry to add to the list of registries to use.
         */
        public Builder addVisitTypeRegistry(VisitTypeRegistry delegate) {
            if (null == delegate)
                throw new NullPointerException("Expected non-null delegate registry");
            if (this == delegate)
                throw new IllegalArgumentException("No circular type registries are allowed");
            instance.visitRegistries.add(delegate);
            return this;
        }

        /**
         * Creates a new DataBind and resets the internal built up state.
         * 
         * @return the new DataBind
         */
        public DataBind build() {
            DataBind result = instance;
            instance = new DataBind();
            return result;
        }
    }

    private DataBind() {
    }

    private DataBind(DataBind other) {
        factories = new HashMap<>(other.factories);
        factoryRegistries = new ArrayList<>(other.factoryRegistries);
        visits = new HashMap<>(other.visits);
        visitRegistries = new ArrayList<>(other.visitRegistries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TypeFactory<?> getTypeFactory(final Type type) {
        TypeFactory<?> factory;
        // Try to obtain with read lock:
        lock.readLock().lock();
        try {
            factory = factories.get(type);
        } finally {
            lock.readLock().unlock();
        }
        // Search with write lock:
        if (null == factory) {
            lock.writeLock().lock();
            try {
                factory = factories.get(type);
                if (null == factory) {
                    Type actualType = extractActualType(type);
                    if (type != actualType) {
                        factory = factories.get(actualType);
                    }
                    if (null == factory) {
                        factory = findTypeFactory(actualType);
                    }
                    if (null != factory) {
                        factories.put(type, factory);
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongFactory getLongFactory() {
        return longFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IntFactory getIntFactory() {
        return intFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ShortFactory getShortFactory() {
        return shortFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ByteFactory getByteFactory() {
        return byteFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharFactory getCharFactory() {
        return charFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DoubleFactory getDoubleFactory() {
        return doubleFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FloatFactory getFloatFactory() {
        return floatFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanFactory getBooleanFactory() {
        return booleanFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VisitType<?> getVisitType(final Type type) {
        VisitType<?> visit;
        lock.readLock().lock();
        try {
            visit = visits.get(type);
        } finally {
            lock.readLock().unlock();
        }
        // Search with write lock:
        if (null == visit) {
            lock.writeLock().lock();
            try {
                visit = visits.get(type);
                if (null == visit) {
                    Type actualType = extractActualType(type);
                    if (type != actualType) {
                        visit = visits.get(actualType);
                    }
                    if (null == visit) {
                        visit = findVisitType(actualType);
                    }
                    if (null != visit) {
                        visits.put(type, visit);
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return visit;
    }

    /**
     * Transform an input of the specified type to a target type.
     */
    public <Input, Target> Target transform(Input input, Class<Target> targetType) {
        return (Target) transform(input, (Type) targetType);
    }

    public <Input> Object transform(Input input, Type targetType) {
        // FIXME: Why the type cast needed?
        VisitType<Input> visit = getVisitType((Class<Input>) (null == input ? null : input.getClass()));
        if (null == visit) {
            throw new UnsupportedTypeError("No registered VisitType for " + input.getClass());
        }
        TypeFactory<?> factory = getTypeFactory(targetType);
        if (null == factory) {
            throw new UnsupportedTypeError("No registered VisitType for " + targetType);
        }
        Object[] result = new Object[1];
        TypeVisitorImpl<?> builder = new TypeVisitorImpl<>(this, targetType, obj -> result[0] = obj);
        visit.visit(input, builder);
        return result[0];
    }

    private TypeFactory<?> findTypeFactory(final Type type) {
        ListIterator<TypeFactoryRegistry> it = factoryRegistries.listIterator(factoryRegistries.size());
        while (it.hasPrevious()) {
            TypeFactoryRegistry registry = it.previous();
            TypeFactory<?> factory = registry.getTypeFactory(type);
            if (null != factory) {
                return factory;
            }
        }
        return null;
    }

    private VisitType<?> findVisitType(final Type type) {
        ListIterator<VisitTypeRegistry> it = visitRegistries.listIterator(visitRegistries.size());
        while (it.hasPrevious()) {
            VisitTypeRegistry registry = it.previous();
            VisitType<?> visit = registry.getVisitType(type);
            if (null != visit) {
                return visit;
            }
        }
        return null;
    }

    private Type extractActualType(Type type) {
        if (type instanceof WildcardType) {
            WildcardType wildcard = (WildcardType) type;
            if (0 == wildcard.getLowerBounds().length && 1 == wildcard.getUpperBounds().length) {
                return wildcard.getUpperBounds()[0];
            }
        }
        return type;
    }
}
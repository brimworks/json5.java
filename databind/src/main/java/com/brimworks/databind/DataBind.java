package com.brimworks.databind;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * DataBind instances must be created with a {@link DataBind.Builder}. The
 * DataBind object is immutable and thus thread-safe, but the builder is not
 * thread safe.
 * 
 */
public class DataBind implements TypeRegistry {
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<Type, TypeFactory<?>> factories = new HashMap<>();
    private List<TypeFactoryRegistry> factoryRegistries = new ArrayList<>();
    private Map<Type, VisitType<?>> visits = new HashMap<>();
    private List<VisitTypeRegistry> visitRegistries = new ArrayList<>();

    /**
     * Obtain a builder instance for the same state as the this DataBind.
     * 
     * @return a new builder.
     */
    public Builder builder() {
        return new Builder(new DataBind(this));
    }

    public static class Builder {
        private DataBind instance;

        /**
         * Create a new builder instance, do this first.
         */
        public Builder() {
            this.instance = new DataBind();
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
        for (PrimitiveAdapter primitiveAdapter : PrimitiveAdapter.values()) {
            TypeAdapter<?> adapter = primitiveAdapter.getAdapter();
            Type type = adapter.getRawType();
            factories.put(type, adapter);
            visits.put(type, adapter);
        }
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
    public VisitType<?> getVisitType(final Type type) {
        VisitType<?> visit;
        lock.readLock().lock();
        try {
            visit = visits.get(type);
        } finally {
            lock.readLock().unlock();
        }
        // Search with write lock:
        if ( null == visit ) {
            lock.writeLock().lock();
            try {
                visit= visits.get(type);
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
        Object[] result = new Object[1];
        VisitorBuilder<?> builder = new VisitorBuilder<>(factory, obj -> result[0] = obj, this);
        visit.visit(input, builder);
        builder.visitFinish();
        return result[0];
    }

    private TypeFactory<?> findTypeFactory(final Type type) {
        for (TypeFactoryRegistry registry : factoryRegistries) {
            TypeFactory<?> factory = registry.getTypeFactory(type);
            if (null != factory) {
                return factory;
            }
        }
        return null;
    }

    private VisitType<?> findVisitType(final Type type) {
        for (VisitTypeRegistry registry : visitRegistries) {
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
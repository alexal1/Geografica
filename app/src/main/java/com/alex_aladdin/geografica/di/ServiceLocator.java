package com.alex_aladdin.geografica.di;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused"})
public class ServiceLocator {

    private static final Map<String, Object> sServicesInstances = new HashMap<>();
    private static final Map<String, Class> sServicesImplementationsMapping = new HashMap<>();
    private static final Map<String, Creator> sServicesCreators = new HashMap<>();
    private static final Object sServicesInstancesLock = new Object();

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static void init(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Return instance of desired class or object that implement desired interface.
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T get(@NonNull Class<T> clazz) {
        if (mContext == null) {
            throw new RuntimeException("init() must be called");
        }

        @SuppressWarnings("ResourceType") T instance = (T) getService(clazz.getName(), mContext);
        return instance;
    }

    /**
     * This method allows to bind a custom service implementation to an interface.
     *
     * @param interfaceClass      interface
     * @param implementationClass class which implement interface specified in first param
     */
    public static void bindCustomServiceImplementation(@NonNull Class interfaceClass, @NonNull Class implementationClass) {
        synchronized (sServicesInstancesLock) {
            sServicesImplementationsMapping.put(interfaceClass.getName(), implementationClass);
        }
    }

    /**
     * Set the way to create an instance of given class.
     *
     * @param clazz     class to create
     * @param creator   Creator object to instantiate this class
     */
    public static void registerServiceCreator(@NonNull Class clazz, @NonNull Creator creator) {
        synchronized (sServicesInstancesLock) {
            sServicesCreators.put(clazz.getName(), creator);
        }
    }

    @NonNull
    private static Object getService(@NonNull String name, Context applicationContext) {
        synchronized (sServicesInstancesLock) {
            Object serviceInstance;
            serviceInstance = sServicesInstances.get(name);
            if (serviceInstance != null) {
                return serviceInstance;
            }

            Creator serviceCreator = sServicesCreators.get(name);
            if (serviceCreator != null) {
                serviceInstance = serviceCreator.newInstance(applicationContext);
                sServicesInstances.put(name, serviceInstance);
                return serviceInstance;
            }

            try {
                Class<?> clazz;
                clazz = sServicesImplementationsMapping.get(name);
                if (clazz == null) {
                    clazz = Class.forName(name);
                }

                try {
                    Constructor constructor = clazz.getConstructor(Context.class);
                    serviceInstance = constructor.newInstance(applicationContext);
                } catch (NoSuchMethodException e) {
                    Constructor constructor = clazz.getConstructor();
                    serviceInstance = constructor.newInstance();
                }

                if (!serviceInstance.getClass().isAnnotationPresent(Service.class)) {
                    throw new IllegalArgumentException("Requested service must be annotated as @ServiceLocator.Service");
                }

                sServicesInstances.put(name, serviceInstance);
                return serviceInstance;
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Requested service class was not found: " + name, e);
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot initialize requested service: " + name, e);
            }
        }
    }

    /**
     * All Services provided by the Service Locator have to be annotated with this annotation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Service {
    }

    public interface Creator<T> {
        T newInstance(@NonNull Context context);
    }
}
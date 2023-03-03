/*
 * Copyright (C) 2019-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.laxture.sbp.util;

import lombok.NonNull;
import org.springframework.beans.factory.BeanFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class BeanUtil {

    private BeanUtil() {};

    public static <T> T getFieldValue(@NonNull Object target,
                                      @NonNull String path) {
        String[] fieldPath = path.split("\\.");
        Object obj = target;
        int i=0;
        while (i<fieldPath.length) {
            if (obj == null) break;
            if ("*".equals(fieldPath[i])) {
                // merge map
                if (obj instanceof Map) {
                    obj = ((Map<?, ?>) obj).values();
                } else if (obj instanceof Collection) {
                    obj = obj;
                } else {
                    // non-support object fields
                    return null;
                }
            } else {
                if (obj instanceof Collection) {
                    List<Object> values = new ArrayList<>();
                    for (Object item : (Collection<?>) obj) {
                        Object value = getFieldValue(item, item.getClass(), fieldPath[i]);
                        values.add(value);
                    }
                    obj = values;
                } else {
                    obj = getFieldValue(obj, obj.getClass(), fieldPath[i]);
                }
            }
            i++;
        }
        return (T) obj;
    }

    public static Class<?> getFieldClass(@NonNull Object target,
                                         @NonNull String fieldName) {
        try {
            return target.getClass().getDeclaredField(fieldName).getType();
        } catch (Exception e) {
            return null;
        }
    }

    private static Object getFieldValue(@NonNull Object target,
                                        @NonNull Class<?> clazz,
                                        @NonNull String fieldName) {
        if (Map.class.isAssignableFrom(clazz)) {
            return ((Map<?, ?>) target).get(fieldName);
        }

        try {
            Field field = target instanceof Class
                ? ((Class<?>) target).getDeclaredField(fieldName)
                : clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException nsfe) {
            if (clazz.getSuperclass() != null) {
                return target instanceof Class
                    ? getFieldValue(((Class<?>) target).getSuperclass(), clazz, fieldName)
                    : getFieldValue(target, clazz.getSuperclass(), fieldName);
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static void setFieldValue(@NonNull Object target,
                                     @NonNull String fieldName,
                                     Object value) {
        setFieldValue(target, target.getClass(), fieldName, value);
    }

    private static void setFieldValue(@NonNull Object target,
                                      @NonNull Class clazz,
                                      @NonNull String fieldName,
                                      Object value) {
        try {
            Field field = target instanceof Class
                ? ((Class<?>) target).getDeclaredField(fieldName)
                : clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException nsfe) {
            if (clazz.getSuperclass() != null) {
                setFieldValue(target, clazz.getSuperclass(), fieldName, value);
            } else {
                throw new RuntimeException("Set field "+fieldName+" failed.", nsfe);
            }
        } catch (Exception e) {
            throw new RuntimeException("Set field "+fieldName+" failed.", e);
        }
    }

    public static <T extends Serializable> T deepClone(@NonNull T o) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(o);
            out.flush();
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()));
            return (T) o.getClass().cast(in.readObject());
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy Object "+o.getClass().getName(), e);
        }
    }

    public static Method getDeclaredMethod(@NonNull Class<?> clazz,
                                           @NonNull String methodName,
                                           Class<?>... parameterTypes) {
        Method method = null;
        Class<?> clz = clazz;
        while (clz != Object.class) {
            try {
                method = parameterTypes.length > 0
                    ? clz.getDeclaredMethod(methodName, parameterTypes)
                    : clz.getDeclaredMethod(methodName);
                break;
            } catch (NoSuchMethodException e) {
                clz = clz.getSuperclass();
            }
        }
        if (method != null) method.setAccessible(true);
        return method;
    }

    public static Method getMethod(@NonNull Class<?> clazz,
                                   @NonNull String methodName,
                                   Class<?>... parameterTypes) {
        Method method;
        try {
            method = parameterTypes.length > 0
                ? clazz.getMethod(methodName, parameterTypes)
                : clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
        if (method != null) method.setAccessible(true);
        return method;
    }

    public static <R, O> R callMethod(O object,
                                      @NonNull String methodName,
                                      Object... parameters) {
        Class<O> clazz = (Class<O>) object.getClass();
        return callMethod(clazz, object, methodName, parameters);
    }

    /**
     * This method doesn't always function as expected. Be 100% sure
     * and tested when you use it.
     *
     * As known, this method is not worked for following case:
     * * parameter type is primitive number, e.g. int.class
     * * parameter type is general type, e.g. Object.class
     */
    public static <R> R callMethod(Class<?> clazz,
                                   Object object,
                                   @NonNull String methodName,
                                   Object... parameters) {
        if (object == null) return null;

        Method method;
        // try get method from `getMethod`
        if (parameters.length == 0) {
            method = getMethod(clazz, methodName);
        } else {
            method = getMethod(clazz, methodName,
                Arrays.stream(parameters).map(Object::getClass).toArray(Class[]::new));
        }

        // try get method from `getDeclaredMethod`
        if (method == null) {
            if (parameters.length == 0) {
                method = getDeclaredMethod(clazz, methodName);
            } else {
                method = getDeclaredMethod(clazz, methodName,
                    Arrays.stream(parameters).map(Object::getClass).toArray(Class[]::new));
            }
        }

        if (method == null) return null;

        try {
            return (R) method.invoke(object, parameters);
        } catch (IllegalAccessException | InvocationTargetException ignore) {
        }
        return null;
    }

    public static String getBeanName(BeanFactory beanFactory, Object bean) {
        String beanName = null;
        Map<String, Object> beans = BeanUtil.getFieldValue(beanFactory, "disposableBeans");
        if (beans != null) {
            beanName = beans.entrySet().stream()
                .filter(entry -> entry.getValue() == bean).findAny()
                .map(Map.Entry::getKey).orElse(null);
        }
        if (beanName == null) {
            beans = BeanUtil.getFieldValue(beanFactory, "singletonObjects");
            if (beans != null) {
                beanName = beans.entrySet().stream()
                    .filter(entry -> entry.getValue() == bean).findAny()
                    .map(Map.Entry::getKey).orElse(null);
            }
        }
        return beanName;
    }

}

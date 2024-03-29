package com.loopswork.loops.admin.utils;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.lang.reflect.*;
import java.util.Date;


/**
 * 反射工具类.
 * 提供调用getter/setter方法, 访问私有变量, 调用私有方法, 获取泛型类型Class, 被AOP过的真实类等工具函数.
 */
public class ReflectUtils {

  private static final String SETTER_PREFIX = "set";

  private static final String GETTER_PREFIX = "get";

  private static final String CGLIB_CLASS_SEPARATOR = "$$";

  private static Logger logger = LoggerFactory.getLogger(ReflectUtils.class);

  /**
   * 直接读取对象属性值, 无视private/protected修饰符, 不经过getter函数.
   */
  public static Object getFieldValue(final Object obj, final String fieldName) {
    Field field = getAccessibleField(obj, fieldName);

    if (field == null) {
      throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
    }

    Object result = null;
    try {
      result = field.get(obj);
    } catch (IllegalAccessException e) {
      logger.error("不可能抛出的异常{}", e.getMessage());
    }
    return result;
  }

  /**
   * 直接设置对象属性值, 无视private/protected修饰符, 不经过setter函数.
   */
  public static void setFieldValue(final Object obj, final String fieldName, final Object value) {
    Field field = getAccessibleField(obj, fieldName);

    if (field == null) {
      throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]");
    }

    try {
      field.set(obj, value);
    } catch (IllegalAccessException e) {
      logger.error("不可能抛出的异常:{}", e.getMessage());
    }
  }

  /**
   * 直接调用对象方法, 无视private/protected修饰符.
   * 用于一次性调用的情况，否则应使用getAccessibleMethod()函数获得Method后反复调用.
   * 同时匹配方法名+参数类型，
   */
  public static Object invokeMethod(final Object obj, final String methodName, final Class<?>[] parameterTypes,
                                    final Object[] args) {
    Method method = getAccessibleMethod(obj, methodName, parameterTypes);
    if (method == null) {
      throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + obj + "]");
    }

    try {
      return method.invoke(obj, args);
    } catch (Exception e) {
      throw convertReflectionExceptionToUnchecked(e);
    }
  }

  /**
   * 直接调用对象方法, 无视private/protected修饰符，
   * 用于一次性调用的情况，否则应使用getAccessibleMethodByName()函数获得Method后反复调用.
   * 只匹配函数名，如果有多个同名函数调用第一个。
   */
  public static Object invokeMethodByName(final Object obj, final String methodName, final Object[] args) {
    Method method = getAccessibleMethodByName(obj, methodName);
    if (method == null) {
      throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + obj + "]");
    }

    try {
      return method.invoke(obj, args);
    } catch (Exception e) {
      throw convertReflectionExceptionToUnchecked(e);
    }
  }

  /**
   * 循环向上转型, 获取对象的DeclaredField, 并强制设置为可访问.
   * <p>
   * 如向上转型到Object仍无法找到, 返回null.
   */
  public static Field getAccessibleField(final Object obj, final String fieldName) {
    for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
      try {
        Field field = superClass.getDeclaredField(fieldName);
        makeAccessible(field);
        return field;
      } catch (NoSuchFieldException e) {//NOSONAR
        // Field不在当前类定义,继续向上转型
        continue;// new add
      }
    }
    return null;
  }

  /**
   * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问.
   * 如向上转型到Object仍无法找到, 返回null.
   * 匹配函数名+参数类型。
   * <p>
   * 用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object... args)
   */
  public static Method getAccessibleMethod(final Object obj, final String methodName,
                                           final Class<?>... parameterTypes) {
    for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
      try {
        Method method = searchType.getDeclaredMethod(methodName, parameterTypes);
        makeAccessible(method);
        return method;
      } catch (NoSuchMethodException e) {
        // Method不在当前类定义,继续向上转型
        continue;// new add
      }
    }
    return null;
  }

  /**
   * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问.
   * 如向上转型到Object仍无法找到, 返回null.
   * 只匹配函数名。
   * <p>
   * 用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object... args)
   */
  public static Method getAccessibleMethodByName(final Object obj, final String methodName) {
    for (Class<?> searchType = obj.getClass(); searchType != Object.class; searchType = searchType.getSuperclass()) {
      Method[] methods = searchType.getDeclaredMethods();
      for (Method method : methods) {
        if (method.getName().equals(methodName)) {
          makeAccessible(method);
          return method;
        }
      }
    }
    return null;
  }

  /**
   * 改变private/protected的方法为public，尽量不调用实际改动的语句，避免JDK的SecurityManager抱怨。
   */
  public static void makeAccessible(Method method) {
    if ((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
      && !method.isAccessible()) {
      method.setAccessible(true);
    }
  }

  /**
   * 改变private/protected的成员变量为public，尽量不调用实际改动的语句，避免JDK的SecurityManager抱怨。
   */
  public static void makeAccessible(Field field) {
    if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) || Modifier
      .isFinal(field.getModifiers())) && !field.isAccessible()) {
      field.setAccessible(true);
    }
  }

  /**
   * 通过反射, 获得Class定义中声明的泛型参数的类型, 注意泛型必须定义在父类处
   * 如无法找到, 返回Object.class.
   * eg.
   * public UserDao extends HibernateDao<User>
   *
   * @param clazz The class to introspect
   * @return the first generic declaration, or Object.class if cannot be determined
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> getClassGenricType(final Class clazz) {
    return getClassGenricType(clazz, 0);
  }

  /**
   * 通过反射, 获得Class定义中声明的父类的泛型参数的类型.
   * 如无法找到, 返回Object.class.
   * <p>
   * 如public UserDao extends HibernateDao<User,Long>
   *
   * @param clazz clazz The class to introspect
   * @param index the Index of the generic ddeclaration,start from 0.
   * @return the index generic declaration, or Object.class if cannot be determined
   */
  public static Class getClassGenricType(final Class clazz, final int index) {

    Type genType = clazz.getGenericSuperclass();

    if (!(genType instanceof ParameterizedType)) {
      logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
      return Object.class;
    }

    Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

    if (index >= params.length || index < 0) {
      logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
        + params.length);
      return Object.class;
    }
    if (!(params[index] instanceof Class)) {
      logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
      return Object.class;
    }

    return (Class) params[index];
  }

  public static Class<?> getUserClass(Object instance) {
    Class clazz = instance.getClass();
    if (clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
      Class<?> superClass = clazz.getSuperclass();
      if (superClass != null && !Object.class.equals(superClass)) {
        return superClass;
      }
    }
    return clazz;

  }

  /**
   * 将反射时的checked exception转换为unchecked exception.
   */
  public static RuntimeException convertReflectionExceptionToUnchecked(Exception e) {
    if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
      || e instanceof NoSuchMethodException) {
      return new IllegalArgumentException(e);
    } else if (e instanceof InvocationTargetException) {
      return new RuntimeException(((InvocationTargetException) e).getTargetException());
    } else if (e instanceof RuntimeException) {
      return (RuntimeException) e;
    }
    return new RuntimeException("Unexpected Checked Exception.", e);
  }

  /**
   * 判断属性是否为日期类型
   *
   * @param clazz     数据类型
   * @param fieldName 属性名
   * @return 如果为日期类型返回true，否则返回false
   */
  public static <T> boolean isDateType(Class<T> clazz, String fieldName) {
    boolean flag = false;
    try {
      Field field = clazz.getDeclaredField(fieldName);
      Object typeObj = field.getType().newInstance();
      flag = typeObj instanceof Date;
    } catch (Exception e) {
      // 把异常吞掉直接返回false
    }
    return flag;
  }

  /**
   * 根据类型将指定参数转换成对应的类型
   *
   * @param value 指定参数
   * @param type  指定类型
   * @return 返回类型转换后的对象
   */
  public static <T> Object parseValueWithType(String value, Class<?> type) {
    Object result = null;
    try { // 根据属性的类型将内容转换成对应的类型
      if (Boolean.TYPE == type) {
        result = Boolean.parseBoolean(value);
      } else if (Byte.TYPE == type) {
        result = Byte.parseByte(value);
      } else if (Short.TYPE == type) {
        result = Short.parseShort(value);
      } else if (Integer.TYPE == type) {
        result = Integer.parseInt(value);
      } else if (Long.TYPE == type) {
        result = Long.parseLong(value);
      } else if (Float.TYPE == type) {
        result = Float.parseFloat(value);
      } else if (Double.TYPE == type) {
        result = Double.parseDouble(value);
      } else {
        result = value;
      }
    } catch (Exception e) {
      // 把异常吞掉直接返回null
    }
    return result;
  }
}

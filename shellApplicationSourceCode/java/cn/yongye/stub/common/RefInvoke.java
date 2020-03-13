package cn.yongye.stub.common;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RefInvoke {

    public static Object invokeStaticMethod(String class_name, String method_name, Class[] pareType, Object[] pareValues){
        try {
            Class obj_class = Class.forName(class_name);
            Method method = obj_class.getMethod(method_name, pareType);
            return method.invoke(null, pareValues);
        } catch (SecurityException e){
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        } catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (NoSuchMethodException e){
            e.printStackTrace();
        } catch (InvocationTargetException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeMethod(String class_name, String method_name, Object obj, Class[] pareType, Object[] pareValues){
        try {
            Class obj_class = Class.forName(class_name);
            Method method = obj_class.getMethod(method_name, pareType);
            return method.invoke(obj, pareValues);
        } catch (SecurityException e){
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        } catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (NoSuchMethodException e){
            e.printStackTrace();
        } catch (InvocationTargetException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }

    public static Object getFieldObject(String class_name, Object obj, String fieldName){
        try {
            Class obj_class = Class.forName(class_name);
            Field field = obj_class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (SecurityException e){
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        } catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        } catch (NoSuchFieldException e){
            e.printStackTrace();
        }
        return null;
    }

    public static Object getStaticFieldObject(String class_name, String fieldName){
        try {
            Class obj_class = Class.forName(class_name);
            Field field = obj_class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (SecurityException e){
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        } catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        } catch (NoSuchFieldException e){
            e.printStackTrace();
        }
        return null;
    }

    public static void setFieldObject(String classname, String fieldName, Object obj, Object fieldValue){
        try {
            Class obj_class = Class.forName(classname);
            Field field = obj_class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, fieldValue);
        }catch (SecurityException e){
            e.printStackTrace();
        }catch (NoSuchFieldException e){
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public static void setStaticObject(String class_name, String fieldName, Object fieldValue){
        try {
            Class obj_class = Class.forName(class_name);
            Field field = obj_class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, fieldValue);
        }catch (SecurityException e){
            e.printStackTrace();
        }catch (NoSuchFieldException e){
            e.printStackTrace();
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }
}

package io.anuke.ucore;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public class UCore{
	public static final boolean isAssets =
			Gdx.app.getType() != ApplicationType.WebGL
					&& getProperty("user.name").equals("anuke")
					&& getAbsolute(Gdx.files.local("na").parent()).endsWith("assets");
	
	public static void log(Object...objects){
		int i = 0;
		for(Object o : objects){
			System.out.print(o);
			if(i++ != objects.length-1)
				System.out.print(", ");
		}
		System.out.println();
	}

	public static String getProperty(String name){
		try{
			Method method = ClassReflection.getMethod(System.class, "getProperty", String.class);
			return (String)method.invoke(null, name);
		}catch(ReflectionException e){
			throw new RuntimeException(e);
		}
	}

	public static String getAbsolute(FileHandle file){
		try{
			Method method = ClassReflection.getMethod(file.getClass(), "file");
			Object object = method.invoke(file);
			Method fm = ClassReflection.getMethod(object.getClass(), "getAbsolutePath");
			return (String)fm.invoke(object);
		}catch(ReflectionException e){
			throw new RuntimeException(e);
		}
	}
	
	public static Object getPrivate(Object object, String name){
		try{
			Field field = ClassReflection.getDeclaredField(object.getClass(), name);
			field.setAccessible(true);
			return field.get(object);
		}catch(ReflectionException e){
            throw new RuntimeException(e);
		}
	}
}

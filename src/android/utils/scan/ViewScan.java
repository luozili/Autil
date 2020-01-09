package android.utils.scan;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.utils.annotation.ViewClassInject;
import android.utils.annotation.ViewFieldsInject;

/**
 * 
 * @author 罗子利
 *
 */
public class ViewScan {
	
	/**
	 * 可以通过组合ViewClassInject与ViewFieldsInject，但此方法执行效率比initOnClass和initOnField慢
	 * 
	 * @param view 视图对象
	 */
	public  void init(Object view) {
		initOnClass(view);
		initOnField(view);
	}

	private  Object getView(Object obj, int id) {
		Method method = null;
		Class<? extends Object> clazz = obj.getClass();
		try {
			method = clazz.getMethod("findViewById", int.class);
		} catch (Exception e) {
			System.out.println("获取findViewById方法失败");
			e.printStackTrace();
		}
		Object view = null;
		try {
			view = method.invoke(obj, id);
		} catch (Exception e) {
			System.out.println("不被允许、不合法参数或执行目标对象错误");
			e.printStackTrace();
		}
		return view;
	}

	/**
	 * 获取包名
	 * 
	 * @param clazz
	 * @return String 包名
	 */
	private  String getPackageName(Class<?> clazz) {
		Package p = clazz.getPackage();
		String packageName = p.getName();
		return packageName;
	}

	/**
	 * 扫描类上是否有ViewClassInject，有则将该类的所有字段（View）进行赋值， 先根据注解上的名字进行匹配，如果没有则默认使用字段名
	 * 
	 * @param  view 视图对象
	 */
	public  void initOnClass(Object view) {
		Class<? extends Object> clazz = view.getClass();
		ViewClassInject annotation = clazz.getAnnotation(ViewClassInject.class);
		if (annotation == null) {
			return;
		}
		Field[] fields = clazz.getDeclaredFields();
		String[] names = annotation.names();
		String packageName = getPackageName(clazz);
		for (int i = 0; i < fields.length; i++) {
			String name = null;
			if (isEmpty(names[i])) {
				name = fields[i].getName();
			} else {
				name = names[i];
			}
			setView(view, packageName, name, fields[i]);
		}
	}

	/**
	 * 扫描自动并将带有ViewFieldsInject注解的字段注入值
	 * 
	 * @param view 视图对象
	 */
	public  void initOnField(Object view) {
		Class<? extends Object> clazz = view.getClass();
		Field[] fields = clazz.getDeclaredFields();
		String packageName = getPackageName(clazz);
		for (int i = 0; i < fields.length; i++) {
			ViewFieldsInject annotation = fields[i].getAnnotation(ViewFieldsInject.class);
			if (annotation == null) {
				continue;
			}
			String name = annotation.name();
			if (isEmpty(name)) {
				name = fields[i].getName();
			}
			setView(view, packageName, name, fields[i]);
		}
	}

	/**
	 * 设置View对象
	 * 
	 * @param ctx         需要注入字段的类
	 * @param packageName 包名
	 * @param name        R.id.控件名称
	 * @param field       需要注入的字段
	 */
	public  void setView(Object ctx, String packageName, String name, Field field) {
		Integer id = getRId(packageName, name);
		if(id == null) {
			return;
		}
		Object view = getView(ctx, id);
		try {
			field.setAccessible(true);
			field.set(ctx, view);
		} catch (Exception e) {
			System.out.println("不合法参数或不被允许");
			e.printStackTrace();
		}
	}

	/**
	 * 获取R.id中的值
	 * 
	 * @param packageName 包名
	 * @param viewName    控件名
	 * @return int R.Id中的字段值
	 */
	private  Integer getRId(String packageName, String viewName) {
		try {
			Class<?> rClass = Class.forName(packageName + ".R$id");
			Field field = rClass.getDeclaredField(viewName);
			field.setAccessible(true);
			Integer id = (Integer) field.get(rClass.newInstance());
			return id;
		} catch (Exception e) {
			System.out.println("获取R文件失败");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 判断字符串是否为空，如果为空，则返回true
	 * 
	 * @param str 需要判断的字符串
	 * @return boolean
	 */
	private  boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}
}

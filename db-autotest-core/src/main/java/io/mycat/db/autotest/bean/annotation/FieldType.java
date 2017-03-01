package io.mycat.db.autotest.bean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldType {
	
	public String childName() ;
	
	public Class<?> type() default List.class;
	
	public  Class<?> childType();

}
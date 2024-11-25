package mg.pokaneliot.annotation.type;

import java.lang.annotation.ElementType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Numeric {
    double limite();
    String errorMessage() default "La limite a été dépassée : ";
}

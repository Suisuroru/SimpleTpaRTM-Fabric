package fun.bm.simpletpartm.configs.flags;

import fun.bm.simpletpartm.enums.EnumConfigCategory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigClassInfo {
    EnumConfigCategory category();

    String[] directory() default {};

    String comments() default "";
}

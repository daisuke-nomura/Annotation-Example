package com.kyaracter.annotation.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface AnnotationObject {
    String name();
}

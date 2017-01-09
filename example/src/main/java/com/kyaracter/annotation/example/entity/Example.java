package com.kyaracter.annotation.example.entity;

import com.kyaracter.annotation.annotation.AnnotationKey;
import com.kyaracter.annotation.annotation.AnnotationObject;


@AnnotationObject(name = "Example")
public class Example {
    @AnnotationKey(key = "name")
    private String name;
}

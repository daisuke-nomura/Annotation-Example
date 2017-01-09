package com.kyaracter.annotation.processor;


import com.kyaracter.annotation.annotation.AnnotationObject;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({
        "com.kyaracter.annotation.annotation.AnnotationObject",
        "com.kyaracter.annotation.annotation.AnnotationKey"
})
public class AnnotationProcessor extends AbstractProcessor{

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, final RoundEnvironment roundEnvironment) {
        final List<Class<? extends Annotation>> list = new ArrayList<>();
        list.add(AnnotationObject.class);

        Observable
                .fromIterable(list)
                .flatMapIterable(new Function<Class<? extends Annotation>, Set<? extends Element>>() {
                    @Override
                    public Set<? extends Element> apply(Class<? extends Annotation> aClass) throws Exception {
                        return roundEnvironment.getElementsAnnotatedWith(aClass);
                    }
                })
                .blockingForEach(new Consumer<Element>() {
                    @Override
                    public void accept(Element element) throws Exception {
                        TypeElement typeElement = (TypeElement) element;

                        TypeSpec typeSpec = TypeSpec
                                .classBuilder(typeElement.getSimpleName() + "Entity")
                                .addModifiers(Modifier.PUBLIC)
                                .build();

                        String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();

                        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
                        javaFile.writeTo(processingEnv.getFiler());
                    }
                });

        return false;
    }
}

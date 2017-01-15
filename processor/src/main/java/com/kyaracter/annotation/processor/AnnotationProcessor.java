package com.kyaracter.annotation.processor;


import com.kyaracter.annotation.annotation.AnnotationKey;
import com.kyaracter.annotation.annotation.AnnotationObject;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

import static com.squareup.javapoet.MethodSpec.methodBuilder;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({
        "com.kyaracter.annotation.annotation.AnnotationKey",
        "com.kyaracter.annotation.annotation.AnnotationObject"
})
public class AnnotationProcessor extends AbstractProcessor{

    private static final String SUFFIX = "Entity";

    private Class<? extends Annotation> annotation;

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
                        annotation = aClass;
                        return roundEnvironment.getElementsAnnotatedWith(aClass);
                    }
                })
                .filter(new Predicate<Element>() {
                    @Override
                    public boolean test(Element element) throws Exception {
                        return element instanceof TypeElement;
                    }
                })
                .blockingForEach(new Consumer<Element>() {
                    @Override
                    public void accept(Element element) throws Exception {
                        TypeElement typeElement = (TypeElement) element;

                        if (annotation.equals(AnnotationObject.class)) {
                            AnnotationObject annotationObject = typeElement.getAnnotation(AnnotationObject.class);
                            //annotation name() value
                            System.out.println(annotationObject.name());

                            final TypeSpec.Builder builder = TypeSpec
                                    .classBuilder(typeElement.getSimpleName() + SUFFIX)
                                    .addModifiers(Modifier.PUBLIC)
                                    .addMethod(constructor());

                            Observable
                                    .fromIterable(typeElement.getEnclosedElements())
                                    .filter(new Predicate<Element>() {
                                        @Override
                                        public boolean test(Element element) throws Exception {
                                            return element instanceof VariableElement;
                                        }
                                    })
                                    .blockingForEach(new Consumer<Element>() {
                                        @Override
                                        public void accept(Element element) throws Exception {
                                            if (element.getAnnotation(AnnotationKey.class) != null) {
                                                builder.addField(field(element));
                                                builder.addMethod(getter(element));
                                                builder.addMethod(setter(element));
                                            }
                                        }
                                    });

                            String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();

                            JavaFile javaFile = JavaFile.builder(packageName, builder.build()).build();
                            javaFile.writeTo(processingEnv.getFiler());
                        }
                    }
                });

        return false;
    }

    private MethodSpec constructor() {
        return MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addCode("super();\n")
                .build();
    }

    private FieldSpec field(Element element) {
        Name name = element.getSimpleName();

        return FieldSpec
                .builder(TypeName.get(element.asType()), name.toString())
                .addModifiers(Modifier.PRIVATE)
                .build();
    }

    private MethodSpec setter(Element element) {
        AnnotationKey annotation = element.getAnnotation(AnnotationKey.class);

        //annotation key() value
        System.out.println(annotation.key());

        Name name = element.getSimpleName();

        ParameterSpec parameterSpec = ParameterSpec
                .builder(TypeName.get(element.asType()), name.toString()).build();

        String setter = String.format(Locale.US, "set%s%s", name.toString().substring(0, 1).toUpperCase(), name.toString().substring(1, name.toString().length()));

        return
                methodBuilder(setter)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterSpec)
                .addCode(String.format(Locale.US, "this.%s = %s;\n", name.toString(), name.toString()))
                .build();
    }

    private MethodSpec getter(Element element) {
        AnnotationKey annotation = element.getAnnotation(AnnotationKey.class);

        //annotation key() value
        System.out.println(annotation.key());

        Name name = element.getSimpleName();

        String getter = String.format(Locale.US, "get%s%s", name.toString().substring(0, 1).toUpperCase(), name.toString().substring(1, name.toString().length()));

        return
                methodBuilder(getter)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(element.asType()))
                .addCode(String.format(Locale.US, "return this.%s;\n", name.toString()))
                .build();
    }
}

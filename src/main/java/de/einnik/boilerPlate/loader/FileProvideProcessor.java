package de.einnik.boilerPlate.loader;

import de.einnik.boilerPlate.annotations.AutoProvideFile;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.LinkedHashSet;
import java.util.Set;

@SupportedAnnotationTypes("de.einnik.boilerPlate.annotations.AutoProvideFile")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class FileProvideProcessor extends AbstractProcessor {

    private final Set<String> fileFields = new LinkedHashSet<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(AutoProvideFile.class)) {
            if (element.getKind() == ElementKind.FIELD) {
                VariableElement fieldElement = (VariableElement) element;
                String fieldName = fieldElement.getEnclosingElement().toString() + "." + fieldElement.getSimpleName();
                fileFields.add(fieldName);
            }
        }

        return false;
    }
}
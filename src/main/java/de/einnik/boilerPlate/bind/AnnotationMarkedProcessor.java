package de.einnik.boilerPlate.bind;

import de.einnik.boilerPlate.annotations.AutoCommand;
import de.einnik.boilerPlate.annotations.AutoListener;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes({
        "de.einnik.boilerPlate.annotations.AutoCommand",
        "de.einnik.boilerPlate.annotations.AutoListener"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AnnotationMarkedProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(AutoCommand.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                validateAndMarkUsed(element, "AutoCommand");
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(AutoListener.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                validateAndMarkUsed(element, "AutoListener");
            }
        }

        return false;
    }

    private void validateAndMarkUsed(Element element, String annotationType) {
        TypeElement classElement = (TypeElement) element;

        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE,
                "Registered a class annotated with @" + annotationType + " as name " + classElement.getQualifiedName()
        );
    }
}
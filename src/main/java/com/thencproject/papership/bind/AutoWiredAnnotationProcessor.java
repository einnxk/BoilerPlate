package com.thencproject.papership.bind;

import com.thencproject.papership.annotations.AutoWiredCommand;
import com.thencproject.papership.annotations.AutoWiredFile;
import com.thencproject.papership.annotations.AutoWiredListener;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Annotation Processor for the AutoWired Listener and Commands
 * annotation so that they are no longer marked as not used in
 * IntelliJ IDEA by creating a small helper class
 */
@SupportedAnnotationTypes({
        "com.thencproject.papership.annotations.AutoWiredCommand",
        "com.thencproject.papership.annotations.AutoWiredListener",
        "com.thencproject.papership.annotations.AutoWiredFile"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AutoWiredAnnotationProcessor extends AbstractProcessor {

    private final Set<String> collectedClasses = new HashSet<>();
    private final Set<String> fileFields = new LinkedHashSet<>();

    /**
     * Add all classes marked with that annotation to the collectedClasses
     * list before generating a helper class
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (!roundEnv.processingOver()) {
            for (Element element : roundEnv.getElementsAnnotatedWith(AutoWiredFile.class)) {
                if (element.getKind() == ElementKind.FIELD) {
                    VariableElement fieldElement = (VariableElement) element;
                    String fieldName = fieldElement.getEnclosingElement().toString() + "." + fieldElement.getSimpleName();
                    fileFields.add(fieldName);
                }
            }

            for (Element element : roundEnv.getElementsAnnotatedWith(AutoWiredCommand.class)) {
                if (element.getKind() == ElementKind.CLASS) {
                    collectedClasses.add(((TypeElement) element).getQualifiedName().toString());
                }
            }

            for (Element element : roundEnv.getElementsAnnotatedWith(AutoWiredListener.class)) {
                if (element.getKind() == ElementKind.CLASS) {
                    collectedClasses.add(((TypeElement) element).getQualifiedName().toString());
                }
            }

        } else {
            if (!collectedClasses.isEmpty()) {
                generateRegistry();
            }
        }

        return false;
    }

    /**
     * help method to create a file in this package that helps
     * to mark the classes as used in IDEA's like IntelliJ
     */
    private void generateRegistry() {
        String packageName = "com.thencproject.papership.generated";
        String className = "GeneratedRegistry";

        try {
            JavaFileObject file = processingEnv.getFiler()
                    .createSourceFile(packageName + "." + className);

            try (Writer writer = file.openWriter()) {

                writer.write("package " + packageName + ";\n\n");
                writer.write("public final class " + className + " {\n\n");
                writer.write("    private " + className + "() {}\n\n");
                writer.write("    public static final Class<?>[] REGISTERED = new Class<?>[] {\n");

                int index = 0;
                for (String fqcn : collectedClasses) {
                    writer.write("        " + fqcn + ".class");
                    if (++index < collectedClasses.size()) {
                        writer.write(",");
                    }
                    writer.write("\n");
                }

                writer.write("    };\n");
                writer.write("}\n");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
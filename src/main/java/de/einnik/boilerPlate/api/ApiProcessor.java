package de.einnik.boilerPlate.api;

import de.einnik.boilerPlate.annotations.BoilerPlateAPI;
import de.einnik.boilerPlate.annotations.SubAPI;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

@SupportedAnnotationTypes({
        "de.einnik.boilerPlate.annotations.BoilerPlateAPI",
        "de.einnik.boilerPlate.annotations.SubAPI"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ApiProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(BoilerPlateAPI.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "@BoilerPlateAPI can only be applied to classes",
                        element
                );
                continue;
            }

            TypeElement classElement = (TypeElement) element;
            try {
                generateAPIAccessor(classElement);
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Failed to generate API accessor: " + e.getMessage(),
                        element
                );
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(SubAPI.class)) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    "Registered API class marked @SubAPI name: " + element.getSimpleName()
            );
        }

        return false;
    }

    private void generateAPIAccessor(TypeElement apiClass) throws IOException {
        String packageName = processingEnv.getElementUtils().getPackageOf(apiClass).getQualifiedName().toString();
        String className = apiClass.getSimpleName().toString();
        String accessorClassName = className + "Provider";

        JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + accessorClassName);

        try (PrintWriter writer = new PrintWriter(file.openWriter())) {
            writer.println("package " + packageName + ";");
            writer.println();
            writer.println("import de.einnik.boilerPlate.api.APIServiceRegistry;");
            writer.println();
            writer.println("public final class " + accessorClassName + " {");
            writer.println();
            writer.println("    private " + accessorClassName + "() {}");
            writer.println();
            writer.println("    public static " + className + " getAPI() {");
            writer.println("        return APIServiceRegistry.getAPI(" + className + ".class);");
            writer.println("    }");
            writer.println();
            writer.println("    public static boolean isLoaded() {");
            writer.println("        return APIServiceRegistry.isRegistered(" + className + ".class);");
            writer.println("    }");
            writer.println("}");
        }
    }
}
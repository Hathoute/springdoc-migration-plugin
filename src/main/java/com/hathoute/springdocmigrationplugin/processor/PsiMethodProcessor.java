package com.hathoute.springdocmigrationplugin.processor;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.SF_APIOPERATION_ANNOTATION;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.SF_APIPARAM_ANNOTATION;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.SF_APIRESPONSES_ANNOTATION;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.SF_APIRESPONSE_ANNOTATION;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.migrateApiOperationAnnotation;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.migrateApiResponseAnnotation;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.migrateApiResponsesAnnotation;

import com.hathoute.springdocmigrationplugin.PsiMigrationsHelper;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import java.util.Arrays;
import java.util.Objects;

public class PsiMethodProcessor implements PsiElementProcessor<PsiMethod> {

  private final PsiMethod method;

  public PsiMethodProcessor(final PsiMethod method) {
    this.method = method;
  }

  @Override
  public PsiMethod getElement() {
    return method;
  }

  @Override
  public void process() {
    final var annotations = Arrays.stream(method.getAnnotations())
        .filter(a -> Objects.nonNull(a.getQualifiedName()))
        .filter(a -> a.getQualifiedName().startsWith("io.swagger.annotations"))
        .toList();

    annotations.forEach(this::migrateAnnotation);

    final var parameters = method.getParameterList().getParameters();
    final var paramAnnotations = Arrays.stream(parameters)
        .map(PsiParameter::getAnnotations)
        .flatMap(Arrays::stream)
        .filter(a -> Objects.nonNull(a.getQualifiedName()))
        .filter(a -> a.getQualifiedName().equals(SF_APIPARAM_ANNOTATION))
        .toList();
    paramAnnotations.forEach(PsiMigrationsHelper::migrateApiParam);
  }

  private void migrateAnnotation(final PsiAnnotation annotation) {
    final var qualifiedName = annotation.getQualifiedName();
    switch (qualifiedName) {
      case SF_APIOPERATION_ANNOTATION -> migrateApiOperationAnnotation(method, annotation);
      case SF_APIRESPONSE_ANNOTATION -> migrateApiResponseAnnotation(method, annotation);
      case SF_APIRESPONSES_ANNOTATION -> migrateApiResponsesAnnotation(method, annotation);
      default -> throw new UnsupportedOperationException(
          "Migration of annotation '%s' is not supported on a method".formatted(qualifiedName));
    }
  }
}

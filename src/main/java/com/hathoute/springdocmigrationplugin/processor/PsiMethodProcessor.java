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
import java.util.Set;
import java.util.stream.Collectors;

public class PsiMethodProcessor implements PsiElementProcessor<PsiMethod> {

  private static final Set<String> SF_ANNOTATIONS = Set.of(
      SF_APIOPERATION_ANNOTATION,
      SF_APIRESPONSE_ANNOTATION,
      SF_APIRESPONSES_ANNOTATION
  );

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
        .filter(a -> SF_ANNOTATIONS.contains(a.getQualifiedName()))
        .collect(Collectors.toList());

    annotations.forEach(this::migrateAnnotation);

    final var parameters = method.getParameterList().getParameters();
    final var paramAnnotations = Arrays.stream(parameters)
        .map(PsiParameter::getAnnotations)
        .flatMap(Arrays::stream)
        .filter(a -> a.getQualifiedName().equals(SF_APIPARAM_ANNOTATION))
        .collect(Collectors.toList());
    paramAnnotations.forEach(PsiMigrationsHelper::migrateApiParam);
  }

  private void migrateAnnotation(final PsiAnnotation annotation) {
    final var qualifiedName = annotation.getQualifiedName();
    if (qualifiedName == null) {
      return;
    }

    switch (qualifiedName) {
      case SF_APIOPERATION_ANNOTATION:
        migrateApiOperationAnnotation(method, annotation);
        break;
      case SF_APIRESPONSE_ANNOTATION:
        migrateApiResponseAnnotation(method, annotation);
        break;
      case SF_APIRESPONSES_ANNOTATION:
        migrateApiResponsesAnnotation(method, annotation);
        break;
    }
  }
}

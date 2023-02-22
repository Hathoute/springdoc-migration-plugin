package com.hathoute.springdocmigrationplugin.processor;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.SF_APIMODEL_ANNOTATION;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.SF_API_ANNOTATION;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.migrateApiAnnotation;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.migrateApiModel;

import com.hathoute.springdocmigrationplugin.PsiMigrationsHelper;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PsiClassProcessor implements PsiElementProcessor<PsiClass> {

  private static final Set<String> SF_ANNOTATIONS = Set.of(
      SF_API_ANNOTATION,
      SF_APIMODEL_ANNOTATION
  );

  private final PsiClass psiClass;

  public PsiClassProcessor(final PsiClass psiClass) {
    this.psiClass = psiClass;
  }

  @Override
  public PsiClass getElement() {
    return psiClass;
  }

  @Override
  public void process() {
    final var annotations = Optional.ofNullable(psiClass.getModifierList())
        .map(m -> Arrays.stream(m.getAnnotations()))
        .orElse(Stream.of())
        .filter(a -> SF_ANNOTATIONS.contains(a.getQualifiedName()))
        .collect(Collectors.toList());

    assert annotations.size() < 2;

    annotations.stream().findFirst().ifPresent(a -> {
      if (SF_API_ANNOTATION.equals(a.getQualifiedName())) {
        processController(a);
      } else {
        processModel(a);
      }
    });

  }

  void processController(final PsiAnnotation apiAnnotation) {
    migrateApiAnnotation(psiClass, apiAnnotation);

    final var methods = Arrays.stream(psiClass.getMethods())
        .filter(PsiMigrationsHelper::isEndpointMethod)
        .collect(Collectors.toList());

    methods.stream()
        .map(PsiMethodProcessor::new)
        .forEach(PsiMethodProcessor::process);
  }

  void processModel(final PsiAnnotation apiAnnotation) {
    migrateApiModel(psiClass, apiAnnotation);

    final var fields = Arrays.stream(psiClass.getFields())
        .filter(PsiMigrationsHelper::isModelField)
        .collect(Collectors.toList());

    fields.stream()
        .map(PsiFieldProcessor::new)
        .forEach(PsiFieldProcessor::process);
  }
}

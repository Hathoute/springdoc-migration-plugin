package com.hathoute.springdocmigrationplugin.processor;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.SF_APIMODEL_ANNOTATION;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.SF_API_ANNOTATION;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.migrateApiAnnotation;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.migrateApiModel;

import com.intellij.psi.PsiClass;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class PsiClassProcessor implements PsiElementProcessor<PsiClass> {

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
        .filter(a -> Objects.nonNull(a.getQualifiedName()))
        .filter(a -> a.getQualifiedName().startsWith("io.swagger.annotations"))
        .toList();

    annotations.forEach(a -> {
      switch (a.getQualifiedName()) {
        case SF_API_ANNOTATION -> migrateApiAnnotation(psiClass, a);
        case SF_APIMODEL_ANNOTATION -> migrateApiModel(psiClass, a);
        default -> throw new UnsupportedOperationException(
            "Migration of annotation '%s' is not supported on a class".formatted(
                a.getQualifiedName()));
      }
    });
  }
}

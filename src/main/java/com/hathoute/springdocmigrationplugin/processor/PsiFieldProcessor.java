package com.hathoute.springdocmigrationplugin.processor;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.SF_APIMODELPROPERTY_ANNOTATION;
import static com.hathoute.springdocmigrationplugin.PsiMigrationsHelper.migrateApiModelProperty;

import com.intellij.psi.PsiField;
import java.util.Arrays;
import java.util.Objects;

public class PsiFieldProcessor implements PsiElementProcessor<PsiField> {
  private final PsiField field;

  public PsiFieldProcessor(final PsiField field) {
    this.field = field;
  }

  @Override
  public PsiField getElement() {
    return field;
  }

  @Override
  public void process() {
    final var annotations = Arrays.stream(field.getAnnotations())
        .filter(a -> Objects.nonNull(a.getQualifiedName()))
        .filter(a -> a.getQualifiedName().startsWith("io.swagger.annotations"))
        .toList();

    annotations.forEach(a -> {
      switch (a.getQualifiedName()) {
        case SF_APIMODELPROPERTY_ANNOTATION -> migrateApiModelProperty(field, a);
        default -> throw new UnsupportedOperationException(
            "Migration of annotation '%s' is not supported on a field".formatted(
                a.getQualifiedName()));
      }
    });
  }
}

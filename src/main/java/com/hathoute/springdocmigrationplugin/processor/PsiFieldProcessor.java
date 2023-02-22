package com.hathoute.springdocmigrationplugin.processor;
import com.hathoute.springdocmigrationplugin.PsiMigrationsHelper;
import com.intellij.psi.PsiField;
import java.util.Arrays;
import java.util.stream.Collectors;

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
        .filter(a -> PsiMigrationsHelper.SF_APIMODELPROPERTY_ANNOTATION.equals(a.getQualifiedName()))
        .collect(Collectors.toList());

    annotations.forEach(a -> PsiMigrationsHelper.migrateApiModelProperty(field, a));
  }
}

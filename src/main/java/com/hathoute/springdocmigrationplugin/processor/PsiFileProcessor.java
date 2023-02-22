package com.hathoute.springdocmigrationplugin.processor;
import com.hathoute.springdocmigrationplugin.PsiContext;
import com.hathoute.springdocmigrationplugin.PsiFileDiff;
import com.hathoute.springdocmigrationplugin.PsiMigrationsHelper;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

public class PsiFileProcessor implements PsiElementProcessor<PsiFile> {

  private final PsiFile file;
  private final PsiFileDiff diff;
  public PsiFileProcessor(final PsiFile file) {
    this.file = file;
    diff = PsiFileDiff.getInstance(file);
  }

  public PsiJavaFile getFile() {
    return (PsiJavaFile) file;
  }

  public PsiFileDiff getDiff() {
    return diff;
  }

  @Override
  public PsiFile getElement() {
    return file;
  }

  @Override
  public void process() {
    if (!(file instanceof PsiJavaFile)) {
      return;
    }

    PsiContext.getInstance().setFileProcessor(this);

    final var classes = getFile().getClasses();
    for (final var clazz : classes) {
      final var processor = new PsiClassProcessor(clazz);
      processor.process();
    }

    diff.getSfActions().forEach(Runnable::run);
    PsiMigrationsHelper.removeSpringfoxImports();
    diff.getSdActions().forEach(Runnable::run);
  }
}

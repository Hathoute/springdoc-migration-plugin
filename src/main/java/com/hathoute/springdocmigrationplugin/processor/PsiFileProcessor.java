package com.hathoute.springdocmigrationplugin.processor;
import com.hathoute.springdocmigrationplugin.PsiContext;
import com.hathoute.springdocmigrationplugin.PsiFileDiff;
import com.hathoute.springdocmigrationplugin.PsiMigrationsHelper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiRecursiveElementVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

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

    final var javaFile = getFile();
    final var hasSpringfox = Optional.ofNullable(javaFile.getImportList())
        .map(PsiImportList::getImportStatements)
        .map(Arrays::stream)
        .map(s -> s.map(PsiImportStatement::getQualifiedName)
            .filter(Objects::nonNull)
            .anyMatch(qn -> qn.startsWith("io.swagger.annotations")))
        .orElse(false);

    if (!hasSpringfox) {
      return;
    }

    final var migrationVisitor = new MigrationVisitor();
    javaFile.acceptChildren(migrationVisitor);

    migrationVisitor.processors.forEach(PsiElementProcessor::process);

    diff.getSfActions().forEach(Runnable::run);
    PsiMigrationsHelper.removeSpringfoxImports();
    diff.getSdActions().forEach(Runnable::run);
  }

  static final class MigrationVisitor extends PsiRecursiveElementVisitor {
    private final Collection<PsiElementProcessor<?>> processors;

    private MigrationVisitor() {
      processors = new ArrayList<>();
    }

    @Override
    public void visitElement(@NotNull final PsiElement element) {
      if (element instanceof PsiClass klass) {
        processors.add(new PsiClassProcessor(klass));
        super.visitElement(element);
      } else if (element instanceof PsiMethod method) {
        processors.add(new PsiMethodProcessor(method));
        super.visitElement(element);
      } else if (element instanceof PsiField field) {
        processors.add(new PsiFieldProcessor(field));
        super.visitElement(element);
      }
    }
  }
}

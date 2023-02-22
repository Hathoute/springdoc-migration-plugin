package com.hathoute.springdocmigrationplugin;

import com.hathoute.springdocmigrationplugin.processor.PsiFileProcessor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

public class SpringfoxMigrationAction extends AnAction {


  @Override
  public void actionPerformed(final AnActionEvent anActionEvent) {
    final var project = anActionEvent.getProject();
    WriteCommandAction.runWriteCommandAction(project, () -> {
      final var files = PsiClassFinder.getJavaFiles(project);
      System.out.printf("Processing %d files%n", files.size());
      files.stream()
          .filter(PsiMigrationsHelper::hasSpringfoxImports)
          .map(PsiFileProcessor::new)
          .forEach(PsiFileProcessor::process);
    });
  }


  @Override
  public void update(final AnActionEvent e) {
    final Editor editor = e.getData(CommonDataKeys.EDITOR);
    final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    e.getPresentation().setEnabled(editor != null && psiFile != null);
  }

}

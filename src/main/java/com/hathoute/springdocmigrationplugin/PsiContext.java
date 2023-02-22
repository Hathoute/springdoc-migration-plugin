package com.hathoute.springdocmigrationplugin;
import com.hathoute.springdocmigrationplugin.processor.PsiFileProcessor;

public class PsiContext {

  private PsiFileProcessor fileProcessor;

  private static final PsiContext INSTANCE = new PsiContext();

  public static PsiContext getInstance() {
    return INSTANCE;
  }

  private PsiContext() {
  }

  public void setFileProcessor(final PsiFileProcessor fileProcessor) {
    this.fileProcessor = fileProcessor;
  }

  public PsiFileProcessor currentProcessor() {
    return fileProcessor;
  }
}

package com.hathoute.springdocmigrationplugin.processor;
import com.intellij.psi.PsiElement;

public interface PsiElementProcessor<T extends PsiElement> {
  T getElement();
  void process();
}

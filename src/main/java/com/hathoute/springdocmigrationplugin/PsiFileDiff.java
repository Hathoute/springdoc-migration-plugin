package com.hathoute.springdocmigrationplugin;
import com.intellij.psi.PsiFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PsiFileDiff {

  private static final Map<PsiFile, PsiFileDiff> instances = new HashMap<>();

  public static PsiFileDiff getInstance(final PsiFile file) {
    return instances.computeIfAbsent(file, f -> new PsiFileDiff());
  }

  private final List<Runnable> sfActions = new ArrayList<>();
  private final List<Runnable> sdActions = new ArrayList<>();

  private PsiFileDiff() {
  }

  public void addSfAction(final Runnable action) {
    sfActions.add(action);
  }

  public void addSdAction(final Runnable action) {
    sdActions.add(action);
  }

  public List<Runnable> getSfActions() {
    return Collections.unmodifiableList(sfActions);
  }

  public List<Runnable> getSdActions() {
    return Collections.unmodifiableList(sdActions);
  }
}

package com.hathoute.springdocmigrationplugin;
import com.intellij.psi.PsiFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PsiFileDiff {

  public interface PsiAction {
    void run();
  }

  private static final Map<PsiFile, PsiFileDiff> instances = new HashMap<>();

  public static PsiFileDiff getInstance(final PsiFile file) {
    return instances.computeIfAbsent(file, PsiFileDiff::new);
  }

  private final PsiFile file;
  private final List<PsiAction> sfActions = new ArrayList<>();
  private final List<PsiAction> sdActions = new ArrayList<>();
  private PsiFileDiff(final PsiFile file) {
    this.file = file;
  }

  public synchronized void addSfAction(final PsiAction action) {
    sfActions.add(action);
  }

  public synchronized void addSdAction(final PsiAction action) {
    sdActions.add(action);
  }

  public synchronized List<PsiAction> getSfActions() {
    return Collections.unmodifiableList(sfActions);
  }

  public synchronized List<PsiAction> getSdActions() {
    return Collections.unmodifiableList(sdActions);
  }
}

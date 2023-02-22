package com.hathoute.springdocmigrationplugin;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class PsiClassFinder {

  private PsiClassFinder() {
  }

  private static Collection<VirtualFile> getProjectFiles(final Project project) {
    return FileBasedIndex.getInstance()
        .getContainingFiles(
            FileTypeIndex.NAME,
            JavaFileType.INSTANCE,
            new MyGlobalSearchScope(project));
  }

  public static Collection<PsiJavaFile> getJavaFiles(final Project project) {
    return getProjectFiles(project).stream()
        .map(vf -> PsiManager.getInstance(project).findFile(vf))
        .filter(PsiJavaFile.class::isInstance)
        .map(PsiJavaFile.class::cast)
        .collect(Collectors.toList());
  }

  public static Collection<PsiClass> getProjectClasses(final Project project) {
    return getJavaFiles(project).stream()
        .map(PsiJavaFile::getClasses)
        .flatMap(Arrays::stream)
        .collect(Collectors.toList());
  }

  public static PsiClass findClassEverywhere(final Project project, final String className) {
    return JavaPsiFacade.getInstance(project)
        .findClass(className, GlobalSearchScope.allScope(project));
  }

  private static class MyGlobalSearchScope extends GlobalSearchScope {
    private final ProjectFileIndex index;

    public MyGlobalSearchScope(final Project project) {
      super(project);
      index = ProjectRootManager.getInstance(project).getFileIndex();
    }

    @Override
    public boolean isSearchInModuleContent(@NotNull final Module aModule) {
      return false;
    }

    @Override
    public boolean isSearchInLibraries() {
      return false;
    }

    @Override
    public boolean contains(@NotNull final VirtualFile file) {
      return index.isInSourceContent(file);
    }
  }
}

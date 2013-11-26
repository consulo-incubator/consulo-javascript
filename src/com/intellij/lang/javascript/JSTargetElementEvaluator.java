package com.intellij.lang.javascript;

import com.intellij.codeInsight.TargetElementEvaluator;
import com.intellij.lang.javascript.psi.JSClass;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 *         Date: Oct 13, 2008
 *         Time: 3:53:42 PM
 */
public class JSTargetElementEvaluator implements TargetElementEvaluator {
  public boolean includeSelfInGotoImplementation(@NotNull final PsiElement element) {
    if (element instanceof JSFunction) {
      final PsiElement parent = JSResolveUtil.findParent(element);
      if (parent instanceof JSClass && ((JSClass)parent).isInterface()) return false;
    } else if (element instanceof JSClass) {
      return false;
    }
    return true;
  }

  public PsiElement getElementByReference(final PsiReference ref, final int flags) {
    return null;
  }
}

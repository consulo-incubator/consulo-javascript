/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.lang.javascript.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: max
 * Date: Jan 30, 2005
 * Time: 11:24:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class JSSuperExpressionImpl extends JSExpressionImpl implements JSSuperExpression {
  private PsiReference[] references;

  public JSSuperExpressionImpl(final ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JSElementVisitor) {
      ((JSElementVisitor)visitor).visitJSSuperExpression(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  @Override
  public PsiReference getReference() {
    return getReferences()[0];
  }

  @Override
  @NotNull
  public PsiReference[] getReferences() {
    if (references != null) return references;
    PsiReference[] refs = { new PsiReference() {
      public PsiElement getElement() {
        return JSSuperExpressionImpl.this;
      }

      public TextRange getRangeInElement() {
        return new TextRange(0, getTextLength());
      }

      @Nullable
      public PsiElement resolve() {
        final PsiElement element = findClass();

        if (getElement().getParent() instanceof JSCallExpression &&
            element instanceof JSClass
           ) {
          final JSClass clazz = (JSClass)element;
          final ResolveProcessor processor = new ResolveProcessor(clazz.getName(), JSSuperExpressionImpl.this);
          element.processDeclarations(processor, ResolveState.initial(), clazz, getElement());
          if(processor.getResult() != null) return processor.getResult();
        }

        return element;
      }

      private PsiElement findClass() {
        final JSClass jsClass = PsiTreeUtil.getParentOfType(getElement(), JSClass.class);

        if (jsClass != null) {
          final JSReferenceList extendsList = jsClass.getExtendsList();
          if (extendsList != null) {
            final JSReferenceExpression[] referenceExpressions = extendsList.getExpressions();
            if (referenceExpressions != null && referenceExpressions.length > 0) {
              final ResolveResult[] results = referenceExpressions[0].multiResolve(false);
              return results.length > 0 ? results[0].getElement() : null;
            }
          }
        } else {
          final JSFile jsFile = PsiTreeUtil.getParentOfType(getElement(), JSFile.class);

          if (jsFile != null) {
            return JSResolveUtil.getClassReferenceForXmlFromContext(jsFile);
          }
        }
        return null;
      }

      public String getCanonicalText() {
        return getText();
      }

      public PsiElement handleElementRename(final String newElementName) throws IncorrectOperationException {
        return null;
      }

      public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
        return null;
      }

      public boolean isReferenceTo(final PsiElement element) {
        return false;
      }

      public Object[] getVariants() {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
      }

      public boolean isSoft() {
        return true;
      }
    }
    };
    return references = refs;
  }
}
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
package com.intellij.lang.javascript.surroundWith;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.psi.JSDoWhileStatement;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;

/**
 * Created by IntelliJ IDEA.
 * User: yole
 * Date: 12.07.2005
 * Time: 18:07:02
 * To change this template use File | Settings | File Templates.
 */
public class JSWithDoWhileSurrounder extends JSStatementSurrounder {
  public String getTemplateDescription() {
    return JSBundle.message("javascript.surround.with.do.while");
  }

  protected String getStatementTemplate(final Project project, PsiElement context) {
    return "do { } while (true);";
  }

  protected ASTNode getInsertBeforeNode(final ASTNode statementNode) {
    JSDoWhileStatement stmt = (JSDoWhileStatement) statementNode.getPsi();
    return stmt.getBody().getLastChild().getNode();
  }

  protected TextRange getSurroundSelectionRange(final ASTNode statementNode) {
    JSDoWhileStatement stmt = (JSDoWhileStatement) statementNode.getPsi();
    return stmt.getCondition().getTextRange();
  }
}

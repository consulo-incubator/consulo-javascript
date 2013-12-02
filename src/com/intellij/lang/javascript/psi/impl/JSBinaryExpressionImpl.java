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

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSBinaryExpression;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * Created by IntelliJ IDEA.
 * User: max
 * Date: Jan 30, 2005
 * Time: 11:41:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class JSBinaryExpressionImpl extends JSExpressionImpl implements JSBinaryExpression
{
	private static final TokenSet BINARY_OPERATIONS = TokenSet.orSet(JSTokenTypes.OPERATIONS, JSTokenTypes.RELATIONAL_OPERATIONS);
	private static final TokenSet BINARY_OPERATIONS_WITH_DEFS = TokenSet.create(JSTokenTypes.COMMA, JSTokenTypes.EQ);

	public JSBinaryExpressionImpl(final ASTNode node)
	{
		super(node);
	}

	public JSExpression getLOperand()
	{
		final ASTNode astNode = getNode();
		final ASTNode firstExpression = astNode.findChildByType(JSElementTypes.EXPRESSIONS);
		if(firstExpression != null && astNode.findChildByType(BINARY_OPERATIONS, firstExpression) == null)
		{
			return null; // =a
		}
		return firstExpression != null ? (JSExpression) firstExpression.getPsi() : null;
	}

	public JSExpression getROperand()
	{
		final ASTNode myNode = getNode();
		final ASTNode secondExpression = myNode.findChildByType(JSElementTypes.EXPRESSIONS, myNode.findChildByType(BINARY_OPERATIONS));
		return secondExpression != null ? (JSExpression) secondExpression.getPsi() : null;
	}

	public IElementType getOperationSign()
	{
		final ASTNode operationASTNode = getNode().findChildByType(BINARY_OPERATIONS);
		return operationASTNode != null ? operationASTNode.getElementType() : null;
	}

	public void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof JSElementVisitor)
		{
			((JSElementVisitor) visitor).visitJSBinaryExpression(this);
		}
		else
		{
			visitor.visitElement(this);
		}
	}

	public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent,
			@NotNull PsiElement place)
	{
		final IElementType operationType = getOperationSign();

		if(BINARY_OPERATIONS_WITH_DEFS.contains(operationType))
		{
			final JSExpression loperand = getLOperand();
			final JSExpression roperand = getROperand();

			if(loperand != null)
			{
				return loperand.processDeclarations(processor, state, lastParent, place) && (roperand != null ? roperand.processDeclarations(processor, state,
						lastParent, place) : true);
			}
		}

		return true;
	}
}

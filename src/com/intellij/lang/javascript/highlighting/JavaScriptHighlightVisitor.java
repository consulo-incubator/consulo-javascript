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

package com.intellij.lang.javascript.highlighting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;

public class JavaScriptHighlightVisitor extends JSElementVisitor implements HighlightVisitor
{
	private HighlightInfoHolder myHighlightInfoHolder;

	@Override
	public boolean suitableForFile(@NotNull PsiFile psiFile)
	{
		return psiFile instanceof JSFile;
	}

	@Override
	@RequiredReadAction
	public void visitElement(PsiElement element)
	{
		super.visitElement(element);

		PsiElement parent = element.getParent();
		IElementType elementType = PsiUtilCore.getElementType(element);
		if((elementType == JSTokenTypes.STRING_LITERAL || elementType == JSTokenTypes.IDENTIFIER) && parent instanceof JSProperty && ((JSProperty) parent).getNameIdentifier() == element)
		{
			highlightPropertyName((JSProperty) parent, element);
		}
		else if(elementType == JSTokenTypes.IDENTIFIER)
		{
			addElementHighlight(parent, element);
		}
	}

	@RequiredReadAction
	private void highlightPropertyName(@NotNull JSProperty property, @NotNull PsiElement nameIdentifier)
	{
		final JSExpression expression = property.getValue();
		TextAttributesKey type;

		if(expression instanceof JSFunctionExpression)
		{
			type = JSHighlighter.JS_INSTANCE_MEMBER_FUNCTION;
		}
		else
		{
			type = JSHighlighter.JS_INSTANCE_MEMBER_VARIABLE;
		}
		myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(nameIdentifier).textAttributes(type).create());
	}

	@Override
	@RequiredReadAction
	public void visitJSAttribute(JSAttribute jsAttribute)
	{
		super.visitJSAttribute(jsAttribute);

		myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(jsAttribute).textAttributes(JSHighlighter.JS_METADATA).create());
	}

	@Override
	@RequiredReadAction
	public void visitJSReferenceExpression(JSReferenceExpression element)
	{
		super.visitJSReferenceExpression(element);

		final ResolveResult[] results = element.multiResolve(false);

		PsiElement validResult = null;
		for(ResolveResult result : results)
		{
			if(result.isValidResult())
			{
				validResult = result.getElement();
				break;
			}
		}

		if(validResult == null)
		{
			return;
		}

		PsiElement referenceNameElement = element.getReferenceNameElement();
		if(referenceNameElement == null)
		{
			return;
		}
		addElementHighlight(validResult, referenceNameElement);
	}

	@RequiredReadAction
	private void addElementHighlight(@NotNull PsiElement resolvedElement, @NotNull PsiElement targetForHighlight)
	{
		boolean isStatic = false;
		boolean isMethod = false;
		boolean isFunction = false;
		boolean isVariable = false;
		boolean isField = false;
		TextAttributesKey type = null;

		if(resolvedElement instanceof JSNamedElementProxy)
		{
			final JSNamedElementProxy elementProxy = (JSNamedElementProxy) resolvedElement;
			final JSNamedElementProxy.NamedItemType namedItemType = elementProxy.getType();

			if(namedItemType == JSNamedElementProxy.NamedItemType.AttributeValue)
			{
				type = JSHighlighter.JS_INSTANCE_MEMBER_VARIABLE;
			}
			else
			{
				isStatic = elementProxy.hasProperty(JSNamedElementProxy.Property.Static);

				isMethod = (namedItemType == JSNamedElementProxy.NamedItemType.MemberFunction || namedItemType == JSNamedElementProxy.NamedItemType.FunctionProperty);
				isFunction = namedItemType == JSNamedElementProxy.NamedItemType.Function;
				isVariable = namedItemType == JSNamedElementProxy.NamedItemType.Variable;
				isField = namedItemType == JSNamedElementProxy.NamedItemType.Definition || namedItemType == JSNamedElementProxy.NamedItemType.Property;

				if(namedItemType == JSNamedElementProxy.NamedItemType.FunctionExpression)
				{
					isFunction = true;
				}
			}
		}
		else if(resolvedElement instanceof JSAttributeListOwner)
		{
			final JSAttributeList attributeList = ((JSAttributeListOwner) resolvedElement).getAttributeList();

			if(attributeList != null)
			{
				isStatic = attributeList.hasModifier(JSAttributeList.ModifierType.STATIC);
			}

			isMethod = resolvedElement instanceof JSFunction;
			if(isMethod && !isClass(resolvedElement.getParent()))
			{
				isMethod = false;
				isFunction = true;
			}
		}
		else if(resolvedElement instanceof JSDefinitionExpression)
		{
			final PsiElement parent = resolvedElement.getParent();
			if(parent instanceof JSAssignmentExpression)
			{
				final JSExpression jsExpression = ((JSAssignmentExpression) parent).getROperand();
				if(jsExpression instanceof JSFunctionExpression)
				{
					isMethod = true;
				}
				else
				{
					isField = true;
				}
			}
		}
		else if(resolvedElement instanceof JSProperty)
		{
			final JSExpression expression = ((JSProperty) resolvedElement).getValue();

			if(expression instanceof JSFunctionExpression)
			{
				isMethod = true;
			}
			else
			{
				isField = true;
			}
		}

		if(isMethod)
		{
			if(isStatic)
			{
				type = JSHighlighter.JS_STATIC_MEMBER_FUNCTION;
			}
			else
			{
				type = JSHighlighter.JS_INSTANCE_MEMBER_FUNCTION;
			}
		}
		else if(isFunction)
		{
			type = JSHighlighter.JS_GLOBAL_FUNCTION;
		}
		else if(isVariable)
		{
			type = JSHighlighter.JS_GLOBAL_VARIABLE;
		}
		else if(isField)
		{
			type = JSHighlighter.JS_INSTANCE_MEMBER_VARIABLE;
		}

		if(type == null)
		{
			if(resolvedElement instanceof JSVariable)
			{
				myHighlightInfoHolder.add(buildHighlightForVariable(resolvedElement, targetForHighlight));
			}
			return;
		}

		myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(targetForHighlight).textAttributes(type).create());
	}

	@Nullable
	@RequiredReadAction
	private static HighlightInfo buildHighlightForVariable(@NotNull final PsiElement element, @NotNull final PsiElement markerAddTo)
	{
		TextAttributesKey type;

		if(element instanceof JSParameter)
		{
			type = JSHighlighter.JS_PARAMETER;
		}
		else
		{
			if(isClass(element.getParent().getParent()))
			{
				final JSAttributeList attributeList = ((JSAttributeListOwner) element).getAttributeList();
				final boolean isStatic = attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.STATIC);
				type = isStatic ? JSHighlighter.JS_STATIC_MEMBER_VARIABLE : JSHighlighter.JS_INSTANCE_MEMBER_VARIABLE;
			}
			else
			{
				if(PsiTreeUtil.getParentOfType(element, JSFunction.class) != null)
				{
					type = JSHighlighter.JS_LOCAL_VARIABLE;
				}
				else
				{
					type = JSHighlighter.JS_GLOBAL_VARIABLE;
				}
			}
		}

		if(type == null)
		{
			return null;
		}

		return HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(markerAddTo).textAttributes(type).create();
	}

	@Override
	public void visit(@NotNull PsiElement element)
	{
		element.acceptChildren(this);
	}

	@Override
	public boolean analyze(@NotNull PsiFile psiFile, boolean b, @NotNull HighlightInfoHolder highlightInfoHolder, @NotNull Runnable runnable)
	{
		myHighlightInfoHolder = highlightInfoHolder;
		runnable.run();
		return true;
	}

	@NotNull
	@Override
	public HighlightVisitor clone()
	{
		return new JavaScriptHighlightVisitor();
	}

	@Override
	public int order()
	{
		return 0;
	}

	private static boolean isClass(final PsiElement element)
	{
		return element instanceof JSClass || element instanceof JSFile && element.getContext() != null;
	}
}

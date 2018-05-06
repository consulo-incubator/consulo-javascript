/*
 * Copyright 2000-2005 JetBrains s.r.o
 * Copyright 2013-2015 must-be.org
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

package com.intellij.lang.javascript.psi.impl.reference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.annotations.RequiredReadAction;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.VariantsProcessor;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;

public class JSPropertyNameReference implements PsiReference
{
	private final JSProperty myProperty;
	private final PsiElement myNameIdentifier;

	public JSPropertyNameReference(JSProperty property, PsiElement nameIdentifier)
	{
		this.myProperty = property;
		myNameIdentifier = nameIdentifier;
	}

	@Override
	public PsiElement getElement()
	{
		return myNameIdentifier;
	}

	@Override
	@RequiredReadAction
	public TextRange getRangeInElement()
	{
		int quotesDelta = myNameIdentifier.getNode().getElementType() == JSTokenTypes.STRING_LITERAL ? 1 : 0;
		final int startOffsetInParent = myNameIdentifier.getStartOffsetInParent();
		int startOffset = startOffsetInParent + quotesDelta;
		int endOffset = startOffsetInParent + myNameIdentifier.getTextLength() - quotesDelta;
		if(endOffset <= startOffset)
		{
			return new TextRange(0, myProperty.getTextLength());
		}
		return new TextRange(startOffset, endOffset);
	}

	@Override
	@Nullable
	public PsiElement resolve()
	{
		return myProperty;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public String getCanonicalText()
	{
		return StringUtil.stripQuotesAroundValue(myNameIdentifier.getText());
	}

	@Override
	public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException
	{
		myProperty.setName(newElementName);
		return null;
	}

	@Override
	public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	@RequiredReadAction
	public boolean isReferenceTo(PsiElement element)
	{
		final PsiElement element2 = resolve();
		boolean proxyExpanded = false;

		if(element instanceof JSDefinitionExpression)
		{
			final JSExpression expression = ((JSDefinitionExpression) element).getExpression();
			if(expression instanceof JSReferenceExpression)
			{
				return ((JSReferenceExpression) expression).isReferenceTo(element2);
			}
		}

		if(element != element2 && element instanceof JSProperty && element2 instanceof JSProperty)
		{
			return ((JSProperty) element).getName().equals(((JSProperty) element2).getName());
		}
		return proxyExpanded && element == element2;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public Object[] getVariants()
	{
		final VariantsProcessor processor = new VariantsProcessor(null, myProperty.getContainingFile(), false, myProperty);

		JSResolveUtil.processGlobalSymbols(myProperty, processor);

		return processor.getResult();
	}

	@Override
	public boolean isSoft()
	{
		return true;
	}
}

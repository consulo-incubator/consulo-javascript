/*
 * Copyright 2000-2005 JetBrains s.r.o
 * Copyright 2013-2016 must-be.org
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

package com.intellij.lang.javascript.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.javascript.lang.psi.JavaScriptType;
import org.mustbe.consulo.javascript.psi.JSComputedName;
import com.intellij.psi.PsiElement;

/**
 * @author max
 * @since 7:39:29 PM Jan 30, 2005
 */
public interface JSProperty extends JSNamedElement
{
	@Nullable
	@RequiredReadAction
	JSExpression getValue();

	@Nullable
	@RequiredReadAction
	PsiElement getColonElement();

	@Nullable
	@RequiredReadAction
	JSComputedName getComputedName();

	@NotNull
	@RequiredReadAction
	JavaScriptType getType();
}

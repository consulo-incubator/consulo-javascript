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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.extensions.CompositeExtensionPointName;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.psi.PsiReference;

/**
 * @author VISTALL
 * @since 02.12.2015
 */
public interface JSPropertyNameReferenceProvider
{
	CompositeExtensionPointName<JSPropertyNameReferenceProvider> EP_NAME = CompositeExtensionPointName.applicationPoint("org.mustbe.consulo.javascript.propertyNameReferenceProvider",
			JSPropertyNameReferenceProvider.class);

	@Nullable
	@RequiredReadAction
	PsiReference getReference(@NotNull JSProperty property);
}

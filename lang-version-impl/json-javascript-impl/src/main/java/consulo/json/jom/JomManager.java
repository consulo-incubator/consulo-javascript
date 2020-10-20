/*
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

package consulo.json.jom;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import consulo.annotation.access.RequiredReadAction;
import consulo.json.JsonFileType;
import consulo.util.dataholder.Key;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jakarta.inject.Singleton;

/**
 * @author VISTALL
 * @since 10.11.2015
 */
@Singleton
public class JomManager
{
	@Nonnull
	public static JomManager getInstance(@Nonnull Project project)
	{
		return ServiceManager.getService(project, JomManager.class);
	}

	private static final Key<CachedValue<JomFileElement<?>>> JOM_FILE_ELEMENT = Key.create("jom.file.lement");

	private final Project myProject;

	@Inject
	public JomManager(Project project)
	{
		myProject = project;
	}

	@Nullable
	@RequiredReadAction
	@SuppressWarnings("unchecked")
	public <T extends JomElement> JomFileElement<T> getFileElement(@Nonnull final PsiFile psiFile)
	{
		FileType fileType = psiFile.getFileType();
		if(fileType != JsonFileType.INSTANCE)
		{
			return null;
		}

		CachedValue<JomFileElement<?>> cachedValue = psiFile.getUserData(JOM_FILE_ELEMENT);
		if(cachedValue == null)
		{
			cachedValue = CachedValuesManager.getManager(myProject).createCachedValue(new CachedValueProvider<JomFileElement<?>>()
			{
				@Nullable
				@Override
				public Result<JomFileElement<?>> compute()
				{
					JomFileElement<?> value = null;
					JomFileDescriptor fileDescriptor = findFileDescriptor(psiFile);
					if(fileDescriptor != null)
					{
						value = new JomFileElement((JSFile) psiFile, fileDescriptor);
					}
					return Result.<JomFileElement<?>>create(value, psiFile, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
				}
			}, false);

			psiFile.putUserData(JOM_FILE_ELEMENT, cachedValue);
		}

		return (JomFileElement<T>) cachedValue.getValue();
	}

	@Nullable
	private static JomFileDescriptor findFileDescriptor(@Nonnull PsiFile psiFile)
	{
		JomFileDescriptor jomFileDescriptor = null;
		for(JomFileDescriptor temp : JomFileDescriptor.EP_NAME.getExtensions())
		{
			if(temp.isMyFile(psiFile))
			{
				jomFileDescriptor = temp;
				break;
			}
		}
		return jomFileDescriptor;
	}
}

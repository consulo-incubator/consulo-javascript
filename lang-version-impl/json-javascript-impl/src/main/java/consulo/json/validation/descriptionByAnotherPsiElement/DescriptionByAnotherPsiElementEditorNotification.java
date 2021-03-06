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

package consulo.json.validation.descriptionByAnotherPsiElement;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiModificationTrackerImpl;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import consulo.editor.notifications.EditorNotificationProvider;
import consulo.json.JsonFileType;
import consulo.json.jom.JomElement;
import consulo.json.jom.JomFileElement;
import consulo.json.jom.JomManager;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.dataholder.Key;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 12.11.2015
 */
public class DescriptionByAnotherPsiElementEditorNotification<T extends PsiElement> implements EditorNotificationProvider<EditorNotificationPanel>
{
	private Key<EditorNotificationPanel> myPanelKey;
	private Project myProject;
	private DescriptionByAnotherPsiElementProvider<T> myProvider;

	public DescriptionByAnotherPsiElementEditorNotification(@Nonnull Project project, @Nonnull DescriptionByAnotherPsiElementProvider<T> provider)
	{
		myProject = project;
		myProvider = provider;

		myPanelKey = Key.create("DescriptionByAnotherPsiElementEditorNotification." + provider.getId());
	}

	@Nonnull
	@Override
	public Key<EditorNotificationPanel> getKey()
	{
		return myPanelKey;
	}

	@Nullable
	@Override
	@RequiredUIAccess
	public EditorNotificationPanel createNotificationPanel(@Nonnull final VirtualFile file, @Nonnull FileEditor fileEditor)
	{
		if(file.getFileType() != JsonFileType.INSTANCE)
		{
			return null;
		}

		final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
		if(psiFile == null)
		{
			return null;
		}

		JomFileElement<JomElement> fileElement = JomManager.getInstance(myProject).getFileElement(psiFile);
		if(fileElement != null)
		{
			return null;
		}

		if(!myProvider.isAvailable(myProject))
		{
			return null;
		}

		String registeredPsiElementId = DescriptionByAnotherPsiElementService.getInstance(myProject).getRegisteredPsiElementId(file);
		if(registeredPsiElementId == null)
		{
			EditorNotificationPanel panel = new EditorNotificationPanel();
			panel.text(StringUtil.SINGLE_QUOTER.fun(myProvider.getId()) + " model description is available for this file");
			panel.createActionLabel("Choose " + myProvider.getPsiElementName(), new Runnable()
			{
				@Override
				@RequiredUIAccess
				public void run()
				{
					T chooseElement = myProvider.chooseElement(myProject);
					if(chooseElement == null)
					{
						return;
					}

					DescriptionByAnotherPsiElementService.getInstance(myProject).registerFile(file, chooseElement, myProvider);

					wantUpdate(psiFile);
				}
			});
			return panel;
		}
		else
		{
			EditorNotificationPanel panel = new EditorNotificationPanel();
			panel.text(StringUtil.SINGLE_QUOTER.fun(myProvider.getId()) + " model description is registered for this file. " + myProvider.getPsiElementName() + ": " + registeredPsiElementId);
			panel.createActionLabel("Cancel", new Runnable()
			{
				@Override
				public void run()
				{
					if(DescriptionByAnotherPsiElementService.getInstance(myProject).removeFile(file))
					{
						wantUpdate(psiFile);
					}
				}
			});
			return panel;
		}
	}

	private void wantUpdate(PsiFile psiFile)
	{
		((PsiModificationTrackerImpl) PsiModificationTracker.SERVICE.getInstance(myProject)).incCounter();

		DaemonCodeAnalyzer.getInstance(myProject).restart(psiFile);

		EditorNotifications.getInstance(psiFile.getProject()).updateNotifications(psiFile.getVirtualFile());
	}
}

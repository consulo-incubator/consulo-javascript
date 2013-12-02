package com.intellij.lang.javascript.findUsages;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageGroup;
import com.intellij.usages.UsageView;
import com.intellij.usages.impl.FileStructureGroupRuleProvider;
import com.intellij.usages.rules.PsiElementUsage;
import com.intellij.usages.rules.UsageGroupingRule;

/**
 * @author Maxim.Mossienko
 */
abstract class JavaScriptGroupRuleProviderBase<T extends JSNamedElement> implements FileStructureGroupRuleProvider
{
	@Nullable
	public UsageGroupingRule getUsageGroupingRule(final Project project)
	{
		return new UsageGroupingRule()
		{
			@Nullable
			public UsageGroup groupUsage(final Usage usage)
			{
				if(usage instanceof PsiElementUsage)
				{
					PsiElement psiElement = ((PsiElementUsage) usage).getElement();

					if(!psiElement.getLanguage().isKindOf(JavaScriptSupportLoader.JAVASCRIPT.getLanguage()))
					{
						return null;
					}
					final JSNamedElement element = PsiTreeUtil.getParentOfType(psiElement, getUsageClass());

					if(isAcceptableElement(element))
					{
						return createUsageGroup((T) element);
					}
				}
				return null;
			}
		};
	}

	protected boolean isAcceptableElement(JSNamedElement element)
	{
		return element != null;
	}

	protected abstract Class<? extends JSNamedElement> getUsageClass();

	protected abstract UsageGroup createUsageGroup(final T t);

	/**
	 * @author Maxim.Mossienko
	 */
	abstract static class PsiNamedElementUsageGroupBase<T extends PsiNamedElement & NavigationItem> implements UsageGroup
	{
		private SmartPsiElementPointer myElementPointer;
		private String myName;
		private Icon myIcon;

		PsiNamedElementUsageGroupBase(@NotNull T element, Icon icon)
		{
			myIcon = icon;

			myName = element.getName();
			if(myName == null)
			{
				myName = "<anonymous>";
			}
			myElementPointer = SmartPointerManager.getInstance(element.getProject()).createLazyPointer(element);
		}

		public Icon getIcon(boolean isOpen)
		{
			return myIcon;
		}

		public T getElement()
		{
			return (T) myElementPointer.getElement();
		}

		@NotNull
		public String getText(UsageView view)
		{
			return myName;
		}

		public FileStatus getFileStatus()
		{
			return isValid() ? FileStatusManager.getInstance(getElement().getProject()).getStatus(getElement().getContainingFile().getVirtualFile()) : null;
		}

		public boolean isValid()
		{
			final T element = getElement();
			return element != null && element.isValid();
		}

		public void navigate(boolean focus) throws UnsupportedOperationException
		{
			if(canNavigate())
			{
				getElement().navigate(focus);
			}
		}

		public boolean canNavigate()
		{
			return isValid();
		}

		public boolean canNavigateToSource()
		{
			return canNavigate();
		}

		public void update()
		{
		}

		public int compareTo(final UsageGroup o)
		{
			return myName.compareTo(((PsiNamedElementUsageGroupBase) o).myName);
		}

		public boolean equals(final Object obj)
		{
			if(!(obj instanceof PsiNamedElementUsageGroupBase))
			{
				return false;
			}
			PsiNamedElementUsageGroupBase group = (PsiNamedElementUsageGroupBase) obj;
			if(isValid() && group.isValid())
			{
				return getElement().getManager().areElementsEquivalent(getElement(), group.getElement());
			}
			return Comparing.equal(myName, ((PsiNamedElementUsageGroupBase) obj).myName);
		}

		public int hashCode()
		{
			return myName.hashCode();
		}

		public void calcData(final DataKey key, final DataSink sink)
		{
			if(!isValid())
			{
				return;
			}
			if(LangDataKeys.PSI_ELEMENT == key)
			{
				sink.put(LangDataKeys.PSI_ELEMENT, getElement());
			}
			if(UsageView.USAGE_INFO_KEY == key)
			{
				T element = getElement();
				if(element != null)
				{
					sink.put(UsageView.USAGE_INFO_KEY, new UsageInfo(element));
				}
			}
		}
	}
}
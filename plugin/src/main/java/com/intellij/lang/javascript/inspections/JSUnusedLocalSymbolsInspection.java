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

package com.intellij.lang.javascript.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.highlighting.JavaScriptLineMarkerProvider;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.JSClassImpl;
import com.intellij.lang.javascript.psi.resolve.JSImportHandlingUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import consulo.util.dataholder.Key;
import gnu.trove.THashSet;
import gnu.trove.TObjectIntHashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import javax.annotation.Nonnull;
import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

/**
 * @author Maxim.Mossienko
 */
public class JSUnusedLocalSymbolsInspection extends JSInspection
{
	private static final Logger LOG = Logger.getInstance("JSUnusedLocalSymbols");
	@NonNls
	public static final String SHORT_NAME = "JSUnusedLocalSymbols";

	@Override
	@Nonnull
	public String getGroupDisplayName()
	{
		return JavaScriptBundle.message("js.inspection.group.name");
	}

	@Override
	@Nonnull
	public String getDisplayName()
	{
		return JavaScriptBundle.message("js.unused.local.symbol.inspection.name");
	}

	@Override
	@Nonnull
	@NonNls
	public String getShortName()
	{
		return SHORT_NAME;
	}

	private static final Key<Set<PsiElement>> ourUnusedLocalDeclarationsSetKey = Key.create("js unused local dcls key");
	private static final Key<Set<PsiElement>> ourUsedLocalDeclarationsSetKey = Key.create("js used local functions key");

	@Override
	protected JSElementVisitor createVisitor(final ProblemsHolder holder)
	{
		return new JSElementVisitor()
		{
			@Override
			public void visitJSVariable(final JSVariable node)
			{
				handleLocalDeclaration(node);
			}

			@Override
			public void visitJSParameterList(final JSParameterList node)
			{
				PsiElement parent = node.getParent();
				final Set<PsiElement> set = parent.getUserData(ourUnusedLocalDeclarationsSetKey);

				if(set == null)
				{
					parent.putUserData(ourUnusedLocalDeclarationsSetKey, Collections.synchronizedSet(new THashSet<PsiElement>(3)));
				}
				else if(node.getParameters().length == 0)
				{
					set.clear();
				}
			}

			@Override
			public void visitFile(final PsiFile file)
			{
				final Set<PsiElement> set = file.getUserData(ourUnusedLocalDeclarationsSetKey);
				if(set != null)
				{
					set.clear();
				}
			}

			@Override
			public void visitJSParameter(final JSParameter node)
			{
				final PsiElement scopeNode = PsiTreeUtil.getParentOfType(node, JSFunction.class, JSCatchBlock.class);

				if(scopeNode == null || scopeNode instanceof JSCatchBlock)
				{
					return;
				}
				// TODO: calculate more accurately right here (we depend in this place for slow marker pass to finish before)
				if(scopeNode.getUserData(JavaScriptLineMarkerProvider.ourParticipatesInHierarchyKey) != null)
				{
					return;
				}
				if(scopeNode instanceof JSFunction)
				{
					JSAttributeList attributeList = ((JSFunction) scopeNode).getAttributeList();
					if(attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.OVERRIDE))
					{
						return;
					}
				}

				final Set<PsiElement> unusedParametersSet;
				final PsiElement parameterList = node.getParent();

				if(parameterList.getNode().findChildByType(JSElementTypes.FORMAL_PARAMETER) == node.getNode())
				{
					unusedParametersSet = Collections.synchronizedSet(new THashSet<PsiElement>(3));
					scopeNode.putUserData(ourUnusedLocalDeclarationsSetKey, unusedParametersSet);
				}
				else
				{
					unusedParametersSet = scopeNode.getUserData(ourUnusedLocalDeclarationsSetKey);
					if(unusedParametersSet == null)
					{
						return;
					}
				}
				unusedParametersSet.add(node);
			}

			@Override
			public void visitJSReferenceExpression(final JSReferenceExpression node)
			{
				if(node.getParent() instanceof JSFunction)
				{
					return;
				}
				if(node.getQualifier() == null)
				{
					if("arguments".equals(node.getText()))
					{
						JSFunction function = PsiTreeUtil.getParentOfType(node, JSFunction.class);
						if(function == null)
						{
							return;
						}
						Set<PsiElement> unusedParametersSet = function.getUserData(ourUnusedLocalDeclarationsSetKey);
						if(unusedParametersSet == null)
						{
							return;
						}
						for(JSParameter p : function.getParameterList().getParameters())
						{
							unusedParametersSet.remove(p);
						}
						return;
					}

					ResolveResult[] results = node.multiResolve(false);

					for(ResolveResult r : results)
					{
						final PsiElement element = r.getElement();

						if(element instanceof JSVariable || (element instanceof JSFunction && isSupportedFunction((JSFunction) element)))
						{
							assert !(element instanceof JSFunctionExpression);
							PsiElement scopeHandler = PsiTreeUtil.getParentOfType(element, JSFunction.class);

							if(scopeHandler != null)
							{
								Set<PsiElement> unusedParametersSet = scopeHandler.getUserData(ourUnusedLocalDeclarationsSetKey);

								if(unusedParametersSet != null)
								{
									final boolean removed = unusedParametersSet.remove(element);

									if(!removed)
									{
										Set<PsiElement> set = scopeHandler.getUserData(ourUsedLocalDeclarationsSetKey);
										if(set == null)
										{
											set = new THashSet<PsiElement>(3);
											scopeHandler.putUserData(ourUsedLocalDeclarationsSetKey, set);
										}
										set.add(element);
									}
								}
							}
						}
					}
				}
			}

			@Override
			public void visitJSFunctionExpression(final JSFunctionExpression node)
			{
				visitJSFunctionDeclaration((JSFunction) node);
			}

			@Override
			public void visitJSFunctionDeclaration(final JSFunction node)
			{
				processDeclarationHost(node, holder);
				handleLocalDeclaration(node);
			}
		};
	}

	private static boolean isSupportedFunction(final JSFunction element)
	{
		return !(element instanceof JSFunctionExpression || element.getParent() instanceof JSProperty);
	}

	private static void processDeclarationHost(final PsiElement node, final ProblemsHolder holder)
	{
		Set<PsiElement> unusedDeclarationsSet = node.getUserData(ourUnusedLocalDeclarationsSetKey);
		if(unusedDeclarationsSet == null || node instanceof JSFunction && ((JSFunction) node).getBody().length == 0)
		{
			return;
		}

		try
		{
			unusedDeclarationsSet = new THashSet<PsiElement>(unusedDeclarationsSet);

			final int nonCounted = -2;
			int lastUsedParameterIndex = nonCounted;
			TObjectIntHashMap<JSParameter> parameterIndexMap = null;

			for(final PsiElement p : unusedDeclarationsSet)
			{
				if(!p.isValid())
				{
					continue;
				}
				final @NonNls @PropertyKey(resourceBundle = JavaScriptBundle.BUNDLE) String messageId;
				final @Nonnull PsiElement highlightedElement;

				if(p instanceof JSParameter)
				{
					// There are cases of predefined sinatures for which we are not interested in reported unused parameters
					final boolean ecma = node.getContainingFile().getLanguage() == JavaScriptSupportLoader.ECMA_SCRIPT_L4;
					if(ecma && node instanceof JSFunctionExpression)
					{
						continue; // do not report unused parameters
					}
					else if(ecma && node instanceof JSFunction)
					{
						final JSParameter[] params = ((JSFunction) node).getParameterList().getParameters();

						if(params.length == 1)
						{
							@NonNls String type = ((JSParameter) p).getTypeString();
							if(type != null)
							{
								type = JSImportHandlingUtil.resolveTypeName(type, p);
							}

							if(type != null)
							{
								String eventType = "flash.events.Event";
								if(eventType.equals(type))
								{
									continue;
								}
								final PsiElement clazz = JSClassImpl.findClassFromNamespace(type, node);

								if(clazz instanceof JSClass)
								{
									final ResolveProcessor processor = new ResolveProcessor(eventType)
									{
										{
											setTypeContext(true);
											setToProcessMembers(false);
											setToProcessHierarchy(true);
										}

										@Override
										public boolean execute(final PsiElement element, final ResolveState state)
										{
											if(!(element instanceof JSClass))
											{
												return true;
											}
											return !myName.equals(((JSClass) element).getQualifiedName());
										}
									};
									processor.setLocalResolve(true);
									final boolean b = clazz.processDeclarations(processor, ResolveState.initial(), clazz, clazz);
									if(!b)
									{
										continue;
									}
								}
							}
						}
					}

					final JSParameter[] params = ((JSFunction) node).getParameterList().getParameters();

					if(lastUsedParameterIndex == nonCounted)
					{
						BitSet unusedSet = new BitSet(params.length);
						parameterIndexMap = new TObjectIntHashMap<JSParameter>();
						for(int i = 0; i < params.length; ++i)
						{
							parameterIndexMap.put(params[i], i);
						}

						for(PsiElement param : unusedDeclarationsSet)
						{
							if(!(param instanceof JSParameter))
							{
								continue;
							}
							unusedSet.set(parameterIndexMap.get((JSParameter) param));
						}

						lastUsedParameterIndex = -1;

						for(int i = params.length - 1; i >= 0; --i)
						{
							if(!unusedSet.get(i))
							{
								lastUsedParameterIndex = i;
								break;
							}
						}
					}

					if(parameterIndexMap.get((JSParameter) p) < lastUsedParameterIndex)
					{
						continue; // no sense to report unused symbol before used since it will change signature
					}

					messageId = "js.unused.parameter";
					highlightedElement = ((JSParameter) p).getNameIdentifier();
				}
				else if(p instanceof JSFunction)
				{
					final PsiElement nameIdentifier = ((JSFunction) p).getNameIdentifier();
					if(nameIdentifier == null)
					{
						continue;
					}
					highlightedElement = nameIdentifier;
					messageId = "js.unused.function.declaration";
				}
				else
				{
					highlightedElement = ((JSVariable) p).getNameIdentifier();
					messageId = "js.unused.local.variable";
				}

				if(p.getParent() instanceof JSCatchBlock)
				{
					holder.registerProblem(highlightedElement, JavaScriptBundle.message(messageId), ProblemHighlightType.LIKE_UNUSED_SYMBOL);
				}
				else
				{
					holder.registerProblem(highlightedElement, JavaScriptBundle.message(messageId), ProblemHighlightType.LIKE_UNUSED_SYMBOL, new RemoveElementLocalQuickFix());
				}
			}
		}
		finally
		{
			node.putUserData(ourUnusedLocalDeclarationsSetKey, null);
		}
	}

	private static void handleLocalDeclaration(final JSNamedElement node)
	{
		if(node instanceof JSFunction && !isSupportedFunction((JSFunction) node))
		{
			return;
		}
		final PsiElement scopeNode = PsiTreeUtil.getParentOfType(node, JSFunction.class, JSCatchBlock.class);
		if(scopeNode == null)
		{
			return;
		}
		final Set<PsiElement> unusedParametersSet = scopeNode.getUserData(ourUnusedLocalDeclarationsSetKey);
		final Set<PsiElement> usedSet = scopeNode.getUserData(ourUsedLocalDeclarationsSetKey);
		if(usedSet != null && usedSet.contains(node))
		{
			return;
		}
		if(unusedParametersSet == null)
		{
			return;
		}
		unusedParametersSet.add(node);
	}

	@Override
	@Nonnull
	public HighlightDisplayLevel getDefaultLevel()
	{
		return HighlightDisplayLevel.WARNING;
	}

	private static class RemoveElementLocalQuickFix implements LocalQuickFix
	{
		@Override
		@Nonnull
		public String getName()
		{
			return JavaScriptBundle.message("js.unused.symbol.remove");
		}

		@Override
		@Nonnull
		public String getFamilyName()
		{
			return getName();
		}

		@Override
		public void applyFix(@Nonnull final Project project, @Nonnull final ProblemDescriptor descriptor)
		{
			try
			{
				PsiElement element = descriptor.getPsiElement();
				if(!(element instanceof JSNamedElement))
				{
					element = element.getParent();
				}
				element.delete();
			}
			catch(IncorrectOperationException e)
			{
				LOG.error(e);
			}
		}
	}
}
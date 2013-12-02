/*
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: Nov 15, 2006
 * Time: 4:48:46 PM
 */
package com.intellij.javascript.documentation;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.TokenType;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;

public class JSDocumentationUtils
{
	private static final
	@NonNls
	Pattern ourDojoParametersPattern = Pattern.compile("^\\s*(\\w+):(.*)$");
	private static final
	@NonNls
	Pattern ourJSDocParametersPattern = Pattern.compile("^\\s*@param\\s*(?:\\{([^\\}]+)\\}\\s*)?(\\w+)?(?:\\s:\\s(\\S+))?(?:\\s*\\[(\\w+)(?:\\.(\\w+))?" +
			"(?:=([^\\]]*))?\\])?(.*)$");
	private static final
	@NonNls
	Pattern ourJSDocEventPattern = Pattern.compile("^\\s*@event\\s*(\\w+)(?:\\s:\\s(\\S+))?(.*)$");
	private static final
	@NonNls
	Pattern ourJSDocRemarkPattern = Pattern.compile("^\\s*@remarks (.*)$");
	private static final
	@NonNls
	Pattern ourJSDocMethodPattern = Pattern.compile("^\\s*@method\\s*(\\w+)(.*)$");
	private static final
	@NonNls
	Pattern ourJSDocClassPattern = Pattern.compile("^\\s*@class\\s*(\\w+(?:\\.\\w+)*)(.*)$");
	private static final
	@NonNls
	Pattern ourJSDocDeprecatedPattern = Pattern.compile("^\\s*@deprecated\\s*(.*)$");

	private static final
	@NonNls
	Pattern ourJSDocConstructorPattern = Pattern.compile("^\\s*@constructor$");
	private static final
	@NonNls
	Pattern ourJSDocFinalPattern = Pattern.compile("^\\s*@final$");
	private static final
	@NonNls
	Pattern ourJSDocPrivatePattern = Pattern.compile("^\\s*@private$");
	private static final
	@NonNls
	Pattern ourJSDocPublicPattern = Pattern.compile("^\\s*@public$");
	private static final
	@NonNls
	Pattern ourJSDocProtectedPattern = Pattern.compile("^\\s*@protected$");

	private static final
	@NonNls
	Pattern ourJSDocOptionalPattern = Pattern.compile("^\\s*@optional(.*)$");
	private static final
	@NonNls
	Pattern ourJSDocStaticPattern = Pattern.compile("^\\s*@static$");
	private static final
	@NonNls
	Pattern ourJSDocSeePattern = Pattern.compile("^\\s*@see (.*)$");
	private static final
	@NonNls
	Pattern ourJSDocDescriptionPattern = Pattern.compile("^\\s*@description\\s*(.+)$");
	private static final
	@NonNls
	Pattern ourJSDocReturnPattern = Pattern.compile("^\\s*@return(?:s)?\\s*(?:(?:\\{|:)?\\s*([^\\s\\}]+)\\s*\\}?\\s*)?(.*)$");
	private static final
	@NonNls
	Pattern ourJSDocNamespacePattern = Pattern.compile("^\\s*@namespace\\s*([\\w\\.]+)(.*)$");

	private static final
	@NonNls
	Pattern ourJSDocPropertyPattern = Pattern.compile("^\\s*@property\\s*(\\w+)(.*)$");
	private static final
	@NonNls
	Pattern ourJSDocTypePattern = Pattern.compile("^\\s*@type\\s*\\{?([^\\s\\}]+)\\}?(.*)$");
	private static final
	@NonNls
	Pattern ourJSDocRequiresPattern = Pattern.compile("^\\s*@requires\\s*(\\S+)(.*)$");
	private static final
	@NonNls
	Pattern ourJSDocDefaultPattern = Pattern.compile("^\\s*@default\\s*(.*)$");
	private static final
	@NonNls
	Pattern ourJSDocExtendsPattern = Pattern.compile("^\\s*@extends\\s*(.*)$");

	private static final
	@NonNls
	Map<Pattern, String> patternToHintMap = new HashMap<Pattern, String>();
	private static final
	@NonNls
	Map<Pattern, JSDocumentationProcessor.MetaDocType> patternToMetaDocTypeMap = new HashMap<Pattern, JSDocumentationProcessor.MetaDocType>();

	static
	{
		patternToHintMap.put(ourDojoParametersPattern, ":");
		patternToMetaDocTypeMap.put(ourDojoParametersPattern, JSDocumentationProcessor.MetaDocType.PARAMETER);
		patternToHintMap.put(ourJSDocParametersPattern, "@pa");
		patternToMetaDocTypeMap.put(ourJSDocParametersPattern, JSDocumentationProcessor.MetaDocType.PARAMETER);

		patternToHintMap.put(ourJSDocMethodPattern, "@m");
		patternToMetaDocTypeMap.put(ourJSDocMethodPattern, JSDocumentationProcessor.MetaDocType.METHOD);

		patternToHintMap.put(ourJSDocOptionalPattern, "@o");
		patternToMetaDocTypeMap.put(ourJSDocOptionalPattern, JSDocumentationProcessor.MetaDocType.OPTIONAL_PARAMETERS);

		patternToHintMap.put(ourJSDocEventPattern, "@ev");
		patternToMetaDocTypeMap.put(ourJSDocEventPattern, JSDocumentationProcessor.MetaDocType.EVENT);

		patternToHintMap.put(ourJSDocExtendsPattern, "@ex");
		patternToMetaDocTypeMap.put(ourJSDocExtendsPattern, JSDocumentationProcessor.MetaDocType.EXTENDS);

		patternToHintMap.put(ourJSDocRemarkPattern, "@rem");
		patternToMetaDocTypeMap.put(ourJSDocRemarkPattern, JSDocumentationProcessor.MetaDocType.NOTE);

		patternToHintMap.put(ourJSDocReturnPattern, "@ret");
		patternToMetaDocTypeMap.put(ourJSDocReturnPattern, JSDocumentationProcessor.MetaDocType.RETURN);

		patternToHintMap.put(ourJSDocPublicPattern, "@pu");
		patternToMetaDocTypeMap.put(ourJSDocPublicPattern, JSDocumentationProcessor.MetaDocType.PUBLIC);

		patternToHintMap.put(ourJSDocProtectedPattern, "@prot");
		patternToMetaDocTypeMap.put(ourJSDocProtectedPattern, JSDocumentationProcessor.MetaDocType.PROTECTED);

		patternToHintMap.put(ourJSDocStaticPattern, "@st");
		patternToMetaDocTypeMap.put(ourJSDocStaticPattern, JSDocumentationProcessor.MetaDocType.STATIC);

		patternToHintMap.put(ourJSDocSeePattern, "@se");
		patternToMetaDocTypeMap.put(ourJSDocSeePattern, JSDocumentationProcessor.MetaDocType.SEE);

		patternToHintMap.put(ourJSDocDescriptionPattern, "@des");
		patternToMetaDocTypeMap.put(ourJSDocDescriptionPattern, JSDocumentationProcessor.MetaDocType.DESCRIPTION);

		patternToHintMap.put(ourJSDocDeprecatedPattern, "@dep");
		patternToMetaDocTypeMap.put(ourJSDocDeprecatedPattern, JSDocumentationProcessor.MetaDocType.DEPRECATED);

		patternToHintMap.put(ourJSDocConstructorPattern, "@co");

		patternToMetaDocTypeMap.put(ourJSDocConstructorPattern, JSDocumentationProcessor.MetaDocType.CONSTRUCTOR);

		patternToHintMap.put(ourJSDocClassPattern, "@cl");
		patternToMetaDocTypeMap.put(ourJSDocClassPattern, JSDocumentationProcessor.MetaDocType.CLASS);
		patternToHintMap.put(ourJSDocPrivatePattern, "@pri");
		patternToMetaDocTypeMap.put(ourJSDocPrivatePattern, JSDocumentationProcessor.MetaDocType.PRIVATE);

		patternToHintMap.put(ourJSDocNamespacePattern, "@n");
		patternToMetaDocTypeMap.put(ourJSDocNamespacePattern, JSDocumentationProcessor.MetaDocType.NAMESPACE);

		patternToHintMap.put(ourJSDocPropertyPattern, "@prop");
		patternToHintMap.put(ourJSDocTypePattern, "@t");
		patternToMetaDocTypeMap.put(ourJSDocTypePattern, JSDocumentationProcessor.MetaDocType.TYPE);

		patternToHintMap.put(ourJSDocFinalPattern, "@f");
		patternToMetaDocTypeMap.put(ourJSDocFinalPattern, JSDocumentationProcessor.MetaDocType.FINAL);
		patternToHintMap.put(ourJSDocRequiresPattern, "@req");
		patternToMetaDocTypeMap.put(ourJSDocRequiresPattern, JSDocumentationProcessor.MetaDocType.REQUIRES);

		patternToHintMap.put(ourJSDocDefaultPattern, "@def");
		patternToMetaDocTypeMap.put(ourJSDocDefaultPattern, JSDocumentationProcessor.MetaDocType.DEFAULT);
	}

	private static final
	@NonNls
	Map<Pattern, String> prefixToPatternToHintMap = new HashMap<Pattern, String>();

	static
	{
		prefixToPatternToHintMap.put(Pattern.compile("^\\s*description:(.*)$"), "descr");

		prefixToPatternToHintMap.put(Pattern.compile("^ summary(?:\\:)?(.*)$"), "summ");

		prefixToPatternToHintMap.put(Pattern.compile("^\\s*\\*(?:\\*)?(.*)$"), "*");

		prefixToPatternToHintMap.put(Pattern.compile("^[/]+(.*)$"), "/");

		prefixToPatternToHintMap.put(Pattern.compile("^\\s*Parameters:(.*)$"), "Parame");
	}

	public static void processDocumentationTextFromComment(ASTNode _initialComment, JSDocumentationProcessor processor)
	{
		ASTNode prev = _initialComment.getTreePrev();

		if(prev != null && prev.getPsi() instanceof OuterLanguageElement)
		{
			while(prev.getPsi() instanceof OuterLanguageElement)
			{
				prev = prev.getTreePrev();
			}
		}
		else
		{
			prev = null;
		}

		if(prev != null && !(prev.getPsi() instanceof PsiComment))
		{
			prev = null;
		}

		final ASTNode initialComment = prev != null ? prev : _initialComment;
		Enumeration<Object> commentLineIterator;

		if(initialComment.getElementType() == JSTokenTypes.END_OF_LINE_COMMENT)
		{
			commentLineIterator = new Enumeration<Object>()
			{
				ASTNode commentNode = initialComment;

				public boolean hasMoreElements()
				{
					return commentNode != null;
				}

				public String nextElement()
				{
					ASTNode resultCommentNode = commentNode;
					commentNode = commentNode.getTreeNext();

					if(commentNode != null && commentNode.getElementType() == TokenType.WHITE_SPACE)
					{
						commentNode = commentNode.getTreeNext();
					}

					if(commentNode != null && commentNode.getElementType() != JSTokenTypes.END_OF_LINE_COMMENT)
					{
						commentNode = null;
					}

					final String text = resultCommentNode.getText();
					if(text.startsWith("//"))
					{
						return text.substring(2);
					}
					return "";
				}
			};
		}
		else
		{
			String text = initialComment.getText();
			text = unwrapCommentDelimiters(text);

			commentLineIterator = new StringTokenizer(text, "\r\n");
		}

		final boolean needPlainCharData = processor.needsPlainCommentData();

		while(commentLineIterator.hasMoreElements())
		{
			final String s = (String) commentLineIterator.nextElement();
			if(s.indexOf('@') == -1 && s.indexOf(':') == -1 && !needPlainCharData)
			{
				continue;
			}

			String commentText = s.replace('\t', ' ');

			for(Map.Entry<Pattern, String> entry : prefixToPatternToHintMap.entrySet())
			{
				final Matcher matcher = commentText.indexOf(entry.getValue()) != -1 ? entry.getKey().matcher(commentText) : null;
				if(matcher == null)
				{
					continue;
				}

				if(matcher.matches())
				{
					commentText = matcher.group(1);
					break;
				}
			}

			boolean matchedSomething = false;

			for(Map.Entry<Pattern, String> entry : patternToHintMap.entrySet())
			{
				final Matcher matcher = commentText.indexOf(entry.getValue()) != -1 ? entry.getKey().matcher(commentText) : null;
				if(matcher == null)
				{
					continue;
				}

				if(matcher.matches())
				{
					final JSDocumentationProcessor.MetaDocType docType = patternToMetaDocTypeMap.get(entry.getKey());
					if(docType != null)
					{
						final int groupCount = matcher.groupCount();
						String remainingLineContent = groupCount > 0 ? matcher.group(groupCount) : null;
						String matchName = groupCount > 1 ? matcher.group(1) : null;
						String matchValue = groupCount > 2 ? matcher.group(2) : null;

						boolean reportAboutOptionalParameter = false;
						boolean reportAboutFieldInParameter = false;
						boolean reportAboutDefaultValue = false;

						final int groupForInitialValue = 6;
						final int groupForFieldName = 5;
						String fieldName = null;

						if(groupCount == 7 && entry.getKey() == ourJSDocParametersPattern)
						{
							String paramNameInBracket = matcher.group(4);

							if(paramNameInBracket != null)
							{
								String tmp = matchName;
								matchName = paramNameInBracket;
								matchValue = tmp;
								reportAboutFieldInParameter = (fieldName = matcher.group(groupForFieldName)) != null;
								reportAboutOptionalParameter = true;
								reportAboutDefaultValue = matcher.group(groupForInitialValue) != null;
							}
							else
							{
								String typeAfterParamName = matcher.group(3);
								if(typeAfterParamName != null)
								{
									matchName = matchValue;
									matchValue = typeAfterParamName;
								}
								else
								{
									String tmp = matchValue;
									matchValue = matchName;
									matchName = tmp;
								}
							}
						}

						String matched = entry.getKey().pattern();

						if(reportAboutFieldInParameter)
						{
							if(!processor.onPatternMatch(JSDocumentationProcessor.MetaDocType.FIELD, matchName, null, matcher.group(groupForFieldName), commentText, matched))

							{
								break;
							}
						}
						else
						{
							if(!processor.onPatternMatch(docType, matchName, matchValue, remainingLineContent, commentText, matched))
							{
								break;
							}
						}
						if(reportAboutOptionalParameter)
						{
							if(!processor.onPatternMatch(JSDocumentationProcessor.MetaDocType.OPTIONAL_PARAMETERS, matchName, fieldName, null, commentText, matched))
							{
								break;
							}
						}

						if(reportAboutDefaultValue)
						{
							if(!processor.onPatternMatch(JSDocumentationProcessor.MetaDocType.DEFAULT, matchName, fieldName, matcher.group(groupForInitialValue), commentText, matched))
							{
								break;
							}
						}
					}
					matchedSomething = true;
					break;
				}
			}

			if(!matchedSomething && needPlainCharData)
			{
				if(!processor.onCommentLine(commentText))
				{
					break;
				}
			}
		}
	}

	public static String unwrapCommentDelimiters(String text)
	{
		if(text.startsWith("/**"))
		{
			text = text.substring(3);
		}
		else if(text.startsWith("/*") || text.startsWith("//"))
		{
			text = text.substring(2);
		}

		if(text.endsWith("*/"))
		{
			text = text.substring(0, text.length() - 2);
		}
		return text;
	}

	static
	@Nullable
	ASTNode findTrailingCommentInFunctionBody(final @NotNull JSFunction function)
	{
		final ASTNode block = function.getNode().findChildByType(JSElementTypes.BLOCK_STATEMENT);
		if(block == null)
		{
			return null;
		}

		for(ASTNode prev = block.getLastChildNode(); prev != null; prev = prev.getTreePrev())
		{
			if(prev.getElementType() == JSElementTypes.RETURN_STATEMENT)
			{
				return block.findChildByType(JSTokenTypes.COMMENTS, prev);
			}
			else if(JSElementTypes.STATEMENTS.contains(prev.getElementType()))
			{
				break;
			}
		}
		return null;
	}

	static
	@Nullable
	ASTNode findLeadingCommentInFunctionBody(final @NotNull PsiElement element)
	{
		final ASTNode functionNode = element.getNode();
		final ASTNode block = functionNode.findChildByType(JSElementTypes.BLOCK_STATEMENT);
		if(block == null)
		{
			return null;
		}

		for(ASTNode node = block.getFirstChildNode().getTreeNext(); node != null; node = node.getTreeNext())
		{
			final IElementType nodeType = node.getElementType();

			if(nodeType != TokenType.WHITE_SPACE)
			{
				if(JSTokenTypes.COMMENTS.contains(nodeType))
				{
					return node;
				}
				return null;
			}
		}

		return null;
	}

	public static PsiElement findDocComment(PsiElement element)
	{
		return findDocComment(element, null);
	}

	public static PsiElement findDocComment(PsiElement element, PsiElement context)
	{
		PsiElement docComment = null;
		boolean skippedExprStatementOnce = false;

		if(element instanceof JSAttributeListOwner && context == null)
		{
			final JSAttributeListOwner jsClass = (JSAttributeListOwner) element;
			final JSAttributeList attributeList = jsClass.getAttributeList();
			PsiElement anchor = null;

			if(attributeList != null)
			{
				for(ASTNode currentNode = attributeList.getNode().getLastChildNode(); currentNode != null; currentNode = currentNode.getTreePrev())
				{
					final IElementType nodeType = currentNode.getElementType();

					if(!JSTokenTypes.MODIFIERS.contains(nodeType) &&
							nodeType != JSTokenTypes.WHITE_SPACE &&
							nodeType != JSElementTypes.REFERENCE_EXPRESSION // namespace
							)
					{
						final ASTNode nextNode = currentNode.getTreeNext();
						if(nextNode != null)
						{
							anchor = nextNode.getPsi();
						}
						break;
					}
				}
			}

			if(anchor != null)
			{
				element = anchor;
			}
		}

		if(!element.isValid() || element.getContainingFile() == null)
		{
			return docComment;
		}

		boolean shouldSkipPrevExprStatement = false;
		String propName = getPropertyNameFromExprStatement(element);
		if(propName != null)
		{
			shouldSkipPrevExprStatement = true;
		}

		for(PsiElement prev = element.getPrevSibling(); prev != null; prev = prev.getPrevSibling())
		{
			if(prev instanceof PsiWhiteSpace)
			{
				continue;
			}

			if(prev instanceof PsiComment)
			{
				docComment = prev;
				if(((PsiComment) prev).getTokenType() == JSTokenTypes.DOC_COMMENT)
				{
					break;
				}
			}
			else if(shouldSkipPrevExprStatement &&
					prev instanceof JSExpressionStatement &&
					!skippedExprStatementOnce)
			{
				String propNameFromPrev = getPropertyNameFromExprStatement(prev);
				if(!propName.equals(propNameFromPrev))
				{
					break;
				}
				skippedExprStatementOnce = true; // presumably another accessor definition
				continue;
			}

			break;
		}

		if(docComment != null)
		{
			while(docComment.getPrevSibling() instanceof OuterLanguageElement)
			{
				PsiElement siblingSibling = docComment.getPrevSibling().getPrevSibling();

				if(siblingSibling != null && siblingSibling instanceof PsiComment)
				{
					docComment = siblingSibling;
				}
				else
				{
					break;
				}
			}
		}
		return docComment;
	}

	private static
	@Nullable
	String getPropertyNameFromExprStatement(@NotNull PsiElement element)
	{
		String propName = null;

		if(element instanceof JSExpressionStatement)
		{
			JSExpression expression = ((JSExpressionStatement) element).getExpression();
			if(expression instanceof JSAssignmentExpression)
			{
				JSExpression rOperand = ((JSAssignmentExpression) expression).getROperand();
				if(rOperand instanceof JSFunctionExpression)
				{
					String name = ((JSFunctionExpression) rOperand).getName();
					if(name != null && (StringUtil.startsWith(name, "get") || (StringUtil.startsWith(name, "set"))))
					{
						propName = name.substring(3);
					}
				}
			}
		}
		return propName;
	}

	private static String findTypeFromParameter(final JSVariable parameter, final PsiElement docComment)
	{
		if(docComment != null)
		{
			final String[] detectedType = new String[1];

			processDocumentationTextFromComment(docComment.getNode(), new JSDocumentationProcessor()
			{
				final String name = parameter.getName();
				final boolean isparameter = parameter instanceof JSParameter;

				public boolean needsPlainCommentData()
				{
					return false;
				}

				public boolean onCommentLine(@NotNull final String line)
				{
					return true;
				}

				public boolean onPatternMatch(@NotNull final MetaDocType type, @Nullable final String matchName, @Nullable final String matchValue,
						@Nullable final String remainingLineContent, @NotNull final String line, final String patternMatched)
				{
					if(isparameter && type == MetaDocType.PARAMETER && matchName != null && matchName.equals(name))
					{
						detectedType[0] = matchValue;
						return false;
					}
					else if(type == MetaDocType.TYPE)
					{
						detectedType[0] = matchName;
						return false;
					}
					return true;
				}
			});

			return detectedType[0];
		}
		return null;
	}

	public static final TokenSet ourPrimitiveTypeFilter = TokenSet.create(JSTokenTypes.INT_KEYWORD, JSTokenTypes.UINT_KEYWORD,
			JSTokenTypes.VOID_KEYWORD, JSTokenTypes.ANY_IDENTIFIER);
	public static final TokenSet ourTypeFilter = TokenSet.orSet(TokenSet.create(JSElementTypes.REFERENCE_EXPRESSION), ourPrimitiveTypeFilter);

	private static String findDocForAnchor(final PsiElement _anchor, final JSDocumentationProcessor.MetaDocType... expectedTypes)
	{
		PsiElement anchor = _anchor;
		if(_anchor instanceof JSExpression)
		{
			anchor = PsiTreeUtil.getParentOfType(_anchor, JSStatement.class, JSProperty.class);
		}

		if(anchor != null)
		{
			final PsiElement docComment = findDocComment(anchor);

			if(docComment != null)
			{
				final Ref<String> detectedType = new Ref<String>();

				final PsiElement anchor1 = anchor;
				processDocumentationTextFromComment(docComment.getNode(), new JSDocumentationProcessor()
				{
					public boolean needsPlainCommentData()
					{
						return false;
					}

					public boolean onCommentLine(@NotNull final String line)
					{
						return true;
					}

					public boolean onPatternMatch(@NotNull final MetaDocType type, @Nullable final String matchName, @Nullable final String matchValue,
							@Nullable final String remainingLineContent, @NotNull final String line, final String patternMatched)
					{
						for(MetaDocType expectedType : expectedTypes)
						{
							if(type == expectedType)
							{
								if(type == MetaDocType.TYPE && _anchor instanceof JSFunction)
								{
									final JSParameterList jsParameterList = ((JSFunction) _anchor).getParameterList();
									if(jsParameterList != null && jsParameterList.getParameters().length > 0)
									{
										return true;
									}
									if(_anchor.getParent() instanceof JSProperty)
									{
										return true;
									}
								}
								else if(type == MetaDocType.RETURN)
								{
									//if (!isOurDocComment()) return true;
								}

								detectedType.set(matchName);
								return false;
							}
						}
						return true;
					}
				});

				return detectedType.get();
			}
		}
		return null;
	}

	public static String findType(final PsiElement def)
	{
		return findDocForAnchor(def, JSDocumentationProcessor.MetaDocType.TYPE);
	}

	public static boolean isDeprecated(PsiElement element)
	{
		if(element instanceof JSNamedElementProxy)
		{
			return ((JSNamedElementProxy) element).isDeprecated();
		}
		else if(element instanceof JSClass)
		{
			return ((JSClass) element).isDeprecated();
		}
		else if(element instanceof JSFunction)
		{
			return ((JSFunction) element).isDeprecated();
		}
		else if(element instanceof JSVariable)
		{
			return ((JSVariable) element).isDeprecated();
		}
		else if(element instanceof JSNamespaceDeclaration)
		{
			return ((JSNamespaceDeclaration) element).isDeprecated();
		}

		return calculateDeprecated(element);
	}

	public static boolean calculateDeprecated(PsiElement element)
	{
		if(element instanceof JSExpression)
		{
			element = PsiTreeUtil.getParentOfType(element, JSStatement.class, JSProperty.class);
		}

		final PsiElement docComment = element != null ? findDocComment(element) : null;
		if(docComment != null)
		{
			final boolean[] deprecatedStatus = new boolean[1];

			processDocumentationTextFromComment(docComment.getNode(), new JSDocumentationProcessor()
			{
				public boolean needsPlainCommentData()
				{
					return false;
				}

				public boolean onCommentLine(@NotNull final String line)
				{
					return true;
				}

				public boolean onPatternMatch(@NotNull final MetaDocType type, @Nullable final String matchName, @Nullable final String matchValue,
						@Nullable final String remainingLineContent, @NotNull final String line, final String patternMatched)
				{
					if(type == MetaDocType.DEPRECATED)
					{
						deprecatedStatus[0] = true;
						return false;
					}
					return true;
				}
			});

			return deprecatedStatus[0];
		}
		return false;
	}

	public static void appendHyperLinkToElement(@Nullable PsiElement element, String elementName, final StringBuilder buffer,
			final String presentableName, final @Nullable String presentableFileName)
	{
		final PsiFile containingFile = element != null ? element.getContainingFile() : null;
		String fileName = containingFile == null ? null : !JSResolveUtil.isPredefinedFile(containingFile) ? containingFile.getVirtualFile()
				.getPresentableUrl() : containingFile.getViewProvider().getVirtualFile().getName();

		DocumentationManager.createHyperlink(buffer, (fileName != null ? fileName + ":" : "") + elementName + (element != null ? ":" + element
				.getTextOffset() : ""), presentableName + (presentableFileName != null ? " in " + presentableFileName : ""), true);
	}

	private static String evaluateTypeFromParameter(final JSParameter parameter)
	{
		String s = evaluateTypeFromVariable(parameter);

		if(s == null)
		{
			s = findTypeFromParameter(parameter, findFunctionComment(parameter));
		}

		return s;
	}

	private static PsiElement findFunctionComment(JSParameter parameter)
	{
		PsiElement anchor = PsiTreeUtil.getParentOfType(parameter, JSFunction.class);

		if(anchor instanceof JSFunctionExpression)
		{
			anchor = PsiTreeUtil.getParentOfType(anchor, JSStatement.class, JSProperty.class);
		}

		if(anchor != null)
		{
			return findDocComment(anchor);
		}
		return null;
	}

	private static String evaluateTypeFromFunction(final JSFunction function)
	{
		final ASTNode lastCommentInFunctionBody = findTrailingCommentInFunctionBody(function);

		String typeString = null;
		if(lastCommentInFunctionBody != null)
		{
			typeString = unwrapCommentDelimiters(lastCommentInFunctionBody.getText()).trim();
		}

		if(typeString == null)
		{
			typeString = findDocForAnchor(function, JSDocumentationProcessor.MetaDocType.RETURN, JSDocumentationProcessor.MetaDocType.TYPE);
		}
		return typeString;
	}

	private static String evaluateTypeFromVariable(final JSVariable variable)
	{
		PsiElement prevSibling = variable.getFirstChild();
		if(prevSibling != null && prevSibling.getNode().getElementType() == JSTokenTypes.IDENTIFIER)
		{
			prevSibling = variable.getPrevSibling();
		}

		if(prevSibling instanceof PsiWhiteSpace)
		{
			prevSibling = prevSibling.getPrevSibling();
		}

		if(prevSibling instanceof PsiComment && prevSibling.getNode().getElementType() != JSTokenTypes.END_OF_LINE_COMMENT)
		{
			String parameterCommentText = prevSibling.getText();
			parameterCommentText = unwrapCommentDelimiters(parameterCommentText).trim();

			if(parameterCommentText.length() > 0 && (Character.isUpperCase(parameterCommentText.charAt(0)) || parameterCommentText.indexOf(' ') == -1))
			{
				return parameterCommentText;
			}
		}

		if(prevSibling != null && prevSibling.getNode() != null && prevSibling.getNode().getElementType() == JSTokenTypes.VAR_KEYWORD)
		{
			prevSibling = variable.getParent().getPrevSibling();

			if(prevSibling instanceof PsiWhiteSpace)
			{
				prevSibling = prevSibling.getPrevSibling();
			}

			if(prevSibling instanceof PsiComment)
			{
				return findTypeFromParameter(variable, prevSibling);
			}

		}
		return null;
	}

	public static String findTypeFromComments(final JSNamedElement element)
	{
		if(element instanceof JSParameter)
		{
			return evaluateTypeFromParameter((JSParameter) element);
		}
		else if(element instanceof JSVariable)
		{
			return evaluateTypeFromVariable((JSVariable) element);
		}
		else if(element instanceof JSFunction)
		{
			return evaluateTypeFromFunction((JSFunction) element);
		}
		return null;
	}

	public static boolean isSymbolReference(String content)
	{
		int i = content.indexOf(' ');
		if(i == -1)
		{
			return false;
		}
		String text = content.substring(0, i);
		return text.indexOf('.') != -1 || text.indexOf('#') != -1;
	}

	public static boolean findOptionalStatusFromComments(final JSParameter parameter)
	{
		PsiElement docComment = findFunctionComment(parameter);
		if(docComment == null)
		{
			return false;
		}

		final boolean[] detectedType = new boolean[1];

		processDocumentationTextFromComment(docComment.getNode(), new JSDocumentationProcessor()
		{
			final String name = parameter.getName();

			public boolean needsPlainCommentData()
			{
				return false;
			}

			public boolean onCommentLine(@NotNull final String line)
			{
				return true;
			}

			public boolean onPatternMatch(@NotNull final MetaDocType type, @Nullable final String matchName, @Nullable final String matchValue,
					@Nullable final String remainingLineContent, @NotNull final String line, final String patternMatched)
			{
				if(type == MetaDocType.OPTIONAL_PARAMETERS && matchName != null && matchName.equals(name) && matchValue == null)
				{
					detectedType[0] = true;
					return false;
				}

				return true;
			}
		});

		return detectedType[0];
	}
}
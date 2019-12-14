/*
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

package consulo.javascript.lang.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.javascript.ecmascript6.psi.impl.EcmaScript6ElementTypes;
import consulo.javascript.lang.JavaScriptTokenSets;
import consulo.logging.Logger;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 15.02.2016
 */
public class EcmaScript6StatementParsing extends StatementParsing
{
	private static final Logger LOGGER = Logger.getInstance(EcmaScript6StatementParsing.class);

	public EcmaScript6StatementParsing(EcmaScript6ParsingContext context)
	{
		super(context);
	}

	@Override
	public void parseSourceElement(PsiBuilder builder)
	{
		final IElementType tokenType = builder.getTokenType();
		if(tokenType == JSTokenTypes.FUNCTION_KEYWORD)
		{
			getFunctionParsing().parseFunctionDeclaration(builder);
		}
		else if(tokenType == JSTokenTypes.EXPORT_KEYWORD && builder.lookAhead(1) == JSTokenTypes.DEFAULT_KEYWORD)
		{
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();
			builder.advanceLexer();

			parseSourceElement(builder);

			mark.done(EcmaScript6ElementTypes.EXPORT_DEFAULT_ASSIGMENT);
		}
		else if(tokenType == JSTokenTypes.EXPORT_KEYWORD)
		{
			PsiBuilder.Marker mark = builder.mark();
			PsiBuilder.Marker attributeMark = builder.mark();
			builder.advanceLexer();
			attributeMark.done(JSElementTypes.ATTRIBUTE_LIST);

			IElementType nextType = builder.getTokenType();
			if(nextType == JSTokenTypes.FUNCTION_KEYWORD)
			{
				getFunctionParsing().parseFunctionNoMarker(builder, false, mark);
			}
			else if(nextType == JSTokenTypes.CLASS_KEYWORD)
			{
				parseClassWithMarker(builder, mark);
			}
			else if(nextType == JSTokenTypes.VAR_KEYWORD ||
					nextType == JSTokenTypes.CONST_KEYWORD ||
					nextType == JSTokenTypes.LET_KEYWORD)
			{
				parseVarStatementWithMarker(builder, false, mark);
			}
			else
			{
				mark.error("Expected function or variable");
			}
		}
		else
		{
			doParseStatement(builder, true);
		}
	}

	@Override
	protected void doParseStatement(PsiBuilder builder, boolean canHaveClasses)
	{
		if(canHaveClasses)
		{
			if(builder.getTokenType() == JSTokenTypes.IMPORT_KEYWORD)
			{
				parseImportStatement(builder);
				return;
			}
		}
		super.doParseStatement(builder, canHaveClasses);
	}

	private void parseImportStatement(final PsiBuilder builder)
	{
		final PsiBuilder.Marker importStatement = builder.mark();
		try
		{
			builder.advanceLexer();

			boolean wantFrom = true;

			// TODO [VISTALL] specific element
			if(builder.getTokenType() == JSTokenTypes.LBRACE)
			{
				builder.advanceLexer();

				boolean first = true;
				while(!builder.eof())
				{
					if(builder.getTokenType() == JSTokenTypes.RBRACE)
					{
						break;
					}

					if(!first)
					{
						if(builder.getTokenType() == JSTokenTypes.COMMA)
						{
							builder.advanceLexer();
						}
						else
						{
							builder.error("Comma expected");
						}
					}

					first = false;

					if(!getExpressionParsing().parseQualifiedTypeName(builder, false))
					{
						builder.error("Reference expected");
					}
				}

				if(builder.getTokenType() != JSTokenTypes.RBRACE)
				{
					builder.error("'}' expected");
				}
				else
				{
					builder.advanceLexer();
				}
			}
			else if(builder.getTokenType() == JSTokenTypes.ASTERISK)
			{
				builder.advanceLexer();
			}
			else if(builder.getTokenType() == JSTokenTypes.IDENTIFIER)
			{
				getExpressionParsing().parseQualifiedTypeName(builder, false);
			}
			else if(JavaScriptTokenSets.STRING_LITERALS.contains(builder.getTokenType()))
			{
				builder.advanceLexer();
				wantFrom = false;
			}
			else
			{
				builder.error(JavaScriptBundle.message("javascript.parser.message.expected.typename"));
			}

			if(builder.getTokenType() == JSTokenTypes.AS_KEYWORD)
			{
				builder.advanceLexer();

				if(builder.getTokenType() == JSTokenTypes.IDENTIFIER)
				{
					builder.advanceLexer();
				}
				else
				{
					builder.error("Expected identifier");
				}
			}

			if(wantFrom && expectContextKeyword(builder, JSTokenTypes.FROM_KEYWORD))
			{
				advanceContextKeyword(builder, JSTokenTypes.FROM_KEYWORD);

				if(JavaScriptTokenSets.STRING_LITERALS.contains(builder.getTokenType()))
				{
					builder.advanceLexer();
				}
				else
				{
					builder.error("Expecte from target");
				}
			}

			checkForSemicolon(builder);
		}
		finally
		{
			importStatement.done(JSElementTypes.IMPORT_STATEMENT);
		}
	}

	@Override
	protected boolean doParseStatementSub(PsiBuilder builder, boolean canHaveClasses)
	{
		IElementType tokenType = builder.getTokenType();
		if(canHaveClasses)
		{
			if(tokenType == JSTokenTypes.CLASS_KEYWORD)
			{
				parseClass(builder);
				return true;
			}
		}
		return false;
	}

	private void parseClass(final PsiBuilder builder)
	{
		parseClassWithMarker(builder, builder.mark());
	}

	private void parseClassWithMarker(final PsiBuilder builder, final @Nonnull PsiBuilder.Marker clazz)
	{
		builder.advanceLexer();
		if(!JSTokenTypes.IDENTIFIER_TOKENS_SET.contains(builder.getTokenType()))
		{
			builder.error(JavaScriptBundle.message("javascript.parser.message.expected.identifier"));
		}
		else
		{
			builder.advanceLexer();
		}

		if(builder.getTokenType() == JSTokenTypes.EXTENDS_KEYWORD)
		{
			parseReferenceList(builder);
		}

		parseClassBody(builder, BlockType.PACKAGE_OR_CLASS_BODY);
		clazz.done(JSElementTypes.CLASS);
	}

	protected void parseClassBody(final PsiBuilder builder, BlockType type)
	{
		if(builder.getTokenType() != JSTokenTypes.LBRACE)
		{
			builder.error(JavaScriptBundle.message("javascript.parser.message.expected.lbrace"));
			return;
		}

		builder.advanceLexer();
		while(builder.getTokenType() != JSTokenTypes.RBRACE)
		{
			if(builder.eof())
			{
				builder.error(JavaScriptBundle.message("javascript.parser.message.missing.rbrace"));
				return;
			}

			PsiBuilder.Marker attributeListMarker = builder.mark();
			if(expectStaticKeywordExpected(builder))
			{
				attributeListMarker.done(JSElementTypes.ATTRIBUTE_LIST);
			}
			else
			{
				attributeListMarker.drop();
				attributeListMarker = null;
			}

			IElementType tokenType = builder.getTokenType();
			if(tokenType != JSTokenTypes.IDENTIFIER && tokenType != JSTokenTypes.MULT)
			{
				if(attributeListMarker != null)
				{
					attributeListMarker.rollbackTo();
				}

				PsiBuilder.Marker mark = builder.mark();

				if(attributeListMarker != null)
				{
					PsiBuilder.Marker temp = builder.mark();
					expectStaticKeywordExpected(builder);
					temp.done(JSElementTypes.ATTRIBUTE_LIST);
				}

				builder.advanceLexer();
				mark.error("Expected identifier or *");
			}
			else
			{
				if(attributeListMarker != null)
				{
					attributeListMarker.rollbackTo();
				}

				PsiBuilder.Marker mark = builder.mark();

				if(attributeListMarker != null)
				{
					PsiBuilder.Marker temp = builder.mark();
					expectStaticKeywordExpected(builder);
					temp.done(JSElementTypes.ATTRIBUTE_LIST);
				}

				if(builder.getTokenType() == JSTokenTypes.MULT)
				{
					builder.advanceLexer();
				}
				else
				{
					if(expectContextKeyword(builder, JSTokenTypes.GET_SET_TOKEN_SET) != null && builder.lookAhead(1) == JSTokenTypes.IDENTIFIER)
					{
						advanceContextKeyword(builder, JSTokenTypes.GET_SET_TOKEN_SET);
					}
				}

				if(!JSTokenTypes.IDENTIFIER_TOKENS_SET.contains(builder.getTokenType()))
				{
					builder.error(JavaScriptBundle.message("javascript.parser.message.expected.identifier"));
				}
				else
				{
					builder.advanceLexer();
				}

				getFunctionParsing().parseParameterList(builder);
				getStatementParsing().parseFunctionBody(builder);

				checkForSemicolon(builder);

				mark.done(JSElementTypes.FUNCTION_DECLARATION);
			}
		}

		builder.advanceLexer();
	}

	private boolean expectStaticKeywordExpected(PsiBuilder builder)
	{
		IElementType tokenType = builder.getTokenType();
		if(tokenType == JSTokenTypes.STATIC_KEYWORD)
		{
			builder.advanceLexer();
			return true;
		}
		else if(expectContextKeyword(builder, TokenSet.create(JSTokenTypes.STATIC_KEYWORD)) != null)
		{
			if(builder.lookAhead(1) == JSTokenTypes.IDENTIFIER)
			{
				advanceContextKeyword(builder, TokenSet.create(JSTokenTypes.STATIC_KEYWORD));
				return true;
			}
		}
		return false;
	}

	private void parseReferenceList(final PsiBuilder builder)
	{
		final IElementType tokenType = builder.getTokenType();
		LOGGER.assertTrue(tokenType == JSTokenTypes.EXTENDS_KEYWORD || tokenType == JSTokenTypes.IMPLEMENTS_KEYWORD);
		final PsiBuilder.Marker referenceList = builder.mark();
		builder.advanceLexer();

		if(getExpressionParsing().parseQualifiedTypeName(builder))
		{
			while(builder.getTokenType() == JSTokenTypes.COMMA)
			{
				builder.advanceLexer();
				if(!getExpressionParsing().parseQualifiedTypeName(builder))
				{
					break;
				}
			}
		}
		referenceList.done(JSElementTypes.EXTENDS_LIST);
	}
}

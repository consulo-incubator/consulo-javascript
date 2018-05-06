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

package consulo.javascript.lang.parsing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.psi.tree.IElementType;

/**
 * @author max
 */
public class FunctionParsing extends Parsing
{
	public FunctionParsing(JavaScriptParsingContext context)
	{
		super(context);
	}

	public void parseFunctionExpression(PsiBuilder builder)
	{
		parseFunction(builder, true);
	}

	public void parseFunctionDeclaration(PsiBuilder builder)
	{
		parseFunction(builder, false);
	}

	private void parseFunction(PsiBuilder builder, boolean expressionContext)
	{
		parseFunctionNoMarker(builder, expressionContext, builder.mark());
	}

	public void parseFunctionNoMarker(final PsiBuilder builder, final boolean expressionContext, final @Nonnull PsiBuilder.Marker functionMarker)
	{
		if(builder.getTokenType() == JSTokenTypes.FUNCTION_KEYWORD)
		{ // function keyword may be ommited in context of get/set property definition
			builder.advanceLexer();
		}

		// Function name

		final IElementType tokenType = builder.getTokenType();
		if(!expressionContext && (tokenType == JSTokenTypes.GET_KEYWORD || tokenType == JSTokenTypes.SET_KEYWORD))
		{
			builder.advanceLexer();
		}

		if(isIdentifierName(builder, builder.getTokenType()))
		{
			getExpressionParsing().parseQualifiedTypeName(builder, false);
		}
		else
		{
			if(!expressionContext && builder.getTokenType() != JSTokenTypes.LPAR /*get/set as name*/)
			{
				builder.error(JavaScriptBundle.message("javascript.parser.message.expected.function.name"));
			}
		}

		parseParameterList(builder);

		getExpressionParsing().tryParseType(builder);

		if(builder.getTokenType() == JSTokenTypes.SEMICOLON)
		{
			builder.advanceLexer();
		}
		else
		{
			getStatementParsing().parseFunctionBody(builder);
		}

		functionMarker.done(expressionContext ? JSElementTypes.FUNCTION_EXPRESSION : JSElementTypes.FUNCTION_DECLARATION);
	}

	public void parseParameterList(final PsiBuilder builder)
	{
		final PsiBuilder.Marker parameterList;
		if(builder.getTokenType() != JSTokenTypes.LPAR)
		{
			builder.error(JavaScriptBundle.message("javascript.parser.message.expected.lparen"));
			parameterList = builder.mark(); // To have non-empty parameters list at all the time.
			parameterList.done(JSElementTypes.PARAMETER_LIST);
			return;
		}
		else
		{
			parameterList = builder.mark();
			builder.advanceLexer();
		}

		boolean first = true;
		while(builder.getTokenType() != JSTokenTypes.RPAR)
		{
			if(first)
			{
				first = false;
			}
			else
			{
				if(builder.getTokenType() == JSTokenTypes.COMMA)
				{
					builder.advanceLexer();
				}
				else
				{
					builder.error(JavaScriptBundle.message("javascript.parser.message.expected.comma.or.rparen"));
					break;
				}
			}

			parseParameter(builder, null);
		}

		if(builder.getTokenType() == JSTokenTypes.RPAR)
		{
			builder.advanceLexer();
		}

		parameterList.done(JSElementTypes.PARAMETER_LIST);
	}

	public void parseParameter(@Nonnull PsiBuilder builder, @Nullable PsiBuilder.Marker parameterMarker)
	{
		if(parameterMarker == null)
		{
			parameterMarker = builder.mark();
		}

		if(builder.getTokenType() == JSTokenTypes.DOT_DOT_DOT)
		{
			builder.advanceLexer();
		}

		if(builder.getTokenType() == JSTokenTypes.IDENTIFIER)
		{
			builder.advanceLexer();
			getExpressionParsing().tryParseType(builder);
			if(builder.getTokenType() == JSTokenTypes.EQ)
			{
				builder.advanceLexer();
				getExpressionParsing().parseSimpleExpression(builder);
			}
			parameterMarker.done(JSElementTypes.FORMAL_PARAMETER);
		}
		else
		{
			builder.error(JavaScriptBundle.message("javascript.parser.message.expected.formal.parameter.name"));
			parameterMarker.drop();
		}
	}
}

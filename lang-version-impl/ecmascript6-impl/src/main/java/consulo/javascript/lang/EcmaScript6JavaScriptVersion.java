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

package consulo.javascript.lang;

import com.intellij.lang.PsiParser;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.tree.TokenSet;
import consulo.javascript.ecmascript6.psi.impl.resolve.EcmaScript6ResolveHelper;
import consulo.javascript.ide.hightlight.JavaScriptHighlighter;
import consulo.javascript.lang.lexer.JavaScriptFlexAdapter;
import consulo.javascript.lang.lexer._EcmaScript6Lexer;
import consulo.javascript.lang.parsing.EcmaScript6Parser;
import consulo.javascript.lang.psi.impl.resolve.JavaScriptVersionWithHelper;
import consulo.javascript.lang.psi.impl.resolve.ResolveHelper;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 12.12.2015
 */
public class EcmaScript6JavaScriptVersion extends BaseJavaScriptLanguageVersion implements StandardJavaScriptVersions.Marker, JavaScriptVersionWithHelper
{
	@Nonnull
	public static EcmaScript6JavaScriptVersion getInstance()
	{
		return JavaScriptLanguage.INSTANCE.findVersionByClass(EcmaScript6JavaScriptVersion.class);
	}

	@Inject
	public EcmaScript6JavaScriptVersion()
	{
		this("ECMASCRIPT_6");
	}

	protected EcmaScript6JavaScriptVersion(@Nonnull String id)
	{
		super(id);

		addFeature(JavaScriptFeature.CLASS);
		addFeature(JavaScriptFeature.BINARY_LITERAL);
		addFeature(JavaScriptFeature.OCTAL_LITERAL);
		addFeature(JavaScriptFeature.PARAMETER_DEFAULT_VALUE);
		addFeature(JavaScriptFeature.REST_PARAMETER);
		addFeature(JavaScriptFeature.SPREAD_OPERATOR);
	}

	@Nonnull
	@Override
	public String getPresentableName()
	{
		return "ECMAScript 6";
	}

	@Nonnull
	@Override
	public Lexer createLexer()
	{
		return createLexer(false);
	}

	@Nonnull
	@Override
	public SyntaxHighlighter getSyntaxHighlighter()
	{
		return new JavaScriptHighlighter(() -> createLexer(true));
	}

	private Lexer createLexer(boolean hightlight)
	{
		return new MergingLexerAdapter(new JavaScriptFlexAdapter(new _EcmaScript6Lexer(hightlight)), TokenSet.create(JSTokenTypes.XML_JS_SCRIPT));
	}

	@Nonnull
	@Override
	public PsiParser createParser()
	{
		return new EcmaScript6Parser();
	}

	@Override
	public int getWeight()
	{
		return 50;
	}

	@Nonnull
	@Override
	public ResolveHelper getHelper()
	{
		return EcmaScript6ResolveHelper.INSTANCE;
	}
}

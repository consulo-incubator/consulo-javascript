package org.mustbe.consulo.javascript.lang;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.javascript.ide.hightlight.JavaScriptHighlighter;
import org.mustbe.consulo.javascript.lang.lexer.JavaScript15Lexer;
import com.intellij.lang.javascript.JavaScriptParsingLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Factory;

/**
 * @author VISTALL
 * @since 05.03.2015
 */
public class JavaScript15LanguageVersion extends BaseJavaScriptLanguageVersion implements StandardJavaScriptVersions.Marker
{
	private static final Factory<Lexer> ourLexerFactory = new Factory<Lexer>()
	{
		@Override
		public Lexer create()
		{
			return new JavaScript15Lexer();
		}
	};

	@NotNull
	@LazyInstance
	public static JavaScript15LanguageVersion getInstance()
	{
		return JavaScriptLanguage.INSTANCE.findVersionByClass(JavaScript15LanguageVersion.class);
	}

	public JavaScript15LanguageVersion()
	{
		super("JAVASCRIPT_1_5");
	}

	@NotNull
	@Override
	public String getPresentableName()
	{
		return "JavaScript 1.5";
	}
	@NotNull
	@Override
	public Lexer createLexer(@Nullable Project project)
	{
		return new JavaScriptParsingLexer(ourLexerFactory.create(), JavaScript15Lexer.LAST_STATE);
	}

	@NotNull
	@Override
	public SyntaxHighlighter getSyntaxHighlighter()
	{
		return new JavaScriptHighlighter(ourLexerFactory);
	}

	@Override
	public int getWeight()
	{
		return 0;
	}
}
package org.mustbe.consulo.javascript.ide.hightlight;

import org.consulo.fileTypes.LanguageVersionableSyntaxHighlighterFactory;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.javascript.lang.BaseJavaScriptLanguageVersion;
import org.mustbe.consulo.javascript.lang.JavaScriptLanguage;
import com.intellij.lang.LanguageVersion;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;

/**
 * @author VISTALL
 * @since 05.03.2015
 */
public class JavaScriptSyntaxHighlightFactory extends LanguageVersionableSyntaxHighlighterFactory
{
	public JavaScriptSyntaxHighlightFactory()
	{
		super(JavaScriptLanguage.INSTANCE);
	}

	@NotNull
	@Override
	public SyntaxHighlighter getSyntaxHighlighter(@NotNull LanguageVersion languageVersion)
	{
		if(languageVersion instanceof BaseJavaScriptLanguageVersion)
		{
			return ((BaseJavaScriptLanguageVersion) languageVersion).getSyntaxHighlighter();
		}
		throw new IllegalArgumentException(languageVersion.toString());
	}
}

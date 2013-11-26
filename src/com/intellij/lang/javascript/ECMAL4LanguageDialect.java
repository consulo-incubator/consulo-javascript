/*
 * @author max
 */
package com.intellij.lang.javascript;

import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.lang.javascript.highlighting.JSHighlighter;
import org.jetbrains.annotations.NotNull;

public class ECMAL4LanguageDialect extends JSLanguageDialect {
  public static final DialectOptionHolder DIALECT_OPTION_HOLDER = new DialectOptionHolder(true, false);

  public ECMAL4LanguageDialect() {
    super("ECMA Script Level 4");

    SyntaxHighlighterFactory.LANGUAGE_FACTORY.addExplicitExtension(this, new SingleLazyInstanceSyntaxHighlighterFactory() {
      @NotNull
      protected SyntaxHighlighter createHighlighter() {
        return new JSHighlighter(DIALECT_OPTION_HOLDER);
      }
    });
  }

  public String getFileExtension() {
    return JavaScriptSupportLoader.ECMA_SCRIPT_L4_FILE_EXTENSION2;
  }

}
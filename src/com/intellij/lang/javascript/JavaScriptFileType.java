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
package com.intellij.lang.javascript;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: max
 * Date: Jan 27, 2005
 * Time: 6:02:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class JavaScriptFileType extends LanguageFileType {
  public JavaScriptFileType() {
    super(new JavascriptLanguage());
  }

  @NotNull
  public String getName() {
    return "JavaScript";
  }

  @NotNull
  public String getDescription() {
    return JSBundle.message("javascript.filetype.description");
  }

  @NotNull
  public String getDefaultExtension() {
    return "js";
  }

  public Icon getIcon() {
    return IconLoader.getIcon("/fileTypes/javaScript.png");
  }
}

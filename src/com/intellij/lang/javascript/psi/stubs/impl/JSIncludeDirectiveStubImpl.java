package com.intellij.lang.javascript.psi.stubs.impl;

import com.intellij.lang.javascript.psi.JSIncludeDirective;
import com.intellij.lang.javascript.psi.JSStubElementType;
import com.intellij.lang.javascript.psi.impl.JSIncludeDirectiveImpl;
import com.intellij.lang.javascript.psi.stubs.JSIncludeDirectiveStub;
import com.intellij.psi.stubs.*;

import java.io.IOException;

/**
 * @author Maxim.Mossienko
 *         Date: Jun 6, 2008
 *         Time: 8:00:52 PM
 */
public class JSIncludeDirectiveStubImpl extends StubBase<JSIncludeDirective> implements JSIncludeDirectiveStub {
  private String myIncludeText;

  public JSIncludeDirectiveStubImpl(final StubInputStream dataStream, final StubElement parentStub,
                                    final JSStubElementType<JSIncludeDirectiveStub, JSIncludeDirective> type) throws IOException {
    super(parentStub, type);
    final int nameIndex = dataStream.readInt();
    myIncludeText = nameIndex != -1 ? dataStream.stringFromId(nameIndex) : null;
  }

  public JSIncludeDirectiveStubImpl(final JSIncludeDirective psi,
                                        final StubElement parentStub,
                                        final JSStubElementType<JSIncludeDirectiveStub, JSIncludeDirective> type) {
    super(parentStub, type);
    myIncludeText = psi.getIncludeText();
  }

  public JSIncludeDirective createPsi() {
    return new JSIncludeDirectiveImpl(this);
  }

  public void index(final IndexSink sink) {
  }

  public void serialize(final StubOutputStream dataStream) throws IOException {
    dataStream.writeInt(myIncludeText != null ? dataStream.getStringId(myIncludeText):-1);
  }

  public String getIncludeText() {
    return myIncludeText;
  }
}
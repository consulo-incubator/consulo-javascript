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

package consulo.javascript.run.debug;

import org.chromium.sdk.JsDeclarativeVariable;
import org.chromium.sdk.JsEvaluateContext;
import org.chromium.sdk.JsFunction;
import org.chromium.sdk.JsValue;
import org.chromium.sdk.JsVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueModifier;

/**
 * @author VISTALL
 * @since 05.12.2015
 */
public class V8VariableValue extends V8BaseVariableValue
{
	public static void addValue(@NotNull XValueChildrenList valueChildrenList, @NotNull JsEvaluateContext debugContext, @NotNull JsVariable jsVariable)
	{
		JsValue value = jsVariable.getValue();
		if(value instanceof JsFunction)
		{
			return;
		}
		valueChildrenList.add(new V8VariableValue(debugContext, jsVariable));
	}

	private JsVariable myJsVariable;

	public V8VariableValue(@NotNull JsEvaluateContext evaluateContext, @NotNull JsVariable jsVariable)
	{
		super(evaluateContext, jsVariable.getName());
		myJsVariable = jsVariable;
	}

	@Nullable
	@Override
	public XValueModifier getModifier()
	{
		final JsDeclarativeVariable declarativeVariable = myJsVariable.asDeclarativeVariable();
		if(declarativeVariable == null)
		{
			return null;
		}
		final JsValue value = myJsVariable.getValue();
		if(!declarativeVariable.isMutable())
		{
			return null;
		}
		final JsValue.Type valueType = value.getType();
		switch(valueType)
		{
			case TYPE_NUMBER:
			case TYPE_NULL:
			case TYPE_STRING:
			case TYPE_BOOLEAN:
				return new XValueModifier()
				{
					@Override
					public void setValue(@NotNull String expression, @NotNull final XModificationCallback callback)
					{
						JsEvaluateContext.PrimitiveValueFactory valueFactory = myEvaluateContext.getValueFactory();
						JsValue value = null;
						try
						{
							if(expression.equals("null"))
							{
								value = valueFactory.getNull();
							}
							else if(expression.equals("undefined"))
							{
								value = valueFactory.getUndefined();
							}
							else
							{
								switch(valueType)
								{
									case TYPE_NUMBER:
										value = valueFactory.createNumber(expression);
										break;
									case TYPE_STRING:
										value = valueFactory.createString(expression);
										break;
									case TYPE_BOOLEAN:
										value = valueFactory.createBoolean(Boolean.valueOf(expression));
										break;
									case TYPE_ERROR:
										break;
									case TYPE_REGEXP:
										break;
									case TYPE_DATE:
										break;
									case TYPE_ARRAY:
										break;
									case TYPE_UNDEFINED:
										break;
									case TYPE_NULL:
										break;
								}
							}
						}
						catch(Exception e)
						{
							callback.errorOccurred("Bad value");
							return;
						}

						declarativeVariable.setValue(value, new JsDeclarativeVariable.SetValueCallback()
						{
							@Override
							public void success()
							{
								callback.valueModified();
							}

							@Override
							public void failure(Exception e)
							{
								callback.errorOccurred(e.getMessage());
							}
						}, null);
					}

					@Override
					public void calculateInitialValueEditorText(XInitialValueCallback callback)
					{
						callback.setValue(value.getValueString());
					}
				};
			default:
				return null;
		}
	}

	@NotNull
	@Override
	protected JsValue getValue()
	{
		return myJsVariable.getValue();
	}
}
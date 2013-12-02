package com.intellij.lang.javascript.generation;

import org.jetbrains.annotations.NonNls;
import com.intellij.lang.javascript.psi.JSAttributeList;
import com.intellij.lang.javascript.psi.JSClass;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSParameterList;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.validation.BaseCreateMethodsFix;
import com.intellij.psi.PsiElement;

/**
 * @author Maxim.Mossienko
 *         Date: Jul 17, 2008
 *         Time: 9:39:02 PM
 */
public class OverrideMethodsFix extends BaseCreateMethodsFix<JSFunction>
{
	public OverrideMethodsFix(final JSClass jsClass)
	{
		super(jsClass);
	}

	protected String buildFunctionBodyText(final String retType, final JSParameterList parameterList, final JSFunction func)
	{
		@NonNls String functionText = "";
		functionText += "{\n";

		if(!"void".equals(retType))
		{
			functionText += "  return";
		}
		else
		{
			functionText += " ";
		}

		functionText += " super." + func.getName();

		if(func.isGetProperty())
		{

		}
		else if(func.isSetProperty())
		{
			functionText += " = " + parameterList.getParameters()[0].getName();
		}
		else
		{
			functionText += "(";
			boolean first = true;
			for(JSParameter param : parameterList.getParameters())
			{
				if(!first)
				{
					functionText += ",";
				}
				first = false;
				functionText += param.getName();
			}
			functionText += ")";
		}

		functionText += JSChangeUtil.getSemicolon(func.getProject()) + "\n}";
		return functionText;
	}

	protected String buildFunctionAttrText(String attrText, final JSAttributeList attributeList, final JSFunction function)
	{
		attrText = super.buildFunctionAttrText(attrText, attributeList, function);
		final PsiElement element = JSResolveUtil.findParent(function);
		if(attributeList == null || !attributeList.hasModifier(JSAttributeList.ModifierType.OVERRIDE))
		{

			if(element instanceof JSClass && !"Object".equals(((JSClass) element).getQualifiedName()))
			{
				final PsiElement typeElement = attributeList != null ? attributeList.findAccessTypeElement() : null;
				if(typeElement == null)
				{
					attrText += " override";
				}
				else
				{
					final int index = attrText.indexOf(typeElement.getText());
					attrText = attrText.substring(0, index) + ((index > 0) ? " " : "") + "override " + attrText.substring(index);
				}
			}
		}

		return attrText;
	}
}

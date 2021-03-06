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

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.impl.*;
import com.intellij.lang.javascript.psi.stubs.*;
import com.intellij.lang.javascript.types.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.annotation.DeprecationInfo;
import consulo.javascript.ecmascript4.psi.impl.EcmaScript4ElementTypes;
import consulo.javascript.lang.JavaScriptLanguage;
import consulo.javascript.lang.parsing.impl.JavaSciptDestructuringElementImpl;
import consulo.javascript.lang.parsing.impl.JavaSciptDestructuringObjectImpl;
import consulo.javascript.lang.parsing.impl.JavaScriptDestructuringParameterImpl;
import consulo.javascript.lang.parsing.impl.JavaScriptDestructuringShorthandedPropertyImpl;
import consulo.javascript.psi.impl.JSComputedNameImpl;
import consulo.javascript.psi.impl.JavaScriptLambdaExpressionImpl;
import consulo.javascript.psi.impl.JavaScriptSpreadExpressionImpl;
import consulo.javascript.psi.stubs.JSFileStub;
import consulo.psi.tree.ElementTypeAsPsiFactory;

import javax.annotation.Nonnull;

/**
 * Created by IntelliJ IDEA.
 * User: max
 * Date: Jan 28, 2005
 * Time: 12:27:21 AM
 * To change this template use File | Settings | File Templates.
 */
public interface JSElementTypes
{
	IStubFileElementType<JSFileStub> FILE = new JSFileElementType(JavaScriptLanguage.INSTANCE);

	IElementType EMBEDDED_CONTENT = new ILazyParseableElementType("EMBEDDED_CONTENT", JavaScriptLanguage.INSTANCE)
	{
		@Nonnull
		@Override
		public Language getLanguage()
		{
			return JavaScriptLanguage.INSTANCE;
		}
	};
	IElementType EMBEDDED_EXPRESSION = new ElementTypeAsPsiFactory("EMBEDDED_EXPRESSION", JavaScriptLanguage.INSTANCE, JSEmbeddedContentImpl.class);
	JSStubElementType<JSFunctionStub, JSFunction> FUNCTION_DECLARATION = new JSFunctionElementType("FUNCTION_DECLARATION")
	{
		@Override
		public JSFunction createPsi(@Nonnull JSFunctionStub stub)
		{
			return new JSFunctionImpl(stub, this);
		}

		@Nonnull
		@Override
		public PsiElement createElement(@Nonnull ASTNode astNode)
		{
			return new JSFunctionImpl(astNode);
		}
	};
	JSStubElementType<JSParameterListStub, JSParameterList> PARAMETER_LIST = new JSParameterListElementType();

	JSStubElementType<JSParameterStub, JSParameter> FORMAL_PARAMETER = new JSParameterElementType();

	JSStubElementType<JSVariableStub, JSVariable> VARIABLE = new JSVariableElementType();
	IElementType ARGUMENT_LIST = new ElementTypeAsPsiFactory("ARGUMENT_LIST", JavaScriptLanguage.INSTANCE, JSArgumentListImpl.class);

	JSStubElementType<JSAttributeStub, JSAttribute> ATTRIBUTE = new JSAttributeElementType();
	JSStubElementType<JSAttributeNameValuePairStub, JSAttributeNameValuePair> ATTRIBUTE_NAME_VALUE_PAIR = new JSAttributeNameValuePairType();
	JSStubElementType<JSAttributeListStub, JSAttributeList> ATTRIBUTE_LIST = new JSAttributeListElementType();
	JSStubElementType<JSPackageStatementStub, JSPackageStatement> PACKAGE_STATEMENT = new JSPackageStatementElementType();

	@Deprecated
	JSStubElementType<JSImportStatementStub, JSImportStatement> ES4_IMPORT_STATEMENT = EcmaScript4ElementTypes.IMPORT_STATEMENT;

	JSStubElementType<JSClassStub, JSClass> CLASS = new JSClassElementType();
	JSStubElementType<JSReferenceListStub, JSReferenceList> EXTENDS_LIST = new JSReferenceListElementType("EXTENDS_LIST");
	JSStubElementType<JSReferenceListStub, JSReferenceList> IMPLEMENTS_LIST = new JSReferenceListElementType("IMPLEMENTS_LIST");
	JSStubElementType<JSUseNamespaceDirectiveStub, JSUseNamespaceDirective> USE_NAMESPACE_DIRECTIVE = new JSUseNamespaceDirectiveType();
	JSStubElementType<JSIncludeDirectiveStub, JSIncludeDirective> INCLUDE_DIRECTIVE = new JSIncludeDirectiveElementType();
	JSStubElementType<JSNamespaceDeclarationStub, JSNamespaceDeclaration> NAMESPACE_DECLARATION = new JSNamespaceDeclarationElementType();
	IElementType SUPER_EXPRESSION = new ElementTypeAsPsiFactory("SUPER_EXPRESSION", JavaScriptLanguage.INSTANCE, JSSuperExpressionImpl.class);
	IElementType GENERIC_SIGNATURE = new ElementTypeAsPsiFactory("GENERIC_SIGNATURE", JavaScriptLanguage.INSTANCE, JSGenericSignatureImpl.class);

	// Statements
	IElementType BLOCK_STATEMENT = new ElementTypeAsPsiFactory("BLOCK_STATEMENT", JavaScriptLanguage.INSTANCE, JSBlockStatementImpl.class);
	IElementType LABELED_STATEMENT = new ElementTypeAsPsiFactory("LABELED_STATEMENT", JavaScriptLanguage.INSTANCE, JSLabeledStatementImpl.class);
	IElementType EXPRESSION_STATEMENT = new ElementTypeAsPsiFactory("EXPRESSION_STATEMENT", JavaScriptLanguage.INSTANCE,
			JSExpressionStatementImpl.class);
	IElementType YIELD_STATEMENT = new ElementTypeAsPsiFactory("YIELD_STATEMENT", JavaScriptLanguage.INSTANCE, JSYieldStatementImpl.class);
	IElementType LET_STATEMENT = new ElementTypeAsPsiFactory("LET_STATEMENT", JavaScriptLanguage.INSTANCE, JSLetStatementImpl.class);
	JSStubElementType<JSVarStatementStub, JSVarStatement> VAR_STATEMENT = new JSVarStatementElementType();
	IElementType EMPTY_STATEMENT = new ElementTypeAsPsiFactory("EMPTY_STATEMENT", JavaScriptLanguage.INSTANCE, JSEmptyStatementImpl.class);
	IElementType IF_STATEMENT = new ElementTypeAsPsiFactory("IF_STATEMENT", JavaScriptLanguage.INSTANCE, JSIfStatementImpl.class);
	IElementType CONTINUE_STATEMENT = new ElementTypeAsPsiFactory("CONTINUE_STATEMENT", JavaScriptLanguage.INSTANCE, JSContinueStatementImpl.class);
	IElementType BREAK_STATEMENT = new ElementTypeAsPsiFactory("BREAK_STATEMENT", JavaScriptLanguage.INSTANCE, JSBreakStatementImpl.class);
	IElementType DESTRUCTURING_ELEMENT = new ElementTypeAsPsiFactory("DESTRUCTURING_ELEMENT", JavaScriptLanguage.INSTANCE, JavaSciptDestructuringElementImpl.class);
	IElementType DESTRUCTURING_PARAMETER = new ElementTypeAsPsiFactory("DESTRUCTURING_PARAMETER", JavaScriptLanguage.INSTANCE, JavaScriptDestructuringParameterImpl.class);
	IElementType DESTRUCTURING_OBJECT = new ElementTypeAsPsiFactory("DESTRUCTURING_OBJECT", JavaScriptLanguage.INSTANCE, JavaSciptDestructuringObjectImpl.class);
	IElementType DESTRUCTURING_SHORTHANDED_PROPERTY = new ElementTypeAsPsiFactory("DESTRUCTURING_SHORTHANDED_PROPERTY", JavaScriptLanguage.INSTANCE, JavaScriptDestructuringShorthandedPropertyImpl.class);
	IElementType WITH_STATEMENT = new ElementTypeAsPsiFactory("WITH_STATEMENT", JavaScriptLanguage.INSTANCE, JSWithStatementImpl.class);
	IElementType RETURN_STATEMENT = new ElementTypeAsPsiFactory("RETURN_STATEMENT", JavaScriptLanguage.INSTANCE, JSReturnStatementImpl.class);
	IElementType THROW_STATEMENT = new ElementTypeAsPsiFactory("THROW_STATEMENT", JavaScriptLanguage.INSTANCE, JSThrowStatementImpl.class);
	IElementType TRY_STATEMENT = new ElementTypeAsPsiFactory("TRY_STATEMENT", JavaScriptLanguage.INSTANCE, JSTryStatementImpl.class);
	IElementType CATCH_BLOCK = new ElementTypeAsPsiFactory("CATCH_BLOCK", JavaScriptLanguage.INSTANCE, JSCatchBlockImpl.class);
	IElementType CASE_CLAUSE = new ElementTypeAsPsiFactory("CASE_CLAUSE", JavaScriptLanguage.INSTANCE, JSCaseClauseImpl.class);
	IElementType SWITCH_STATEMENT = new ElementTypeAsPsiFactory("SWITCH_STATEMENT", JavaScriptLanguage.INSTANCE, JSSwitchStatementImpl.class);
	IElementType FOR_STATEMENT = new ElementTypeAsPsiFactory("FOR_STATEMENT", JavaScriptLanguage.INSTANCE, JSForStatementImpl.class);
	IElementType FOR_IN_STATEMENT = new ElementTypeAsPsiFactory("FOR_IN_STATEMENT", JavaScriptLanguage.INSTANCE, JSForInStatementImpl.class);
	IElementType WHILE_STATEMENT = new ElementTypeAsPsiFactory("WHILE_STATEMENT", JavaScriptLanguage.INSTANCE, JSWhileStatementImpl.class);
	IElementType DOWHILE_STATEMENT = new ElementTypeAsPsiFactory("DO_WHILE_STATEMENT", JavaScriptLanguage.INSTANCE, JSDoWhileStatementImpl.class);

	@Deprecated
	@DeprecationInfo("Use search by class")
	TokenSet STATEMENTS = TokenSet.create(BLOCK_STATEMENT, LABELED_STATEMENT, VAR_STATEMENT, EMPTY_STATEMENT, IF_STATEMENT, CONTINUE_STATEMENT,
			BREAK_STATEMENT, WITH_STATEMENT, RETURN_STATEMENT, THROW_STATEMENT, TRY_STATEMENT, SWITCH_STATEMENT, FOR_IN_STATEMENT, FOR_STATEMENT,
			WHILE_STATEMENT, DOWHILE_STATEMENT, EXPRESSION_STATEMENT, YIELD_STATEMENT, LET_STATEMENT, ES4_IMPORT_STATEMENT, PACKAGE_STATEMENT,
			USE_NAMESPACE_DIRECTIVE);

	TokenSet SOURCE_ELEMENTS = TokenSet.orSet(STATEMENTS, TokenSet.create(FUNCTION_DECLARATION, CLASS, NAMESPACE_DECLARATION, INCLUDE_DIRECTIVE,
			USE_NAMESPACE_DIRECTIVE));

	// Expressions
	IElementType THIS_EXPRESSION = new ElementTypeAsPsiFactory("THIS_EXPRESSION", JavaScriptLanguage.INSTANCE, JSThisExpressionImpl.class);
	IElementType REFERENCE_EXPRESSION = new ElementTypeAsPsiFactory("REFERENCE_EXPRESSION", JavaScriptLanguage.INSTANCE,
			JSReferenceExpressionImpl.class);
	IElementType LITERAL_EXPRESSION = new ElementTypeAsPsiFactory("LITERAL_EXPRESSION", JavaScriptLanguage.INSTANCE, JSLiteralExpressionImpl.class);
	IElementType XML_LITERAL_EXPRESSION = new ElementTypeAsPsiFactory("XML_LITERAL_EXPRESSION", JavaScriptLanguage.INSTANCE,
			JSXmlLiteralExpressionImpl.class);
	IElementType PARENTHESIZED_EXPRESSION = new ElementTypeAsPsiFactory("PARENTHESIZED_EXPRESSION", JavaScriptLanguage.INSTANCE,
			JSParenthesizedExpressionImpl.class);
	IElementType ARRAY_LITERAL_EXPRESSION = new ElementTypeAsPsiFactory("ARRAY_LITERAL_EXPRESSION", JavaScriptLanguage.INSTANCE,
			JSArrayLiteralExpressionImpl.class);
	IElementType PROPERTY = new ElementTypeAsPsiFactory("PROPERTY", JavaScriptLanguage.INSTANCE, JSPropertyImpl.class);
	IElementType COMPUTED_NAME = new ElementTypeAsPsiFactory("COMPUTED_NAME", JavaScriptLanguage.INSTANCE, JSComputedNameImpl.class);
	IElementType OBJECT_LITERAL_EXPRESSION = new ElementTypeAsPsiFactory("OBJECT_LITERAL_EXPRESSION", JavaScriptLanguage.INSTANCE,
			JSObjectLiteralExpressionImpl.class);
	IElementType ASSIGNMENT_EXPRESSION = new ElementTypeAsPsiFactory("ASSIGNMENT_EXPRESSION", JavaScriptLanguage.INSTANCE,
			JSAssignmentExpressionImpl.class);
	IElementType CONDITIONAL_EXPRESSION = new ElementTypeAsPsiFactory("CONDITIONAL_EXPRESSION", JavaScriptLanguage.INSTANCE,
			JSConditionalExpressionImpl.class);
	IElementType BINARY_EXPRESSION = new ElementTypeAsPsiFactory("BINARY_EXPRESSION", JavaScriptLanguage.INSTANCE, JSBinaryExpressionImpl.class);
	IElementType PREFIX_EXPRESSION = new ElementTypeAsPsiFactory("PREFIX_EXPRESSION", JavaScriptLanguage.INSTANCE, JSPrefixExpressionImpl.class);
	IElementType POSTFIX_EXPRESSION = new ElementTypeAsPsiFactory("POSTFIX_EXPRESSION", JavaScriptLanguage.INSTANCE, JSPostfixExpressionImpl.class);
	IElementType LAMBDA_EXPRESSION = new ElementTypeAsPsiFactory("LAMBDA_EXPRESSION", JavaScriptLanguage.INSTANCE, JavaScriptLambdaExpressionImpl.class);
	IElementType SPREAD_EXPRESSION = new ElementTypeAsPsiFactory("SPREAD_EXPRESSION", JavaScriptLanguage.INSTANCE, JavaScriptSpreadExpressionImpl.class);
	IElementType COMMA_EXPRESSION = new ElementTypeAsPsiFactory("COMMA_EXPRESSION", JavaScriptLanguage.INSTANCE, JSCommaExpressionImpl.class);
	JSStubElementType<JSFunctionStub, JSFunction> FUNCTION_EXPRESSION = new JSFunctionElementType("FUNCTION_EXPRESSION")
	{
		@Override
		public JSFunction createPsi(@Nonnull JSFunctionStub stub)
		{
			return new JSFunctionExpressionImpl(stub, this);
		}

		@Nonnull
		@Override
		public PsiElement createElement(@Nonnull ASTNode astNode)
		{
			return new JSFunctionExpressionImpl(astNode);
		}
	};
	IElementType NEW_EXPRESSION = new ElementTypeAsPsiFactory("NEW_EXPRESSION", JavaScriptLanguage.INSTANCE, JSNewExpressionImpl.class);

	IElementType INDEXED_PROPERTY_ACCESS_EXPRESSION = new ElementTypeAsPsiFactory("INDEXED_PROPERTY_ACCESS_EXPRESSION", JavaScriptLanguage.INSTANCE,
			JSIndexedPropertyAccessExpressionImpl.class);
	IElementType CALL_EXPRESSION = new ElementTypeAsPsiFactory("CALL_EXPRESSION", JavaScriptLanguage.INSTANCE, JSCallExpressionImpl.class);
	IElementType DEFINITION_EXPRESSION = new ElementTypeAsPsiFactory("DEFINITION_EXPRESSION", JavaScriptLanguage.INSTANCE,
			JSDefinitionExpressionImpl.class);
	IElementType LET_EXPRESSION = new ElementTypeAsPsiFactory("LET_EXPRESSION", JavaScriptLanguage.INSTANCE, JSLetExpressionImpl.class);

	TokenSet PARAMETERS = TokenSet.create(FORMAL_PARAMETER, DESTRUCTURING_PARAMETER);

	@Deprecated
	@DeprecationInfo("Use search by class")
	TokenSet EXPRESSIONS = TokenSet.create(THIS_EXPRESSION, REFERENCE_EXPRESSION, LITERAL_EXPRESSION, PARENTHESIZED_EXPRESSION,
			ARRAY_LITERAL_EXPRESSION, OBJECT_LITERAL_EXPRESSION, ASSIGNMENT_EXPRESSION, CONDITIONAL_EXPRESSION, BINARY_EXPRESSION,
			PREFIX_EXPRESSION, POSTFIX_EXPRESSION, COMMA_EXPRESSION, FUNCTION_EXPRESSION, NEW_EXPRESSION, INDEXED_PROPERTY_ACCESS_EXPRESSION,
			CALL_EXPRESSION, DEFINITION_EXPRESSION, XML_LITERAL_EXPRESSION, SUPER_EXPRESSION, LET_EXPRESSION);

}

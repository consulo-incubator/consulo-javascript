package consulo.javascript.ecmascript6.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.psi.impl.JSElementImpl;
import com.intellij.psi.PsiElementVisitor;
import consulo.javascript.ecmascript6.psi.ES6ImportedBinding;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2019-12-14
 */
public class ES6ImportedBindingImpl extends JSElementImpl implements ES6ImportedBinding
{
	public ES6ImportedBindingImpl(ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@Nonnull PsiElementVisitor visitor)
	{
		visitor.visitElement(this);
	}
}

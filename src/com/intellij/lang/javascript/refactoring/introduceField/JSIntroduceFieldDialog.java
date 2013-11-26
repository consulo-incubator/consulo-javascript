package com.intellij.lang.javascript.refactoring.introduceField;

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.refactoring.JSBaseClassBasedIntroduceDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.PsiElement;

import javax.swing.*;

/**
 * @author Maxim.Mossienko
*         Date: Jul 24, 2008
*         Time: 8:46:13 PM
*/
class JSIntroduceFieldDialog extends JSBaseClassBasedIntroduceDialog implements JSIntroduceFieldSettings {
  private JCheckBox myReplaceAllCheckBox;
  private JTextField myNameField;
  private JPanel myPanel;
  private JRadioButton myPublic;
  private JRadioButton myPackageLocal;
  private JRadioButton myProtected;
  private JRadioButton myPrivate;
  private JComboBox myVarType;

  private JRadioButton myFieldDeclaration;
  private JRadioButton myCurrentMethod;
  private JRadioButton myConstructor;

  private static InitializationPlace lastInitializationPlace = InitializationPlace.FieldDeclaration;

  public JSIntroduceFieldDialog(final Project project, final JSExpression[] occurrences, final JSExpression expression) {
    super(project, occurrences, expression, "javascript.introduce.field.title");
    doInit();
  }

  @Override
  protected void doInit() {
    super.doInit();

    final Ref<Boolean> localContextDependent = new Ref<Boolean>();
    myMainOccurence.accept(new JSElementVisitor() {
      @Override
      public void visitJSReferenceExpression(final JSReferenceExpression node) {
        if (node.getQualifier() == null) {
          final ResolveResult[] results = node.multiResolve(true);
          if (results.length == 0) localContextDependent.set(Boolean.TRUE);
          else {
            final PsiElement element = results[0].getElement();
            if (element instanceof JSVariable && !(element.getParent().getParent() instanceof JSClass)) {
              localContextDependent.set(Boolean.TRUE);
            }
          }
        }
        super.visitJSReferenceExpression(node);
      }

      @Override
      public void visitJSElement(final JSElement node) {
        node.acceptChildren(this);
      }
    });

    if (Boolean.TRUE.equals(localContextDependent.get())) {
      myConstructor.setEnabled(false);
      myFieldDeclaration.setEnabled(false);
      myCurrentMethod.setSelected(true);
    } else {
      if (lastInitializationPlace == InitializationPlace.Constructor) myConstructor.setSelected(true);
      else if (lastInitializationPlace == InitializationPlace.CurrentMethod) myCurrentMethod.setSelected(true);
      else myFieldDeclaration.setSelected(true);
    }
  }

  protected JTextField getNameField() {
    return myNameField;
  }

  protected JPanel getPanel() {
    return myPanel;
  }

  protected JCheckBox getReplaceAllCheckBox() {
    return myReplaceAllCheckBox;
  }

  public JComboBox getVarTypeField() {
    return myVarType;
  }

  protected JRadioButton getPrivateRadioButton() {
    return myPrivate;
  }

  protected JRadioButton getPublicRadioButton() {
    return myPublic;
  }

  protected JRadioButton getProtectedRadioButton() {
    return myProtected;
  }

  protected JRadioButton getPackageLocalRadioButton() {
    return myPackageLocal;
  }

  public InitializationPlace getInitializationPlace() {
    return myFieldDeclaration.isSelected() ? InitializationPlace.FieldDeclaration :
           myCurrentMethod.isSelected() ? InitializationPlace.CurrentMethod :
           InitializationPlace.Constructor;
  }

  @Override
  protected void doOKAction() {
    super.doOKAction();
    lastInitializationPlace = getInitializationPlace();
  }

  @Override
  protected String suggestCandidateName(final JSExpression mainOccurence) {
    final String s = super.suggestCandidateName(mainOccurence);
    final JSCodeStyleSettings jsCodeStyleSettings =
        CodeStyleSettingsManager.getSettings(mainOccurence.getProject()).getCustomSettings(JSCodeStyleSettings.class);
    if (jsCodeStyleSettings.FIELD_PREFIX.length() > 0) return jsCodeStyleSettings.FIELD_PREFIX + s;
    if (s.length() > 0) return StringUtil.decapitalize(s);
    return s;
  }
}

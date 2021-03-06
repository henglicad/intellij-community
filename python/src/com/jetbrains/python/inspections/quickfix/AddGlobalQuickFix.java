// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.python.inspections.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.codeInsight.controlflow.ScopeOwner;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * @author oleg
 */
public class AddGlobalQuickFix implements LocalQuickFix {
  @Override
  @NotNull
  public String getFamilyName() {
    return PyBundle.message("QFIX.add.global");
  }

  @Override
  public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
    PsiElement problemElt = descriptor.getPsiElement();
    if (problemElt instanceof PyReferenceExpression) {
      final PyReferenceExpression expression = (PyReferenceExpression)problemElt;
      final String name = expression.getReferencedName();
      final ScopeOwner owner = PsiTreeUtil.getParentOfType(problemElt, ScopeOwner.class);
      assert owner instanceof PyClass || owner instanceof PyFunction : "Add global quickfix is available only inside class or function, but applied for " + owner;
      final Ref<Boolean> added = new Ref<>(false);
      owner.accept(new PyRecursiveElementVisitor(){
        @Override
        public void visitElement(PsiElement element) {
          if (!added.get()){
            super.visitElement(element);
          }
        }
        @Override
        public void visitPyGlobalStatement(final PyGlobalStatement node) {
          if (!added.get()){
            node.addGlobal(name);
            added.set(true);
          }
        }
      });
      if (added.get()){
        return;
      }
      final PyGlobalStatement globalStatement =
        PyElementGenerator.getInstance(project).createFromText(LanguageLevel.getDefault(), PyGlobalStatement.class, "global " + name);
      final PyStatementList statementList;
      boolean hasDocString = false;
      if (owner instanceof PyClass){
        statementList = ((PyClass)owner).getStatementList();
        if (((PyClass)owner).getDocStringExpression() != null) hasDocString = true;
      }
      else {
        statementList = ((PyFunction)owner).getStatementList();
        if (((PyFunction)owner).getDocStringExpression() != null) hasDocString = true;
      }
      PyStatement first = statementList.getStatements()[0];
      if (hasDocString)
        first = statementList.getStatements()[1];
      statementList.addBefore(globalStatement, first);
    }
  }
}

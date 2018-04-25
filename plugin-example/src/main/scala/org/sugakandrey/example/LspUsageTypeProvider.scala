package org.sugakandrey.example

import com.intellij.psi.PsiElement
import com.intellij.usages.impl.rules.{UsageType, UsageTypeProvider}
import LspUsageSearcher.FakeLspPsiElement

/**
  * In your plugin.xml:
  * <extensions defaultExtensionNs="com.intellij">
  *   <usageTypeProvider implementation="org.jetbrains.plugins.scala.findUsages.LspUsageTypeProvider"/>
  * </extensions>
  */
class LspUsageTypeProvider extends UsageTypeProvider {
  import LspUsageTypeProvider._

  override def getUsageType(element: PsiElement): UsageType = element match {
    case _: FakeLspPsiElement => LspUsageType
    case _                    => null
  }
}

object LspUsageTypeProvider {
  private val LspUsageType: UsageType = new UsageType("External LSP Usages")
}

package org.sugakandrey.example

import com.intellij.lang.{ASTNode, Language}
import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.{LocalFileSystem, VirtualFile}
import com.intellij.psi._
import com.intellij.psi.impl.PsiElementBase
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import org.jetbrains.plugins.scala.ScalaLanguage
import org.jetbrains.plugins.scala.extensions._

/**
  * In your plugin.xml:
  * <extensions defaultExtensionNs="com.intellij">
  *   <referencesSearch implementation="org.jetbrains.plugins.scala.findUsages.LspUsageSearcher"/>
  * </extensions>
  */
class LspUsageSearcher
    extends QueryExecutorBase[PsiReference, ReferencesSearch.SearchParameters] {
  import LspUsageSearcher._

  val sourceRoot = "C:\\Users\\jetbrains\\Foo.scala"

  private def lspUsages(element: PsiElement): Seq[LspUsage] =
    Seq(LspUsage(LocalFileSystem.getInstance().findFileByPath(sourceRoot), new TextRange(10, 20), "foobar"))

  override def processQuery(
      param: ReferencesSearch.SearchParameters,
      processor: Processor[PsiReference]
  ): Unit = {
    val elem = param.getElementToSearch
    val project = elem.getProject

    val refs = lspUsages(elem).map { usage =>
      val psiElement = FakeLspPsiElement(usage, project)
      FakeLspReference(psiElement, elem)
    }
    refs.foreach(processor.process)
  }
}

object LspUsageSearcher {
  final case class LspUsage(vfile: VirtualFile, range: TextRange, text: String)

  private def relativeRangeInElement(e: PsiElement): TextRange = {
    val range = e.getTextRange
    range.shiftLeft(range.getStartOffset)
  }

  private final case class FakeLspReference(usage: PsiElement,
                                            refTo: PsiElement)
      extends PsiReferenceBase[PsiElement](usage, relativeRangeInElement(usage)) {
    override def resolve(): PsiElement = usage
    override def getVariants: Array[AnyRef] = Array.empty
  }

  final case class FakeLspPsiElement(usage: LspUsage, project: Project)
      extends PsiElementBase {
    /* you might have to override some more methods if things end up breaking for you */
    override def getContainingFile: PsiFile =
      inReadAction(PsiManager.getInstance(project).findFile(usage.vfile))
    override def getProject: Project = project
    override def getLanguage: Language = ScalaLanguage.INSTANCE
    override def getChildren: Array[PsiElement] = Array.empty
    override def getParent: PsiElement = null
    override def getTextRange: TextRange = usage.range
    override def getStartOffsetInParent: Int = usage.range.getStartOffset
    override def getTextLength: Int = usage.text.length
    override def findElementAt(offset: Int): PsiElement = null
    override def getTextOffset: Int = usage.range.getStartOffset
    override def getText: String = usage.text
    override def textToCharArray(): Array[Char] = usage.text.toCharArray
    override def getNode: ASTNode = null
    override def isValid: Boolean = true
  }
}

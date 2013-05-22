package com.github.nuriaion.test

import com.github.nuriaion.{ScalaFxmlElement, ScalaFxmlTranslator, ScalaFxmlReader}
import org.jcp.xml.dsig.internal.dom.DOMTransform
import org.specs2.matcher.MatchResult

class ScalaFxmlSpec extends org.specs2.Specification with ScalaFxmlTranslator with ScalaFxmlElement { def is =
  "subElement calls" ^
  "top" ! checkGenerateSubElementLink("top", "theTopElement") ^
  "bottom" ! checkGenerateSubElementLink("bottom", "theTopElement") ^
  "left" ! checkGenerateSubElementLink("left", "theTopElement") ^
  "right" ! checkGenerateSubElementLink("right", "theTopElement") ^
  "center" ! checkGenerateSubElementLink("center", "theTopElement") ^
  "children" ! checkGenerateSubElementLink("children", List("someElement"), "content") ^
  "childrens" ! checkGenerateSubElementLink("children", List("someElement", "someOtherElement"), "content") ^
  "no call" ! checkGenerateNoSubElement("huhu") ^
  p^
  "attribute generation" ^
  br^
  "Double" ^
  "height" ! checkAttribute("prefHeight", 412.3) ^
  "width" ! checkAttribute("prefWidth", 300.0) ^
  "-Infinity" ! checkNoAttribute("minHeight", "-Infinity") ^
  "-Infinity" ! checkNoAttribute("minWidth", "-Infinity") ^
  br^ bt^ "String" ^
  "text" ! checkAttribute("text", "someText") ^
  "style" ! checkAttribute("style", "someStyle") ^
  "fx:id" ! checkNoAttribute("fx:id", "someId") ^
  br^ bt^  "Boolean" ^
  "mnemonicParsing" ! checkAttribute("mnemonicParsing", false) ^
  endp^
  p^
  "Single Element Parser" ^
  "a Button" ! checkButton ^
  "a Border Pane" ! checkBorderPane ^
  endp^
  p^
  "Element Tree Parser" ^
  "a Pane with a Button" ! elementTree ^
  end



  import treehugger.forest._
  import definitions._
  import treehuggerDSL._

  def checkNoAttribute[T](id:String, value:T) = {
    (attr(Seq((id, value.toString))):Seq[Tree]).map(treeToString(_)) === Nil
  }

  def checkAttribute[T](id:String, value:T) = {

    val attribute:Tree = REF(id) := LIT(value)
    genAttribute(Seq((id, value.toString))).map(treeToString(_)) === Seq(attribute).map(treeToString(_))
  }

  def checkGenerateNoSubElement(fxmlPane:String) = {
    val subElements = Seq(
      (fxmlPane,
        Seq(Element("", "", Nil, Nil))))
    genSubElementCalls(subElements) === Nil
  }

  def checkGenerateSubElementLink(fxmlPane: String, elementId:Seq[String], scalaPane: String): MatchResult[Any] = {
    val subElements = Seq(
      (fxmlPane,
        elementId.map(Element("someKlassz",
          _,
          Nil,
          Nil))))
    val res:Seq[Tree] = genSubElementCalls(subElements)
    val soll:Seq[Tree] = Seq((REF(scalaPane) := LIST(elementId.map(REF(_)))))
    soll.map(treeToString(_)) === res.map(treeToString(_))
  }

  def checkGenerateSubElementLink(fxmlPane: String, elementId:String): MatchResult[Any] = checkGenerateSubElementLink(fxmlPane, elementId, fxmlPane)
  def checkGenerateSubElementLink(fxmlPane: String, elementId:String, scalaPane: String): MatchResult[Any] = {
    val subElements = Seq(
      (fxmlPane,
        Seq(Element("someKlassz",
                    elementId,
                    Nil,
                    Nil))))
    val res:Seq[Tree] = genSubElementCalls(subElements)
    val soll:Seq[Tree] = Seq((REF(scalaPane) := REF(elementId)))
    soll.map(treeToString(_)) === res.map(treeToString(_))
  }

  val button = Element("Button", "theButton", Seq(("text", "someText")), Nil)
  val borderPane = Element("BorderPane", "aBorderPane",
    Seq(("prefHeight", "400.0"), ("style", "-fx-background-color: black")),
    Seq(("center", Seq(button))))

  def checkButton = {
    val code:Tree =
      VAL("theButton") := NEW(ANONDEF("Button") := BLOCK(
        REF("text") := LIT("someText")
      ))
    treeToString(generateElementCode(button)) === treeToString(code)
  }

  def checkBorderPane = {
    val code:Tree =
      VAL("aBorderPane") := NEW(ANONDEF("BorderPane") := BLOCK(
        REF("center") := REF("theButton"),
        REF("prefHeight") := LIT(400.0),
        REF("style") := LIT("-fx-background-color: black")
      ))
    treeToString(generateElementCode(borderPane)) === treeToString(code)
  }

  def elementTree = {
    val code:Tree =
      PACKAGE("FxmlFiles") := BLOCK (
        TRAITDEF("simple") := BLOCK (
          IMPORT("scalafx.application.JFXApp"),
          IMPORT("scalafx.application.JFXApp.PrimaryStage"),
          IMPORT("scalafx.geometry.Orientation"),
          IMPORT("scalafx.geometry.Pos"),
          IMPORT("scalafx.scene.control.Label"),
          IMPORT("scalafx.scene.control.TextArea"),
          IMPORT("scalafx.scene.control.Button"),
          IMPORT("scalafx.scene.control.SplitPane"),
          IMPORT("scalafx.scene.layout.Priority"),
          IMPORT("scalafx.scene.layout.BorderPane"),
          IMPORT("scalafx.scene.layout.AnchorPane"),
          IMPORT("scalafx.scene.Scene"),
          VAL("theButton") := NEW(ANONDEF("Button") := BLOCK(
            REF("text") := LIT("someText")
          )),
          VAL("aBorderPane") := NEW(ANONDEF("BorderPane") := BLOCK(
            REF("center") := REF("theButton"),
            REF("prefHeight") := LIT(400.0),
            REF("style") := LIT("-fx-background-color: black")
          ))
        )
      )
    treeToString(generateCode("FxmlFiles", "simple", imports, borderPane)) === treeToString(code)
  }
}


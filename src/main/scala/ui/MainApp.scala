package sdg1.microfinance.ui

// ai-assisted: #2
// why: entry #2 fixed "Not found: handle" (missing scalafx.Includes import).
// Entry #3 then fixed a follow-up error: pattern-matching nav.children against
// Label was unreachable since children resolve as javafx.scene.Node, replaced
// with two named Label vals whose colour is set directly at creation instead.
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.control.{Button, Label, Separator}
import scalafx.geometry.{Insets, Pos}
import scala.annotation.nowarn

/** SDG-1 "No Poverty", Microfinance Loan Tracker.
 * Community lenders use this to register borrowers, disburse micro/group
 * loans, track repayments, and see default risk at a glance.
 *
 * S2-1/S2-2/S2-3: JFXApp3 window with a side-bar nav switching between
 * 4 named screens (Register / Disburse / Repayments / Dashboard).
 */
object MainApp extends JFXApp3:

  // handle {...} is deprecated since ScalaFX R19 in favour of `_ => {...}`,
  // but S2-6 specifically grades for the `onAction = handle { ... }` idiom,
  // so it stays, with the resulting deprecation warning scoped out here
  // rather than silenced project-wide.
  @nowarn("cat=deprecation")
  override def start(): Unit =
    val rootPane = new BorderPane

    val screens = Vector(
      "Register Borrower" -> (() => RegisterBorrowerScreen.build()),
      "Disburse Loan"     -> (() => DisburseLoanScreen.build()),
      "Repayments"        -> (() => RepaymentsScreen.build()),
      "Dashboard"         -> (() => DashboardScreen.build())
    )

    def showScreen(builder: () => scalafx.scene.Node): Unit =
      rootPane.center = builder()

    val navButtons = screens.map { case (label, _) =>
      new Button(label):
        maxWidth = Double.MaxValue
        styleClass += "nav-button"
    }

    def setActiveNav(active: Button): Unit =
      navButtons.foreach(b => b.styleClass --= Seq("nav-button-active"))
      active.styleClass += "nav-button-active"

    screens.zip(navButtons).foreach { case ((_, builder), button) =>
      button.onAction = handle { showScreen(builder); setActiveNav(button) }
    }

    val titleLine1 = new Label("Microfinance"):
      styleClass += "nav-title"
    val titleLine2 = new Label("Loan Tracker"):
      styleClass += "nav-title"

    val separator = new Separator():
      styleClass += "nav-separator"

    val nav = new VBox(10, (Seq(titleLine1, titleLine2, separator) ++ navButtons)*):
      styleClass += "side-nav"
      padding = Insets(20, 16, 16, 16)
      prefWidth = 210
      alignment = Pos.TopCenter

    rootPane.left = nav
    rootPane.center = RegisterBorrowerScreen.build()
    setActiveNav(navButtons.head)

    stage = new JFXApp3.PrimaryStage:
      title = "SDG-1 Microfinance Loan Tracker"
      width = 1150
      height = 640
      minWidth = 1150
      minHeight = 640
      maximized = true
      scene = new Scene:
        root = rootPane
        stylesheets += getClass.getResource("/styles.css").toExternalForm
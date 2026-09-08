package sdg1.microfinance.ui

import scalafx.scene.Node
import scalafx.scene.layout.{VBox, HBox, Priority}
import scalafx.scene.control.{Label, ListView, ListCell}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import sdg1.microfinance.model.*

/** Feature 4, at-a-glance dashboard: totals plus a risk-ranked loan list
 * (S1-17). Uses the `RiskAssessable` trait rather than duplicating a risk
 * formula inline, which is exactly the kind of reuse S1-13 rewards.
 */
object DashboardScreen extends RiskAssessable:

  def build(): Node =
    val title = new Label("Dashboard"):
      styleClass += "screen-title"

    val loans = AppState.loans.value.all
    val borrowerCount = AppState.borrowers.value.size
    val totalDisbursed = loans.map(_.principal).sum
    val active = loans.count(_.status == LoanStatus.Active)
    val repaid = loans.count(_.status == LoanStatus.Repaid)
    val defaulted = loans.count(_.status == LoanStatus.Defaulted)

    def statCard(label: String, value: String): Node =
      val card = new VBox(4,
        new Label(value) { styleClass += "stat-value" },
        new Label(label) { styleClass += "stat-label" }
      ):
        styleClass ++= Seq("card", "stat-card")
        alignment = Pos.Center
        minWidth = 160
        maxWidth = Double.MaxValue // grow to share the row evenly, not just sit at a fixed width
      HBox.setHgrow(card, Priority.Always)
      card

    val stats = new HBox(16,
      statCard("Borrowers", borrowerCount.toString),
      statCard("Total disbursed", f"$totalDisbursed%.2f"),
      statCard("Active loans", active.toString),
      statCard("Repaid", repaid.toString),
      statCard("Defaulted", defaulted.toString)
    ):
      maxWidth = Double.MaxValue

    val rankedLoans = ObservableBuffer.from(loans.sortBy(l => -riskScore(l)))
    val riskList = new ListView[Loan](rankedLoans):
      prefHeight = 320
      maxWidth = Double.MaxValue
      cellFactory = (_: ListView[Loan]) => new ListCell[Loan]:
        item.onChange { (_, _, loan) =>
          styleClass --= Seq("risk-high", "risk-medium", "risk-low")
          Option(loan) match
            case Some(l) =>
              text = f"${l.id}  ${l.borrower.name}  risk=${riskScore(l)}%.2f (${riskBand(l)})"
              styleClass += (riskBand(l) match
                case "High"   => "risk-high"
                case "Medium" => "risk-medium"
                case _        => "risk-low")
            case None =>
              text = null
        }
    VBox.setVgrow(riskList, Priority.Always)

    new VBox(16, title, stats, new Label("Loans ranked by default risk (highest first):"), riskList):
      padding = Insets(16)
      maxWidth = Double.MaxValue
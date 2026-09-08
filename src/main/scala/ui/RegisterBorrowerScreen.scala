package sdg1.microfinance.ui

// ai-assisted: #2
// why: entry #2 fixed "Not found: handle" and an ambiguous cellFactory
// overload here. Entry #3 then caught a real bug while clearing -Wunused
// warnings: this screen's `title` label was built but never actually added
// to the returned layout, it's now wrapped into the outer VBox below. A
// later cleanup pass briefly swapped `handle {...}` for `_ => {...}` and
// dropped this import, but that was reverted: S2-6 specifically requires
// the `onAction = handle { ... }` idiom, so the import is genuinely used.
import scalafx.scene.Node
import scalafx.scene.layout.{VBox, GridPane, HBox, Priority, ColumnConstraints}
import scalafx.scene.control.{Label, TextField, Button, ListView, ListCell}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.Includes.*
import scala.annotation.nowarn
import sdg1.microfinance.model.Borrower
import sdg1.microfinance.validation.Validation

/** Feature 1, register a new borrower (S1-14). */
object RegisterBorrowerScreen:

  // handle {...} is deprecated since ScalaFX R19 in favour of `_ => {...}`,
  // but S2-6 specifically grades for the `onAction = handle { ... }` idiom,
  // so it stays, with the resulting deprecation warning scoped out here
  // rather than silenced project-wide.
  @nowarn("cat=deprecation")
  def build(): Node =
    val title = new Label("Register Borrower"):
      styleClass += "screen-title"

    val nameField = new TextField { promptText = "Full name"; prefWidth = 300 }
    val incomeField = new TextField { promptText = "Monthly income (RM)"; prefWidth = 300 }
    val dependentsField = new TextField { promptText = "Dependents (0-20)"; prefWidth = 300 }
    val villageField = new TextField { promptText = "Village / community"; prefWidth = 300 }
    val errorLabel = new Label(""):
      styleClass += "error-label"

    val listItems = ObservableBuffer.from(AppState.borrowers.value.all)
    AppState.borrowers.onChange { (_, _, newRepo) =>
      listItems.setAll(newRepo.all*)
    }

    val borrowerList = new ListView[Borrower](listItems):
      prefWidth = 420
      prefHeight = 320
      maxWidth = Double.MaxValue
      maxHeight = Double.MaxValue
      cellFactory = (_: ListView[Borrower]) => new ListCell[Borrower]:
        item.onChange { (_, _, b) =>
          text = Option(b).map(bw => f"${bw.id}  ${bw.name}  (${bw.villageName}), income ${bw.monthlyIncome}%.2f").orNull
        }

    val registerButton = new Button("Register"):
      defaultButton = true // Enter submits the focused form (S2-10)
      styleClass += "primary-button"
      onAction = handle {
        val result = for
          name       <- Validation.parseName(nameField.text.value)
          income     <- Validation.parseIncome(incomeField.text.value)
          dependents <- Validation.parseDependents(dependentsField.text.value)
        yield Borrower(AppState.nextBorrowerId(), name, income, dependents, villageField.text.value.trim)

        result match
          case Right(borrower) =>
            AppState.addBorrower(borrower)
            errorLabel.text = ""
            nameField.clear(); incomeField.clear(); dependentsField.clear(); villageField.clear()
            nameField.requestFocus()
          case Left(message) =>
            errorLabel.text = message // graceful message, never a stack trace (S1-18)
      }

    val form = new GridPane:
      styleClass += "form-grid"
      styleClass += "card"
      prefHeight = 380
      maxHeight = Double.MaxValue
      hgap = 10; vgap = 10
      // Fixes the input column at exactly 300px, matching Disburse Loan's
      // card, regardless of how the surrounding HBox squeezes this card.
      columnConstraints = Seq(new ColumnConstraints(), new ColumnConstraints(300))
      add(new Label("Name:"), 0, 0);        add(nameField, 1, 0)
      add(new Label("Monthly income:"), 0, 1); add(incomeField, 1, 1)
      add(new Label("Dependents:"), 0, 2);   add(dependentsField, 1, 2)
      add(new Label("Village:"), 0, 3);      add(villageField, 1, 3)
      add(registerButton, 1, 4)
      add(errorLabel, 1, 5)

    val borrowerColumn = new VBox(8, new Label("Registered borrowers:"), borrowerList):
      maxWidth = Double.MaxValue
      maxHeight = Double.MaxValue
    VBox.setVgrow(borrowerList, Priority.Always)
    HBox.setHgrow(borrowerColumn, Priority.Always)

    val contentRow = new HBox(20, form, borrowerColumn):
      maxWidth = Double.MaxValue
      maxHeight = Double.MaxValue
    VBox.setVgrow(contentRow, Priority.Always)

    new VBox(10, title, contentRow):
      padding = Insets(16)
      maxWidth = Double.MaxValue
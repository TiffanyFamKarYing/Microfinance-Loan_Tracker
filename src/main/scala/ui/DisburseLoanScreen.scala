package sdg1.microfinance.ui

// ai-assisted: #2
// why: same class of error as the other screens, missing scalafx.Includes
// import plus an ambiguous ComboBox cellFactory overload; both fixed here.
// Entry #4 had briefly swapped `onAction = handle {...}` for a plain
// `_ => {...}` function after `-Wunused` flagged the Includes import as
// unused. That was reverted later, since S2-6 specifically requires the
// ScalaFX `handle { ... }` idiom rather than a raw function literal, and
// `handle` is what actually needs this import, so it is genuinely used.
// ai-assisted: #11
// why: made the Borrower field on this screen typeable AND selectable
// (editable ComboBox + StringConverter + live filter, sorted ascending by
// id), then iterated on its CSS across several rounds, including one
// genuine mistake (an edit that made the box fully invisible by making
// both the outer control and its inner TextField transparent at the same
// time), before landing on wrapping it in a StackPane styled with the
// same .text-field class as Principal/Interest/Term, so it matches them
// exactly instead of approximating them.
import scalafx.scene.Node
import scalafx.scene.layout.{VBox, GridPane, Priority, StackPane, ColumnConstraints}
import scalafx.scene.control.{Label, TextField, Button, ComboBox, ListCell, ListView}
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.BooleanProperty
import scalafx.geometry.{Insets, Pos}
import javafx.event.EventHandler
import javafx.scene.input.{KeyEvent, KeyCode}
import sdg1.microfinance.model.*
import sdg1.microfinance.validation.Validation
import scalafx.util.StringConverter
import scalafx.Includes.*
import scala.annotation.nowarn

/** Feature 2, disburse a Micro or Group loan to a registered borrower (S1-15,
 * "meaningfully different" from Feature 1: this creates Loan aggregates
 * against an existing Borrower, not a new Borrower).
 */
object DisburseLoanScreen:

  // private: this is only used to seed this screen's own dropdown; no other
  // screen or module needs to read or mutate the list of loan kinds.
  private val loanKinds = ObservableBuffer("Micro", "Group")

  // Isolated on purpose: builds the "insert a literal space instead of
  // letting the ComboBox's popup treat Space as select" handler. Kept as
  // its own small method, separate from build(), so any mistake here is
  // contained to this one spot rather than breaking build()'s parsing.
  private def spaceInsertingFilter(field: javafx.scene.control.TextField): EventHandler[KeyEvent] =
    new EventHandler[KeyEvent] {
      override def handle(e: KeyEvent): Unit = {
        if e.getCode == KeyCode.SPACE then
          e.consume()
          field.replaceSelection(" ")
      }
    }

  // The ComboBox's own popup can also react to Space on key RELEASE (not
  // just key press) to select whatever row is highlighted, so that release
  // needs swallowing too, or the selection still happens a moment after
  // the space character above was inserted.
  private def spaceSwallowingRelease: EventHandler[KeyEvent] =
    new EventHandler[KeyEvent] {
      override def handle(e: KeyEvent): Unit = {
        if e.getCode == KeyCode.SPACE then e.consume()
      }
    }

  // handle {...} is deprecated since ScalaFX R19 in favour of `_ => {...}`,
  // but S2-6 specifically grades for the `onAction = handle { ... }` idiom,
  // so it stays, with the resulting deprecation warning scoped out here
  // rather than silenced project-wide.
  @nowarn("cat=deprecation")
  def build(): Node =
    val title = new Label("Disburse Loan"):
      styleClass += "screen-title"

    def borrowerLabel(b: Borrower): String = s"${b.id} ${b.name}"

    // Master list, always kept sorted ascending by borrower id (S1-15).
    val allBorrowers = ObservableBuffer.from(AppState.borrowers.value.all.sortBy(_.id))
    // What's actually shown in the dropdown, a filtered view of allBorrowers
    // while the user types (kept separate so filtering never mutates the master list).
    val shownBorrowers = ObservableBuffer.from(allBorrowers)

    val borrowerBox = new ComboBox[Borrower](shownBorrowers):
      promptText = "Type or select borrower"
      editable = true
      cellFactory = (_: ListView[Borrower]) => new ListCell[Borrower]:
        item.onChange((_, _, b) => text = Option(b).map(borrowerLabel).orNull)
    borrowerBox.editor.value.promptText = "Type or select borrower"

    // The ComboBox's own dropdown intercepts the Space key as "select the
    // highlighted item" while its popup is open, so typing a space in the
    // editor otherwise acts like pressing Enter instead of inserting a
    // space character. Attached to the ComboBox itself (not just its inner
    // editor), since capturing-phase filters on a parent run before
    // anything on the child, this way we intercept Space before the
    // ComboBox's own internal popup handling ever sees it, on both press
    // and release, not just press. The handlers are defined separately
    // above (spaceInsertingFilter / spaceSwallowingRelease), rather than
    // inline here, so a mistake in them stays contained to one small
    // method instead of breaking this whole build() method's parsing.
    borrowerBox.delegate.addEventFilter(
      KeyEvent.KEY_PRESSED, spaceInsertingFilter(borrowerBox.editor.value.delegate)
    )
    borrowerBox.delegate.addEventFilter(KeyEvent.KEY_RELEASED, spaceSwallowingRelease)

    // Wrap it in a plain StackPane styled exactly like Principal's box
    // (same "text-field" style class), and let the ComboBox itself render
    // fully transparent inside, so the box you see is the same box, not a
    // reconstruction of it.
    val borrowerField = new StackPane:
      styleClass += "text-field"
      alignment = Pos.CenterLeft
      prefWidth = 300
      children = Seq(borrowerBox)
    borrowerBox.maxWidth = Double.MaxValue
    borrowerBox.editor.value.focused.onChange { (_, _, isFocused) =>
      borrowerField.style = if isFocused then "-fx-border-color: #5B9BD5;" else ""
    }

    // Lets the editable text field commit typed text straight to a Borrower
    // (exact id or "id name" match), so typing + selecting both work.
    borrowerBox.converter = new StringConverter[Borrower]:
      override def toString(b: Borrower): String = Option(b).map(borrowerLabel).getOrElse("")
      override def fromString(s: String): Borrower =
        val query = Option(s).getOrElse("").trim
        allBorrowers.find(b => borrowerLabel(b).equalsIgnoreCase(query))
          .orElse(allBorrowers.find(_.id.equalsIgnoreCase(query)))
          .orNull

    // Live-filter the dropdown as the user types (case-insensitive, matches id or name).
    // A reactive BooleanProperty instead of a raw `var` re-entrancy flag,
    // keeping this file free of mutable state (S1-11) while still being
    // ScalaFX's own idiom for a piece of UI-only reactive state (S2-7).
    val suppressFilter = BooleanProperty(false)
    borrowerBox.editor.value.text.onChange { (_, _, typed) =>
      if !suppressFilter.value then
        val query = Option(typed).getOrElse("")
        val matches =
          if query.isEmpty then allBorrowers.toList
          else allBorrowers.filter(b => borrowerLabel(b).toLowerCase.contains(query.toLowerCase)).toList
        suppressFilter.value = true
        shownBorrowers.setAll(matches*)
        borrowerBox.editor.value.text = query
        borrowerBox.editor.value.positionCaret(query.length)
        suppressFilter.value = false
        if matches.nonEmpty then borrowerBox.show() else borrowerBox.hide()
    }

    AppState.borrowers.onChange { (_, _, newRepo) =>
      allBorrowers.setAll(newRepo.all.sortBy(_.id)*)
      shownBorrowers.setAll(allBorrowers.toSeq*)
    }

    val kindBox = new ComboBox[String](loanKinds) { value = "Micro"; prefWidth = 160; maxWidth = 160 }
    val principalField = new TextField { promptText = "Principal (RM)"; prefWidth = 300 }
    val rateField = new TextField { promptText = "Interest rate % p.a."; prefWidth = 300 }
    val termField = new TextField { promptText = "Term (months)"; prefWidth = 300 }
    val groupSizeField = new TextField { promptText = "Group size"; disable = true; prefWidth = 300 }
    val errorLabel = new Label(""){ styleClass += "error-label" }

    kindBox.value.onChange { (_, _, kind) =>
      groupSizeField.disable = kind != "Group"
      if kind != "Group" then groupSizeField.clear()
    }

    val disburseButton = new Button("Disburse Loan"):
      defaultButton = true
      styleClass += "primary-button"
      onAction = handle {
        val validated =
          for
            borrower <- Option(borrowerBox.value.value).toRight("Please select a borrower.")
            principal <- Validation.parsePrincipal(principalField.text.value)
            rate      <- Validation.parseInterestRate(rateField.text.value)
            term      <- Validation.parseTermMonths(termField.text.value)
          yield (borrower, principal, rate, term)

        validated match
          case Right((borrower, principal, rate, term)) =>
            val loanOrError: Either[String, Loan] =
              if kindBox.value.value == "Group" then
                Validation.parseGroupSize(groupSizeField.text.value).map(size =>
                  GroupLoan(AppState.nextLoanId(), borrower, principal, rate, term, size)
                )
              else
                Right(MicroLoan(AppState.nextLoanId(), borrower, principal, rate, term))

            loanOrError match
              case Right(loan) =>
                AppState.addLoan(loan)
                errorLabel.text = ""
                principalField.clear(); rateField.clear(); termField.clear(); groupSizeField.clear()
              case Left(msg) => errorLabel.text = msg
          case Left(msg) => errorLabel.text = msg
      }

    val form = new GridPane:
      styleClass += "form-grid"
      styleClass += "card"
      prefHeight = 380
      maxHeight = Double.MaxValue
      hgap = 10; vgap = 10
      // Fixes the input column at exactly 300px regardless of how much
      // space the surrounding layout squeezes this card into, so it can't
      // silently drift narrower than the matching column on other screens.
      columnConstraints = Seq(new ColumnConstraints(), new ColumnConstraints(300))
      add(new Label("Borrower:"), 0, 0);      add(borrowerField, 1, 0)
      add(new Label("Loan type:"), 0, 1);     add(kindBox, 1, 1)
      add(new Label("Principal:"), 0, 2);     add(principalField, 1, 2)
      add(new Label("Interest rate:"), 0, 3); add(rateField, 1, 3)
      add(new Label("Term (months):"), 0, 4); add(termField, 1, 4)
      add(new Label("Group size:"), 0, 5);    add(groupSizeField, 1, 5)
      add(disburseButton, 1, 6)
      add(errorLabel, 1, 7)
    VBox.setVgrow(form, Priority.Always)

    new VBox(10, title, form):
      padding = Insets(16)
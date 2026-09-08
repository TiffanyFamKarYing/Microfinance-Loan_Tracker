package sdg1.microfinance.persistence

import sdg1.microfinance.model.*
import scala.util.{Try, Failure}

object BorrowerStore extends CsvStore[Borrower]:
  protected val header = "id,name,monthlyIncome,dependents,villageName"

  protected def encode(borrower: Borrower): String =
    List(borrower.id, csvSafe(borrower.name), f"${borrower.monthlyIncome}%.2f", borrower.dependents, csvSafe(borrower.villageName)).mkString(",")

  protected def decode(line: String): Try[Borrower] = Try {
    val cols = splitCsv(line)
    Borrower(
      id = cols(0),
      name = cols(1),
      monthlyIncome = cols(2).toDouble,
      dependents = cols(3).toInt,
      villageName = cols(4)
    )
  }

  private def csvSafe(s: String): String = s.replace(",", ";")
  private def splitCsv(line: String): Array[String] = line.split(",", -1)

object LoanStore extends CsvStore[Loan]:
  // ai-assisted: #1
  // why: this whole scaffold (including how to (de)serialise a sealed loan
  // hierarchy to a flat CSV row without losing the subtype) came from the
  // single build prompt logged as entry #1 in ai/interaction_log.md.
  protected val header =
    "kind,id,borrowerId,borrowerName,borrowerIncome,borrowerDependents,borrowerVillage," +
      "principal,interestRateAnnual,termMonths,groupSize,status"

  protected def encode(loan: Loan): String =
    val borrower = loan.borrower
    val groupSize = loan match
      case g: GroupLoan => g.groupSize.toString
      case _            => ""
    val kind = loan match
      case _: MicroLoan => "Micro"
      case _: GroupLoan => "Group"
    List(
      kind, loan.id, borrower.id, borrower.name.replace(",", ";"), f"${borrower.monthlyIncome}%.2f", borrower.dependents,
      borrower.villageName.replace(",", ";"), f"${loan.principal}%.2f", f"${loan.interestRateAnnual}%.2f",
      loan.termMonths, groupSize, LoanStatus.label(loan.status)
    ).mkString(",")

  protected def decode(line: String): Try[Loan] =
    Try {
      val cols = line.split(",", -1)
      val borrower = Borrower(cols(2), cols(3), cols(4).toDouble, cols(5).toInt, cols(6))
      val status = LoanStatus.parse(cols(11)).getOrElse(LoanStatus.Active)
      (cols, borrower, status)
    }.flatMap { case (cols, borrower, status) =>
      cols(0) match
        case "Micro" =>
          Try(MicroLoan(cols(1), borrower, cols(7).toDouble, cols(8).toDouble, cols(9).toInt, status))
        case "Group" =>
          Try(GroupLoan(cols(1), borrower, cols(7).toDouble, cols(8).toDouble, cols(9).toInt, cols(10).toInt, status))
        case other =>
          Failure(IllegalArgumentException(s"Unknown loan kind in CSV: '$other'"))
    }

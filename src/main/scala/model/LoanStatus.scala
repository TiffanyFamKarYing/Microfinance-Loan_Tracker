package sdg1.microfinance.model

/** Closed set of states a [[Loan]] can be in. Sealed so the compiler can check
  * pattern matches over it are exhaustive (no wildcard `case _` needed).
  */
sealed trait LoanStatus

object LoanStatus:
  case object Active    extends LoanStatus
  case object Repaid     extends LoanStatus
  case object Defaulted extends LoanStatus

  def label(status: LoanStatus): String = status match
    case Active    => "Active"
    case Repaid    => "Repaid"
    case Defaulted => "Defaulted"

  def parse(text: String): Either[String, LoanStatus] = text.trim.toLowerCase match
    case "active"    => Right(Active)
    case "repaid"    => Right(Repaid)
    case "defaulted" => Right(Defaulted)
    case other       => Left(s"Unknown loan status: '$other'")

package sdg1.microfinance.validation

/** Turns raw string input from ScalaFX TextFields into validated domain
  * values, or a human-readable error the UI can show instead of a stack
  * trace (S1-18: graceful handling of empty / wrong-type / out-of-range input).
  */
object Validation:

  // private: internal helper, callers only need parseIncome/parseDependents/etc,
  // not the raw numeric-parsing plumbing, so we keep it hidden from the API surface.
  private def parsePositiveDouble(raw: String, fieldName: String): Either[String, Double] =
    raw.trim.toDoubleOption match
      case Some(v) if v > 0 => Right(v)
      case Some(_)          => Left(s"$fieldName must be greater than zero.")
      case None             => Left(s"$fieldName must be a valid number.")

  def parseName(raw: String): Either[String, String] =
    if raw.trim.isEmpty then Left("Name cannot be empty.")
    else Right(raw.trim)

  def parseIncome(raw: String): Either[String, Double] =
    parsePositiveDouble(raw, "Monthly income")

  def parseDependents(raw: String): Either[String, Int] =
    raw.trim.toIntOption match
      case Some(v) if v >= 0 && v <= 20 => Right(v)
      case Some(_)                       => Left("Dependents must be between 0 and 20.")
      case None                          => Left("Dependents must be a whole number.")

  def parsePrincipal(raw: String): Either[String, Double] =
    parsePositiveDouble(raw, "Principal")

  def parseInterestRate(raw: String): Either[String, Double] =
    raw.trim.toDoubleOption match
      case Some(v) if v >= 0 && v <= 100 => Right(v)
      case Some(_)                        => Left("Interest rate must be between 0 and 100%.")
      case None                           => Left("Interest rate must be a valid number.")

  def parseTermMonths(raw: String): Either[String, Int] =
    raw.trim.toIntOption match
      case Some(v) if v > 0 && v <= 120 => Right(v)
      case Some(_)                       => Left("Term must be between 1 and 120 months.")
      case None                          => Left("Term must be a whole number.")

  def parseGroupSize(raw: String): Either[String, Int] =
    raw.trim.toIntOption match
      case Some(v) if v >= 2 && v <= 50 => Right(v)
      case Some(_)                       => Left("Group size must be between 2 and 50.")
      case None                          => Left("Group size must be a whole number.")

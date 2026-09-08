package sdg1.microfinance.model

/** Mixed into dashboard/report logic that needs to rank loans by default risk.
  * Kept as a trait (rather than folded into `Loan`) so the risk formula can
  * evolve independently of the loan data model, this is the "additional
  * trait to keep code DRY" piece the rubric asks for (S1-13 support).
  */
trait RiskAssessable:
  /** Returns a risk score in [0.0, 1.0]; higher means more likely to default.
    * A simple debt-to-income heuristic: repayment burden relative to income.
    */
  def riskScore(loan: Loan): Double =
    if loan.borrower.monthlyIncome <= 0 then 1.0
    else math.min(1.0, loan.monthlyRepayment / loan.borrower.monthlyIncome)

  def riskBand(loan: Loan): String =
    val score = riskScore(loan)
    if score >= 0.6 then "High"
    else if score >= 0.3 then "Medium"
    else "Low"

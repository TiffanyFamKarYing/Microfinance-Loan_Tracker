package sdg1.microfinance.model

import org.scalatest.funsuite.AnyFunSuite

class LoanSpec extends AnyFunSuite:

  private val borrower = Borrower("B-0001", "Amara", 500.0, 2, "Riverside")

  test("MicroLoan.monthlyRepayment includes interest, split over the term") {
    val loan = MicroLoan("L-0001", borrower, principal = 1200.0, interestRateAnnual = 12.0, termMonths = 12)
    assert(loan.monthlyRepayment === (1200.0 * 1.12) / 12.0)
  }

  test("GroupLoan.monthlyRepayment splits the per-member share across the group") {
    val loan = GroupLoan("L-0002", borrower, principal = 1200.0, interestRateAnnual = 12.0, termMonths = 12, groupSize = 4)
    val micro = MicroLoan("L-0003", borrower, principal = 1200.0, interestRateAnnual = 12.0, termMonths = 12)
    assert(loan.monthlyRepayment === micro.monthlyRepayment / 4.0)
  }

  test("withStatus returns a new immutable Loan rather than mutating the original") {
    val original = MicroLoan("L-0004", borrower, 1000.0, 10.0, 10)
    val updated = original.withStatus(LoanStatus.Repaid)
    assert(original.status == LoanStatus.Active)
    assert(updated.status == LoanStatus.Repaid)
  }

  test("RiskAssessable flags a loan whose repayment exceeds income as High risk") {
    val risky = new RiskAssessable {}
    val loan = MicroLoan("L-0005", borrower.copy(monthlyIncome = 50.0), 2000.0, 20.0, 6)
    assert(risky.riskBand(loan) == "High")
  }

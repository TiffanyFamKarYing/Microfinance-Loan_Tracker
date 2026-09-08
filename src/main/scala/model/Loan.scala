package sdg1.microfinance.model

/** Abstract base for every loan product the tracker supports.
  *
  * S1-7 (inheritance): abstract class with two concrete subclasses below.
  * S1-8 (subtype polymorphism): `monthlyRepayment` is overridden differently
  * by each subclass because the repayment math genuinely differs per product.
  * S1-11 (immutability): no `var` anywhere, status changes go through
  * `withStatus`, which returns a *new* Loan rather than mutating this one.
  */
abstract class Loan:
  def id: String
  def borrower: Borrower
  def principal: Double
  def interestRateAnnual: Double
  def termMonths: Int
  def status: LoanStatus

  /** Flat per-month repayment amount. Subclasses override this because a
    * group loan splits the repayment across members while a micro loan does not.
    */
  def monthlyRepayment: Double

  /** Total amount still owed assuming no repayments made yet. */
  def totalRepayable: Double = monthlyRepayment * termMonths

  /** Immutable "update", returns a new Loan with the status changed,
    * leaving `this` untouched.
    */
  def withStatus(newStatus: LoanStatus): Loan

/** A loan issued to a single borrower. */
case class MicroLoan(
    id: String,
    borrower: Borrower,
    principal: Double,
    interestRateAnnual: Double,
    termMonths: Int,
    status: LoanStatus = LoanStatus.Active
) extends Loan:
  override def monthlyRepayment: Double =
    (principal * (1 + interestRateAnnual / 100.0)) / termMonths

  override def withStatus(newStatus: LoanStatus): Loan = copy(status = newStatus)

/** A loan shared by a small lending group (common in microfinance circles) ,
  * the monthly repayment is split evenly across `groupSize` members, which is
  * the concrete difference that justifies overriding `monthlyRepayment`.
  */
case class GroupLoan(
    id: String,
    borrower: Borrower,
    principal: Double,
    interestRateAnnual: Double,
    termMonths: Int,
    groupSize: Int,
    status: LoanStatus = LoanStatus.Active
) extends Loan:
  override def monthlyRepayment: Double =
    ((principal * (1 + interestRateAnnual / 100.0)) / termMonths) / math.max(1, groupSize)

  override def withStatus(newStatus: LoanStatus): Loan = copy(status = newStatus)

package sdg1.microfinance.model

/** A person registered by a community lender to receive microfinance support.
  * Immutable by design (S1-11): every field is a `val`; changes produce a new
  * instance rather than mutating an existing one.
  *
  * @param id            unique borrower id, e.g. "B-0001"
  * @param name          full name of the borrower
  * @param monthlyIncome self-reported monthly income (local currency units)
  * @param dependents    number of dependents the borrower supports
  * @param villageName   community / village the borrower belongs to
  */
case class Borrower(
    id: String,
    name: String,
    monthlyIncome: Double,
    dependents: Int,
    villageName: String
)

package sdg1.microfinance.repo

/** Generic, immutable in-memory store used for both `Borrower` and `Loan`
  * (S1-9: parametric polymorphism, one class, many entity types).
  *
  * Deliberately holds no `var`: every mutating-looking operation returns a
  * *new* `Repository[T]`. The UI layer wraps this in a ScalaFX
  * `ObjectProperty`, which is the one place mutability is allowed under the
  * rubric ("excluding ScalaFX bindings").
  */
case class Repository[T](items: Vector[T] = Vector.empty[T]):

  def add(item: T): Repository[T] = copy(items = items :+ item)

  def all: Vector[T] = items

  def find(p: T => Boolean): Option[T] = items.find(p)

  /** Replaces every item matching `p` with `f(item)`; non-matching items pass through. */
  def updateWhere(p: T => Boolean, f: T => T): Repository[T] =
    copy(items = items.map(i => if p(i) then f(i) else i))

  def size: Int = items.size

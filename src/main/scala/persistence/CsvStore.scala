package sdg1.microfinance.persistence

import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.{Try, Using}

/** Shared CSV load/save logic (S1-13, DRY): `BorrowerStore` and `LoanStore`
  * below only need to supply `header`, `encode`, and `decode`, the file
  * handling and exception wrapping lives here exactly once.
  *
  * Every IO / parse operation is wrapped in `Try` (S1-12), callers get a
  * `Success`/`Failure` instead of an uncaught exception reaching the UI.
  */
abstract class CsvStore[T]:
  protected def header: String
  protected def encode(item: T): String
  protected def decode(line: String): Try[T]

  def save(path: String, items: Vector[T]): Try[Unit] =
    Try {
      val file = new File(path)
      Option(file.getParentFile).foreach(_.mkdirs())
      Using.resource(new PrintWriter(file)) { writer =>
        writer.println(header)
        items.foreach(item => writer.println(encode(item)))
      }
    }

  def load(path: String): Try[Vector[T]] =
    Try {
      val file = new File(path)
      if !file.exists() then Vector.empty[T]
      else
        Using.resource(Source.fromFile(path)) { src =>
          val lines = src.getLines().toVector
          val body = if lines.nonEmpty then lines.tail else Vector.empty
          // Skip any line that fails to parse rather than aborting the whole load ,
          // one corrupt row shouldn't lock the user out of every other record.
          body.flatMap(line => decode(line).toOption)
        }
    }

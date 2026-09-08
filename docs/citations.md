# Third-Party Code / Asset Citations and Licenses

This project depends on the following third-party libraries, declared in
`build.sbt`. No source files, assets, fonts, or icons were copied from any
external tutorial, blog post, Stack Overflow answer, or repository, the
application code, CSS, and CSV sample data in this submission are original.

| Library | Version | Source | License | Used for |
|---|---|---|---|---|
| ScalaFX | 21.0.0-R32 | https://www.scalafx.org/ | BSD 3-Clause | The entire GUI layer (`src/main/scala/ui/`), screens, controls, layout, and CSS styling hooks. |
| JavaFX (OpenJFX) | 21.0.2 | https://openjfx.io/ | GPLv2 with Classpath Exception | Underlying UI toolkit that ScalaFX wraps; pulled in as `javafx-base`, `javafx-controls`, `javafx-fxml`, and `javafx-graphics`. |
| ScalaTest | 3.2.19 | https://www.scalatest.org/ | Apache License 2.0 | Unit tests in `src/test/scala/LoanSpec.scala` (test-scope only, not bundled in the runtime app). |

No external fonts, icons, images, or CDN-hosted assets are referenced.
`styles.css` uses the OS-provided "Segoe UI" system font only.

All dependency declarations above are taken directly from `build.sbt`; no
undeclared third-party code is used elsewhere in the project.

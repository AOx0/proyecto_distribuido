// The project function defines how your document looks.
// It takes your content and some metadata and formats it.
// Go ahead and customize it to your liking!
#let project(title: "", materia: "", authors: (), date: none, body) = {
  // Set the document's basic properties.
  set document(author: authors.map(a => a.name), title: title)
  set page(numbering: "1", number-align: center)

  // Save heading and body font families in variables.
  let body-font = "Linux Libertine"
  let sans-font = "Inria Sans"

  // Set body font family.
  set text(font: body-font, lang: "es", region: "MX")
  show raw: set text(font: "JetBrainsMono NFM", ligatures: true)

  // Set paragraph spacing.
  show par: set block(above: 1.2em, below: 1.2em)

  show heading: set text(font: sans-font)
  set heading(numbering: "1.1.")
  set par(leading: 0.75em)

  v(1fr)

  // Title row.
  align(center)[
    #block(text(font: sans-font, weight: 700, 1.75em, title))
    #v(1.5em, weak: true)
    #text(size: 1.3em, [Universidad Panamericana])
    
    #text(size: 1.3em, materia)

    #v(3.2em, weak: true)
    #date
  ]

  align(center,
    image("UP.png", width: 55%)
  )
  
  // Author information.
  pad(
    top: 0.8em,
    bottom: 0.8em,
    x: 2em,
    grid(
      columns: (1fr),
      gutter: 1em,
      ..authors.map(author => align(center)[
        *#author.name* \
        #author.email
      ]),
    ),
  )

  v(1fr)

  pagebreak()
  outline(indent: true)

  // Main body.
  set par(justify: true)

  body
}

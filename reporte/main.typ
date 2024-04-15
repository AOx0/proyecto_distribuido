#import "@preview/cetz:0.2.2"
#import "@preview/oxifmt:0.2.0": strfmt

#let NUMBERING = "1.1";
#set text(lang: "es", region: "MX")
#set heading (numbering: NUMBERING)

#outline(indent: true)

= Introducción

= Estructura del proyecto


#cetz.canvas(
  import cetz.draw: *
)

= Protocolo

El protocolo está dividido en dos capas, la capa de _mensaje_ (ver @mensaje), que se encarga de controlar el envío de un mensaje en la malla y de asegurar que se entregue solo al recipiente correcto, y la capa de _aplicación_, también llamada _payload_, que son un conjunto de bits opacos para la capa de _mensaje_ con los datos necesarios para que los clientes servidores (_solvers_) y los clientes solicitantes (_requesters_) puedan comunicarse.

== Mensaje <mensaje>

Un mensaje que se transmite por la malla tiene la estructura en bytes mostrada en @estructura_mensaje.

#figure(
  caption: [Estructura de un mensaje en bytes],
  {
    let bits = 27
    table(
      columns: (1fr,) * bits,
      table.cell(colspan: 2, align: center)[`type`],
      table.cell(colspan: 4, align: center)[`len`],
      table.cell(colspan: 8, align: center)[`from`],
      table.cell(colspan: 8, align: center)[`dest`],
      table.cell(colspan: bits - (2 + 4 + 8 * 2), align: center)[`..payload`],
      // Línea de bits vacíos
      ..{ let n = 0; while n < bits { n = n + 1; ([],) } },
      // Línea numerada de bits
      ..{
        let n = 0
        while n < bits {
          n = n + 1
          if n == bits {
            (table.cell(align: center, stroke: none, text(size: 8pt, `..`)),)
          } else {
            (table.cell(align: center, stroke: none, text(size: 8pt, raw(strfmt("{0:X}", n - 1)))),)
          }
        } 
      },
    )
  }
) <estructura_mensaje>

=== `type`

El tipo (o _type_) se refiere al tipo de mensaje que se está transmitiendo, los tipos de mensajes disponibles se pueden observar en la @tipos_de_mensaje.

#figure(
  caption: [Tipos de mensaje],
  table(
    columns: (auto,) * 2,
    [Tipo], [Significado],
    [1], [Identificación],
    [2], [Solicitud],
    [3], [Respuesta],
  )
) <tipos_de_mensaje>

=== ``

== Payload

#show bibliography: set heading (numbering: NUMBERING)

#bibliography("bib.yml")

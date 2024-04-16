#import "@preview/cetz:0.2.2"
#import "@preview/oxifmt:0.2.0": strfmt
#import "@preview/fletcher:0.4.3" as fletcher: diagram, node, edge

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

El tipo, o _type_, se refiere al tipo de mensaje que se está transmitiendo, los tipos de mensajes disponibles se pueden observar en la @tipos_de_mensaje.

#figure(
  caption: [Tipos de conexión],
  table(
    columns: (auto,) * 2,
    [Tipo], table.cell(align: left)[Significado],
    [`1`],  table.cell(align: left)[Identificación],
    [`2`],  table.cell(align: left)[Solicitud],
    [`3`],  table.cell(align: left)[Respuesta],
  )
) <tipos_de_mensaje>

=== `len`

Los 4 bytes del `len` describen  el tamaño del `payload` (ver @payload) del mensaje en bytes en un entero de 32 bits.

=== `from` y `dest`

Cada uno de estos campos representa un identificador único de 128 bits para la máquina de destino y la máquina del que se origina el mensaje. La asignación del identificador único la realiza el nodo al que se conecta un cliente independientemente de si es un servidor o solicitador.

#highlight[Las máquinas de origen y destino solo pueden ser `ClientSolver` y `ClientRequester`, ya que entre los nodos no existe un ID único por ID.]

Se considera un campo `from` y/o `dest` como no especificado cuando los 128 bits del `UUID` son ceros.

Los ID son asignados por los nodos a los clientes de forma automática al tener la conexión inicial. Un ID consiste en un `UUID` versión 4, que se caracteriza por ser aleatorio.


== Payload <payload>

=== Identificación

Un mensaje de identificación consiste de 1 byte que contiene el tipo de conexión de la dirección que realiza la solicitud. Los tipos de conexión se muestran en la 

#figure(
  caption: [Tipos de mensaje],
  table(
    columns: (auto,) * 2,
    [Tipo], table.cell(align: left)[Significado],
    [`1`],  table.cell(align: left)[`Node`],
    [`2`],  table.cell(align: left)[`ClientRequester` (o célula solicitante)],
    [`3`],  table.cell(align: left)[`ClientSolver` (o célula servidor)],
  )
) <tipos_de_conexion>

=== Solicitud

=== Resultado

== Identificación

#figure(
  caption: [Proceso de identificación entre cualquier tipo de conexión y un nodo. `X` se refiere al tipo de conexión que puede ser `Node`, `ClientSolver` (Cliente Servidor) o `ClientRequester` (Cliente Solicitante).],
  cetz.canvas({
    import cetz.draw: *
    set-style(mark: (end: ">"))

    content((5, 0), (7, 1), 
      box(align(center)[*`Nodo`*], stroke: 1pt, width: 100%, height: 100%, inset: 1em)
    )

    content((0, 0), (2, 1), 
      box(align(center)[*`X`*], stroke: 1pt, width: 100%, height: 100%, inset: 1em)
    )

    line((1, -2), (6, -2), name: "line")
    content(
      ("line.start", 2.5, "line.end"),
      angle: "line.end",
      padding: .1,
      anchor: "south",
      [`Identidad: X`]
    )

    // line((6, -3), (6.5, -2.5), (7, -3), (6.5, -3.5), (6, -3), name: "line")

    arc((6,-3), start: 360deg, stop: 30deg, radius: -10pt, name: "line")    
    content(
      (6.8, -2), (15, -4.5),
      box(align(left)[
        + Almacena el `Socket` de la conexión y tipo como una instancia de `Connection`.\
        + El constructor de `Connection` asigna un `UUID` #highlight[si no está especificado en el mensaje.]
      ], stroke: 1pt, width: 100%, height: 100%, inset: 1em)
    )


    line((6, -4), (1, -4), name: "line")
    content(
      ("line.start", 2.5, "line.end"),
      angle: "line.start",
      padding: .1,
      anchor: "south",
      [`Identidad: Nodo`]
    )

    arc((6,-6), start: 360deg, stop: 30deg, radius: -10pt, name: "line")    
    content(
      (6.8, -5.3), (15, -6.8),
      box(align(left)[
        Queda a la espera de nuevos `Message` en un nuevo hilo con la función `nodo.App.handle`
      ], stroke: 1pt, width: 100%, height: 100%, inset: 1em)
    )
    
    

    line((1,-1), (1,-8))

    
    line((6,-1), (6,-8))
  })
) <identificacion_nodo>

// https://xkcd.com/1195/
#import fletcher.shapes: diamond
#set text(font: "Comic Neue", weight: 600)

// #{
//   let width = 1
//   diagram(
//     node-stroke: 0.5pt,
//     edge-stroke: 0.5pt,
//     node((0,0), [Nodo], stroke: 2pt),
//     node((2 + width,0), [Nodo], stroke: 2pt),
//     // edge("-|>"),
//     // node((2,0), align(center)[
//       // Hey, wait,\ this flowchart\ is a trap!
//     // ], shape: diamond),
//     // edge("d,r,u,l", "-|>", [Yes], label-pos: 0.1)
//   )
// }

#show bibliography: set heading (numbering: NUMBERING)

#bibliography("bib.yml")

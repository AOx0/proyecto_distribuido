#import "@preview/cetz:0.2.2"
#import "@preview/oxifmt:0.2.0": strfmt
#import "@preview/fletcher:0.4.3" as fletcher: diagram, node, edge
#import "template.typ": project

#show "June": "Junio"

#show: project.with(
  title: "Proyecto Final",
  materia: "Cómputo Distribuido",
  authors: (
    (name: "Osornio López Daniel Alejandro", email: "0244685@up.edu.mx"),
  ),
  date: datetime.today().display("[month repr:long] [day], [year]"),
)

#let cgray(body, ..args) = table.cell(fill: gray.lighten(70%), ..args, body)

= Introducción

En este reporte se describe la estructura del proyecto, las especificaciones del protocolo desarrollado y el flujo de los mensajes por la malla de nodos.

= Estructura del proyecto

El proyecto está contenido dentro de un _pom_ general llamado `proyecto` que cuenta con 4 módulos: `core`, `server`, `cliente_servidor` y `cliente_solicitante`.

El módulo `core` define la funcionalidad compartida entre los nodos (`server`) y los clientes (`cliente_servidor` y `cliente_solicitante`), como lo es la clase `Message` que transporta los mensajes por la red de nodos (ver la @mensaje) y la estructura `MessageBuilder` que provee funciones estáticas que facilitan la creación de los distintos tipos de mensaje.

#pagebreak()
= Protocolo

El protocolo está dividido en dos capas, la capa de _mensaje_ (ver @mensaje), que se encarga de controlar el envío de un mensaje en la malla y de asegurar que se entregue solo al recipiente correcto, y la capa de _aplicación_, también llamada _payload_, que son un conjunto de bits opacos para la capa de _mensaje_ con los datos necesarios para que los clientes servidores (_solvers_) y los clientes solicitantes (_requesters_) puedan comunicarse.

== Mensaje <mensaje>

Un mensaje que se transmite por la red tiene la estructura en bytes mostrada en @estructura_mensaje.

#figure(
  caption: [Estructura de un mensaje en bytes, *`t`* se refiere al campo *`type`* y *`d`* se refiere al campo *`dest`*.],
  {
    let bits = 1 + 1 + 4 + 8 + 4 + 8
    table(
      columns: (1fr,) * bits,
      cgray(colspan: 1, align: center)[*`t`*],
      cgray(colspan: 1, align: center)[*`d`*],
      cgray(colspan: 4, align: center)[*`e_len`*],
      cgray(colspan: 8, align: center)[*`event...`*],
      cgray(colspan: 4, align: center)[*`p_len`*],
      cgray(colspan: 8, align: center)[*`payload...`*],
      // table.cell(colspan: bits - (2 + 4 + 4 + 8 + 2), align: center)[`..payload`],
      // Línea de bits vacíos
      ..{ let n = 0; while n < bits { n = n + 1; ([],) } },
      // Primeros 10 bytes
      ..{
        let n = 0
        while n < 12 {
          n = n + 1
            (table.cell(align: center, stroke: none, text(size: 8pt, raw(strfmt("{0:X}", n - 1)))),)
        } 
      },
      table.cell(align: center, stroke: none, text(size: 8pt, raw(".."))),
      table.cell(align: center, stroke: none, text(size: 8pt, raw("e"))),
      ..{
        let n = 1
        while n < 11 {
          n = n + 1
            (table.cell(align: center, stroke: none, text(size: 8pt, raw(strfmt("e+{0:X}", n - 1)))),)
        } 
      },
      table.cell(align: center, stroke: none, text(size: 8pt, raw(".."))),
      table.cell(align: center, stroke: none, text(size: 8pt, raw("p"))),
    )
  }
) <estructura_mensaje>

=== `type`

#grid(
  columns: 2,
  column-gutter: 15pt,
  [
    El tipo o _type_, se refiere al tipo de mensaje que se está transmitiendo, los tipos de mensajes disponibles se pueden observar en la @tipos_de_mensaje.

    El tipo concreto de solicitud es opaco para la capa de Mensaje y viene codificado dentro de los bytes del _payload_, de esta forma la infraestructura necesaria para enviar un mensaje no cambia a pesar de que se agreguen nuevos tipos de solicitudes.
  ],
  [
    #figure(
      caption: [Tipos de mensaje],
      table(
        columns: (auto,) * 2,
        cgray[Tipo], cgray(align: left)[Significado],
        [*`1`*],  table.cell(align: left)[Identificación],
        [*`2`*],  table.cell(align: left)[Solicitud],
        [*`3`*],  table.cell(align: left)[Respuesta],
      )
    ) <tipos_de_mensaje>
  ]
)

=== `dest`

#grid(
  columns: 2,
  column-gutter: 15pt,
  [
    El campo `dest` o destino se refiere al tipo de célula al que va dirgido el mensaje, dependiendo del valor especificado en el campo se hará la redirección correspondiente en cada nodo para asegurar que el mensaje se entregue a la(s) célula(s) correspondiente(s).
  ],
  [
    #figure(
      caption: [Tipos de destino],
      table(
        columns: (auto,) * 2,
        cgray[Tipo], cgray(align: left)[Significado],
        [*`1`*],  table.cell(align: left)[Node],
        [*`2`*],  table.cell(align: left)[Server],
        [*`3`*],  table.cell(align: left)[Client],
      )
    ) <tipos_de_destino>
  ],
)

=== `e_len` y `event` <event>

El evento o `event` es un identificador único de tamaño variable que se usa para distinguir entre mensajes, de esta forma los clientes pueden recibir respuestas a las solicitudes que realizan y descartar los paquetes que no están dirigidos a cada uno.

#grid(
  columns: 2,
  align: horizon,
  column-gutter: 15pt,
  [
    Aunque en la teoría el campo `event` es de tamaño variable, en la práctica está formado por dos componentes que corresponden a `Integers`:
    - El id de la célula de destino
    - El número de paquete actual
  ],
  [
    #figure(
      caption: [Nomenclatura de un evento, con `C322:EAB7:0000:002A` como ejemplo.],
      table(
        columns: (auto,) * 3,
        column-gutter: 0pt,
        row-gutter: 0pt,
        inset: 0pt,
        table.cell(stroke: none)[*`C322:EAB7`*], 
        table.cell(stroke: none)[*`:`*], 
        table.cell(stroke: none)[*`0000:002A`*],
        table.cell(stroke: none, fill: blue.lighten(50%), block(height: 3pt)[\ ]), 
        table.cell(stroke: none, block(height: 3pt)[\ ]), 
        table.cell(stroke: none, fill: red.lighten(50%), block(height: 3pt)[\ ]), 
        table.cell(stroke: none, block(height: 5pt)[\ ]), 
        table.cell(stroke: none, block(height: 5pt)[\ ]), 
        table.cell(stroke: none, block(height: 5pt)[\ ]), 
        table.cell(stroke: none)[#text(fill: blue.darken(10%), "ID célula")], 
        table.cell(stroke: none)[], 
        table.cell(stroke: none)[#text(fill: red.darken(10%), "No. msg")],
      )
    ) <desc_event>
  ],
)

El ID de célula es un entero aleatorio asignado a cada célula cuando se establece una conexión con un nodo, es gracias a este campo que un nodo puede _razonar_ sobre si debe mandar un mensaje o no a cada célula conectada al mismo. Además el número de mensaje permite que los nodos sepan en todo momento en número de mensaje esperado para cada célula almacenando estado sobre su conexión, de esta forma sabe si la célula ya ha recibido el mensaje y evitar mandar mensajes a las células que ya no son relevantes, por ejejemplo si una célula recibió una respuesta a una solicitud muy antigua cuando ya ha recibido la misma respuesta más rápido por medio de otra célula.

=== Payload <payload>

El _payload_ es el mensaje en sí que se transmite entre los clientes, el _payload_ es transportado opacamente usando el _frame_ que provee la clase `Message` (ver la @mensaje). Los bytes que se transportan en el _payload_ permiten que los clientes intercambien solicitudes y respuestas.

En el proyecto existe una clase de ayuda `MessageBuilder` que permite crear de manera sencilla los tipos de _payload_ válidos que emplean los clientes y servidores, por ejemplo, el siguiente código demuestra como un cliente puede crear un nuevo mensaje que contiene un _payload_ con una solicitud de suma de los números `10.0` y `11.0`:

```java
Message solicitud = MessageBuilder.Request(Message.RequestType.Add, 10.0, 11.0);
Messenger.send(out, solicitud);
```

==== Identificación

#grid(
  columns: 2,
  column-gutter: 15pt,
  [
    Un mensaje de identificación consiste de 1 byte que contiene el tipo de conexión de la dirección que realiza la solicitud. Los tipos de conexión se muestran en la @tipos_de_conexion.
  ],
  [
    #figure(
      caption: [Tipos de conexiones],
      table(
        columns: (auto,) * 2,
        cgray[Tipo], cgray(align: left)[Significado],
        [*`1`*],  table.cell(align: left)[`Node`],
        [*`2`*],  table.cell(align: left)[`ClientRequester` (o célula solicitante)],
        [*`3`*],  table.cell(align: left)[`ClientSolver` (o célula servidor)],
      )
    ) <tipos_de_conexion>
  ]
)

==== Solicitud

#figure(
  caption: [Estructura de una solicitud.],
  {
    let bits = 17
    table(
      columns: (1fr,) * bits,
      cgray(colspan: 1, align: center)[*`op`*],
      cgray(colspan: 8, align: center)[*`lhs`*],
      cgray(colspan: 8, align: center)[*`rhs`*],
      // Línea de bits vacíos
      ..{ let n = 0; while n < bits { n = n + 1; ([],) } },
      // Línea numerada de bits
      ..{
        let n = 0
        while n < bits {
          n = n + 1
          (table.cell(align: center, stroke: none, text(size: 8pt, raw(strfmt("{0:X}", n - 1)))),)
        } 
      },
    )
  }
) <estructura_solicitud>

#grid(
  columns: 2,
  column-gutter: 15pt,
  [
    Un mensaje de solicitud consta de 17 bytes, el primero indica el tipo de operación, y le sigue la representación de los dos argumentos de la operación de 64 bits cada uno correspondiente a `doubles` como se ve en la @estructura_solicitud. Los tipos de operaciones soportados se pueden ver en la @tipos_de_op.
  ],
  [
    #figure(
      caption: [Tipos de operaciones],
      table(
        columns: (auto,) * 2,
        cgray[Tipo], cgray(align: left)[Significado],
        [*`1`*],  table.cell(align: left)[Suma],
        [*`2`*],  table.cell(align: left)[Resta],
        [*`3`*],  table.cell(align: left)[División],
        [*`4`*],  table.cell(align: left)[Multiplicación],
      )
    ) <tipos_de_op>
  ],
)


==== Resultado

Los bytes correspondientes al resultado son 8, conteniendo únicamente la representación binaria del número flotante de 64 bits con el resultado de la operación.

== Identificación <identification>

Durante el proceso de identificación ambas máquinas intercambian información sobre su propósito, y opcionalmente su ID único. Los nodos almacenan la información sobre la conexión para enviar mensajes futuros por medio de la misma, los clientes no tienen por qué almacenar la información que provee un nodo al identificarse, más si deben recibirlo.

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
      (6.8, -2), (15, -5),
      box(align(left)[
        + Almacena el `Socket` de la conexión y tipo como una instancia de `Connection`.\
        + El constructor de `Connection` genera y asigna un id aleatorio.
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

== Flujo de un mensaje

Cuando un nodo recibe una solicitud por parte de un cliente solicitante, las que realiza son:
+ Modificar el _frame_ del `Message` para anotar el id de la conexión de la que llegó el mensaje. De esta forma, podemos identificar a qué conexión se debe enviar la respuesta.
+ Reenviar el mensaje a todos los nodos conocidos.
+ Enviar el mensaje a todos los clientes servidores conocidos.

#figure(
  caption: [Proceso de agregación de metadatos como es el campo `from` y `dest`, un proceso muy similar ocurre con el ID de mensaje con `Message::id` y `Connection::pkg`],
  cetz.canvas({
    import cetz.draw: *
    set-style(mark: (end: ">"))

    content((0, 0), (2, 1.4), 
      box(align(center)[*`Cliente 4444`*], stroke: 1pt, width: 100%, height: 100%, inset: 1em)
    )

    content((5, 0), (7, 1.4), 
      box(align(center)[*`Nodo`*], stroke: 1pt, width: 100%, height: 100%, inset: 1em)
    )

    content((10, 0), (12, 1.4), 
      box(align(center)[*`Servidor 8888`*], stroke: 1pt, width: 100%, height: 100%, inset: 1em)
    )

    line((1, -2), (6, -2), name: "line")
    content(
      ("line.start", 2.5, "line.end"),
      angle: "line.end",
      padding: .2,
      anchor: "south",
      [`Solicitud: Add 4 5`]
    )
    content(
      ("line.start", 2.5, "line.end"),
      angle: "line.end",
      padding: -.4,
      anchor: "south",
      [`dest: Server event: 0000`]
    )

    arc((6,-3.5), start: 360deg, stop: 30deg, radius: -10pt, name: "line") 
    content(
      (6.8, -2.2), (10, -4.4),
      box(align(left)[
      El nodo agrega al mensaje el event correspondiente con el id y msg.
      ], stroke: 1pt, width: 100%, height: 100%, inset: 0.5em)
    )
    
    line((6, -5.5), (11, -5.5), name: "line")
    content(
      ("line.start", 2.5, "line.end"),
      angle: "line.end",
      padding: .2,
      anchor: "south",
      [`Solicitud: Add 4 5`]
    )
    content(
      ("line.start", 2.5, "line.end"),
      angle: "line.end",
      padding: -.4,
      anchor: "south",
      [`dest: Server event: 0123`]
    )

    line((11, -7), (6, -7), name: "line")
    content(
      ("line.start", 2.5, "line.end"),
      angle: "line.start",
      padding: .2,
      anchor: "south",
      [`Resultado: 9`]
    )
    content(
      ("line.start", 2.5, "line.end"),
      angle: "line.start",
      padding: -.4,
      anchor: "south",
      [`dest: Client event: 0123`]
    )

    // arc((6,-9), start: 360deg, stop: 30deg, radius: 10pt, name: "line") 
    // content(
    //   (1.5, -6.7), (5.2, -10),
    //   box(align(left)[
    //     El nodo agrega el `UUID` registrado para el servidor en el campo `from` y pone el `UUID` original en `dest`.
    //   ], stroke: 1pt, width: 100%, height: 100%, inset: 0.5em)
    // )

    line((6, -8), (1, -8), name: "line")
    content(
      ("line.start", 2.5, "line.end"),
      angle: "line.start",
      padding: .2,
      anchor: "south",
      [`Resultado: 9`]
    )
    content(
      ("line.start", 2.5, "line.end"),
      angle: "line.start",
      padding: -.4,
      anchor: "south",
      [`dest: Client event: 0123`]
    )

    line((1,-1.4), (1,-9))
    line((6,-1.4), (6,-9))
    line((11,-1.4), (11,-9))
  })
) <identificacion_nodo>

= Conclusión

Se desarrolló un programa que permite:
- Tener una malla de nodos que transmiten mensajes
- Registrar clientes servidores y solicitantes en los nodos
- Administrar los mensajes de solicitud y respuesta de los clientes de forma que:
  - Le llega el mensaje adecuado a los clientes con base en su tipo (servidor o solicitante) e identificador
  - Le llega una sola respuesta a cada solicitud aunque existan múltiples respuestas enviadas por clientes servidores.
- Se tiene un método para identificar clientes y nodos específicos para el manejo de mensajes



// #show bibliography: set heading (numbering: NUMBERING)
// #bibliography("bib.yml")

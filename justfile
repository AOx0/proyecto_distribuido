celulas: build
    zellij --layout .zellij/celulas.kdl

celulas4: build
    zellij --layout .zellij/celulas4.kdl

nodos: build
    zellij --layout .zellij/nodes.kdl

build:
    mvn clean package --quiet

nodo:
    java -jar nodo/target/nodo-1.0-jar-with-dependencies.jar -- config.toml

solver PORT ADDR="127.0.0.1":
    java -jar celula_servidor/target/celula_servidor-1.0-jar-with-dependencies.jar -- {{ADDR}} {{PORT}}

req PORT ADDR="127.0.0.1":
    java -jar celula_solicitante/target/celula_solicitante-1.0-jar-with-dependencies.jar -- {{ADDR}} {{PORT}}
    

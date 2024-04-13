build:
    mvn package

nodo:
    java -jar nodo/target/nodo-1.0-jar-with-dependencies.jar -- config.toml

server PORT ADDR="127.0.0.1":
    java -jar celula_servidor/target/celula_servidor-1.0-jar-with-dependencies.jar -- {{ADDR}} {{PORT}}

consumer PORT ADDR="127.0.0.1":
    java -jar celula_solicitante/target/celula_solicitante-1.0-jar-with-dependencies.jar -- {{ADDR}} {{PORT}}
    

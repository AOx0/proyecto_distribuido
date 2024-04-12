#!/usr/bin/fish
set -l __run_curr (pwd)

for ruta in (ls)
    if test -e "$ruta/pom.xml"
        echo "Compiling $ruta"
        cd $ruta
        mvn package --quiet
        cd -
    end
end

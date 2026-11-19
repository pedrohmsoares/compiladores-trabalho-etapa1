#!/bin/bash
# Compila o compilador, gera o jar e salva a saída de cada teste em saidas/
# Uso (a partir da pasta trabalhoFinal): bash relatorio/etapa2/gerar_saidas.sh

cd "$(dirname "$0")/../.."
rm -rf bin
javac -encoding UTF-8 -d bin src/*/*.java || exit 1
jar cfe compilador.jar main.Main -C bin .

for f in relatorio/etapa2/fontes/*.txt; do
    nome=$(basename "$f" .txt)
    java -jar compilador.jar "$f" > "relatorio/etapa2/saidas/$nome.out"
    echo "$nome: $(cat "relatorio/etapa2/saidas/$nome.out")"
done

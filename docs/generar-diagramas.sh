#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
mkdir -p docs/imagenes

CHROMIUM="${CHROMIUM:-/usr/bin/chromium}"

echo "Hexágono de puertos y adaptadores"
python3 docs/generar_hexagono.py
"$CHROMIUM" --headless=new --no-sandbox --disable-gpu --hide-scrollbars \
    --force-device-scale-factor=2 --window-size=1500,930 \
    --default-background-color=ffffff \
    --screenshot="$PWD/docs/imagenes/hexagono.png" \
    "file://$PWD/docs/imagenes/hexagono.svg" 2> /dev/null

for fuente in docs/diagramas/*.mmd; do
    nombre=$(basename "$fuente" .mmd)
    echo "Diagrama $nombre"
    for formato in svg png; do
        PUPPETEER_EXECUTABLE_PATH="$CHROMIUM" npx -y @mermaid-js/mermaid-cli \
            -i "$fuente" \
            -o "docs/imagenes/$nombre.$formato" \
            -c docs/mermaid.json \
            -b white \
            --scale 2 > /dev/null
    done
done

echo "Listo. Las imágenes quedaron en docs/imagenes/"

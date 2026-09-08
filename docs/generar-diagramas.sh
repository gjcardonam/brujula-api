#!/usr/bin/env bash
# Regenera los diagramas de docs/imagenes a partir de docs/diagramas.
#
#   ./docs/generar-diagramas.sh
#
# Necesita Node (para mermaid-cli, que se baja con npx) y Python 3 para el hexágono.
# Si el Chromium que trae Puppeteer no funciona en esta máquina, se puede apuntar al del sistema:
#   PUPPETEER_EXECUTABLE_PATH=/usr/bin/chromium ./docs/generar-diagramas.sh
set -euo pipefail

cd "$(dirname "$0")/.."
mkdir -p docs/imagenes

echo "Hexágono de puertos y adaptadores"
python3 docs/generar_hexagono.py

for fuente in docs/diagramas/*.mmd; do
    nombre=$(basename "$fuente" .mmd)
    echo "Diagrama $nombre"
    for formato in svg png; do
        npx -y @mermaid-js/mermaid-cli \
            -i "$fuente" \
            -o "docs/imagenes/$nombre.$formato" \
            -c docs/mermaid.json \
            -b white \
            --scale 2 > /dev/null
    done
done

echo "Listo. Las imágenes quedaron en docs/imagenes/"

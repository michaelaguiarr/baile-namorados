#!/bin/bash
# Executa o seed.sql no banco de dados.
# O seed é idempotente — a tabela db_seeds garante que cada versão rode apenas uma vez.
#
# Uso:
#   ./run-seed.sh              # produção (container baile-db)
#   ./run-seed.sh baile-db-dev # desenvolvimento (container baile-db-dev)

set -e

CONTAINER=${1:-baile-db}
SQL_FILE="$(dirname "$0")/src/main/resources/db/seed.sql"

if [ ! -f "$SQL_FILE" ]; then
  echo "Erro: $SQL_FILE não encontrado."
  exit 1
fi

if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
  echo "Erro: container '$CONTAINER' não está rodando."
  echo "Suba o banco primeiro: docker compose up -d postgres"
  exit 1
fi

echo "Executando seed no container '$CONTAINER'..."
docker exec -i "$CONTAINER" psql -U baile -d baile_namorados < "$SQL_FILE"
echo "Seed concluído."

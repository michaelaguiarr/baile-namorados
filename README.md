# 🌹 Baile dos Namorados — Sistema de Controle de Lista

Sistema web para gerenciar reservas, pagamentos e gerar relatório do Baile dos Namorados da Igreja Nossa Senhora do Perpétuo Socorro.

## Tecnologias

- Java 17 + Spring Boot 3.2
- Spring Security (login com usuário e senha)
- Spring Data JPA + PostgreSQL
- Thymeleaf (templates HTML server-side)
- Docker + Docker Compose

---

## Rodando localmente

A aplicação roda pela IDE ou Maven. Apenas o PostgreSQL sobe no Docker, na porta **5433** (para não conflitar com instalações locais).

### 1. Subir o banco

```bash
docker compose -f docker-compose.dev.yml up -d
```

### 2. Rodar a aplicação com o profile `dev`

**Maven:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**IntelliJ IDEA:**  
`Run/Debug Configurations` → selecione a configuração da aplicação → aba `Spring Boot` → campo **Active profiles**: `dev` → OK

### 3. Acessar

```
http://localhost:8080
```

Login padrão: usuário `admin`, senha `baile@2025`

### 4. Parar o banco

```bash
docker compose -f docker-compose.dev.yml down
```

---

## Deploy na VPS (passo a passo)

### Pré-requisitos na VPS
```bash
# Docker
curl -fsSL https://get.docker.com | sh

# Docker Compose plugin
sudo apt-get install docker-compose-plugin -y

# Verificar
docker --version
docker compose version
```

### 1. Enviar os arquivos para a VPS
```bash
# Na sua máquina local — compacte a pasta
zip -r baile-namorados.zip baile-namorados/

# Envie via scp (substitua user e IP)
scp baile-namorados.zip user@SEU_IP_VPS:~/
```

### 2. Na VPS — descompactar e configurar
```bash
unzip baile-namorados.zip
cd baile-namorados

# Criar o arquivo .env com suas senhas
cp .env.example .env
nano .env   # edite as senhas antes de continuar
```

### 3. Subir os containers
```bash
docker compose up -d --build
```

A primeira build demora alguns minutos (baixa dependências Maven).  
Após concluir, acesse: `http://SEU_IP_VPS:8080`

### 4. Verificar se está rodando
```bash
docker compose ps
docker compose logs app --tail=30
```

---

## Usando com Nginx (recomendado para produção)

Se quiser acessar pelo domínio sem porta (ex: `http://baile.suaigreja.com`):

```nginx
# /etc/nginx/sites-available/baile
server {
    listen 80;
    server_name baile.suaigreja.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/baile /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

---

## Variáveis de ambiente (.env)

| Variável     | Descrição                        | Padrão       |
|-------------|----------------------------------|--------------|
| `DB_PASS`   | Senha do PostgreSQL               | baile123     |
| `ADMIN_USER`| Usuário do painel web             | admin        |
| `ADMIN_PASS`| Senha do painel web               | baile@2025   |
| `APP_PORT`  | Porta exposta no host             | 8080         |

---

## Funcionalidades

- **Login protegido** — somente você acessa
- **Dashboard** com totais: reservas, pagos, pendentes, valor arrecadado
- **Cadastro** de casais (R$ 100) e individuais (R$ 50)
- **Confirmar pagamento** com um clique
- **Busca** por nome
- **Filtros** por status (todos / pagos / pendentes)
- **Relatório** completo com:
  - Resumo financeiro (arrecadado, previsto, a receber)
  - Lista de confirmação na entrada (com caixa para marcar)
  - Lista de pagos com data
  - Lista de pendentes
- **Impressão** otimizada do relatório

---

## Comandos úteis

```bash
# Parar
docker compose down

# Reiniciar
docker compose restart

# Ver logs em tempo real
docker compose logs -f

# Backup do banco
docker exec baile-db pg_dump -U baile baile_namorados > backup.sql

# Restaurar backup
cat backup.sql | docker exec -i baile-db psql -U baile baile_namorados
```

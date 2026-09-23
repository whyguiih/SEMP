<div align="center">

# SEMP
**Sistema de Estoque Multiplataforma**

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Cloudflare-F38020?style=for-the-badge&logo=cloudflare&logoColor=white" alt="Cloudflare Workers" />
  <img src="https://img.shields.io/badge/SQLite_D1-003B57?style=for-the-badge&logo=sqlite&logoColor=white" alt="SQLite D1" />
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle" />
</p>

*Plataforma para gestão centralizada de estoque, carrinho de reservas e pedidos de empréstimos entre unidades SENAI.*

</div>

---

## 📋 Visão Geral

O **SEMP** é uma solução completa para controle de inventário distribuído, composta por um aplicativo Android nativo que consome uma API REST hospedada no **Cloudflare Workers** com banco de dados **SQLite D1 (Serverless)**. O sistema atende unidades como Garibaldi, Farroupilha e Encantado.

### ✨ Principais Funcionalidades

| Funcionalidade | Descrição |
| :--- | :--- |
| **Autenticação** | Login via API com gestão de sessões por unidade e níveis de acesso (0 a 3) |
| **Estoque em Tempo Real** | Listagem de produtos com busca, filtros e atualização instantânea de quantidades |
| **Carrinho & Pedidos** | Seleção de itens, manipulação de carrinho e formalização de reservas de empréstimos inter-unidades |
| **Aprovação (Gestão)** | Gestores autorizam ou recusam solicitações pendentes de suas respectivas filiais |
| **Cadastro de Itens** | Cadastramento de novos materiais com upload de fotos |
| **Rastreamento** | Histórico de movimentações e status de devolução dos empréstimos |

---

## 📱 Aplicativo Android

Construído nativamente em **Kotlin** com arquitetura baseada em Activities e comunicação via **Retrofit2 + Gson**.

### Especificações Técnicas

| Item | Versão/Configuração |
| :--- | :--- |
| **Min SDK** | 28 (Android 9.0) |
| **Target SDK** | 36 (Android 14) |
| **Compile SDK** | 36 |
| **Java/Kotlin** | Java 11 / Kotlin 1.9+ |
| **Build System** | Gradle Kotlin DSL (KTS) |
| **AGP** | 9.2.1 |

### Dependências Principais

```kotlin
// Rede & Serialização
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation(libs.volley)                    // Requisições adicionais

// UI & Material Design
implementation(libs.appcompat)
implementation(libs.material)
implementation(libs.constraintlayout)
implementation(libs.activity.ktx)
implementation(libs.splashscreen)

// Autenticação
implementation(libs.credentials)
implementation(libs.credentials.play.services.auth)
implementation(libs.googleid)                  // Google Sign-In
```

### Estrutura de Telas (Activities)

| Activity | Responsabilidade |
| :--- | :--- |
| `MainActivity` | Login, autenticação e seleção de unidade |
| `EstoqueActivity` | Listagem principal do estoque com busca/filtros |
| `ProdutoDetalheActivity` | Visualização detalhada de um item |
| `CarrinhoActivity` | Gerenciamento do carrinho de reservas |
| `FazerPedidoActivity` | Finalização e envio de pedidos de empréstimo |
| `ConfigEstoqueActivity` | Cadastro/edição de produtos (Operador+) |
| `AutorizarPedidosActivity` | Aprovação/rejeição de pedidos (Gerente+) |
| `CadastrarUsuarioActivity` | Gestão de usuários (Admin) |
| `CadastrarUnidades` | Cadastro de unidades físicas |
| `MovimentacoesActivity` | Histórico de movimentações |
| `RastreioActivity` | Rastreamento de empréstimos |
| `ItensEmprestadosActivity` | Itens atualmente emprestados |
| `VisualizarPedidoActivity` / `TelaPedidoActivity` | Detalhamento de pedidos específicos |

### Models (Entidades)

```
com.example.semp.models
├── Emprestimo.kt
├── Estoque.kt
├── ProdutoCarrinho.kt
├── Usuario.kt
└── ...
```

### Comunicação com API

- **Base URL**: Configurada via `RetrofitClient` / `SempUtils`
- **Formato**: JSON
- **Autenticação**: Sessão baseada em usuário/unidade (enviada nos headers/body)
- **Endpoints principais**: `/estoque`, `/emprestimos`, `/usuarios`, `/carrinho`, `/auth`

---

## 🌐 Backend (Cloudflare Workers + D1)

> **Nota**: O código do backend (Workers) não está neste repositório. Este README documenta a estrutura de dados esperada pela aplicação Android.

### Banco de Dados (SQLite D1)

#### Tabelas Principais

**`tb_estoque`** — Produtos e quantidades
```sql
id_estoque       INTEGER PK
nome             TEXT      -- Ex: "Monitor LED 24 Polegadas"
codigo           TEXT      -- Ex: "MON-001" (único)
descricao        TEXT
descricao_detalhada TEXT
cor              TEXT
quant            INTEGER   -- Quantidade disponível
uni_intermediarias TEXT   -- Setores intermediários (TI, Almoxarifado, etc.)
marca_ref        TEXT
uni_natal        TEXT      -- Unidade de origem
pedido           TEXT      -- '0' ou '1' (se está em pedido)
foto             TEXT      -- Caminho/URL da imagem
uni_atual        TEXT      -- Localização atual
produto_retornado INTEGER  -- Flag de devolução
```

**`tb_emprestimo`** — Solicitações de empréstimo entre unidades
```sql
id_emprestimo    INTEGER PK
nome             TEXT      -- Nome do solicitante
email            TEXT
data_reserva     TEXT
unidade          TEXT      -- Unidade solicitante
nome_produto     TEXT
quant            INTEGER
destinatario     TEXT
processamento    INTEGER   -- 0=pendente, 1=em andamento, 2=concluído
unidade_natal    TEXT
unidade_atual    TEXT
data_devolucao   DATE
data_postagem    DATE
motivo           TEXT
produto_codigo   TEXT      -- Código(s) do(s) produto(s)
prioridade       TEXT      -- 'alto', 'intermediário', 'baixo'
aprovacao        INTEGER   -- 0=pendente, 1=aprovado, 2=rejeitado
```

**`tb_usuarios`** — Contas de acesso
```sql
id             INTEGER PK
usuario        TEXT UNIQUE
senha          TEXT      -- Hash (implementação no backend)
nivel_conta    INTEGER   -- 0 a 3
unidade        TEXT      -- Unidade vinculada
```

**`tb_carrinho`** — Itens temporários no carrinho
```sql
id_carrinho    INTEGER PK
produto        TEXT FK → tb_estoque(nome)
quantidade     INTEGER
usuario        TEXT FK → tb_usuarios(usuario)
carrinho       INTEGER   -- Flag ativo (1)
```

---

## 👥 Níveis de Acesso

O sistema segmenta permissões rigorosamente por nível de conta:

| Nível | Nomenclatura | Permissões |
| :---: | :--- | :--- |
| **0** | **Comum** | Visualizar estoque, gerenciar carrinho próprio, fazer pedidos. Sem painel administrativo. |
| **1** | **Operador** | Permissões de Comum + Cadastrar/editar produtos + Autorizar pedidos da própria base. |
| **2** | **Gerente** | Visualização global do estoque + Aprovar/recusar pedidos gerenciais de movimentação inter-unidades. |
| **3** | **Admin** | Acesso administrativo total + Cadastro de usuários raiz + Gestão de unidades. |

---

## 🚀 Como Executar

### Pré-requisitos
- **Android Studio** Koala (2024.1.2) ou superior
- **JDK 11+**
- **Git**

### Passos

```bash
# 1. Clone o repositório
git clone <url-do-repositorio>
cd SEMP

# 2. Abra no Android Studio
# File → Open → Selecione a pasta do projeto

# 3. Aguarde o Gradle sincronizar (ou execute)
./gradlew sync

# 4. Configure a URL da API
# Edite: app/src/main/java/com/example/semp/RetrofitClient.kt
# Ou: app/src/main/java/com/example/semp/SempUtils.kt

# 5. Execute no emulador ou dispositivo físico (minSdk 28)
```

### Configuração da API

A URL base da API Cloudflare Workers deve ser definida em:
- `RetrofitClient.kt` — Para chamadas Retrofit
- `SempUtils.kt` — Para constantes compartilhadas

```kotlin
// Exemplo
const val BASE_URL = "https://seu-worker.seu-subdominio.workers.dev/"
```

---

## 📁 Estrutura do Projeto

```
SEMP/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/semp/
│   │   │   │   ├── models/           # Entidades de dados
│   │   │   │   ├── *.java            # Activities e Adapters
│   │   │   │   ├── ApiService.java   # Interface Retrofit
│   │   │   │   ├── RetrofitClient.java
│   │   │   │   ├── SempUtils.java
│   │   │   │   └── MenuSidebarHelper.java
│   │   │   ├── res/
│   │   │   │   ├── layout/           # 20+ XMLs de UI
│   │   │   │   ├── values/           # strings, colors, themes
│   │   │   │   ├── font/             # Fontes customizadas (Neo Sans)
│   │   │   │   └── xml/              # Backup & data extraction rules
│   │   │   └── assets/
│   │   │       └── index.js          # (Possível WebView/híbrido)
│   │   └── test/ / androidTest/      # Testes unitários e instrumentados
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml            # Catálogo de versões
│   └── wrapper/
├── db.sql                            # Schema + seeds do SQLite D1
├── build.gradle.kts                  # Configuração root
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## 🗄️ Script de Banco de Dados

O arquivo [`db.sql`](db.sql) contém:
- Schema completo das 4 tabelas principais
- Índices únicos (`usuario`, `produto`)
- Dados de seed para desenvolvimento/teste:
  - 5 produtos de exemplo (Monitor, Teclado, Cadeira, etc.)
  - 14 usuários de teste (todos níveis, múltiplas unidades)
  - 37 empréstimos de exemplo com diversos status
  - Itens de carrinho de exemplo

> **Útil para**: Testes locais, desenvolvimento do backend, migrações D1.

---

## 🔧 Scripts Gradle Úteis

```bash
# Compilar debug
./gradlew assembleDebug

# Executar testes unitários
./gradlew test

# Executar testes instrumentados (requer device/emulator)
./gradlew connectedAndroidTest

# Verificar dependências
./gradlew dependencies

# Limpar build
./gradlew clean
```

---

## 📝 Licença

Este projeto é de uso interno para fins educacionais e operacionais das unidades SENAI.

---

## 🤝 Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'feat: adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

---

<div align="center">

**Desenvolvido para otimizar a gestão de recursos entre unidades SENAI**

</div>
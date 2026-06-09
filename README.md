# SEMP — Sistema de Estoque e Movimentação de Produtos

Aplicação web em **PHP** para gestão de estoque, carrinho de reservas e pedidos entre unidades SENAI. O frontend comunica com uma API REST hospedada num **Cloudflare Worker**; não há base de dados local no PHP.

**Autores:** Threeeo (Gabriel Artuso, Guilherme Brandalize, Larissa Gazoli) — © 2026

---

## Índice

- [Visão geral](#visão-geral)
- [Requisitos e instalação](#requisitos-e-instalação)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Arquitetura](#arquitetura)
- [Documentação detalhada](#documentação-detalhada)
- [Fluxos principais](#fluxos-principais)
- [Níveis de conta](#níveis-de-conta)

---

## Visão geral


| Funcionalidade | Descrição                                                           |
| -------------- | ------------------------------------------------------------------- |
| Login          | Autenticação via API; sessão PHP guarda utilizador, nível e unidade |
| Estoque        | Lista produtos com pesquisa em tempo real (JavaScript)              |
| Produto        | Detalhe, quantidade e adicionar ao carrinho                         |
| Carrinho       | Seleção de itens, remoção e avanço para pedido                      |
| Pedido         | Formulário de reserva (nome, email, data) e submissão à API         |
| Autorização    | Gestores aprovam ou recusam pedidos pendentes da unidade            |
| Cadastro       | Administradores registam novos produtos (com upload de foto)        |


---

## Requisitos e instalação

1. **PHP** 7.4+ com extensões `curl` e `session` ativas
2. Servidor web (ex.: **XAMPP**, Apache, nginx + php-fpm)
3. Pasta `uploads/` com permissão de escrita (criada automaticamente no cadastro de produtos)
4. Imagens em `img/` (logos SENAI, ícones da sidebar)

### Passos

```bash
# Colocar o projeto na pasta do servidor (ex.: htdocs/semp_web)
# Editar a URL da API em api.php se necessário
```

Abrir no browser: `http://localhost/semp_web/index.php`

### Configuração da API

Em `api.php`, altere a constante se o Worker mudar de URL:

```php
define('API_URL', 'https://api-estoque.whyguiih.workers.dev');
```

---

## Estrutura do projeto

```
semp_web-main-main/
├── api.php                 # Cliente HTTP para o Cloudflare Worker
├── index.php               # Página de login
├── processa_login.php      # Processamento do login
├── logout.php              # Encerrar sessão
├── estoque.php             # Grelha de produtos
├── produto.php             # Detalhe do produto
├── cadastro_produto.php    # Cadastro (nível 1)
├── acao_carrinho.php       # Adicionar ao carrinho
├── remover_carrinho.php    # Remover item do carrinho
├── carrinho.php            # Listagem do carrinho
├── tela_pedido.php         # Dados da reserva
├── fazer_pedido.php        # Submissão do pedido
├── autorizar_pedidos.php   # Aprovar/recusar pedidos
├── info.php                # phpinfo() (apenas desenvolvimento)
├── css/
│   ├── style.css           # Estilos do login
│   └── dashboard.css       # Estilos das páginas internas
├── img/                    # Logos e ícones (não versionados neste repo)
├── uploads/                # Fotos enviadas no cadastro
└── docs/
    └── DOCUMENTACAO.md     # Referência completa (funções, API, ficheiros)
```

---

## Arquitetura

```
┌─────────────┐     chamarAPI()      ┌──────────────────────────┐
│  Browser    │ ◄──────────────────► │  PHP (sessões + views)   │
└─────────────┘                      └────────────┬─────────────┘
                                                  │ cURL + JSON
                                                  ▼
                                       ┌──────────────────────────┐
                                       │ Cloudflare Worker (API)  │
                                       │ + base de dados na nuvem │
                                       └──────────────────────────┘
```

- **Estado de autenticação:** `$_SESSION` no PHP  
- **Dados de negócio:** sempre via API; o PHP não consulta MySQL diretamente

---

## Documentação detalhada

Consulte **[docs/DOCUMENTACAO.md](docs/DOCUMENTACAO.md)** para:

- Função `chamarAPI()` e todos os endpoints
- Variáveis de sessão e permissões por ficheiro
- Campos de produtos e pedidos
- JavaScript embutido nas páginas
- Classes CSS principais
- Notas de segurança e limitações conhecidas

---

## Fluxos principais

### Utilizador comum

`index.php` → `processa_login.php` → `estoque.php` → `produto.php` → `acao_carrinho.php` → `carrinho.php` → `tela_pedido.php` → `fazer_pedido.php` → mensagem de sucesso no carrinho

### Gestor (nível 1 ou 2)

`autorizar_pedidos.php` → aceitar (`novoStatus=1`) ou recusar (`novoStatus=2`) via query string

### Administrador (nível 1)

Acesso extra a `cadastro_produto.php` e ícone de cadastro na sidebar

---

## Níveis de conta


| `nivel_conta` | Acesso                                              |
| ------------- | --------------------------------------------------- |
| `0`           | Estoque, produto, carrinho, pedidos (sem autorizar) |
| `1`           | Tudo + cadastro de produtos + autorizar pedidos     |
| `2`           | Estoque + autorizar pedidos (sem cadastro)          |


A sidebar mostra links conforme `$_SESSION['nivel_conta']`.

---

## Licença e contacto

Copyright © Threeeo 2026. Todos os direitos reservados.

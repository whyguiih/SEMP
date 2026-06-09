<div align="center">

# SEMP
**Sistema de Estoque Multiplataforma**
<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/PHP-777BB4?style=for-the-badge&logo=php&logoColor=white" alt="PHP" />
  <img src="https://img.shields.io/badge/Cloudflare-F38020?style=for-the-badge&logo=cloudflare&logoColor=white" alt="Cloudflare" />
  <img src="https://img.shields.io/badge/SQLite_D1-003B57?style=for-the-badge&logo=sqlite&logoColor=white" alt="SQLite D1" />
</p>

*Plataforma para gestão centralizada de estoque, carrinho de reservas e pedidos de empréstimos entre unidades SENAI.*

</div>

---

##  Visão Geral

O **SEMP** é uma solução completa para controle de inventário. O projeto conta com um sistema web (painel em PHP) e um aplicativo nativo para dispositivos móveis. Todo o ecossistema se comunica através de uma API REST hospedada nativamente no **Cloudflare Workers**.

###  Principais Funcionalidades

| Funcionalidade | Descrição |
| :--- | :--- |
| **Autenticação** | Login integrado via API; gestão de sessões por unidade e níveis de acesso. |
| **Estoque Vivo** | Listagem de produtos (chaves, alicates, tijolos, etc.) com atualização e pesquisa em tempo real. |
| **Carrinho & Pedidos** | Seleção de itens, manipulação de carrinho e formalização de reservas de empréstimos inter-unidades. |
| **Aprovação (Gestão)** | Gestores autorizam ou recusam as solicitações pendentes para as suas respectivas filiais. |
| **Gestão de Itens** | Cadastramento de novos materiais (com captura/upload de fotos). |

---

##  Aplicativo Android

O aplicativo Android foi construído nativamente em **Kotlin** para fornecer agilidade nas operações das unidades (como Garibaldi, Farroupilha e Encantado).

* **SDKs Suportados:** Mínimo SDK 29, com o projeto focado (Target) na API 36.
* **Comunicação Web:** Implementação com a biblioteca `Retrofit2` (versão 2.9.0) com conversor GSON para consumo rápido da API REST.
* **Módulos do App (Telas principais):** O aplicativo cobre de forma fluida os fluxos web através das atividades `MainActivity`, `EstoqueActivity`, `ProdutoDetalheActivity`, `CarrinhoActivity`, `FazerPedidoActivity`, `ConfigEstoqueActivity`, `AutorizarPedidosActivity` e `CadastrarUsuarioActivity`.

---

##  Aplicação Web

O dashboard na web requer os seguintes pré-requisitos:
1. **PHP 7.4+** com extensões `curl` e `session` habilitadas.
2. Um servidor web compatível (Apache, XAMPP, Nginx).
3. Diretórios com permissão correta (`/uploads` para salvar anexos e `/img` para interface).

### Arquitetura de Comunicação
A interface PHP não armazena e nem consulta o banco de dados localmente. Todas as requisições de negócio são disparadas via requisições HTTP (utilizando a função interna `chamarAPI()`) com formato de transporte **JSON**, processadas pela camada de servidor do **Cloudflare**.

---

##  Estrutura de Dados (Cloudflare D1)

A base de dados remota utiliza migrações D1 para SQLite Serverless. Ela é fundamentada em três grandes coleções principais:

* `tb_estoque`: Salva as referências, quantidades em tempo real, unidade natural do objeto e rastreia itens atualmente em carrinhos (`carrinho`) ou sob encomenda (`pedido`).
* `tb_emprestimo`: Relaciona nome, e-mail, origem e destino dos empréstimos, com colunas para `processamento` e `aprovacao` das diretorias.
* `tb_usuarios`: Mantém as credenciais cadastradas e a vinculação de cada usuário com o nível de conta e sua respectiva unidade física.

---

##  Níveis de Conta

O sistema segmenta de forma rigorosa as permissões na aplicação:

<div align="center">

| Nível | Nomenclatura | Permissões Concedidas |
| :---: | :--- | :--- |
| **0** | **Comum** | Visualizar estoque, gerir carrinho e fazer pedidos de material. Não tem painel administrativo. |
| **1** | **Operador** | Permissões Comuns + Acesso livre ao Cadastro de novos Produtos + Autorizar Pedidos da base. |
| **2** | **Gerente** | Visualização do estoque global + Aprovar e recusar pedidos gerenciais de movimentação. |
| **3** | **Admin** | Permissão administrativa ampla global da plataforma e cadastros de usuários raiz (ex: Administrador). |

</div>

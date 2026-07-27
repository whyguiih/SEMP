export default {
  // =========================================================
  // 1. FUNÇÃO FETCH (ATENDE OS CLIQUES DOS USUÁRIOS E DO PHP)
  // =========================================================
  async fetch(request, env) {
    const corsHeaders = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, X-Usuario-ID",
    };

    if (request.method === "OPTIONS") return new Response(null, { headers: corsHeaders });

    const url = new URL(request.url);
    let path = url.pathname;
    if (path.endsWith("/") && path.length > 1) path = path.slice(0, -1);

    try {
      // =========================================================
           // =========================================================
      // LOGIN COM RECUPERAÇÃO DE IDENTIFICADORES DE UNIDADE
      // =========================================================
      if (request.method === "POST" && path === "/login") {
        const { usuario, senha } = await request.json();
        
        // 1. Busca o usuário
        const { results: userResults } = await env.DB.prepare("SELECT * FROM tb_usuarios WHERE usuario = ? AND senha = ?")
          .bind(usuario, senha)
          .all();

        if (userResults && userResults.length > 0) {
          const user = userResults[0];

          // 2. Busca os caracteres identificadores da unidade deste usuário na tb_unidade
          const { results: unidadeInfo } = await env.DB.prepare(`
            SELECT caracter_identificador_estado, 
                   caracter_identificador_regiao, 
                   caracter_identificador_unidade 
            FROM tb_unidade 
            WHERE nome_unidade = ?
          `).bind(user.unidade).all();
          
          const ids = unidadeInfo && unidadeInfo.length > 0 ? unidadeInfo[0] : {};

          // 3. Retorna tudo para o Aplicativo salvar na sessão
          return new Response(JSON.stringify({ 
            sucesso: true, 
            mensagem: "Login efetuado!", 
            usuario: user.usuario, 
            nivel_conta: String(user.nivel_conta), 
            unidade: user.unidade,
            // Estes campos são essenciais para o gerador de código de produto
            estado_identificador: ids.caracter_identificador_estado || "X",
            regiao_identificador: ids.caracter_identificador_regiao || "X",
            unidade_identificador: ids.caracter_identificador_unidade !== undefined ? ids.caracter_identificador_unidade : 0
          }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
        }

        return new Response(JSON.stringify({ sucesso: false, mensagem: "Usuário ou senha incorretos!" }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }

    else if (request.method === "GET" && path === "/produtos") {
  const hoje = new Date().toISOString().split('T')[0]; // Pega a data de hoje (AAAA-MM-DD)

  const { results } = await env.DB.prepare(`
    SELECT e.*, 
    (e.quant - IFNULL((
        SELECT SUM(it.quantidade_produto)
        FROM tb_itens_pedido it
        JOIN tb_emprestimo emp ON it.pedido_produto = emp.codigo_pedido
        WHERE it.nome_produto = e.nome 
        AND (emp.aprovacao = 1 OR emp.aprovacao = 3)
        AND date(?) >= date(substr(emp.data_reserva, 1, 10))
        AND date(?) <= date(substr(emp.data_reserva, -10))
    ), 0)) as estoque_real,
    (SELECT emp.data_reserva 
     FROM tb_emprestimo emp 
     JOIN tb_itens_pedido it ON emp.codigo_pedido = it.pedido_produto 
     WHERE it.nome_produto = e.nome 
     AND (emp.aprovacao = 1 OR emp.aprovacao = 3)
     ORDER BY emp.id_emprestimo DESC LIMIT 1) as data_reserva
    FROM tb_estoque e
  `).bind(hoje, hoje).all();
  
  return new Response(JSON.stringify(results), { headers: { "Content-Type": "application/json", ...corsHeaders } });
}

      // =========================================================
      // CARRINHO DE COMPRAS
      // =========================================================
      else if (request.method === "POST" && path === "/carrinho/adicionar") {
        const usuarioLogado = request.headers.get("X-Usuario-ID");
        if (!usuarioLogado) return new Response(JSON.stringify({ erro: "Não autenticado" }), { status: 401, headers: { "Content-Type": "application/json", ...corsHeaders } });
        const corpo = await request.json();
        const { results: existe } = await env.DB.prepare("SELECT * FROM tb_carrinho WHERE produto = ? AND usuario = ?").bind(corpo.nome_produto, usuarioLogado).all();

        if (existe && existe.length > 0) {
          await env.DB.prepare("UPDATE tb_carrinho SET quantidade = quantidade + ? WHERE produto = ? AND usuario = ?").bind(corpo.quantidade || 1, corpo.nome_produto, usuarioLogado).run();
        } else {
          await env.DB.prepare("INSERT INTO tb_carrinho (produto, quantidade, usuario, carrinho) VALUES (?, ?, ?, 1)").bind(corpo.nome_produto, corpo.quantidade || 1, usuarioLogado).run();
        }
        return new Response(JSON.stringify({ sucesso: true }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }

      else if (request.method === "POST" && path === "/carrinho/remover") {
        const usuarioLogado = request.headers.get("X-Usuario-ID");
        const { nome_produto } = await request.json();
        await env.DB.prepare("DELETE FROM tb_carrinho WHERE produto = ? AND usuario = ?").bind(nome_produto, usuarioLogado).run();
        return new Response(JSON.stringify({ sucesso: true }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }

      else if (request.method === "GET" && path === "/carrinho") {
        const usuarioLogado = request.headers.get("X-Usuario-ID");
        if (!usuarioLogado) return new Response(JSON.stringify({ erro: "Não autenticado" }), { status: 401, headers: { "Content-Type": "application/json", ...corsHeaders } });

        const { results } = await env.DB.prepare(
          `SELECT c.*, 
                  e.nome, 
                  e.foto, 
                  SUM(e.quant) as estoque_max
           FROM tb_carrinho c 
           LEFT JOIN tb_estoque e ON c.produto = e.nome 
           WHERE c.carrinho = 1 AND c.usuario = ?
           GROUP BY c.produto`
        ).bind(usuarioLogado).all();
        
        return new Response(JSON.stringify(results), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }

      else if (request.method === "GET" && path === "/pedidos/pendentes") {
        const req_unidade = url.searchParams.get("unidade") || "";
        let query;
        let params = [];

        const baseSelect = `
            SELECT e.*, 
                   GROUP_CONCAT(i.nome_produto, ', ') as nome_produto,
                   GROUP_CONCAT(i.codigo_produto, ', ') as produto_codigo,
                   SUM(i.quantidade_produto) as quant
            FROM tb_emprestimo e
            LEFT JOIN tb_itens_pedido i ON e.codigo_pedido = i.pedido_produto
        `;

        if (req_unidade !== "") {
            query = baseSelect + " WHERE e.processamento = 1 AND ((e.aprovacao = 0 AND i.unidade_produto = ?) OR (e.aprovacao = 3 AND i.unidade_produto = ?)) GROUP BY e.id_emprestimo";
            params = [req_unidade, req_unidade];
        } else {
            query = baseSelect + " WHERE e.processamento = 1 AND (e.aprovacao = 0 OR e.aprovacao = 3) GROUP BY e.id_emprestimo";
        }

        const { results } = await env.DB.prepare(query).bind(...params).all();
        return new Response(JSON.stringify(results), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }

      else if (request.method === "GET" && path === "/pedidos") {
        const usuario = url.searchParams.get("usuario") || "";
        const nivel = url.searchParams.get("nivel") || "0";
        const req_unidade = url.searchParams.get("unidade") || "";

        let query = "";
        let params = [];

        const baseSelect = `
            SELECT e.*, 
                   GROUP_CONCAT(i.nome_produto, ', ') as nome_produto,
                   GROUP_CONCAT(i.codigo_produto, ', ') as produto_codigo,
                   SUM(i.quantidade_produto) as quant
            FROM tb_emprestimo e
            LEFT JOIN tb_itens_pedido i ON e.codigo_pedido = i.pedido_produto
        `;

        if (nivel === "0") {
          query = baseSelect + " WHERE e.nome = ? GROUP BY e.id_emprestimo ORDER BY e.id_emprestimo DESC";
          params = [usuario];
        } else if (nivel === "1" || nivel === "2") {
          query = baseSelect + " WHERE i.unidade_produto = ? GROUP BY e.id_emprestimo ORDER BY e.id_emprestimo DESC";
          params = [req_unidade];
        } else if (nivel === "3") {
          query = baseSelect + " GROUP BY e.id_emprestimo ORDER BY e.id_emprestimo DESC";
          params = [];
        } else {
          return new Response(JSON.stringify([]), { headers: { "Content-Type": "application/json", ...corsHeaders } });
        }

        const { results } = await env.DB.prepare(query).bind(...params).all();
        return new Response(JSON.stringify(results), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }

      else if (request.method === "GET" && path === "/pedidos/emprestados") {
        const req_unidade = url.searchParams.get("unidade"); 
        const baseSelect = `
            SELECT e.*, 
                   GROUP_CONCAT(i.nome_produto, ', ') as nome_produto,
                   GROUP_CONCAT(i.codigo_produto, ', ') as produto_codigo,
                   SUM(i.quantidade_produto) as quant
            FROM tb_emprestimo e
            LEFT JOIN tb_itens_pedido i ON e.codigo_pedido = i.pedido_produto
            WHERE i.unidade_produto = ? AND e.aprovacao = 1
            GROUP BY e.id_emprestimo
        `;
        const { results } = await env.DB.prepare(baseSelect).bind(req_unidade).all();
        return new Response(JSON.stringify(results), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }

      // =========================================================
      // OUTRAS FUNCIONALIDADES SECUNDÁRIAS MANTIDAS
      // =========================================================
      else if (request.method === "POST" && path === "/pedidos/autorizar") {
        const { id_emprestimo, novoStatus } = await request.json();
        await env.DB.prepare("UPDATE tb_emprestimo SET aprovacao = ? WHERE id_emprestimo = ?").bind(novoStatus, id_emprestimo).run();
        return new Response(JSON.stringify({ sucesso: true }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }

      else if (request.method === "POST" && path === "/pedidos/solicitar_retorno") {
        const { id_emprestimo, data_retorno } = await request.json(); 
        await env.DB.prepare("UPDATE tb_emprestimo SET aprovacao = 3, data_reserva = ? WHERE id_emprestimo = ?").bind(data_retorno, id_emprestimo).run();
        return new Response(JSON.stringify({ sucesso: true }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }

      else if (request.method === "POST" && path === "/usuario/cadastrar") {
        try {
          const corpo = await request.json();
          await env.DB.prepare("INSERT INTO tb_usuarios (usuario, senha, nivel_conta, unidade) VALUES (?, ?, ?, ?)").bind(corpo.usuario, corpo.senha, corpo.nivel || corpo.nivel_conta || "0", corpo.unidade || "SENAI").run();
          return new Response(JSON.stringify({ sucesso: true, mensagem: "Usuário cadastrado com sucesso!" }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
        } catch (erroDB) {
          return new Response(JSON.stringify({ sucesso: false, mensagem: "Erro ao salvar: " + erroDB.message }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
        }
      }

   else if (request.method === "POST" && path === "/unidade/cadastrar") {
  try {
    const corpo = await request.json();
    
    // O .trim() garante que se o front-end mandar com espaço no final, ele limpa antes de buscar
    const regiaoOriginal = corpo.regiao?.trim(); 
    const estadoOriginal = corpo.estado?.trim();
     const unidadeOriginal = corpo.identificacao?.trim();

    const mapeamentoRegiao = {
      'Metropolitana 1': 'a',
      'Metropolitana 3': 'b',
      'Serra 1': 'c',
      'Metropolitana 2': 'd',
      'Vale dos Sinos 2': 'e',
      'Noroeste 2': 'f',
      'Noroeste 1': 'g',
      'Vale dos Sino 1': 'h',  // Verifique se não é "Vale dos Sinos 1" com 's'
      'Central': 'i',
      'Vale do Rio Pardo': 'j',
      'Serra 3': 'k',
      'Vale do Taquari 2': 'l',
      'Norte 1': 'm',
      'Sul 1': 'n',
      'Vale dos Sinos 3': 'o',
      'Norte 2': 'p',
      'Vale do Taquari 1': 'q',
      'Serra 2': 'r',
      'Encosta da Serra': 's',
      'Sul 2': 't'
    };

    const mapeamentoEstado = {
      'Rio Grande do Sul': 'A',
      'Santa Catarina': 'B',
      'Paraná': 'C',
      'São Paulo': 'D',
      'Rio de Janeiro': 'E',
      'Espiríto Santo': 'F',
      'Minas Gerais': 'G',
      'Goiás': 'H',
      'Mato Grosso': 'I',
      'Mato Grosso do Sul': 'J',
      'Rio Grande do Norte': 'K',
      'Acre': 'L',
      'Amapá': 'M',
      'Amazonas': 'N',
      'Pará': 'O',
      'Rondônia': 'P',
      'Roraima': 'Q',
      'Tocantins': 'R',
      'Alagoas': 'S',
      'Bahia': 'T',
      'Ceará': 'U',
      'Maranhão': 'V',
      'Paraíba': 'W',
      'Pernambuco': 'X',
      'Piauí': 'Y',
      'Sergipe': 'Z'
    };


 const mapeamentoUnidade = {
      'Adendo': 1,
      'Unidade': 0
    };

    const regiaoReformulada = mapeamentoRegiao[regiaoOriginal] ?? null;
    const estadoReformulada = mapeamentoEstado[estadoOriginal] ?? null;
    const unidadeReformulada = mapeamentoUnidade[unidadeOriginal] ?? null;


    // ATENÇÃO: No bind, passamos as variáveis reformuladas e protegemos o restante com ?? null
    await env.DB.prepare(
      "INSERT INTO tb_unidade (nome_unidade, estado, regiao, identificacao_uni, caracter_identificador_unidade,caracter_identificador_regiao, caracter_identificador_estado) VALUES (?, ?, ?, ?,?,?,?)"
    )
    .bind(
      corpo.nome_unidade ?? null, 
      corpo.estado ?? null,
      corpo.regiao?? null,
      corpo.identificacao ?? null,
      unidadeReformulada,
      regiaoReformulada, 
      estadoReformulada    
      
    )
    .run();

    return new Response(
      JSON.stringify({ sucesso: true, mensagem: "Unidade cadastrada com sucesso!" }), 
      { headers: { "Content-Type": "application/json", ...corsHeaders } }
    );
    
  } catch (erroDB) {
    return new Response(
      JSON.stringify({ sucesso: false, mensagem: "Erro ao salvar: " + erroDB.message }), 
      { headers: { "Content-Type": "application/json", ...corsHeaders } }
    );
  }
}



      else if (request.method === "POST" && path === "/pedido/rastreio") {
        try {
          const corpo = await request.json();
          await env.DB.prepare("INSERT INTO tb_rastreio (codigo, unidade_original, unidade_destino, data_entrada, data_saida) VALUES (?, ?, ?, ?, ?)").bind(corpo.codigo, corpo.unidade_original, corpo.unidade_destino, corpo.data_entrada, corpo.data_saida).run();
          return new Response(JSON.stringify({ sucesso: true }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
        } catch (erroDB) {
          return new Response(JSON.stringify({ erro: "Erro ao salvar no banco: " + erroDB.message }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
        }
      }

      else if (request.method === "POST" && path === "/produto/cadastrar") {
        const { nome, codigo, descricao, quant, uni_natal, marca_ref, cor, descricao_detalhada, foto, codigo_rfid } = await request.json();
        await env.DB.prepare("INSERT INTO tb_estoque (nome, codigo, descricao, quant, uni_natal, marca_ref, cor, descricao_detalhada, foto, codigo_rfid ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?)").bind(nome || "", codigo || "", descricao || "", quant || 0, uni_natal || "", marca_ref || "", cor || "", descricao_detalhada || "", foto || "", codigo_rfid || "").run();
        return new Response(JSON.stringify({ sucesso: true, mensagem: "Produto cadastrado!" }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }


      // =========================================================
      // NOVA ARQUITETURA: SALVAR PEDIDO COM VALIDAÇÃO DE DATAS
      // =========================================================
      else if (request.method === "POST" && (path === "/pedidos/solicitar" || path === "/pedido/fazer")) {
        const payload = await request.json();
        const { remetente, email, data_reserva, produtos, prioridade, motivo, data_postagem, codigo_pedido, unidade } = payload;
        const prioFormatada = prioridade ? prioridade.toLowerCase() : 'baixo';

        // 1. Extrair datas para validação de sobreposição
        const partesNovas = data_reserva.split(/\s+até\s+/);
        if (partesNovas.length !== 2) {
          return new Response(JSON.stringify({ sucesso: false, mensagem: "Formato de período inválido!" }), { headers: corsHeaders });
        }
        const novoInicio = partesNovas[0];
        const novoFim = partesNovas[1];

        try {
          // 2. VALIDAÇÃO DE CONFLITO (OVERLAP)
         // 2. VALIDAÇÃO DE ESTOQUE POR PERÍODO
if (produtos && produtos.length > 0) {
  for (const item of produtos) {
    // Busca estoque total e quanto já está ocupado no período solicitado
    const dadosEstoque = await env.DB.prepare(`
      SELECT e.quant as total,
      (SELECT SUM(it.quantidade_produto)
       FROM tb_itens_pedido it
       JOIN tb_emprestimo emp ON it.pedido_produto = emp.codigo_pedido
       WHERE it.nome_produto = e.nome 
       AND (emp.aprovacao = 1 OR emp.aprovacao = 3)
       AND (date(?) <= date(substr(emp.data_reserva, -10)) AND date(substr(emp.data_reserva, 1, 10)) <= date(?))
      ) as ocupado
      FROM tb_estoque e WHERE e.nome = ?
    `).bind(novoInicio, novoFim, item.nome_produto).first();

    const disponivelNoPeriodo = dadosEstoque.total - (dadosEstoque.ocupado || 0);

    if (item.quantidade_produto > disponivelNoPeriodo) {
      return new Response(JSON.stringify({ 
        sucesso: false, 
        mensagem: `Estoque insuficiente para o período! Disponível: ${disponivelNoPeriodo} unidades de '${item.nome_produto}'.` 
      }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
    }
  }
}

          // 3. BUSCA UNIDADE DO USUÁRIO
          const { results: userResults } = await env.DB.prepare("SELECT unidade FROM tb_usuarios WHERE usuario = ?").bind(remetente).all();
          const unidade_oficial = (userResults && userResults.length > 0) ? userResults[0].unidade : unidade;

          // 4. INSERE O CABEÇALHO DO PEDIDO
          await env.DB.prepare(`
            INSERT INTO tb_emprestimo (nome, email, data_reserva, prioridade, motivo, data_postagem, codigo_pedido, processamento, aprovacao, unidade_atual, unidade_natal, destinatario) 
            VALUES (?, ?, ?, ?, ?, ?, ?, 1, 0, ?, ?, ?)
          `).bind(remetente, email, data_reserva, prioFormatada, motivo, data_postagem, codigo_pedido, unidade_oficial, unidade_oficial, unidade_oficial).run();

          // 5. REGISTRA OS ITENS E LIMPA O CARRINHO
          if (produtos && produtos.length > 0) {
            for (const item of produtos) {
              await env.DB.prepare(`
                INSERT INTO tb_itens_pedido (
                  codigo_produto, nome_produto, quantidade_produto, unidade_produto, 
                  descricao_produto, descricao_detalhada_produto, cor_produto, marca_produto, pedido_produto
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
              `).bind(
                item.codigo_produto, item.nome_produto, item.quantidade_produto, 
                item.unidade_produto, item.descricao_produto || "", item.descricao_detalhada_produto || "", item.cor_produto || "", item.marca_produto || "", item.pedido_produto
              ).run();

              await env.DB.prepare("DELETE FROM tb_carrinho WHERE produto = ? AND usuario = ?").bind(item.nome_produto, remetente).run();
            }
          }
          
          return new Response(JSON.stringify({ sucesso: true, mensagem: "Pedido realizado com sucesso!" }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
          
        } catch (error) {
          return new Response(JSON.stringify({ erro: "Falha no banco: " + error.message }), { status: 500, headers: { "Content-Type": "application/json", ...corsHeaders } });
        }
      }



      else if (request.method === "POST" && path === "/produto/atualizar") {
        const corpo = await request.json();
        const { id, coluna, valor } = corpo;

        // Lista de colunas permitidas para atualização individual
        const colunasPermitidas = ["nome", "codigo", "descricao", "descricao_detalhada", "uni_atual", "quant", "cor", "marca_ref", "codigo_rfid", "uni_natal", "foto"];

        if (coluna) {
          // Lógica legada: atualiza apenas uma coluna por vez
          if (!colunasPermitidas.includes(coluna)) {
            return new Response(JSON.stringify({ sucesso: false, mensagem: "Coluna não permitida." }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
          }
          await env.DB.prepare(`UPDATE tb_estoque SET ${coluna} = ? WHERE id_estoque = ?`).bind(valor, id).run();
        } else {
          // Lógica para atualização completa vinda do objeto UpdateProdutoRequest do App
          await env.DB.prepare(`
            UPDATE tb_estoque SET
              nome = ?,
              codigo = ?,
              codigo_rfid = ?,
              descricao = ?,
              quant = ?,
              uni_natal = ?,
              uni_atual = ?,
              cor = ?,
              marca_ref = ?,
              descricao_detalhada = ?,
              foto = COALESCE(?, foto)
            WHERE id_estoque = ?
          `).bind(
            corpo.nome || "",
            corpo.codigo || "",
            corpo.codigo_rfid || "",
            corpo.descricao || "",
            corpo.quant || 0,
            corpo.uni_natal || "",
            corpo.uni_intermediarias || corpo.uni_atual || "",
            corpo.cor || "",
            corpo.marca_ref || "",
            corpo.descricao_detalhada || "",
            corpo.foto || null,
            id
          ).run();
        }
        return new Response(JSON.stringify({ sucesso: true, mensagem: "Produto atualizado!" }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }

      else if (request.method === "POST" && path === "/produto/deletar") {
        const { codigo } = await request.json();
        await env.DB.prepare("DELETE FROM tb_estoque WHERE codigo = ?").bind(codigo).run();
        return new Response(JSON.stringify({ sucesso: true, mensagem: "Produto deletado!" }), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }

      else if (request.method === "GET" && path === "/rastreio/todos") {
        const { results } = await env.DB.prepare("SELECT * FROM tb_rastreio ORDER BY id_rastreio ASC").all();
        return new Response(JSON.stringify(results), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }

      else if (request.method === "GET" && path === "/rastreio/hoje") {
        const hoje = new Date().toISOString().split('T')[0]; 
        const { results } = await env.DB.prepare("SELECT * FROM tb_rastreio WHERE data_entrada = ?").bind(hoje).all();
        return new Response(JSON.stringify(results), { headers: { "Content-Type": "application/json", ...corsHeaders } });
      }

      return new Response(JSON.stringify({ erro: `Rota não encontrada: ${path}` }), { status: 404, headers: { "Content-Type": "application/json", ...corsHeaders } });

    } catch (e) {
      return new Response(JSON.stringify({ erro: e.message }), { status: 500, headers: { "Content-Type": "application/json", ...corsHeaders } });
    }
  }, 

  // =========================================================
  // 2. FUNÇÃO SCHEDULED (RODA SOZINHA COM O RELÓGIO)
  // =========================================================
  async scheduled(event, env, ctx) {
      const hoje = new Date().toISOString().split('T')[0]; 
      const { results } = await env.DB.prepare("SELECT * FROM tb_rastreio WHERE data_entrada = ?").bind(hoje).all();
      if (results && results.length > 0) {
          for (let pedido of results) {
              // Lógica agendada vai aqui
          }
      }
  }
};
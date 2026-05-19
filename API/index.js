const express = require('express');
const mysql = require('mysql2');

const app = express();
app.use(express.json());

// Configuração do banco baseada no seu código original
const db = mysql.createConnection({
    host: 'localhost',   // Como a API roda no PC, o MySQL está em localhost
    user: 'root',
    password: '',
    database: 'db_estoque' // Seu banco
});

db.connect((err) => {
    if (err) console.error('Erro de conexão com MySQL:', err);
    else console.log('Conectado ao MySQL - db_estoque');
});

// Rota que o Android vai acessar para fazer login
app.post('/login', (req, res) => {
    const { usuario, senha } = req.body;

    if (!usuario || !senha) {
        return res.status(400).json({ sucesso: false, mensagem: "Usuário ou senha vazios." });
    }

    // Consulta na sua tabela
    const sql = "SELECT * FROM tb_usuarios WHERE usuario = ? AND senha = ?";
    db.query(sql, [usuario, senha], (err, results) => {
        if (err) {
            return res.status(500).json({ sucesso: false, mensagem: "Erro interno no servidor." });
        }

        if (results.length > 0) {
            const user = results[0];
            // Se achou o usuário, devolve os dados
            res.json({
                sucesso: true,
                mensagem: "Login realizado com sucesso!",
                usuario: user.usuario,
                nivel_conta: user.nivel_conta,
                unidade: user.unidade
            });
        } else {
            res.json({ sucesso: false, mensagem: "Usuário ou senha incorretos!" });
        }
    });
});

// Rodando na porta 3000 em 0.0.0.0 (para permitir acesso do seu celular/emulador)
app.listen(3000, '0.0.0.0', () => {
    console.log('API rodando! Pronta para receber conexões do App.');
});
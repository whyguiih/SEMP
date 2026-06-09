PRAGMA defer_foreign_keys=TRUE;
CREATE TABLE d1_migrations(
		id         INTEGER PRIMARY KEY AUTOINCREMENT,
		name       TEXT UNIQUE,
		applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
INSERT INTO "d1_migrations" ("id","name","applied_at") VALUES(1,'0001_create_comments_table.sql','2026-05-19 16:34:01');
CREATE TABLE comments (
    id INTEGER PRIMARY KEY NOT NULL,
    author TEXT NOT NULL,
    content TEXT NOT NULL
);
INSERT INTO "comments" ("id","author","content") VALUES(1,'Kristian','Congrats!');
INSERT INTO "comments" ("id","author","content") VALUES(2,'Serena','Great job!');
INSERT INTO "comments" ("id","author","content") VALUES(3,'Max','Keep up the good work!');
CREATE TABLE tb_emprestimo (   id_emprestimo INTEGER PRIMARY KEY AUTOINCREMENT,   nome TEXT NOT NULL,   email TEXT NOT NULL,   data_reserva TEXT NOT NULL,   unidade TEXT NOT NULL,   codigo_empres TEXT,   nome_produto TEXT,   quant INTEGER,   remetente TEXT,   destinatario TEXT,   processamento INTEGER NOT NULL DEFAULT 0,   unidade_natal TEXT,   aprovacao INTEGER NOT NULL DEFAULT 0 );
CREATE TABLE tb_estoque (   id_estoque INTEGER PRIMARY KEY AUTOINCREMENT,   nome TEXT NOT NULL,   codigo TEXT NOT NULL,   descricao TEXT NOT NULL,   descricao_detalhada TEXT,   cor TEXT,   quant INTEGER NOT NULL,   uni_intermediarias TEXT,   marca_ref TEXT,   uni_natal TEXT NOT NULL,   carrinho INTEGER NOT NULL DEFAULT 0,   pedido TEXT NOT NULL DEFAULT '0',   foto TEXT );
INSERT INTO "tb_estoque" ("id_estoque","nome","codigo","descricao","descricao_detalhada","cor","quant","uni_intermediarias","marca_ref","uni_natal","carrinho","pedido","foto") VALUES(21,'alicate','2311','alicate ','alicate tramontina',NULL,12,'','','SENAI',1,'0',NULL);
INSERT INTO "tb_estoque" ("id_estoque","nome","codigo","descricao","descricao_detalhada","cor","quant","uni_intermediarias","marca_ref","uni_natal","carrinho","pedido","foto") VALUES(22,'chaves','3213','chavinha','chavinha',NULL,3,'','','SENAI',0,'0',NULL);
INSERT INTO "tb_estoque" ("id_estoque","nome","codigo","descricao","descricao_detalhada","cor","quant","uni_intermediarias","marca_ref","uni_natal","carrinho","pedido","foto") VALUES(23,'chaves','3213','chavinha','chavinha',NULL,3,'','','SENAI',0,'0',NULL);
INSERT INTO "tb_estoque" ("id_estoque","nome","codigo","descricao","descricao_detalhada","cor","quant","uni_intermediarias","marca_ref","uni_natal","carrinho","pedido","foto") VALUES(24,'tijolo','888','tijolinho','tijolo laranja',NULL,9,'','sla','senai',0,'0',NULL);
INSERT INTO "tb_estoque" ("id_estoque","nome","codigo","descricao","descricao_detalhada","cor","quant","uni_intermediarias","marca_ref","uni_natal","carrinho","pedido","foto") VALUES(25,'saco de cimento','9999','cimento tupi','cimento tupi',NULL,1,'','','encantado',0,'0',NULL);
INSERT INTO "tb_estoque" ("id_estoque","nome","codigo","descricao","descricao_detalhada","cor","quant","uni_intermediarias","marca_ref","uni_natal","carrinho","pedido","foto") VALUES(26,'lala','123456','mjgxv ','\\,shjdcv',NULL,24,'','tramontina','encantado',0,'0','C:\\fotos_estoque\\1776108031928_carrinho2.jpg');
INSERT INTO "tb_estoque" ("id_estoque","nome","codigo","descricao","descricao_detalhada","cor","quant","uni_intermediarias","marca_ref","uni_natal","carrinho","pedido","foto") VALUES(27,'wtge','segrg','wegseths','',NULL,2,'','','SENAI',0,'0',NULL);
CREATE TABLE tb_usuarios (   id INTEGER PRIMARY KEY AUTOINCREMENT,   usuario TEXT NOT NULL,   senha TEXT NOT NULL,   nivel_conta INTEGER NOT NULL,   unidade TEXT NOT NULL );
INSERT INTO "tb_usuarios" ("id","usuario","senha","nivel_conta","unidade") VALUES(1,'lari','12345',0,'SENAI');
INSERT INTO "tb_usuarios" ("id","usuario","senha","nivel_conta","unidade") VALUES(2,'pato','12345',1,'SENAI');
INSERT INTO "tb_usuarios" ("id","usuario","senha","nivel_conta","unidade") VALUES(3,'threeeo','t3',3,'SENAI');
INSERT INTO "tb_usuarios" ("id","usuario","senha","nivel_conta","unidade") VALUES(4,'user_comum_gari','senha123',0,'garibaldi');
INSERT INTO "tb_usuarios" ("id","usuario","senha","nivel_conta","unidade") VALUES(5,'user_comum_farr','senha123',0,'farroupilha');
INSERT INTO "tb_usuarios" ("id","usuario","senha","nivel_conta","unidade") VALUES(6,'operador_gari','senha123',1,'garibaldi');
INSERT INTO "tb_usuarios" ("id","usuario","senha","nivel_conta","unidade") VALUES(7,'operador_farr','senha123',1,'farroupilha');
INSERT INTO "tb_usuarios" ("id","usuario","senha","nivel_conta","unidade") VALUES(8,'gerente_gari','senha123',2,'garibaldi');
INSERT INTO "tb_usuarios" ("id","usuario","senha","nivel_conta","unidade") VALUES(9,'gerente_farr','senha123',2,'farroupilha');
INSERT INTO "tb_usuarios" ("id","usuario","senha","nivel_conta","unidade") VALUES(10,'admin_gari','senha123',3,'garibaldi');
INSERT INTO "tb_usuarios" ("id","usuario","senha","nivel_conta","unidade") VALUES(11,'admin_farr','senha123',3,'farroupilha');
DELETE FROM sqlite_sequence;
INSERT INTO "sqlite_sequence" ("name","seq") VALUES('d1_migrations',1);
INSERT INTO "sqlite_sequence" ("name","seq") VALUES('tb_usuarios',11);
INSERT INTO "sqlite_sequence" ("name","seq") VALUES('tb_estoque',27);

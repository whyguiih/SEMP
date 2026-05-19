-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 19/05/2026 às 02:45
-- Versão do servidor: 10.4.32-MariaDB
-- Versão do PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `db_estoque`
--
CREATE DATABASE IF NOT EXISTS `db_estoque` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `db_estoque`;

-- --------------------------------------------------------

--
-- Estrutura para tabela `tb_emprestimo`
--

DROP TABLE IF EXISTS `tb_emprestimo`;
CREATE TABLE IF NOT EXISTS `tb_emprestimo` (
  `id_emprestimo` int(11) NOT NULL AUTO_INCREMENT,
  `nome` varchar(70) NOT NULL,
  `email` varchar(150) NOT NULL,
  `data_reserva` date NOT NULL,
  `unidade` varchar(50) NOT NULL,
  `produto_codigo` varchar(15) DEFAULT NULL,
  `processamento` int(11) DEFAULT 0,
  `destinatario` varchar(70) NOT NULL,
  `remetente` varchar(70) NOT NULL,
  `unidade_natal` varchar(100) NOT NULL,
  `unidade_atual` varchar(100) NOT NULL,
  `status` int(11) NOT NULL,
  `nome_produto` varchar(250) NOT NULL,
  `codigo_empres` varchar(15) DEFAULT NULL,
  `data_devolucao` date DEFAULT NULL,
  `data_retirada` date DEFAULT NULL,
  `quant` int(11) DEFAULT NULL,
  `aprovacao` int(11) DEFAULT 0,
  PRIMARY KEY (`id_emprestimo`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `tb_emprestimo`
--

INSERT INTO `tb_emprestimo` (`id_emprestimo`, `nome`, `email`, `data_reserva`, `unidade`, `produto_codigo`, `processamento`, `destinatario`, `remetente`, `unidade_natal`, `unidade_atual`, `status`, `nome_produto`, `codigo_empres`, `data_devolucao`, `data_retirada`, `quant`, `aprovacao`) VALUES
(1, 'lari', '', '2009-10-17', '', NULL, 0, '', '', '', '', 0, '', NULL, NULL, NULL, NULL, 0),
(2, 'lari', '', '0000-00-00', '', NULL, 0, '', '', '', '', 0, '', NULL, NULL, NULL, NULL, 0),
(3, 'lari', '', '0000-00-00', '', NULL, 0, '', '', '', '', 0, '', NULL, NULL, NULL, NULL, 0),
(4, 'lari', '', '2009-10-17', '', NULL, 0, '', '', '', '', 0, '', NULL, NULL, NULL, 26, 0),
(5, '', '', '0000-00-00', '', NULL, 0, 'Usuário não logado', '', 'Garibaldi', '', 0, 'Camiseta Polo Básica', 'POLO001', NULL, NULL, 32, 0),
(6, 'pato', '', '0000-00-00', '', NULL, 1, '', '', '', '', 0, '', NULL, NULL, NULL, NULL, 0),
(7, 'lari', '', '0000-00-00', '', NULL, 1, '', '', '', '', 0, '', NULL, NULL, NULL, NULL, 0),
(8, 'municipio', '', '0000-00-00', '', NULL, 1, '', '', '', '', 0, '', NULL, NULL, NULL, NULL, 0),
(9, 'municipio', '', '0000-00-00', '', NULL, 1, '', '', 'farroupilha', '', 0, '', NULL, NULL, NULL, NULL, 0);

-- --------------------------------------------------------

--
-- Estrutura para tabela `tb_estoque`
--

DROP TABLE IF EXISTS `tb_estoque`;
CREATE TABLE IF NOT EXISTS `tb_estoque` (
  `id_estoque` int(11) NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) DEFAULT NULL,
  `codigo` varchar(15) DEFAULT NULL,
  `descricao` varchar(300) DEFAULT NULL,
  `descricao_detalhada` varchar(1000) DEFAULT NULL,
  `cor` varchar(20) DEFAULT NULL,
  `quant` int(11) DEFAULT NULL,
  `uni_intermediarias` varchar(1000) DEFAULT NULL,
  `marca_ref` varchar(70) DEFAULT NULL,
  `uni_natal` varchar(150) DEFAULT NULL,
  `carrinho` int(11) DEFAULT 0,
  `pedido` varchar(10) DEFAULT '0',
  `foto` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_estoque`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `tb_estoque`
--

INSERT INTO `tb_estoque` (`id_estoque`, `nome`, `codigo`, `descricao`, `descricao_detalhada`, `cor`, `quant`, `uni_intermediarias`, `marca_ref`, `uni_natal`, `carrinho`, `pedido`, `foto`) VALUES
(1, 'Camiseta Polo Básica', 'POLO001', 'Camiseta polo masculina algodão penteado', 'Camiseta polo com gola reforçada e acabamento premium, ideal para uso diário e uniforme.', 'Azul Marinho', 245, 'P,M,G,GG', 'Nike', 'Garibaldi', 0, 'CiQ8Ydk54y', NULL),
(2, 'Calça Jeans Slim Fit', 'JEANS045', 'Calça jeans masculina slim com elastano', 'Modelagem slim, tecido com 2% elastano para maior conforto e mobilidade.', 'Preto', 87, '38,40,42,44', 'Levis', 'Farroupilha', 0, '0', NULL),
(3, 'Tênis Casual Branco', 'TENIS112', 'Tênis casual em couro sintético leve', 'Tênis confortável com solado antiderrapante e amortecimento.', 'Branco', 132, '37,38,39,40,41', 'Adidas', 'Garibaldi', 0, '0', NULL),
(4, 'Blusa Moletom com Capuz', 'MOLET001', 'Moletom fleece com capuz e bolso canguru', 'Moletom quentinho e macio, perfeito para o outono/inverno.', 'Cinza', 68, 'P,M,G,GG', 'Puma', 'Farroupilha', 0, '0', NULL),
(5, 'Vestido Floral Midi', 'VEST012', 'Vestido feminino floral leve e fluido', 'Vestido midi com estampa floral delicada e alças reguláveis.', 'Rosa', 45, 'P,M,G', 'Zara', 'Garibaldi', 0, '0', NULL),
(6, 'Jaqueta Jeans', 'JAQJ001', 'Jaqueta jeans clássica masculina', 'Jaqueta jeans com botões e bolsos funcionais.', 'Azul Claro', 52, 'P,M,G,GG', 'Hering', 'Farroupilha', 0, '0', NULL),
(7, 'Shorts Masculino tactel', 'SHORT078', 'Shorts de tactel para esporte e lazer', 'Short leve e secagem rápida, ideal para treino e praia.', 'Preto', 210, 'P,M,G,GG', 'Olympikus', 'Garibaldi', 0, '0', NULL),
(8, 'Blusa Feminina de Tricô', 'BLUSA033', 'Blusa de tricô fina com gola alta', 'Blusa elegante em tricô, confortável e versátil.', 'Bege', 39, 'P,M,G', 'Renner', 'Farroupilha', 0, '0', NULL),
(9, 'Boné Aba Curva', 'BONE009', 'Boné aba curva bordado', 'Boné ajustável com bordado frontal.', 'Preto', 175, 'Único', 'New Era', 'Garibaldi', 0, '0', NULL),
(10, 'Cinto de Couro Masculino', 'CINT025', 'Cinto em couro legítimo com fivela metálica', 'Cinto clássico de 3,5cm de largura.', 'Marrom', 64, 'Único', 'Tommy Hilfiger', 'Farroupilha', 0, '0', NULL),
(11, 'Meia Cano Médio 3 pares', 'MEIA015', 'Kit com 3 pares de meia cano médio', 'Meia confortável com elastano, ideal para dia a dia.', 'Preto', 320, 'Único', 'Lupo', 'Garibaldi', 0, '0', NULL),
(12, 'Mochila Escolar', 'MOCH045', 'Mochila escolar com compartimentos', 'Mochila resistente com bolso frontal e alças acolchoadas.', 'Azul', 28, 'Único', 'Adidas', 'Farroupilha', 0, '0', NULL),
(13, 'Óculos de Sol Quadrado', 'OCUL012', 'Óculos de sol com armação quadrada', 'Proteção UV400, lente polarizada.', 'Preto Fosco', 91, 'Único', 'Ray-Ban', 'Garibaldi', 0, '0', NULL),
(14, 'pato', 'RELO089', 'Relógio digital com cronômetro e alarme', 'Relógio resistente à água, pulseira de silicone.', 'Preto', 47, 'Único', 'Casio', 'Farroupilha', 0, '0', NULL),
(16, 'pato', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, '0', NULL),
(17, 'patp', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, '0', NULL),
(18, 'lari', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, '0', NULL),
(19, 'pato', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, '0', NULL),
(20, 'pato', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'farroupilha', 1, '0', NULL),
(21, 'lari', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'farroupilha', 1, '0', NULL),
(22, 'municipio', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'farroupilha', 1, '0', NULL),
(23, 'lari', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'farroupilha', 1, '0', NULL),
(24, 'lari patinha', '123456', 'jgksvsjh', 'ajshvcamjsv ', NULL, 3, 'erbv', '', 'encantado', 0, 'R2w2V66pTY', NULL),
(25, 'lal', '1234567', 'a,sdhkvb', 'a,jsdhcv', NULL, 24, '', 'tramotnina', 'encantado', 0, '0', '?PNG\r\n\Z\n\0\0\0\rIHDR\0\0\0?\0\0\0?  \0\0\0	m\"H\0\0 ?PLTE???\0\0\0????????ɝ $? %???m #000? $?????????? %\r\r\r? #? $? %??ݼ #???v %? $??????+++         &&&?????????? %qqq~~~???? %x@@?????????>>@ccc???SSS? #k\0\0???? (?\0\0䬮?Z\\? )?;@?04  !FFG?\0\0?\0\0[[]@@A *,?uwg-2Y\"&U  N\0\0?EI?SX?d'),
(26, 'lala', '123456', 'mjgxv ', '\\,shjdcv', NULL, 24, '', 'tramontina', 'encantado', 0, '0', 'C:\\fotos_estoque\\1776108031928_carrinho2.jpg');

-- --------------------------------------------------------

--
-- Estrutura para tabela `tb_usuarios`
--

DROP TABLE IF EXISTS `tb_usuarios`;
CREATE TABLE IF NOT EXISTS `tb_usuarios` (
  `id` int(5) NOT NULL AUTO_INCREMENT,
  `usuario` varchar(64) NOT NULL,
  `senha` varchar(255) NOT NULL,
  `nivel_conta` tinyint(1) NOT NULL,
  `unidade` enum('garibaldi','farroupilha','','') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Despejando dados para a tabela `tb_usuarios`
--

INSERT INTO `tb_usuarios` (`id`, `usuario`, `senha`, `nivel_conta`, `unidade`) VALUES
(1, 'lari', '12345', 1, ''),
(2, 'pato', '12345', 0, 'garibaldi'),
(3, 'patinho', '12345', 1, 'farroupilha'),
(4, 'lari', '123456', 0, 'garibaldi'),
(5, 'Threeeo', 'Thr333o', 3, 'garibaldi');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

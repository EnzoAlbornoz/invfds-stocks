CREATE TABLE IF NOT EXISTS `cadastro_cias_abertas` (
	`CNPJ_CIA` VARCHAR(20) NOT NULL,
	`DENOM_SOCIAL` VARCHAR(100) NOT NULL,
	`SITUACAO` VARCHAR(40) NOT NULL,
	PRIMARY KEY (`CNPJ_CIA`)
);

CREATE TABLE IF NOT EXISTS `tickers_cias_abertas` (
	`TICKER` VARCHAR(12) NOT NULL,
	`CNPJ_CIA` VARCHAR(20) NOT NULL,
	PRIMARY KEY (`TICKER`),
	FOREIGN KEY (`CNPJ_CIA`) REFERENCES `cadastro_cias_abertas`(`CNPJ_CIA`)
);


CREATE TABLE IF NOT EXISTS `serie_hist_cias_abertas` (
	`TICKER` VARCHAR(12) NOT NULL,
	`DT_PREGAO` DATE NOT NULL,
	`MOEDA` VARCHAR(4) NOT NULL,
	`PRE_ABE` DECIMAL(11,2) NOT NULL,
	`PRE_MAX` DECIMAL(11,2) NOT NULL,
	`PRE_MIN` DECIMAL(11,2) NOT NULL,
	`PRE_MED` DECIMAL(11,2) NOT NULL,
	`PRE_FCH` DECIMAL(11,2) NOT NULL,
	PRIMARY KEY (`TICKER`,`DT_PREGAO`),
	FOREIGN KEY (`TICKER`) REFERENCES `tickers_cias_abertas`(`TICKER`)
);

CREATE TABLE IF NOT EXISTS `dre_cias_abertas` (
    `CNPJ_CIA` VARCHAR(20) NOT NULL,
    `DT_REFERENCIA_INI` DATE NOT NULL,
    `DT_REFERENCIA_FIM` DATE NOT NULL,
    `MOEDA` VARCHAR(4) NOT NULL,
    `MOEDA_ESCALA` VARCHAR(10) NOT NULL,
    `COD_CONTA` VARCHAR(20) NOT NULL,
    `DSC_CONTA` VARCHAR(255) NOT NULL,
    `VAL_CONTA` DECIMAL(29, 10) NOT NULL,
    PRIMARY KEY (`CNPJ_CIA`, `DT_REFERENCIA_INI`, `COD_CONTA`),
    KEY `dre_cias_abertas_DSC_CONTA_IDX` (`DSC_CONTA`) USING BTREE,
    FOREIGN KEY (`CNPJ_CIA`) REFERENCES `cadastro_cias_abertas`(`CNPJ_CIA`)
);

CREATE TABLE IF NOT EXISTS `dmpl_cias_abertas` (
    `CNPJ_CIA` VARCHAR(20) NOT NULL,
    `DT_REFERENCIA_INI` DATE NOT NULL,
    `DT_REFERENCIA_FIM` DATE NOT NULL,
    `MOEDA` VARCHAR(4) NOT NULL,
    `MOEDA_ESCALA` VARCHAR(10) NOT NULL,
    `COLUNA_DF` VARCHAR(255) NOT NULL,
    `COD_CONTA` VARCHAR(20) NOT NULL,
    `DSC_CONTA` VARCHAR(255) NOT NULL,
    `VAL_CONTA` DECIMAL(29, 10) NOT NULL,
    PRIMARY KEY (`CNPJ_CIA`, `DT_REFERENCIA_INI`, `COLUNA_DF`, `COD_CONTA`),
    KEY `dmpl_cias_abertas_COLUNA_DFA_IDX` (`COLUNA_DF`) USING BTREE,
    KEY `dmpl_cias_abertas_DSC_CONTA_IDX` (`DSC_CONTA`) USING BTREE,
    FOREIGN KEY (`CNPJ_CIA`) REFERENCES `cadastro_cias_abertas`(`CNPJ_CIA`)
);

CREATE TABLE IF NOT EXISTS `dfc_cias_abertas` (
    `CNPJ_CIA` VARCHAR(20) NOT NULL,
    `DT_REFERENCIA_INI` DATE NOT NULL,
    `DT_REFERENCIA_FIM` DATE NOT NULL,
    `MOEDA` VARCHAR(4) NOT NULL,
    `MOEDA_ESCALA` VARCHAR(10) NOT NULL,
    `COD_CONTA` VARCHAR(20) NOT NULL,
    `DSC_CONTA` VARCHAR(255) NOT NULL,
    `VAL_CONTA` DECIMAL(29, 10) NOT NULL,
    PRIMARY KEY (`CNPJ_CIA`, `DT_REFERENCIA_INI`, `COD_CONTA`),
    KEY `dre_cias_abertas_DSC_CONTA_IDX` (`DSC_CONTA`) USING BTREE,
    FOREIGN KEY (`CNPJ_CIA`) REFERENCES `cadastro_cias_abertas`(`CNPJ_CIA`)
);

CREATE TABLE IF NOT EXISTS `bpa_cias_abertas` (
    `CNPJ_CIA` VARCHAR(20) NOT NULL,
    `DT_REFERENCIA_INI` DATE NOT NULL,
    `DT_REFERENCIA_FIM` DATE NOT NULL,
    `MOEDA` VARCHAR(4) NOT NULL,
    `MOEDA_ESCALA` VARCHAR(10) NOT NULL,
    `COD_CONTA` VARCHAR(20) NOT NULL,
    `DSC_CONTA` VARCHAR(255) NOT NULL,
    `VAL_CONTA` DECIMAL(29, 10) NOT NULL,
    PRIMARY KEY (`CNPJ_CIA`, `DT_REFERENCIA_INI`, `COD_CONTA`),
    KEY `dre_cias_abertas_DSC_CONTA_IDX` (`DSC_CONTA`) USING BTREE,
    FOREIGN KEY (`CNPJ_CIA`) REFERENCES `cadastro_cias_abertas`(`CNPJ_CIA`)
);

CREATE TABLE IF NOT EXISTS `bpp_cias_abertas` (
    `CNPJ_CIA` VARCHAR(20) NOT NULL,
    `DT_REFERENCIA_INI` DATE NOT NULL,
    `DT_REFERENCIA_FIM` DATE NOT NULL,
    `MOEDA` VARCHAR(4) NOT NULL,
    `MOEDA_ESCALA` VARCHAR(10) NOT NULL,
    `COD_CONTA` VARCHAR(20) NOT NULL,
    `DSC_CONTA` VARCHAR(255) NOT NULL,
    `VAL_CONTA` DECIMAL(29, 10) NOT NULL,
    PRIMARY KEY (`CNPJ_CIA`, `DT_REFERENCIA_INI`, `COD_CONTA`),
    KEY `dre_cias_abertas_DSC_CONTA_IDX` (`DSC_CONTA`) USING BTREE,
    FOREIGN KEY (`CNPJ_CIA`) REFERENCES `cadastro_cias_abertas`(`CNPJ_CIA`)
);

INSERT IGNORE INTO
    `bpa_cias_abertas`
    (
        `CNPJ_CIA`,
        `DT_REFERENCIA_INI`,
        `DT_REFERENCIA_FIM`,
        `MOEDA`,
        `MOEDA_ESCALA`,
        `COD_CONTA`,
        `DSC_CONTA`,
        `VAL_CONTA`
    )
VALUES (?,?,?,?,?,?,?,?)

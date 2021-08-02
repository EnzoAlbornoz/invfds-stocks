package br.ufsc.investfunds.companies.entities;

import java.math.BigDecimal;
import java.sql.Date;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Key;
import io.requery.Table;

@Entity
@Table(name = "serie_hist_cias_aberta")
public class AbstractPublicCompanyStockSerie {

    @Key
    @ForeignKey(references = AbstractPublicCompanyTicker.class, referencedColumn = "TICKER")
    @Column(name = "TICKER", nullable = false)
    AbstractPublicCompanyTicker ticker;

    @Key
    @Column(name = "DT_PREGAO", nullable = false)
    Date tradeDate;

    @Column(name = "MOEDA", nullable = false)
    String coin;

    @Column(name = "PRE_ABE", definition = "NUMERIC(11,2) NOT NULL")
    BigDecimal openingPrice;

    @Column(name = "PRE_MAX", definition = "NUMERIC(11,2) NOT NULL")
    BigDecimal maximumPrice;

    @Column(name = "PRE_MIN", definition = "NUMERIC(11,2) NOT NULL")
    BigDecimal minimumPrice;

    @Column(name = "PRE_MED", definition = "NUMERIC(11,2) NOT NULL")
    BigDecimal averagePrice;

    @Column(name = "PRE_MED", definition = "NUMERIC(11,2) NOT NULL")
    BigDecimal closingPrice;
}

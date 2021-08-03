package br.ufsc.investfunds.companies.entities;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.Table;

@Entity()
@Table(name = "tickers_cia_aberta")
public class AbstractPublicCompanyTicker {

    @Key
    @Column(name = "TICKER", length = 12, nullable = false)
    String ticker;

    @Key
    @ForeignKey(referencedColumn = "CNPJ_CIA")
    @Column(name = "CNPJ_CIA", length = 20, nullable = false)
    PublicCompanyRegister companyRegister;

    public AbstractPublicCompanyTicker() {}

}

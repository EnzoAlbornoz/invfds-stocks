package br.ufsc.investfunds.companies.entities;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Key;
import io.requery.Table;

@Entity
@Table(name = "tickers_cia_aberta")
public class AbstractPublicCompanyTicker {

    @Key
    @ForeignKey(references = AbstractPublicCompanyRegister.class, referencedColumn = "CNPJ_CIA")
    @Column(name = "CNPJ_CIA", length = 20, nullable = false)
    AbstractPublicCompanyRegister companyRegister;

    @Key
    @Column(name = "TICKER", nullable = false)
    String ticker;

    public AbstractPublicCompanyTicker() {}

    public AbstractPublicCompanyTicker(AbstractPublicCompanyRegister companyRegister, String ticker) {
        this.companyRegister = companyRegister;
        this.ticker = ticker;
    }

}

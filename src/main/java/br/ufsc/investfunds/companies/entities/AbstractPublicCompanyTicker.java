package br.ufsc.investfunds.companies.entities;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Key;
import io.requery.Table;

@Entity
@Table()
public class AbstractPublicCompanyTicker {

    @Key
    @ForeignKey(references = PublicCompanyRegister.class)
    @Column(name = "CNPJ_CIA", length = 20, nullable = false)
    String companyRegister;

    @Key
    @ForeignKey(references = PublicCompanyTicker.class, referencedColumn = "TICKER")
    @Column(name = "TICKER", nullable = false)
    String ticker;

    public AbstractPublicCompanyTicker() {}

    public AbstractPublicCompanyTicker(String companyRegister, String ticker) {
        this.companyRegister = companyRegister;
        this.ticker = ticker;
    }

}

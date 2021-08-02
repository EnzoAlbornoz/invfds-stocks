package br.ufsc.investfunds.companies.entities;

import java.math.BigDecimal;
import java.sql.Date;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Index;
import io.requery.Key;
import io.requery.Table;

@Entity
@Table(name = "itr_dre_cia_aberta")
public class AbstractPublicCompanyTrimestralResultsDemonstrative {

    @Key
    @ForeignKey(references = AbstractPublicCompanyRegister.class, referencedColumn = "CNPJ_CIA")
    @Column(name = "CNPJ_CIA", length = 20, nullable = false)
    AbstractPublicCompanyRegister companyRegister;

    @Key
    @Column(name = "DT_REFERENCIA", nullable = false)
    Date referenceDate;

    @Column(name = "MOEDA", nullable = false)
    String coin;

    @Column(name = "MOEDA_ESCALA", nullable = false)
    String scale;

    @Index
    @Column(name = "COD_CONTA", nullable = true)
    String accountCode;

    @Column(name = "DSC_CONTA", nullable = true)
    String accountDescription;

    @Column(name = "VAL_CONTA", nullable = false)
    BigDecimal accountValue;

    public AbstractPublicCompanyTrimestralResultsDemonstrative(AbstractPublicCompanyRegister companyRegister, Date referenceDate) {
        this.companyRegister = companyRegister;
        this.referenceDate = referenceDate;
    }

    public AbstractPublicCompanyTrimestralResultsDemonstrative() {}

}

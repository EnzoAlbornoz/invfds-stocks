package br.ufsc.investfunds.companies.entities;
import br.ufsc.investfunds.companies.converters.EPublicCompanyRegisterStatusConverter;
import io.requery.Column;
import io.requery.Convert;
// Import Dependencies
import io.requery.Entity;
import io.requery.Key;
import io.requery.Table;
// Declare Entity
@Entity()
@Table(name = "cadastro_cias_abertas")
public abstract class AbstractPublicCompanyRegister {
    // Declare Attributes
    @Key
    @Column(name = "CNPJ_CIA", length = 20, nullable = false)
    String cnpj;

    @Column(name = "DENOM_SOCIAL", length = 100, nullable = false)
    String socialName;

    @Column(name = "SIT", length = 40, nullable = false)
    @Convert(EPublicCompanyRegisterStatusConverter.class)
    EPublicCompanyRegisterStatus status;
}

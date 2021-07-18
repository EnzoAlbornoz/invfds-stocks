package br.ufsc.investfunds.companies.converters;
// Import Dependencies
import br.ufsc.investfunds.companies.entities.EPublicCompanyRegisterStatus;
import io.requery.converter.EnumStringConverter;
// Declare Converter
public class EPublicCompanyRegisterStatusConverter extends EnumStringConverter<EPublicCompanyRegisterStatus> {
    public EPublicCompanyRegisterStatusConverter() {
        super(EPublicCompanyRegisterStatus.class);
    }
}


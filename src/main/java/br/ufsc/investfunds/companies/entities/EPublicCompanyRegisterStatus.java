package br.ufsc.investfunds.companies.entities;
// Import Dependencies
import io.requery.converter.EnumStringConverter;
// Declare Enum
public enum EPublicCompanyRegisterStatus {
    // Declare Values
    CANCELED,
    SUSPENDED,
    ACTIVE;
    // Map Into Values
    @Override
    public String toString() {
        switch(this) {
        case CANCELED: return "CANCELADO";
        case ACTIVE: return "ATIVO";
        case SUSPENDED: return "SUSPENSO(A) - DECISÃO ADM";
        default: throw new IllegalArgumentException();
        }
    }
}

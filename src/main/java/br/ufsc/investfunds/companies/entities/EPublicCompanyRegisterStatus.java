package br.ufsc.investfunds.companies.entities;
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
        case CANCELED: return "CANCELADA";
        case ACTIVE: return "ATIVO";
        case SUSPENDED: return "SUSPENSO(A) - DECISÃO ADM";
        default: throw new IllegalArgumentException();
        }
    }

    public static EPublicCompanyRegisterStatus fromString(String string) {
        switch(string) {
            case "CANCELADA": return CANCELED;
            case "ATIVO": return ACTIVE;
            case "SUSPENSO(A) - DECISÃO ADM": return SUSPENDED;
            default: throw new IllegalArgumentException(String.format("Cannot convert '%s' into EPublicCompanyRegisterStatus", string));
        }
    }
}

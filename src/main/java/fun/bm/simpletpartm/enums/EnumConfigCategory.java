package fun.bm.simpletpartm.enums;

public enum EnumConfigCategory {
    CORE("core"),
    TPA("tpa"),
    TPHERE("tphere"),
    BACK("back"),
    REMOVED("removed"); // removed config

    private final String baseKeyName;

    EnumConfigCategory(String baseKeyName) {
        this.baseKeyName = baseKeyName;
    }

    public String getBaseKeyName() {
        return this.baseKeyName;
    }
}
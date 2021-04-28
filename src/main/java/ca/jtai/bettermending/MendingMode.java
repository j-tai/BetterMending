package ca.jtai.bettermending;

public enum MendingMode {
    VANILLA("Vanilla mechanics", "repair one item per XP orb, then the rest of the orb adds levels"),
    OFF("Ignore Mending", "all XP orbs add levels, no repairing"),
    REPAIR("Repair first", "repair all eligible items before adding levels");

    private final String description;
    private final String explanation;

    MendingMode(String description, String explanation) {
        this.description = description;
        this.explanation = explanation;
    }

    public String getDescription() {
        return description;
    }

    public String getExplanation() {
        return explanation;
    }
}

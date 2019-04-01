package warframeRelics.beans;

public class PrimeItem {
    private String uniqueName;
    private String displayName;
    private boolean vaulted;

    public PrimeItem(String uniqueName, String displayName, boolean vaulted) {
        this.uniqueName = uniqueName;
        this.displayName = displayName;
        this.vaulted = vaulted;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isVaulted() {
        return vaulted;
    }
}

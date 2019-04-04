package warframeRelics.beans;

public class PrimeItem {
    private String uniqueName;
    private String displayName;
    private boolean vaulted;
    private int ducats;

    public PrimeItem(String uniqueName, String displayName, boolean vaulted, int ducats) {
        this.uniqueName = uniqueName;
        this.displayName = displayName;
        this.vaulted = vaulted;
        this.ducats = ducats;
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

    public int getDucats() {
        return ducats;
    }
}

package warframeRelics.gui.priceControls;

public class NamePricer extends Pricer {


    NamePricer(String id) {
        super(id);
    }

    @Override
    public PriceDisplayer getPriceDisplayer() {
        return new TextPriceDisplayer((item) -> {
            if (item.isVaulted()) {
                return item.getDisplayName() + " (v)";
            } else {
                return item.getDisplayName();
            }
        });
    }

    @Override
    public String getName() {
        return "Name";
    }

    @Override
    public double getColumnWidth() {
        return 100;
    }
}

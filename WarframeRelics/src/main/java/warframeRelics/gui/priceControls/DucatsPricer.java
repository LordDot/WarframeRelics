package warframeRelics.gui.priceControls;

public class DucatsPricer extends Pricer {

    public DucatsPricer(String id) {
        super(id);
    }

    @Override
    public String getName() {
        return "Ducats";
    }

    @Override
    public PriceDisplayer getPriceDisplayer() {
        return new TextPriceDisplayer((item) -> "" + item.getDucats());
    }

    @Override
    public double getColumnWidth() {
        return 50;
    }
}

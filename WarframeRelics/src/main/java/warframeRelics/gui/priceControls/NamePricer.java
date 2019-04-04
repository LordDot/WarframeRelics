package warframeRelics.gui.priceControls;

import warframeRelics.dataBase.IDataBase;

public class NamePricer extends Pricer {


    NamePricer(String id, IDataBase database) {
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

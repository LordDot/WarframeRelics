package warframeRelics.gui.priceControls;

import org.reflections.Reflections;
import warframeRelics.dataBase.IDataBase;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class PricerFactory {

    public static final String NAME = "warframeRelics.gui.priceControls.NamePricer";
    public static final String DUCATS = "warframeRelics.gui.priceControls.DucatsPricer";

    private Map<String, Pricer> pricers;

    public PricerFactory(IDataBase database) {
        pricers = new HashMap<>();
        Set<Class<? extends Pricer>> subTypes = new Reflections().getSubTypesOf(Pricer.class);
        for (Class<? extends Pricer> c : subTypes) {
            try {
                Constructor<? extends Pricer> declaredConstructor = c.getDeclaredConstructor(String.class);
                String name = c.getCanonicalName();
                pricers.put(name, declaredConstructor.newInstance(name));
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                //ignore
            }
            ;
        }
    }

    public List<Pricer> getAllPricers() {
        return new ArrayList<>(pricers.values());
    }

    public Pricer get(String id) {
        return pricers.get(id);
    }
}

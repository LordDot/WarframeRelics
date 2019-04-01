package warframeRelics.gui.settings;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.jnativehook.keyboard.NativeKeyEvent;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import warframeRelics.gui.priceControls.PricerFactory;
import warframeRelics.screenCapture.ResolutionFile;
import warframeRelics.screenCapture.ScreenResolution;

public class Settings {
    private ScreenResolution resolution;
    private List<String> priceDisplayers;
    private int readRewardsHotkey;
    private float onTopTime;

    public Settings(ScreenResolution resolution, List<String> priceDisplayers, int readRewardsHotkey, float onTopTime) {
        this.resolution = resolution;
        this.priceDisplayers = priceDisplayers;
        this.readRewardsHotkey = readRewardsHotkey;
        this.onTopTime = onTopTime;
    }

    public ScreenResolution getResolution() {
        return resolution;
    }

    public void setResolution(ScreenResolution resolution) {
        this.resolution = resolution;
    }

    public List<String> getPriceDisplayers() {
        return priceDisplayers;
    }

    public void setPriceDisplayers(List<String> priceDisplayers) {
        this.priceDisplayers = priceDisplayers;
    }

    public int getReadRewardsHotkey() {
        return readRewardsHotkey;
    }

    public float getOnTopTime() {
        return onTopTime;
    }

    public void setReadRewardsHotkey(int readRewardsHotkey) {
        this.readRewardsHotkey = readRewardsHotkey;
    }

    public void setOnTopTime(float onTopTime) {
        this.onTopTime = onTopTime;
    }


}

package com.n1kdo.adif;

import android.annotation.SuppressLint;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * enumeration of ADIF bands
 * see http://www.adif.org/304/ADIF_304.htm
 *
 * @author n1kdo
 */
public enum AdifBand {
    BAND_2190M("2190m"),
    BAND_630M("630m"),
    BAND_560M("560m"),
    BAND_160M("160m"),
    BAND_80M("80m"),
    BAND_60M("60m"),
    BAND_40M("40m"),
    BAND_30M("30m"),
    BAND_20M("20m"),
    BAND_17M("17m"),
    BAND_15M("15m"),
    BAND_12M("12m"),
    BAND_10M("10m"),
    BAND_6M("6m"),
    BAND_4M("4m"),
    BAND_2M("2m"),
    BAND_1_25M("1.25m"),
    BAND_70CM("70cm"),
    BAND_33CM("33cm"),
    BAND_23CM("23cm"),
    BAND_13CM("13cm"),
    BAND_9CM("9cm"),
    BAND_6CM("6cm"),
    BAND_3CM("3cm"),
    BAND_1_25CM("1.25cm"),
    BAND_6MM("6mm"),
    BAND_4MM("4mm"),
    BAND_2_5MM("2.5mm"),
    BAND_2MM("2mm"),
    BAND_1MM("1mm");

    private static final Map<String, AdifBand> nameToBandMap;
    private final String name;

    static {
        nameToBandMap = new LinkedHashMap<>();
        for (AdifBand adifBand : AdifBand.values()) {
            nameToBandMap.put(adifBand.name, adifBand);
        }
    }

    AdifBand(String name) {
        this.name = name;
    }

    @SuppressLint("unused")
    private String getName() {
        return name;
    }

    @SuppressLint("unused")
    public static AdifBand getAdifBandByName(String name) {
        return nameToBandMap.get(name);
    }

    public static Set<String> getAdifBandNames() {
        return nameToBandMap.keySet();
    }

    public static float wavelength(String bandname) {
        float wavelength = 0.0f;
        if (bandname.toLowerCase().endsWith("cm")) {
            String num = bandname.substring(0, bandname.length() - 2);
            wavelength = Integer.parseInt(num) / 100.0f;
        } else {
            if (bandname.toLowerCase().endsWith("m")) {
                String num = bandname.substring(0, bandname.length() - 1);
                wavelength = Integer.parseInt(num);
            }
        }
        return wavelength;
    } // wavelength()

} // enum

package com.n1kdo.adif;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * enumeration of ADIF modes.
 * see http://www.adif.org/308/ADIF_308.htm
 * @author n1kdo
 */
public enum AdifMode
{
    AM("AM"),
    ARDOP("ARDOP"),
    ATV("ATV"),
    C4FM("C4FM"),
    CHIP("CHIP"),
    CLO("CLO"),
    CONTESTI("Contestia"),
    CW("CW"),
    DIGITALVOICE("Digital Voice"),
    DOMINO("Domino"),
    DSTAR("D-STAR"),
    FAX("FAX"),
    FM("FM"),
    FSK441("FSK441"),
    FT8("FT8"),
    HELL("Hellschreiber"),
    ISCAT("ISCAT"),
    JT4("JT4"),
    JT6M("JT6M"),
    JT9("JT9"),
    JT44("JT44"),
    JT65("JT65"),
    MFSK("MFSK"),
    MSK144("MSK144"),
    MT63("MT63"),
    OLIVIA("Olivia"),
    OPERA("Opera"),
    PAC("PAC"),
    PAX("PAX"),
    PKT("PKT"),
    PSK("PSK"),
    PSK2K("PSK2K"),
    Q15("Q15"),
    QRA64("QRA64"),
    ROS("ROS"),
    RTTY("RTTY"),
    RTTYM("RTTYM"),
    SSB("SSB"),
    SSTV("SSTV"),
    THOR("THOR"),
    THRB("THRB"),
    TOR("TOR"),
    V4("V4"),
    VOI("VOI"),
    WINMOR("WINMOR"),
    WSPR("WSPR");
    
    private static final Map<String, AdifMode> nameToModeMap;
    private final String name;
    
    static 
    {
        nameToModeMap = new LinkedHashMap<>();
        for (AdifMode adifMode : AdifMode.values())
        {
            nameToModeMap.put(adifMode.name,  adifMode);
        }
    }
    
    AdifMode(String name)
    {
        this.name = name;
    }
    
    public final String getName() {return name;}
    
    public static AdifMode getAdifModeByName(String name) { return nameToModeMap.get(name); }
    
    public static Set<String> getAdifModeNames() { return nameToModeMap.keySet(); }
    
} // enum

package xyz.cyanclay.buptallinone.network.info;

import java.util.HashMap;
import java.util.Map;

public enum InfoCategory {

    SCHOOL_NOTICE,
    SCHOOL_NEWS,
    PARTY_OVERTNESS,
    SCHOOL_OVERTNESS,
    INNER_CONTROL_OVERTNESS,
    NOTICE_OVERTNESS;

    static final Map<InfoCategory, String> pens = new HashMap<InfoCategory, String>() {{
        put(SCHOOL_NOTICE, ".pen=pe1144");
        put(SCHOOL_NEWS, ".pen=pe1142");
        put(PARTY_OVERTNESS, ".pen=pe1942");
        put(SCHOOL_OVERTNESS, ".pen=pe1941");
        put(INNER_CONTROL_OVERTNESS, ".pen=pe1962");
        put(NOTICE_OVERTNESS, ".pen=pe1921");
    }};


}

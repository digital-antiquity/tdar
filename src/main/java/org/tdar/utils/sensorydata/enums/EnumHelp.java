package org.tdar.utils.sensorydata.enums;

import java.util.HashMap;
import java.util.Map;

public class EnumHelp {

    //bah! why wont you work!!?   wanted an easy way to create these lookup maps rather than implement it in every enum that has labels.
    public static <E extends Enum & HasLabel> Map<String, E> createLookupMap(E enums) {
        Map<String, E> map = new HashMap<String, E>();
//        for(E val : E.values()) {
//            map.put(val.getLabel(), val);
//        }
        return null;
    }
    
}

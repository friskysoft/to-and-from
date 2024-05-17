package com.friskysoft.tools.taf.utils;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class CommonUtil {

    public static Set<String> findDuplicateIgnoreCase(Set<String> keys) {
        Map<String, String> lowercaseToOriginal = new HashMap<>();
        Set<String> duplicateValues = new LinkedHashSet<>();

        for (String value : keys) {
            String lowercaseValue = value.toLowerCase();
            // Check if the lowercase version of the value is already in the map
            if (lowercaseToOriginal.containsKey(lowercaseValue)) {
                String originalValue = lowercaseToOriginal.get(lowercaseValue);
                // Add both the original and the duplicate value to the result set
                duplicateValues.add(originalValue);
                duplicateValues.add(value);
            } else {
                // If not, add it to the map
                lowercaseToOriginal.put(lowercaseValue, value);
            }
        }

        return duplicateValues;
    }

}

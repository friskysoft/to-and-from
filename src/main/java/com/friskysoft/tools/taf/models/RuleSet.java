package com.friskysoft.tools.taf.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.friskysoft.tools.taf.utils.MapperUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RuleSet {

    @Builder.Default
    private String rootInput = "";

    @Builder.Default
    private String rootOutput = "";

    @Builder.Default
    private Map<String, Rule> output = new LinkedHashMap<>();

    @Override
    public String toString() {
        return MapperUtil.yaml().write(this);
    }
}

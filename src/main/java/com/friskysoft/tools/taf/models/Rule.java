package com.friskysoft.tools.taf.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Rule {

    private String from;

    @JsonAlias("default")
    private String defaultValue;

    @Builder.Default
    private DataType type = DataType.STRING;

    private String condition;
    private String expression;
    private Map<String, String> lookup;

}

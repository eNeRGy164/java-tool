package dev.dendrodocs.tool.descriptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * Description of a field.
 *
 * @param member       {@link MemberDescription}: the name, modifiers and annotations of the field.
 * @param type         Type of the field (string).
 * @param initialValue The explicit initialization value (string).
 */
public record FieldDescription(

    @JsonUnwrapped
    MemberDescription member,

    @JsonProperty("Type")
    String type,

    @JsonProperty("Initializer")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String initialValue

) implements Description {

}

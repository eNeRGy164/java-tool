package dev.dendrodocs.tool.descriptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.List;

/**
 * Description for a method. Meant to be contained in a list of methods, within a TypeDescription.
 *
 * @param member     {@link MemberDescription}: the name, modifiers and annotations of the method.
 * @param returnType Type of the return value of the method (string).
 * @param parameters List of method {@link ParameterDescription}.
 * @param statements List of statements from the method body.
 */
public record MethodDescription(
    @JsonUnwrapped
    MemberDescription member,

    @JsonProperty("ReturnType")
    @JsonInclude(Include.NON_EMPTY)
    String returnType,

    @JsonProperty("Parameters")
    @JsonInclude(Include.NON_EMPTY)
    List<Description> parameters,

    @JsonProperty("Statements")
    @JsonInclude(Include.NON_EMPTY)
    List<Description> statements
) implements Description {

}

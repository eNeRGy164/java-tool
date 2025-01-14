package dev.dendrodocs.tool.descriptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConstructorDescriptionJsonTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void constructor_description_serializes_as_expected() throws IOException {
    assertEquals(
        mapper.readTree("{\"Name\":\"Foo\",\"Parameters\":[{\"Name\":\"bar\",\"Type\":\"Baz\"}]}"),
        mapper.valueToTree(new ConstructorDescription(
            new MemberDescription("Foo"),
            List.of(new ParameterDescription("Baz", "bar", List.of())), List.of()))
    );
  }
}

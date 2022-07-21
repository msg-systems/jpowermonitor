package group.msg.jpowermonitor.measurement.ohm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class DataElem {
    Integer id;
    @JsonProperty("Text")
    String text;
    @JsonProperty("Min")
    String min;
    @JsonProperty("Value")
    String value;
    @JsonProperty("Max")
    String max;
    @JsonProperty("ImageUrl")
    String imageUrl;
    @JsonProperty("Children")
    DataElem[] children;
}

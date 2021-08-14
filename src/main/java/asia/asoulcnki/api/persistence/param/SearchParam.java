package asia.asoulcnki.api.persistence.param;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SearchParam {

    @NotEmpty
    private String q;

    private Integer size = 10;

    private Integer from = 0;
}

package today.wishwordrobe.clothes.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultDto {
    private boolean success;
    private String msg;
    private int code;
}

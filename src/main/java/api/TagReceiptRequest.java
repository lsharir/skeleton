package api;
import org.hibernate.validator.constraints.NotEmpty;

public class TagReceiptRequest {
  @NotEmpty
  public int id;
}

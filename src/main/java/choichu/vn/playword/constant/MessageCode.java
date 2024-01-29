package choichu.vn.playword.constant;

public enum MessageCode {
  SUCCESS("0000", "Operation is successful"),
  WORD_NOT_FOUND("0001", "Word not found");

  private final String code;
  private final String message;

  MessageCode(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}

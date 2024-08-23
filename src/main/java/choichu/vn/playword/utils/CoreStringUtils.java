package choichu.vn.playword.utils;

import choichu.vn.playword.constant.CommonConstant;

public class CoreStringUtils {
  public static String removeExtraSpaces(String input) {
    return input.trim().replaceAll(" +", CommonConstant.SPACE);
  }

  public static String getFirstWord(String input) {
    return input.split(CommonConstant.SPACE)[0];
  }

  public static String getLastWord(String input) {
    String[] words = input.split(CommonConstant.SPACE);
    return words[words.length - 1];
  }
}

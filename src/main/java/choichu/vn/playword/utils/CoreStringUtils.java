package choichu.vn.playword.utils;

import choichu.vn.playword.constant.CommonStringConstant;

public class CoreStringUtils {
  public static String removeExtraSpaces(String input) {
    return input.trim().replaceAll(" +", CommonStringConstant.SPACE);
  }

  public static String getFirstWord(String input) {
    return input.split(CommonStringConstant.SPACE)[0];
  }

  public static String getLastWord(String input) {
    String[] words = input.split(CommonStringConstant.SPACE);
    return words[words.length - 1];
  }
}

package com.littlesaya;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtil {

	public static String[] getSheetList(HSSFWorkbook book) {
		String[] sheetList = new String[book.getNumberOfSheets()];
		for (int i = 0; i < sheetList.length; ++i) {
			sheetList[i] = book.getSheetName(i);
		}
		return sheetList;
	}
	
	public static String[] getSheetList(XSSFWorkbook book) {
		String[] sheetList = new String[book.getNumberOfSheets()];
		for (int i = 0; i < sheetList.length; ++i) {
			sheetList[i] = book.getSheetName(i);
		}
		return sheetList;
	}
	
	// 任何小于等于零的返回值都非法
	public static int strToNum(String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return alphaToDigit(str);
		}
	}
	
	private static int alphaToDigit(String alpha) {
		if (alpha.length() == 0) {
			return -1;
		}
		int value = 0;
		for (int chIdx = alpha.length() - 1, i = 0; chIdx >= 0; --chIdx, ++i) {
			int code = alpha.codePointAt(chIdx), digit;
			if (code >= 97 /* a */ && code <= 122 /* z */) {
				digit = code - 97 + 1;
				value += Math.pow(26, i) * digit;
			} else if (code >= 65 /* A */ && code <= 90 /* Z */) {
				digit = code - 65 + 1;
				value += Math.pow(26,  i) * digit;
			} else {
				// 不是字母
				return -1;
			}
		}
		return value;
	}
}

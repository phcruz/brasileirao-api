package br.com.phc.brasileiraoapi.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataUtil {

	public static String formatarDateEmString(Date data, String mask) {
		DateFormat formatter = new SimpleDateFormat(mask);
		return formatter.format(data);
	}
}

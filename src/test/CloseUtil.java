package test;

import java.io.Closeable;

public class CloseUtil {
	public static void closeAll(Closeable... io) {
		for (Closeable temp : io) {
			try {
				if (null != temp) {
					temp.close();
				}
			} catch (Exception e) {
				System.out.println("关闭流失败");
				e.printStackTrace();
			}
		}
	}
}

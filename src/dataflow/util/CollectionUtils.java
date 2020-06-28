package dataflow.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectionUtils {

	/**
	 * Listを指定したサイズ毎に分割します。
	 *
	 * @param origin 分割元のList
	 * @param size Listの分割単位
	 * @return サイズ毎に分割されたList。但し、Listがnullまたは空の場合、もしくはsizeが0以下の場合は空のListを返す。
	 */
	public static <T> List<List<T>> devide(List<T> origin, int size) {
		if (origin == null || origin.isEmpty() || size <= 0) {
			return Collections.emptyList();
		}

		int block = origin.size() / size + (origin.size() % size > 0 ? 1 : 0 );

		List<List<T>> devidedList = new ArrayList<List<T>>(block);
		for (int i = 0; i < block; i ++) {
			int start = i * size;
			int end = Math.min(start + size, origin.size());
			devidedList.add(new ArrayList<T>(origin.subList(start, end)));
		}
System.out.println("Devided PathList Size: " + devidedList.size());		
		return devidedList;
	}
}
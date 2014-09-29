package Armadillo.Core.Math;

import java.util.ArrayList;

import Armadillo.Core.Concurrent.BufferItem;

public class BinarySearch {

	public static int binarySearch(ArrayList<BufferItem> a, BufferItem b0) {

		if (a.size() == 0) {
			return -1;
		}
		int low = 0;
		int high = a.size() - 1;
		long b = b0.getAge();
		while (low <= high) {
			int middle = (low + high) / 2;
			if (b > a.get(middle).getAge()) {
				low = middle + 1;
			} else if (b < a.get(middle).getAge()) {
				high = middle - 1;
			} else { // The element has been found
				return middle;
			}
		}
		return -1;
	}
}

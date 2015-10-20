package storm.starter.util;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class Sampler<T> {

	private Random rand = new Random(new Date().getTime());

	public List<T> knuthSample(List<T> list, int sampleSize) {
		if (sampleSize > list.size())
			sampleSize = list.size();
		for (int i = 0; i < sampleSize; i++) {
			int position = i + rand.nextInt(list.size() - i);
			T temp = list.get(position);
			list.set(position, list.get(i));
			list.set(i, temp);

		}
		return list.subList(0, sampleSize);
	}
}



/*
 * Gensort Class so that merge sort sorts the data using only 10 bytes of data.
 */
public class gensortString implements Comparable<gensortString> {

	String line;

	public gensortString(String line) {
		this.line = line;
	}

	@Override
	public int compareTo(gensortString o) {

		// compares only first 10 lines
		String line1 = line.substring(0, 10);
		String line2 = o.line.substring(0, 10);

		if (line1.compareTo(line2) > 0) {
			return 1;
		} else if (line1.compareTo(line2) < 0) {
			return -1;
		} else {
			return 0;
		}
	}
}

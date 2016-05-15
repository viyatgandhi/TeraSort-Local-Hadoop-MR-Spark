import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class TeraMapper extends Mapper<Text, Text, Text, Text> {

	private Text finalKey = new Text();
	private Text finalValue = new Text();

	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

		String input = value.toString();
		String[] splits = input.split("  ", 2);

		String splitKey = splits[0];
		String splitValue = splits[1];
		
		// if key length is less than 10 update key and value as key can contain double spaces too
		if (splitKey.length() < 10) {
			String[] newinput = splits[1].split("  ", 2);
			String splitKeyNew = newinput[0];
			String splitValueNew = newinput[1];
			splitKey = splitKey + "  " + splitKeyNew;
			splitValue = splitValueNew;
		}

		finalKey.set(splitKey);
		finalValue.set(splitValue);
		context.write(finalKey, finalValue);

	}
}
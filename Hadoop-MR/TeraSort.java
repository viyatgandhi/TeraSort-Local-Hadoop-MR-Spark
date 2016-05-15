import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.InputSampler;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/*
 * References taken from 
 * https://hadoopi.wordpress.com/2013/06/05/hadoop-implementing-the-tool-interface-for-mapreduce-driver/
 * http://blog.ditullio.fr/2016/01/04/hadoop-basics-total-order-sorting-mapreduce/
 * http://www.philippeadjiman.com/blog/2009/12/20/hadoop-tutorial-series-issue-2-getting-started-with-customized-partitioning/
 * https://pipiper.wordpress.com/2013/05/02/sorting-using-hadoop-totalorderpartitioner/
 * http://stackoverflow.com/questions/19624607/mapper-input-key-value-pair-in-hadoop
 * http://stackoverflow.com/questions/8603788/hadoop-jobconf-class-is-deprecated-need-updated-example
 * https://hadoop.apache.org/docs/current/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html
 * https://svn.apache.org/repos/asf/hadoop/common/branches/MAPREDUCE-233/src/examples/org/apache/hadoop/examples/terasort/
 * https://developer.yahoo.com/hadoop/tutorial/
 * .....and many more 
 */

public class TeraSort extends Configured implements Tool {

	private static final Log logger = LogFactory.getLog(TeraSort.class);

	public static void main(String[] args) throws IOException {

		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

		if (otherArgs.length != 3) {
			System.out.println("Not a valid Input. Please provide input in below way");
			System.out.println("TeraSort <inputFolder> <outputFolder> <partitionFileFolder>");
			System.exit(1);
		}

		try {
			ToolRunner.run(new TeraSort(), otherArgs);
		} catch (Exception ex) {
			logger.error(null, ex);
		}
	}

	@Override
	public int run(String[] newargs) throws Exception {

		Job job = Job.getInstance(new Configuration());
		job.setJarByClass(TeraSort.class);

		// set mapper and reducer class
		job.setMapperClass(TeraMapper.class);
		job.setReducerClass(TeraReducer.class);

		// set number of reducers
		job.setNumReduceTasks(32);

		job.setInputFormatClass(KeyValueTextInputFormat.class);

		// set output of map class text as key
		job.setMapOutputKeyClass(Text.class);

		// set output of reducer as text class as key and value both are Text
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		// set input path for the job
		FileInputFormat.addInputPath(job, new Path(newargs[0]));

		Path partitionFile = new Path(new Path(newargs[2]), "partitioning");
		TotalOrderPartitioner.setPartitionFile(job.getConfiguration(), partitionFile);

		// use random sampler to write partitioner file
		InputSampler.Sampler<Text, Text> sampler = new InputSampler.RandomSampler<>(0.01, 1000,32);
		InputSampler.writePartitionFile(job, sampler);

		// set partitioner to TotalOrderPartitioner
		job.setPartitionerClass(TotalOrderPartitioner.class);

		// set output directory for the job
		FileOutputFormat.setOutputPath(job, new Path(newargs[1]));

		int ret = job.waitForCompletion(true) ? 0 : 1;
		logger.info("Done");
		return ret;

	}
}

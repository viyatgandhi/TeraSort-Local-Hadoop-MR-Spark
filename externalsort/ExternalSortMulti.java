import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/*
 *  References for external sorting algorithm is taken from 
 *  https://en.wikipedia.org/wiki/External_sorting
 *  http://www.ashishsharma.me/2011/08/external-merge-sort.html
 *  http://www.csbio.unc.edu/mcmillan/Media/Comp521F10Lecture17.pdf 
 *  http://www.ashishsharma.me/2011/08/external-merge-sort.html
 *  
 */

/*
 * This External Sort algorithm can be ran in Multithreaded mode.
 * Threading can be performed during splitting and sorting phase 
 * In the merge phase files are written to single output file   
 */

public class ExternalSortMulti {

	// list of split files
	static List<File> files = new ArrayList<File>();

	public static void splitAndSort(File file, int thread) throws IOException {

		System.out.println("Creating buffer reader for input file");
		BufferedReader filereader = new BufferedReader(new FileReader(file));

		System.out.println("Estimating file size to be split based on JVM free memory");
		
		long freespace = Runtime.getRuntime().freeMemory();
		
		System.out.println("JVM free memory: "+freespace);

		long splitsize = freespace / 2;// in bytes
        //long splitsize = 64000000;// in bytes
		// estimate number of loops
		//float loopcount = (float) file.length() / splitsize;
		//int loop = (int) Math.ceil(loopcount);
		//int loop =;

		// temp list to store the input from large file and pass it to thread

		List<String> tmpSplit = new ArrayList<String>();

		System.out.println("Block size is: " + splitsize);

		// define number of threads here
		int noOfThreads = thread;
		Thread[] threads = new Thread[noOfThreads];

		System.out.println("Number of threads are: " + noOfThreads);
		//System.out.println("Number of loops are: " + loop);

		try {

			String line = "";
			//int count = 0;
			try {
				while (line != null) {

					for (int i = 0; i < threads.length; i++) {
						long tempfilesize = 0;
						while ((tempfilesize < splitsize) && ((line = filereader.readLine()) != null)) {
							tmpSplit.add(line);
							tempfilesize += line.length()+2; // 2 added for new line char
						}

						//System.out.println("Creating Split file number: " + count);
						
						if (tmpSplit.size()!=0) {
							threads[i] = new Thread(new sortAndSaveFile(tmpSplit));
							threads[i].start();
							tmpSplit.clear();
							//count++;
						} else {
							break;
						}
					}

					//System.out.println("waiting for threads to complete");
					// wait for threads to complete so that only specific number
					// of threads work for split and save
					while (Thread.activeCount() != 1) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					//System.out.println("File added to list: " + count);
				}

			} catch (EOFException oef) {
				System.out.println("EOF exception occured");
			}
		} finally {
			filereader.close();
		}
	}

	public static void mergeFiles(List<File> files, File outputfile) throws IOException {

		// using custom Comparator so that only first 10 bytes are compared by
		// the PQ
		final Comparator<String> comparator = new Comparator<String>() {
			public int compare(String l1, String l2) {

				String line1 = l1.substring(0, 10);
				String line2 = l2.substring(0, 10);

				if (line1.compareTo(line2) > 0) {
					return 1;
				} else if (line1.compareTo(line2) < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		};

		// PQ for the binary file buffer - used to get lowest value from the
		// files
		PriorityQueue<BufferReaderFile> pq = new PriorityQueue<BufferReaderFile>(10,new Comparator<BufferReaderFile>() {
					public int compare(BufferReaderFile i, BufferReaderFile j) {
						return comparator.compare(i.peek(), j.peek());
					}
				});

		// int count = 1;
		for (File f : files) {
			// System.out.println("Adding temp file to the PQ: " + count);
			BufferReaderFile splitFile = new BufferReaderFile(f);
			pq.add(splitFile);
			// count++;
		}

		BufferedWriter filewriter = new BufferedWriter(new FileWriter(outputfile));
		System.out.println("Writing data to final file... please wait");
		try {

			// using while loop to get the lowest value from the file and write
			// it to final file
			while (pq.size() > 0) {
				BufferReaderFile filereader = pq.poll();
				String r = filereader.pop();
				filewriter.write(r);
				filewriter.write("\r\n");
				if (filereader.empty()) {
					filereader.fbr.close();
					// delete the tmp file if empty to save space
					filereader.originalfile.delete();
				} else {
					// add file back to PQ if not empty for the rest of the
					// records
					pq.add(filereader);
				}
			}
		} finally {
			filewriter.close();
			for (BufferReaderFile bfb : pq)
				bfb.close();
		}
	}

	public static void main(String[] args) throws IOException {
	
		 if(args.length != 3) {
           	    System.out.println("Please provide proper parameters to run the program");
		    System.out.println("Usage: ExternalSortMulti <inputfilename> <outputfilename> <No Of Threads>");
	            return;
        	 }

		long startTime, finishTime, elapsedTime;

		String inputfile =  args[0];;
		String outputfile =  args[1];
		int noOfThreads =  Integer.valueOf(args[2]);

		System.out.println("input file is: " + inputfile);
		System.out.println("output file is: " + outputfile);
		System.out.println("Starting splitAndSort Process");

		startTime = System.currentTimeMillis();

		splitAndSort(new File(inputfile),noOfThreads);

		System.out.println("splitAndSort Completed");
		System.out.println("Active Current thread count : " + Thread.activeCount());
		System.out.println("Starting merge phase");

		mergeFiles(files, new File(outputfile));

		finishTime = System.currentTimeMillis();
		elapsedTime = finishTime - startTime;
		float elapsedTimeSeconds = (float) (elapsedTime / 1000.0);

		System.out.println("Merge Phase completed, Sorting Done.");
		System.out.println("Total time taken is: " + elapsedTimeSeconds / 60 + " minutes");

	}
}

//BufferReaderFile class used for pq

class BufferReaderFile {
	
	public static int BUFFERSIZE = 200;
	public BufferedReader fbr;
	public File originalfile;
	private String cache;
	private boolean empty;

	public BufferReaderFile(File f) throws IOException {
		originalfile = f;
		fbr = new BufferedReader(new FileReader(f), BUFFERSIZE);
		reload();
	}

	public boolean empty() {
		return empty;
	}

	private void reload() throws IOException {
		try {
			if ((this.cache = fbr.readLine()) == null) {
				empty = true;
				cache = null;
			} else {
				empty = false;
			}
		} catch (EOFException oef) {
			empty = true;
			cache = null;
		}
	}

	public void close() throws IOException {
		fbr.close();
	}

	public String peek() {
		if (empty())
			return null;
		return cache.toString();
	}

	public String pop() throws IOException {
		String answer = peek();
		reload();
		return answer;
	}

}

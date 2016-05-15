import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * This class sorts the data using merge sort and creates the temp file
 *  
 */

public class sortAndSaveFile extends ExternalSortMulti implements Runnable {

	private List<String> tmplistrun = null;

	public sortAndSaveFile(List<String> tmplistnew) {
		tmplistrun = new ArrayList<String>();
		// creating deep copy
		for (String item : tmplistnew)
			tmplistrun.add(item);
	}

	@Override
	public void run() {

		//System.out.println("Sorting the file");

		int length = tmplistrun.size();
		
		// creating gensortString array 
		gensortString mergeSortArray[] = new gensortString[length];

		for (int i = 0; i < length; i++) {
			gensortString line = new gensortString(tmplistrun.get(i));
			mergeSortArray[i] = line;
		}
		// sort using merge sort
		mergeSort.sort(mergeSortArray);

		File newtmpfile = null;

		try {
			newtmpfile = File.createTempFile("externalSortfile", null);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// delete the file when program exits
		newtmpfile.deleteOnExit();
		BufferedWriter fbw = null;
		try {
			fbw = new BufferedWriter(new FileWriter(newtmpfile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			for (gensortString ans : mergeSortArray) {
				fbw.write(ans.line);
				fbw.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fbw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//System.out.println("Writing to temp file completed for: " + Thread.currentThread().getName());
		
		// add the temp file to file list for further processing
		files.add(newtmpfile);
	}
}

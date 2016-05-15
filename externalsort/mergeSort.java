

/*
 * Merge Sort - Reference taken from CS401 Fall 2015 Class 
 */

public class mergeSort {
	
	public static void sort(Object[] a){
		Object aux[] = (Object [])a.clone();
		mergesort(aux, a, 0, a.length);
	}	


	@SuppressWarnings("unchecked")
	private static void mergesort(Object src[], Object dest[], int low, int high)
	{
		int length = high - low;
	
		if(length < 7){
			for(int i = low; i<high; i++)
				for(int j=i; j> low && ((Comparable)dest[j-1]).compareTo(dest[j]) > 0; j--)
						swap(dest, j, j-1);

			return;
		}	
		int mid = (low + high) >> 1;
		mergesort(dest, src, low, mid);
		mergesort(dest, src, mid, high);

		if(((Comparable)src[mid-1]).compareTo(src [mid]) <= 0){
			System.arraycopy(src, low, dest, low, length);
			return;
		}

		for(int i=low, p=low, q=mid; i<high; i++)
			if( (q>=high) || (p<mid && ((Comparable)src[p]).compareTo(src[q]) <= 0))
				dest[i] = src[p++];
			else
				dest[i] = src[q++];
	}

	public static void swap (Object [] x, int a, int b){
        	 Object t = x[a];
	         x[a] = x[b];
        	 x[b] = t;
	}
	

}

package cloudscapes;

import java.util.concurrent.RecursiveTask;

public class SumArr extends RecursiveTask<Float>  {
	  int      lo;
	  int	   hi;
	  float[] arr;
	  static final int SEQUENTIAL_CUTOFF=35000;

	  int ans = 0; 
	    

	SumArr(float[] arr, int l, int h) { 
	    lo=l; 
	    hi=h;
	    this.arr=arr;
	  }


	  protected Float compute(){
		  if((hi-lo) < SEQUENTIAL_CUTOFF) {
			  float ans = 0;
		      for(int i=lo; i < hi; i++)
		        ans += arr[i];
		      return ans;
		  }
		  else {
			  SumArr left = new SumArr(arr,lo,(hi+lo)/2);
			  SumArr right= new SumArr(arr,(hi+lo)/2,hi);
			  
			  left.fork();
			  float rightAns = right.compute();
			  float leftAns  = left.join();
			  return leftAns + rightAns;     
		  }
	 }
}

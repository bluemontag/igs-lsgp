/**
 * Creation date: 09/03/2016
 * 
 */
package mckaywormald.model.generators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import commons.generators.IRandomLatinRectangleGenerator;
import commons.generators.IRandomLatinSquareGenerator;
import commons.model.OrderedTriple;
import commons.model.latinsquares.ILatinRectangle;
import commons.model.latinsquares.ILatinSquare;
import commons.utils.RandomUtils;
import mckaywormald.model.LatinRectangle;

/**
 * 
 *  McKay-Wormald method for generating LRs of n * k where k=O(n^(1/3)). 
 * 
 * @author igallego
 *
 */
public class McKayLRGenerationMethod implements IRandomLatinRectangleGenerator, IRandomLatinSquareGenerator {
	
	//auxiliary variables for the algorithm
	private Set<Integer> initiallyAvInRow = null;

	//from mckay paper
	private int conflictsCount = 0;
	private boolean hasOverlappingConflicts = false;
	private int[][] timesSymbolOccursInColumn = null; 
	private List<OrderedTriple> conflictList = null; 
	private int k;
	private int n;
	
	/**
	 * Initializes the instance with the dimensions of the LR, available symbols and the random number generator.
	 * 
	 * @param k
	 * @param n
	 */
	public McKayLRGenerationMethod(int k, int n) {
		this.k = k;
		this.n = n;
		this.initiallyAvInRow = RandomUtils.oneToN(n);
		RandomUtils.initRand();
	}
	
	/**
	 * Implements the method of the interface {@link IRandomLatinSquareGenerator}.
	 * 
	 */
	@Override
	public ILatinSquare generateLS() {
		//if (this.k==this.n)
			return this.generateLR();
		//else
			/*System.out.println("Could not generate a square of "+this.k+" rows by "+this.n+" columns.");
		return null;*/
	}
	
	/**
	 * Implements the method of the interface {@link IRandomLatinRectangleGenerator}.
	 * 
	 */	
	@Override
	public ILatinRectangle generateLR() {
		
		boolean rejected = false;
		LatinRectangle a = null;
		double pow = Math.pow(n, 2);
		
		int outerLoop = 0;
//		int maxInnerLoop = 0;
		do {
			if (outerLoop%1000==0 && outerLoop>0)
				System.out.println("Outer:"+outerLoop);
			
//			outerLoop++;
//			int matrixCount = 0;
			do {
		    	a = randomMemberOfMkn();
//		    	matrixCount++;
//		    	System.out.println("Generated matrix A N�"+matrixCount+" with conflicts count:"+this.conflictsCount+" (>"+pow+"?) Overlapping:"+this.hasOverlappingConflicts);
		    } while (this.conflictsCount>pow || this.hasOverlappingConflicts); //replaced "repeat-until(p)" by "do-while(!p)"
	
		    rejected = false;
//		    int innerLoop = 0;
		    
		    while (this.conflictsCount>0 && !rejected) {
//		    	innerLoop++;
//		    	if (innerLoop>maxInnerLoop) {
//		    		maxInnerLoop=innerLoop;
//		    		System.out.println("MaxInnerLoop:"+maxInnerLoop);
//		    	}
//		    	
		    	//take a conflict at random in constant time
		    	OrderedTriple conflict = RandomUtils.randomTriple(this.conflictList);
		    	
		    	int i1 = conflict.x;
		    	int i2 = conflict.y;
		    	int j1 = conflict.z;
		    	
		    	Set<Integer> nMinusj1 = new HashSet<Integer>(this.initiallyAvInRow);//can this be avoided? is O(n)??
		    	nMinusj1.remove(j1);
		    	
		    	int j2 = RandomUtils.randomChoice(nMinusj1);
		    	
		    	nMinusj1.remove(j2);
		    	
		    	int j3 = RandomUtils.randomChoice(nMinusj1);
		    	
		    	//test if (i1,i2,j1,j2,j3) \in sw(a)
		    	int y = a.getValueAt(i1, j1);
		    	int u = a.getValueAt(i1, j2);
		    	int v = a.getValueAt(i1, j3);
		    	
		    	//x1
		    	boolean x1 = (y == a.getValueAt(i2, j1));
		    	//x2 : u \notIn A[C_{j2} - {i1,j2}]
		    	boolean x2 = (this.timesSymbolOccursInColumn[u][j2]==1);
		    	//x3 : v \notIn A[C_{j3} - {i1,j3}]
		    	boolean x3 = (this.timesSymbolOccursInColumn[v][j3]==1);
		    	//x4 : y \notIn A[C_{j2}]
		    	boolean x4 = (this.timesSymbolOccursInColumn[y][j2]==0);
		    	//x5 : u \notIn A[C_{j3}] 
		    	boolean x5 = (this.timesSymbolOccursInColumn[u][j3]==0);
		    	//x6 : v \notIn A[C_{j1}]
		    	boolean x6 = (this.timesSymbolOccursInColumn[v][j1]==0);
		    	
		    	if (x1 && x2 && x3 && x4 && x5 && x6) {// (i1,i2,j1,j2,j3) \in sw(a), apply the switching
		    	  	a.setValueAt(i1, j1, v);
		    	  	a.setValueAt(i1, j2, y);
		    	  	a.setValueAt(i1, j3, u);
		    	  	rejected = false;//don't do "rejected=true with Probability(...)" allways accept A if sw(A) is possible
		    	} else {
		    		rejected = true;
		    	}
		    	
		    	//remove conflict
		    	this.conflictList.remove(conflict);
	    	  	this.conflictsCount--;
		    }
		} while (rejected);
	    return a;
	}
	
	/**
	 * Implementation of auxiliary method in McKay's algorithm.
	 * 
	 * @return
	 */
	private LatinRectangle randomMemberOfMkn() {
		this.conflictsCount = 0;
		this.hasOverlappingConflicts = false;
		this.conflictList = new ArrayList<OrderedTriple>();
		this.timesSymbolOccursInColumn = new int[n][n]; //tells how many times a symbol occurs in the column
	    
		LatinRectangle lr = new LatinRectangle(k, n);
	    
	    for (int i=0; i<k; i++) {
	    	ArrayList<Integer> row = this.generateRow(i, lr);
	    	
	    	lr.setRow(i, row);
	    }
	    return lr;
	}
	
	/**
	 * Generates one row with no repetitions, but possibly with conflicts with previous columns. It counts and saves the conflicts.
	 * 
	 * @param rowIndex
	 * @param lr
	 * @return
	 */
	private ArrayList<Integer> generateRow(int rowIndex, LatinRectangle lr) {
	    List<Integer> availableInRow = new ArrayList<Integer>(this.initiallyAvInRow);
	    
	    ArrayList<Integer> row = new ArrayList<Integer>();
	    int colIndex = 0;
	    while (colIndex < n) {
	    	//select symbol
	    	Integer symbol = RandomUtils.randomChoice(availableInRow);

	    	this.timesSymbolOccursInColumn[symbol][colIndex]++;
	    	
	    	//conflict checks
            if (this.timesSymbolOccursInColumn[symbol][colIndex]>1) {//!this.availableInCol[colIndex].contains(symbol)) {
            	this.conflictsCount++;
            	int last = this.lastRowIndexOf(symbol, lr, rowIndex, colIndex);//this step is at most of O(k)
            	this.conflictList.add(new OrderedTriple(last, rowIndex, colIndex));
            	if (this.timesSymbolOccursInColumn[symbol][colIndex]>2) {
            		this.hasOverlappingConflicts=true;
            	}
            }

            //remove from available
            availableInRow.remove(symbol);

            //put symbol in result
            row.add(symbol);

            colIndex++;
	    }
	    
	    return row;
	}
	
	
	private int lastRowIndexOf(int symbol, LatinRectangle lr, int row, int col) {
		int result = -1;
		boolean found = false;
		int i = row-1;//search from last row until row 0
		while (!found && i>=0) {
			found = (lr.getValueAt(i,col)==symbol);
			if (found)
				result = i;
			i--;
		}
		return result;
	}
	
	/**
	 * Reimplement method name.
	 * 
	 */
	@Override
	public String getMethodName() {
		return "McKay-Wormald generation of Latin rectangle.";
	}

	@Override
	public void setVerbose(boolean show) {
		//to be implemented soon...
	}

}
		        
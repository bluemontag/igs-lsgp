/**
 * Creation date: 14/03/2016
 * 
 */
package seqgen.model.generators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import commons.generators.IRandomLatinSquareGenerator;
import commons.model.latinsquares.ArrayListLatinSquare;
import commons.model.latinsquares.ILatinSquare;
import commons.utils.RandomUtils;

/**
 * This class abstracts the common behaviour of all generators that operate sequentially, generating one random symbol at the time,
 * and completing the LS by rows (up to down) and columns (left to write). This type of algorithms is based on the concept of the 
 * theorem that claims that an i*n Latin Rectangle (where i&lt;n) can always be completed to an n*n Latin Square.
 * 
 * @author igallego
 */
public abstract class AbstractSequentialGenerator implements IRandomLatinSquareGenerator {

	
	protected int n = 0;//the size of the LSs to be generated
	
	//auxiliary structures
	protected Set<Integer>[] availableInCol;
	protected ILatinSquare ls;
	protected int[] failedAttemptsPerRow;
	protected int[][] collisions;
	
	//the set of all possible symbols
	protected Set<Integer> symbols = null;

	/**
	 * Constructs the instance that generates LS of order n, with the auxiliary variables initialized to count conflicts and so.
	 * 
	 * @param n
	 */
	public AbstractSequentialGenerator(int n) {
		this.n = n;
		
		RandomUtils.initRand();
		this.symbols = RandomUtils.oneToN(n);
	}
	
	/**
	 * It prepares the generator for a new generation
	 */
	@SuppressWarnings("unchecked")
	public void initialize() {
		
		availableInCol = new HashSet[n];
		failedAttemptsPerRow = new int[n];
		collisions = new int[n][n];
		
		//initially available in each column
	    for (int i=0; i<n; i++) {
	    	availableInCol[i] = new HashSet<Integer>(this.symbols);
	    }
	    
	    ls = new ArrayListLatinSquare(n);//default implementation
	}
	
	/**
	 * Generates the LS row by row
	 */
	@Override
	public ILatinSquare generateLS() { 
		this.initialize();
		
	    for (int i=0; i<n; i++) {
	    	List<Integer> row = this.generateRow(i);
	    	ls.setRow(i, row);
	    }

	    return ls;
	  }

	/** 
	 * Generates row i_row of LS. 
	 * 
	 * This default implementation does not take into account the conflicts in the generated row with previous columns.
	 *  
	 * @param i_row
	 * @return
	 */
	protected List<Integer> generateRow(int i_row) {
	    HashSet<Integer> availableInRow = new HashSet<Integer>(this.symbols);//all symbols initially available in the row 
	    
	    ArrayList<Integer> row = new ArrayList<Integer>();
	    int i_col = 0;
	    
	    while (i_col < n) {
            Integer symbol = RandomUtils.randomChoice(availableInRow);

            i_col = i_col + 1;
            availableInRow.remove(symbol);//to avoid repetition in the generated row
            row.add(symbol);
	    }

	    return row;
	}
	
	/**
	 * Returns the method name to print in the console.
	 */
	@Override
	public String getMethodName() {
		return "Sequential generation abstract.";
	}

	@Override
	public void setVerbose(boolean show) {
		//to be implemented soon...
	}

}
	        
/**
 * Creation date: 12/05/2015
 * 
 */

/**
 * � Copyright 2012-2015 Ignacio Gallego Sagastume
 * 
 * This file is part of IGS-ls-generation package.
 * IGS-ls-generation package is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * 
 * IGS-ls-generation package is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with IGS-ls-generation package.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 */
package seqgen.model.generators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import commons.utils.RandomUtils;

/**
 *  This class implements the method with the replacement graph.
 *   The generation is done row by row. When a conflict occurs,
 *  a replacement graph is constructed and the row is fixed to make room for the conflicting element.
 *  This is the best method of the SimpleGen-type algorithms. It's very efficient and finishes in polynomial type.
 * 
 * @author igallego
 *
 */
public class SeqGenWithReplGraph extends AbstractSequentialGenerator {

	/**
	 * Constructs the instance that generates LSs of order n.
	 * 
	 * @param n
	 */
	public SeqGenWithReplGraph(int n) {
		super(n);
	}

	/**
	 * Reimplements the method that generates row i_row. When a conflict is encountered, it construct a graph and makes replacements until a symbol is freed.
	 *  This allows for the continuation of the generation (it saves the conflict).
	 *    
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Integer> generateRow(int i_row) {
		Set<Integer> availableInRow = new HashSet<Integer>(this.symbols);
	    
	    Set<Integer>[] initialAvailableInCol = new HashSet[n];
	    
	    for (int j=0; j<n; j++) {
	    	initialAvailableInCol[j] = new HashSet<Integer>(availableInCol[j]);
	    }
	    
	    //result of this method
	    ArrayList<Integer> row = new ArrayList<Integer>();
	    int i_col = 0;
	    
	    while (i_col < n) {//when i_col is n, there are n chosen numbers
	        //available is:
	        HashSet<Integer> available = new HashSet<Integer>(availableInCol[i_col]);
	    	available.retainAll(availableInRow);
	    	
	        if (!available.isEmpty()) { //if there are available
	            //choose a symbol at random
	            Integer symbol = RandomUtils.randomChoice(available);
	            //count the chosen symbol
	            availableInCol[i_col].remove(symbol);
	            availableInRow.remove(symbol);
	            row.add(symbol);
	            i_col++;
	        } else {//collision
	        	HashMap<Integer, HashSet<Integer>> map = this.constructReplGraph(row, i_col, initialAvailableInCol);
	        	int elem = RandomUtils.randomChoice(availableInCol[i_col]);
	        	this.makeElemAvailable(elem, map, row, i_col, availableInRow);
	        }
	    }
	    return row;
	}

	/**
	 *  It constructs the replacement graph.
	 *  
	 * @param row
	 * @param col
	 * @param initialAvailInCol
	 * @return
	 */
	protected HashMap<Integer, HashSet<Integer>> constructReplGraph(ArrayList<Integer> row, 
																 Integer col, 
																 Set<Integer>[] initialAvailInCol) {
		
		HashMap<Integer, HashSet<Integer>> map = new HashMap<Integer, HashSet<Integer>>();
		
		for(int j=col; j>=0; j--) {
			HashSet<Integer> set = new HashSet<Integer>(initialAvailInCol[j]);

			if (set.size()>0)
				map.put(j, set);//the element in position j could potentially be changed for one in the set
		}

		return map;
	}
	
	/**
	 * It makes room to free the element "old".
	 * 
	 * @param old
	 * @param map
	 * @param row
	 * @param col
	 * @param availableInRow
	 */
	protected void makeElemAvailable(Integer old, HashMap<Integer, HashSet<Integer>> map, 
								  ArrayList<Integer> row, Integer col, Set<Integer> availableInRow) {
		boolean finished = false;
		
		int firstElem = new Integer(old);
		
		this.eraseFirstElemFromGraph(map, firstElem);
	
		int idx_old = row.indexOf(old);
		int idx_new;

		int i=0;
		
		HashSet<Integer> path = new HashSet<Integer>();

		while (!finished) {
//			if (idx_old==-1) {//there are no repetitions, but the element is still in the row
//				idx_old = row.indexOf(firstElem);
//				old = firstElem;
//			}
			
			Set<Integer> avail = new HashSet<Integer>(map.get(idx_old));
			avail.removeAll(path);
			
			if (avail.isEmpty()) {
				//Path no good, begin again
				path = new HashSet<Integer>();
				avail.addAll(map.get(idx_old));//cannot avoid addAll. "Magic", do not touch.
			}
			
			Integer newElem = RandomUtils.randomChoice(avail);
			idx_new = row.indexOf(newElem);//index of this elem before replacement because it will be repeated
						
			//replace 
			row.set(idx_old, newElem);
		
			//store in path 
			path.add(newElem);
			
			if (row.indexOf(old)==-1) //if the old element is not in the row
				availableInRow.add(old);
			availableInRow.remove(newElem);

			//if (availInCol[idx_old].indexOf(old)==-1)//check to avoid repetition
			availableInCol[idx_old].add(old);
			availableInCol[idx_old].remove(newElem);
			
			finished = (availableInRow.contains(firstElem) && idx_new==-1);// || (path.size()==n);//the element is now available in row and there are no repetitions
			
			idx_old = idx_new;
			old = newElem;
			
			i++;
			if (i%100000==0) {
				System.out.println(i);
				if (i%1000000==0) {
					System.out.println("Is this an infinite loop?");
					System.out.println(firstElem);
					System.out.println(idx_old);
					System.out.println(map);
					System.out.println(row);
				}
			}
		}
	}

	/**
	 * Overrides to return the method's name.
	 */
	@Override
	public String getMethodName() {
		return "Sequential generation with replacement graph";
	}
	

	/**
	 * Auxiliary method.
	 * 
	 * @param map
	 * @param firstElem
	 */
	private void eraseFirstElemFromGraph(HashMap<Integer, HashSet<Integer>> map, Integer firstElem) {
		Iterator<Integer> iter = map.keySet().iterator();
		
		while (iter.hasNext()) {
			Integer elem = iter.next();
			HashSet<Integer> set = map.get(elem);
			set.remove(firstElem);
			map.put(elem, set);
		}
	}
}

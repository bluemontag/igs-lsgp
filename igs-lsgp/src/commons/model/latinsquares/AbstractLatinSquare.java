/**
 * Creation date: 14/03/2016
 * 
 */
package commons.model.latinsquares;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 *  This class provides the default behaviour for ILatinSquare implementations.
 * 
 * @author igallego
 *
 */
public abstract class AbstractLatinSquare implements ILatinSquare {

	protected int n = 0;
	protected MessageDigest md = null;
	
	/**
	 * Constructs the instance of an empty LS of order n.
	 * 
	 * @param n
	 */
	public AbstractLatinSquare(int n) {
		this.n = n;
		
		//initialize the md
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("No such algorithm: md5");
		}
	}
	
	/**
	 * Default behaviour of size().
	 */
	@Override
	public int size() {
		return n;
	}
	
	/**
	 * Writes the instance to a string to print the results in a system console. This method provides the default behaviour for different LS implementations.
	 * 
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Latin square of order "+n+":\n");
		for (int x=0; x<n ; x++) {
			//sb.append("Row "+x+":");
			for (int y=0; y<n ; y++) {
				try {
					Integer elem = this.getValueAt(x, y);
					sb.append(elem); 
					sb.append("    ".substring(elem.toString().length()));
					
				} catch (Exception e) {
					sb.append("--  ");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Writes the instance's representation to a file. This method provides the default behaviour for different LS implementations.
	 * 
	 */
	@Override
	public void writeToFile(String fileName) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			
			for (int i=0; i<n; i++) {
				for (int j=0; j<n; j++) {
					Integer elem = this.getValueAt(i, j);
					bw.write(elem.toString());
					bw.write("    ".substring(elem.toString().length()));
				}
				bw.write("\n");
			}
			bw.close();
		} catch (Exception e) {
			System.out.println("Could not write to file "+fileName);
		}
	}

	/**
	 * Default equality: every symbol must be the same.
	 * 
	 */
	@Override
	public boolean equals(ILatinSquare ls2) {
		int n2 = ls2.size();
		if (this.size()!=n2) return false;
		boolean eq = true;
		for (int i=0; i<n2 && eq; i++) {
			for (int j=0; j<n2 && eq; j++) {
				if (this.getValueAt(i, j).intValue()!=ls2.getValueAt(i, j).intValue()) {
					eq = false;
				}
				
			}
		}
		return eq;
	}
	
	/**
	 * Computes a hash of the structure.
	 * 
	 */
	@Override
	public byte[] hashCodeOfStructure() {
		String str1 = this.serializeStructure();
		return md.digest(str1.getBytes());
	}
	
	/**
	 *  Writes the LS into a string without spaces or new line symbols.
	 *  
	 */
	@Override
	public String serializeStructure() {
		StringBuffer sb = new StringBuffer();
		for (int x=0; x<n ; x++) {
			for (int y=0; y<n ; y++) {
				Integer elem = this.getValueAt(x, y);
				sb.append(elem); 
			}
		}
		return sb.toString();
	}
	
	/**
	 * Compares two hashes.
	 */
	@Override
	public boolean equalHash(byte[] dig1, byte[] dig2) {
		return MessageDigest.isEqual(dig1, dig2);
	}
	
	/**
	 *  Returns true if the current array is a LS, and false if there are any repetitions in a row or column.
	 *  
	 */
	@Override
	public boolean preservesLatinProperty() {
		boolean result = true;
		
		Set<Integer> symbols = new HashSet<Integer>();
		
		//row verification
		for(int i=0; i<n; i++) {
			symbols = new HashSet<Integer>();
			for(int j=0; j<n; j++) {
				symbols.add(this.getValueAt(i, j));	
			}
			if (symbols.size()!=n)
				return false;
		}
		
		//column verification
		for(int j=0; j<n; j++) {
			symbols = new HashSet<Integer>();
			for(int i=0; i<n; i++) {
				symbols.add(this.getValueAt(i, j));	
			}
			if (symbols.size()!=n)
				return false;
		}
		return result;
	}

}

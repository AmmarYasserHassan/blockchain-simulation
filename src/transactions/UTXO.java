package transactions;

import java.lang.reflect.Array;
import java.security.PublicKey;
import java.util.ArrayList;

public class UTXO extends TransactionOutput {
	
	private String senderDisplayName;
	
	private PublicKey senderPublicKey;
	
	public UTXO(PublicKey senderPublicKey, PublicKey recieverPublicKey, String senderDisplayName,String recieverDisplayName, double amount) {
		super(recieverPublicKey,recieverDisplayName,amount);
		this.senderPublicKey = senderPublicKey;
		this.senderDisplayName = senderDisplayName;
	}
	
	public UTXO(PublicKey senderPublicKey, PublicKey recieverPublicKey, String senderDisplayName,
			String recieverDisplayName, double amount,String id) {
		super(recieverPublicKey,recieverDisplayName,amount,id);
		this.senderPublicKey = senderPublicKey;
		this.senderDisplayName = senderDisplayName;
	}
		
	public String toString() {
		return "[ " + this.senderDisplayName + " ] --> [ " + this.recieverDisplayName + " ] "+ amount+" BTC";
	}

	public UTXO clone() {
		return new UTXO(senderPublicKey, recieverPublicKey, senderDisplayName, recieverDisplayName, amount);
	}
	
	public static void main(String[] args) {
		
		ArrayList<Integer> forks = new ArrayList<Integer>();
		
		
		
		
		int longest = 0;
		int indexOfLongestChain = 0;
		int secondLongest = 0;
		
		for (int i = 0; i < forks.size(); i++) {
			int newSize = forks.get(i);
			if (newSize > longest) {
				longest = newSize;
				indexOfLongestChain = i;
			}
		}
		
		for (int i = 0; i < forks.size(); i++) {
			int newSize = forks.get(i);
			if(newSize<=longest && i!=indexOfLongestChain) {
			if (newSize > secondLongest) {
				secondLongest = newSize;
			}
		}
		}
		
		System.out.println(longest);
		System.out.println(secondLongest);
		
	}
}

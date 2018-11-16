package transactions;

public class Trindex {
	
	private Transaction transaction;
	private int index;
	
	public Trindex(Transaction trans,int index) {
		this.transaction = trans;
		this.index = index;
	}
	
	
	public Transaction getTransaction() {
		return this.transaction;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public boolean equals(Object o) {
		Trindex o2 = (Trindex) o;
		return this.transaction.equals(o2.getTransaction()) && this.index == o2.index;
	}
	
	
	public String toString() {
		return this.transaction.getId() + " - "+ "index: " + this.index;
	}
	
	public Trindex clone() {
		return new Trindex(transaction.clone(),index);
	}

}

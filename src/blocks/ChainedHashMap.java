package blocks;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ChainedHashMap {

	private LinkedHashMap<String, Block> hashMap;
	private ArrayList<String> keys;

	public ChainedHashMap() {
		this.hashMap = new LinkedHashMap<>();
		this.keys = new ArrayList<>();
	}

	private ChainedHashMap(LinkedHashMap hashMap2, ArrayList keys2) {
		this.hashMap = hashMap2;
		this.keys = keys2;
	}

	public void put(String key, Block block) {
		hashMap.put(key, block);
		keys.add(key);
	}

	public Block get(int index) {
		return this.hashMap.get(this.keys.get(index));
	}

	public Block get(String key) {
		return this.hashMap.get(key);
	}

	public void remove(String key) {

		this.hashMap.remove(key);
		this.keys.remove(this.keys.indexOf(key));
	}

	public void remove(int index) {
		this.hashMap.remove(this.keys.remove(index));
	}

	public int size() {
		return this.keys.size();
	}

	public String toString() {
		return this.hashMap.toString();
	}

	public Block last() {

		return this.size() == 1 ? this.get(0) : this.get(this.keys.size() - 1);
	}

	public ChainedHashMap clone() {
		LinkedHashMap<String, Block> hashMap = new LinkedHashMap<>();
		ArrayList<String> keys = new ArrayList<String>();

		for (int i = 0; i < this.keys.size(); i++) {
			keys.add(this.keys.get(i));
			hashMap.put(this.hashMap.get(this.keys.get(i)).getThisBlockHashPointer(),
					this.hashMap.get(this.keys.get(i)));
		}
		return new ChainedHashMap(hashMap, keys);
	}

	public static void main(String[] args) {
		 ChainedHashMap chm1 = new ChainedHashMap();
		 chm1.put("b1",new Block());
		 ChainedHashMap chm2 = new ChainedHashMap();
		 chm2.put("b2",new Block());
		 
		 ChainedHashMap chm3 = new ChainedHashMap();
		 chm3.put("b3",new Block());
		 ChainedHashMap chm4 = new ChainedHashMap();
		 chm4.put("b4",new Block());
		 
		 
		 System.out.println(chm1.get("g"));
//		 ArrayList<ChainedHashMap> forks = new ArrayList<ChainedHashMap>();
//		 
//		 forks.add(chm1);
//		 forks.add(chm2);
//		 forks.add(chm4);
//		 
//		forks.get(0).put("b3", new Block());
//		
//		System.out.println(forks);
//		
//		forks.get(0).remove(0);
//		forks.get(0).remove(0);
//		
//		System.out.println(forks.get(0).last());
		
//		 chm.put("b2",new Block());
//		 chm.put("b3",new Block());
//		 chm.put("b4",new Block());
		// System.out.println(chm.get("ammar"));

		 
		// System.out.println(chm.get("b1"));
	}
}

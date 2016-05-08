package datastore;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class Player {

	@Id
	String id;
    String name;
	String character;
	int score;
	//BlobKey upload;
	//private @Ignore
	//List<Mission> missions;


	public Player() {
	}

	public Player(String id, String name, String charclass,
			int score) {
		super();
		this.id = id;
		this.name = name;
		this.character = charclass;
		this.score = score;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCharclass() {
		return character;
	}

	public void setCharclass(String charclass) {
		this.character = charclass;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return "[name=" + name + ", character=" + character + ", score="
				+ score + "]";
	}

	
//	public List<Mission> getMissions() {
//		if (missions == null) {
//			OfyService.ofy();
//			return ofy().load().type(Mission.class)
//					.ancestor(Key.create(Player.class, this.id)).list();
//		} else
//			return missions;
//	}

}

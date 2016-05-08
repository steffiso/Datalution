package datastore;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class Mission {
	@Id
	Long id;
	String parent;
	String description;
	private boolean accomplished;

	public Mission() {
	}

	public Mission(String parent, String description, boolean isAccomplished) {
		super();
		this.parent = parent;
		this.description = description;
		this.accomplished = isAccomplished;
	}

	void accomplish() {
		accomplished = true;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isAccomplished() {
		return accomplished;
	}

	public void setAccomplished(boolean isAccomplished) {
		this.accomplished = isAccomplished;
	};
}

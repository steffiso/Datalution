package datastore;

import java.util.ArrayList;
import java.util.Arrays;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class Schema {
	
	@Id
	public Long id;
	private int version;
	private String kind;
	private String attributes;
	private int ts;
	
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public String getAttributesString() {
		return attributes;
	}
	public void setAttributesString(String attributes) {
		this.attributes = attributes;
	}
	
	public ArrayList<String> getAttributes() {		
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(attributes.split(",")));
		return list;
	}
	
//	public void setAttributes(ArrayList<String> attributes) {
//		this.attributesList = attributes;
//	}
	
	public int getTimestamp() {
		return ts;
	}
	public void setTimestamp(int timestamp) {
		this.ts = timestamp;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
}

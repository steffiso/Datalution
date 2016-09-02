package datastore;
/**
 * Java class for schema entities from Datastore
 */
import java.util.ArrayList;
import java.util.Arrays;

public class Schema {
	
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
	public String getAttributes() {
		return attributes;
	}
	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}	
	public ArrayList<String> getAttributesAsList() {		
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(attributes.split(",")));
		return list;
	}	
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

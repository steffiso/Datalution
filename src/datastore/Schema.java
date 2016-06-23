package datastore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class Schema {
	
	@Id
	public Long id;
	private int schemaversion;
	private String kind;
	private String attributes;
	private Date timestamp;
	
	private ArrayList<String> attributesList;
	
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
	
	public void setAttributes(ArrayList<String> attributes) {
		this.attributesList = attributes;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public int getSchemaversion() {
		return schemaversion;
	}
	public void setSchemaversion(int schemaversion) {
		this.schemaversion = schemaversion;
	}
}

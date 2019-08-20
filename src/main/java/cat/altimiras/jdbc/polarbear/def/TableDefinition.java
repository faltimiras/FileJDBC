package cat.altimiras.jdbc.polarbear.def;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TableDefinition {

	private String name;
	private String format;
	private List<Column> columns;
	@JsonIgnore
	private Map<String, Integer> columnPosition;
	private String tsColumnName;
	private String partitionsFormat;
	@JsonDeserialize(using = DateSerializer.class)
	private LocalDateTime since;
	private int step;
	private int notFoundMaxLimit;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public String getTsColumnName() {
		return tsColumnName;
	}

	public void setTsColumnName(String tsColumnName) {
		this.tsColumnName = tsColumnName;
	}

	public String getPartitionsFormat() {
		return partitionsFormat;
	}

	public void setPartitionsFormat(String partitionsFormat) {
		this.partitionsFormat = partitionsFormat;
	}

	public LocalDateTime getSince() {
		return since;
	}

	public void setSince(LocalDateTime since) {
		this.since = since;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getNotFoundMaxLimit() {
		return notFoundMaxLimit;
	}

	public void setNotFoundMaxLimit(int notFoundMaxLimit) {
		this.notFoundMaxLimit = notFoundMaxLimit;
	}

	public int getPosition(String name){
		if (columnPosition == null){
			for(int i = 0; i< columns.size(); i++){
				columnPosition.put(columns.get(i).getName(), i);
			}
		}
		return columnPosition.get(name);
	}
}
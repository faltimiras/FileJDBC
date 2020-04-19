package cat.altimiras.jdbc.polarbear.query;

import cat.altimiras.jdbc.polarbear.PolarBearException;
import cat.altimiras.jdbc.polarbear.def.TableDefinition;
import cat.altimiras.jdbc.polarbear.def.TableManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Responsible to validate:
 * - Tables exist
 * - Fields in select exist in the table
 * - Fields names is deterministic
 * - Only one partitioned table in the query
 */
class Validator {

	private TableManager tableManager;

	private Map<String, TableDefinition> tableByName = new HashMap<>();
	private Map<String, TableDefinition> tableByAlias = new HashMap<>();

	public Validator(TableManager tableManager) {
		this.tableManager = tableManager;
	}

	public void validate(Query query) throws PolarBearException {
		validateTables(query);
		validateAndFillSelectFields(query);
		validateAndFillWhereFields(query);
	}

	/**
	 * Validates all table exist and populate translations maps name<->alias
	 *
	 * @param query
	 * @throws PolarBearException
	 */
	private void validateTables(Query query) throws PolarBearException {

		boolean onePartitionedTable = false;
		for (Table table : query.getTables()) {

			TableDefinition tableDef = tableManager.getTable(table.getName());
			if (tableDef == null) {
				throw new PolarBearException("Table '" + table.getName() + "' do not exist");
			}
			if (tableDef.isMainTable()) {
				if (onePartitionedTable) {
					throw new PolarBearException("Only one time partitioned tables is allowed");
				} else {
					onePartitionedTable = true;
				}
			}

			tableByName.put(table.getName(), tableDef);
			if (table.getAlias() != null) {
				tableByAlias.put(table.getAlias(), tableDef);
			}
		}
	}

	/**
	 * Validate fields on Select exist on the FROM tables and not repeated alias
	 *
	 * @param query
	 * @throws PolarBearException if alias is repeated or field doesn't exist
	 */
	private void validateAndFillSelectFields(Query query) throws PolarBearException {

		if (query.getFields() != null) {
			Map<String, Object> alreadyDefinedAlias = new HashMap(query.getFields().size());

			for (Field field : query.getFields()) {

				//detect repeated alias
				if (field.getAlias() != null && alreadyDefinedAlias.containsKey(field.getAlias())) {
					throw new PolarBearException("Alias '" + field.getAlias() + "' is already used in another field");
				} else {
					alreadyDefinedAlias.put(field.getAlias(), null);
				}

				checkField(field);
			}
		}
	}

	/**
	 * @param query
	 * @throws PolarBearException
	 */
	private void validateAndFillWhereFields(Query query) throws PolarBearException {
		preOrderExpr(query.getWhere());
	}

	private void preOrderExpr(Expr expr) throws PolarBearException {

		if (expr != null) {
			Object operand1 = expr.getOperand1();
			if (operand1 != null) {
				checkOperand(operand1);
			}
			Object operand2 = expr.getOperand2();
			if (operand2 != null) {
				checkOperand(operand2);
			}
		}
	}

	private void checkOperand(Object operand) throws PolarBearException {
		if (operand instanceof Expr) {
			preOrderExpr((Expr) operand);
		} else if (operand instanceof Field) {
			checkField((Field) operand);
		}
	}

	/**
	 * Check if field exist at any table. If table is not defined into field it is added
	 *
	 * @param field
	 * @throws PolarBearException if field doesn't exist
	 */
	private void checkField(Field field) throws PolarBearException {
		if (field.getTable() == null) { //if field do not have table look for it.
			field.setTable(findFieldInTables(field.getName()).getName());
		} else { //check field exist in table if defined
			TableDefinition tableDef = tableManager.getTable(field.getTable());
			if (tableDef == null) {
				throw new PolarBearException("Table '" + field.getTable() + "' defined on field '" + field.getName() + "' do not exist");
			} else {
				if (!tableDef.getColumnsByName().containsKey(field.getName())) {
					throw new PolarBearException("Field '" + field.getName() + "' do not exist on table '" + field.getTable() + "'");
				}
			}
		}
	}

	/**
	 * Looks for the field in the FROM tables
	 *
	 * @param name
	 * @return
	 * @throws PolarBearException if field doesn't exist
	 */
	private TableDefinition findFieldInTables(String name) throws PolarBearException {

		for (TableDefinition table : tableByName.values()) {
			if (table.getColumnsByName().containsKey(name)) {
				return table;
			}
		}

		throw new PolarBearException("Field '" + name + "' do not exist");
	}
}
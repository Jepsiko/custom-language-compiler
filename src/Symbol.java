public class Symbol {
	public static final int UNDEFINED_POSITION = -1;
	public static final Object NO_VALUE = null;

	private final LexicalUnit type;
	private final Object value;
	private final int line, column;

	public Symbol(LexicalUnit unit, int line, int column, Object value) {
		this.type = unit;
		this.line = line + 1;
		this.column = column;
		this.value = value;
	}

	public Symbol(LexicalUnit unit, int line, int column) {
		this(unit, line, column, NO_VALUE);
	}

	public Symbol(LexicalUnit unit, int line) {
		this(unit, line, UNDEFINED_POSITION, NO_VALUE);
	}

	public Symbol(LexicalUnit unit) {
		this(unit, UNDEFINED_POSITION, UNDEFINED_POSITION, NO_VALUE);
	}

	public Symbol(LexicalUnit unit, Object value) {
		this(unit, UNDEFINED_POSITION, UNDEFINED_POSITION, value);
	}

	/**
	 * Check if the symbol is a terminal
	 * @return a bool
	 */
	public boolean isTerminal() {
		return this.type != null;
	}

	/**
	 * Check if the symbol is a non a terminal
	 * @return a bool
	 */
	public boolean isNonTerminal() {
		return this.type == null;
	}

	/**
	 * Get the type
	 * @return type
	 */
	public LexicalUnit getType() {
		return this.type;
	}

	/**
	 * Get the value
	 * @return value
	 */
	public Object getValue() {
		return this.value;
	}

	/**
	 * Get the line
	 * @return line
	 */
	public int getLine() {
		return this.line;
	}

	/**
	 * Get the column
	 * @return column
	 */
	public int getColumn() {
		return this.column;
	}

	/**
	 * Override of hashcode
	 */
	@Override
	public int hashCode() {
		final String value = this.value != null ? this.value.toString() : "null";
		final String type = this.type != null ? this.type.toString() : "null";
		return new String(value + "_" + type).hashCode();
	}

	/**
	 * Override of To String in order to have a clean print
	 */
	@Override
	public String toString() {
		if (this.isTerminal()) {
			final String value = this.value != null ? this.value.toString() : "null";
			final String type = this.type != null ? this.type.toString() : "null";
			return "token: " + value + "\tlexical unit: " + type;
		}
		return "Non-terminal symbol";
	}

}

package lol.lolpany.ormik.reinsertableBeans;

public final class TableColumnMetaData {
    public final String name;
    public final int type;
    public final int precision;
    public final int scale;
    public final int columnSize;

    public TableColumnMetaData(String name, int type, int precision, int scale, int columnSize) {
        this.name = name;
        this.type = type;
        this.precision = precision;
        this.scale = scale;
        this.columnSize = columnSize;
    }
}

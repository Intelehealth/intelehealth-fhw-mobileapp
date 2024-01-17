package org.intelehealth.ezazi.builder;

/**
 * Created by Vaghela Mithun R. on 24-06-2023 - 23:20.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class QueryBuilder {
    private String select;
    private String from;
    private String join;
    private String where;
    private String orderBy;
    private String inOrderOf;
    private String groupBy;

    private int limit;

    private int offset;

    public QueryBuilder select(String select) {
        this.select = select;
        return this;
    }

    public QueryBuilder from(String from) {
        this.from = from;
        return this;
    }

    public QueryBuilder where(String where) {
        this.where = where;
        return this;
    }

    public QueryBuilder join(String join) {
        this.join = join;
        return this;
    }

    public QueryBuilder joinPlus(String join) {
        this.join = this.join + join;
        return this;
    }

    public QueryBuilder orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public QueryBuilder orderIn(String inOrderOf) {
        this.inOrderOf = inOrderOf;
        return this;
    }

    public QueryBuilder groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public QueryBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public QueryBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ").append(select).append(" FROM ").append(from).append(" ");
        if (join != null && join.length() > 0)
            builder.append(join).append(" ");
        if (where != null && where.length() > 0)
            builder.append(" WHERE ").append(where).append(" ");
        if (groupBy != null && groupBy.length() > 0)
            builder.append(" GROUP BY ").append(groupBy).append(" ");
        if (orderBy != null && orderBy.length() > 0)
            builder.append(" ORDER BY ").append(orderBy).append(" ");
        if (inOrderOf != null && inOrderOf.length() > 0)
            builder.append(inOrderOf).append(" ");
        if (limit > 0) builder.append(" LIMIT ").append(limit).append(" ");
        if (offset > 0) builder.append(" OFFSET ").append(offset).append(" ");

        return builder.toString();
    }
}

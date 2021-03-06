package ca.allanwang.mcgill.db.utils

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.*


/*
 * -----------------------------------------------------
 * Query Extensions
 * -----------------------------------------------------
 */

/**
 * Allow any query to apply a mapping function
 * No safety checks are made to ensure that the mapping is actually possible
 */
fun <T : Any> Query.mapWith(mapper: (row: ResultRow) -> T): List<T> =
        map { mapper(it) }

/**
 * Check if element exists in iterator,
 * and map the first one only if it exists
 */
fun <T : Any> Query.mapSingle(mapper: (row: ResultRow) -> T): T? =
        iterator().run { if (hasNext()) mapper(next()) else null }

/**
 * Replicate an existing table column
 * If an index is supplied, the value will be cascaded
 */
fun <C> Table.referenceCol(ref: Column<C>, index: Int = -1): Column<C> =
        registerColumn<C>("${ref.table.tableName.toUnderscore()}_${ref.name}", ref.columnType).run {
            if (index >= 0)
                primaryKey(index).references(ref, ReferenceOption.CASCADE)
            else
                references(ref)
        }

/**
 * Given expression, convert rows to map
 */
fun Table.getMap(columns: Collection<Column<*>> = this.columns,
                 limit: Int = -1,
                 where: (SqlExpressionBuilder.() -> Op<Boolean>)? = null): List<Map<String, Any?>> =
        (if (where != null) select(where) else selectAll()).apply {
            if (limit > 0) limit(limit)
        }.map { row ->
            columns.map { it.name to row[it] }.toMap()
        }

fun Transaction.stdlog() = logger.addLogger(StdOutSqlLogger)

fun ResultRow.toMap(columns: Collection<Column<*>>): Map<String, Any?> =
        columns.map { it.toSQL(QueryBuilder(false)) to this[it] }.toMap()

fun FieldSet.take(count: Int,
                  order: Pair<Column<*>, SortOrder>? = null,
                  where: SqlExpressionBuilder.() -> Op<Boolean>): Query =
        select(where).limit(count).run {
            if (order != null) orderBy(order)
            else this
        }

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.newOrUpdate(id: ID, update: T.() -> Unit): T {
    val existing = findById(id)
    return if (existing != null) existing.apply(update)
    else new(id, update)
}

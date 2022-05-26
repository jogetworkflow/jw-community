/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.tool.schema.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.QualifiedNameImpl;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Index;
import org.hibernate.tool.schema.spi.Exporter;

/**
 * @author Steve Ebersole
 */
/**
 * CUSTOM : Override this class to set a max length or alter data type when it is form data column 
 */
public class StandardIndexExporter implements Exporter<Index> {
	private final Dialect dialect;

	public StandardIndexExporter(Dialect dialect) {
		this.dialect = dialect;
	}

	@Override
	public String[] getSqlCreateStrings(Index index, Metadata metadata, SqlStringGenerationContext context) {
		final JdbcEnvironment jdbcEnvironment = metadata.getDatabase().getJdbcEnvironment();
		final String tableName = context.format( index.getTable().getQualifiedTableName() );
                
		final String indexNameForCreation;
		if ( dialect.qualifyIndexName() ) {
			indexNameForCreation = context.format(
					new QualifiedNameImpl(
							index.getTable().getQualifiedTableName().getCatalogName(),
							index.getTable().getQualifiedTableName().getSchemaName(),
							jdbcEnvironment.getIdentifierHelper().toIdentifier( index.getQuotedName( dialect ) )
					)
			);
		}
		else {
			indexNameForCreation = index.getName();
		}
                
                /* CUSTOM START : use to check DB type */
                final String dialectName = dialect.getClass().getName().toLowerCase(Locale.ENGLISH);
                /*CUSTOM END*/
                
		final StringBuilder buf = new StringBuilder()
				.append( "create index " )
				.append( indexNameForCreation )
				.append( " on " )
				.append( tableName )
				.append( " (" );

		boolean first = true;
		final Iterator<Column> columnItr = index.getColumnIterator();
		final Map<Column, String> columnOrderMap = index.getColumnOrderMap();
                
                /* CUSTOM START : to set a max length when it is form data field for mysql & maridb or alter the column to nvarchar(255) for mssql */
                List<String> queries = new ArrayList<String>();
		while ( columnItr.hasNext() ) {
			final Column column = columnItr.next();
			if ( first ) {
				first = false;
			}
			else {
				buf.append( ", " );
			}
			buf.append( ( column.getQuotedName( dialect ) ) );
                        
                        if (column.getName().toLowerCase(Locale.ENGLISH).startsWith("c_") && tableName.toLowerCase(Locale.ENGLISH).startsWith("app_fd_")) {
                            if (dialectName.contains("mariadb") || dialectName.contains("mysql")) {
                                buf.append("(255)");
                            } else if (dialectName.contains("sqlserver")) {
                                queries.add("ALTER TABLE "+tableName+" ALTER COLUMN "+column.getQuotedName( dialect )+" " + dialect.getTypeName(-16, 255, 0, 0));
                            }
                        }
                        
			if ( columnOrderMap.containsKey( column ) ) {
				buf.append( " " ).append( columnOrderMap.get( column ) );
			}
		}
		buf.append( ")" );
                
                queries.add(buf.toString());
                return queries.toArray(new String[0]);
                /*CUSTOM END*/
	}

	@Override
	public String[] getSqlDropStrings(Index index, Metadata metadata, SqlStringGenerationContext context) {
		if ( !dialect.dropConstraints() ) {
			return NO_COMMANDS;
		}

		final String tableName = context.format( index.getTable().getQualifiedTableName() );

		final String indexNameForCreation;
		if ( dialect.qualifyIndexName() ) {
			indexNameForCreation = StringHelper.qualify( tableName, index.getName() );
		}
		else {
			indexNameForCreation = index.getName();
		}

		return new String[] { "drop index " + indexNameForCreation };
	}
}

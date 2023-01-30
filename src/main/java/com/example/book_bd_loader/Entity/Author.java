package com.example.book_bd_loader.Entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table(value = "author_by_id")
@Getter
@Setter
@ToString
public class Author {
    @Id
    @PrimaryKeyColumn(name = "author_id", ordinal = 0,
            type = PrimaryKeyType.PARTITIONED)
    private String id;
    @Column("author_name")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String name;
    @Column("personal_name")
    @CassandraType(type = CassandraType.Name.TEXT)
    private String personalName;
}

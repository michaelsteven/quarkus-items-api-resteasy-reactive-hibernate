package com.github.michaelsteven.archetype.quarkus.resteasy.reactive.items.model;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sun.istack.NotNull;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;



/**
 * Instantiates a new item entity.
 */
@Entity
@Table(name = "items")
public class ItemEntity extends PanacheEntityBase{
	
    @Id
    @GeneratedValue
    @NotNull
    private Long id;
	
	/** The name. */
	@Column
	private String name;
	
	/** The description. */
	@Column
	private String description;
	
	@Column(name = "created_ts")
	private ZonedDateTime createdTimestamp;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ZonedDateTime getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(ZonedDateTime createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}
}

package com.github.michaelsteven.archetype.quarkus.resteasy.reactive.items.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.michaelsteven.archetype.quarkus.reactive.items.interceptor.TraceLog;
import com.github.michaelsteven.archetype.quarkus.resteasy.reactive.items.configuration.ItemDtoMapper;
import com.github.michaelsteven.archetype.quarkus.resteasy.reactive.items.model.ConfirmationDto;
import com.github.michaelsteven.archetype.quarkus.resteasy.reactive.items.model.ItemDto;
import com.github.michaelsteven.archetype.quarkus.resteasy.reactive.items.model.ItemEntity;
import com.github.michaelsteven.archetype.quarkus.resteasy.reactive.items.model.ItemStatus;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * The Class ItemsServiceOrmImpl.
 * 
 * Attempt at using Quarkus hibernate reactive library.
 * 
 *  THIS IS A WORK IN PROGRESS - IT DOESNT WORK YET AND IS NOT TESTED
 * 
 */
@TraceLog
@ApplicationScoped
public class ItemsServiceImpl implements ItemsService {
		
	public static final Logger logger = LoggerFactory.getLogger(ItemsServiceImpl.class);
	
	@Inject
	EntityManager entityManager;
	
	@Inject
	ItemDtoMapper itemDtoMapper;
	
	
	
	/**
	 * Gets the items.
	 *
	 * @param pageable the pageable
	 * @return the items
	 */
	@Override
	public Multi<ItemDto> getItems(){
		return ItemEntity.streamAll().map(entity -> itemDtoMapper.mapToDto((ItemEntity) entity));
	}
	
	
	/**
	 * Gets the item by id.
	 *
	 * @param id the id
	 * @return the item by id
	 */
	@Override
	public Uni<ItemDto> getItemById(long id){
		 return ItemEntity.findById(id).map(entity -> itemDtoMapper.mapToDto((ItemEntity) entity));
	}
	
	
	/**
	 * Save item.
	 *
	 * @param itemDto the item dto
	 * @return the confirmation dto
	 */
	@Override
	@Transactional
	public Uni<ConfirmationDto> saveItem(@NotNull @Valid ItemDto itemDto) {
		return Uni.createFrom()
				  .item(itemDto)
				  .map(itemDtoMapper::mapToEntity)
				  .onItem().invoke(entity -> {
					  entity.setId(0);
					  entity.persist();
					})
				  .map(entity -> createConfirmationDto(ItemStatus.SUBMITTED, entity));
	}
	
	
	/**
	 * Edits the item.
	 *
	 * @param itemDto the item dto
	 * @return the confirmation dto
	 */
	@Override
	@Transactional
	public Uni<ConfirmationDto> editItem(@NotNull @Valid ItemDto itemDto) {
		return Uni.createFrom()
				  .item( entityManager.find(ItemEntity.class, itemDto.getId()) )
				  .onItem()
				  .ifNull().failWith( () ->  { throw new ValidationException("Entity not found for ID"); } )
				  .map(entity -> convert(itemDto, entity))
				  .invoke(entity -> {entity.persist();})
				  .map(item -> createConfirmationDto(ItemStatus.SUBMITTED, item));
	}
	
	
	/**
	 * Delete item by id.
	 *
	 * @param id the id
	 */
	@Override
	@Transactional
	public void deleteItemById(long id){
		Uni.createFrom()
		   .item(entityManager.find(ItemEntity.class,id))
		   .onItem()
		   .ifNotNull()
		   .invoke(entityManager::remove);
	}
	
	
	/**
	 * Creates the confirmation dto.
	 *
	 * @param itemStatus the item status
	 * @param entity the entity
	 * @return the confirmation dto
	 */
	private ConfirmationDto createConfirmationDto(ItemStatus itemStatus, ItemEntity entity) {
		logger.info("in CreateConfirmationDTO");
		ConfirmationDto confirmationDto = new ConfirmationDto();
		confirmationDto.setStatus(itemStatus);
		if(null != entity) {
			confirmationDto.setId(entity.getId());
			if(null != entity.getCreatedTimestamp()) {
				//ZonedDateTime dateSubmitted = ZonedDateTime.ofInstant(entity.getCreatedTimestamp(), ZoneOffset.UTC);
				//confirmationDto.setDateSubmitted(dateSubmitted);
				confirmationDto.setDateSubmitted(entity.getCreatedTimestamp());
			}
		}
		else {
			logger.warn("itemEntity is null");
		}
		logger.info("Exiting createConfirmationDto with id {}", confirmationDto.getId());
		return confirmationDto;
	}
	
	/**
	 * Convert.
	 *
	 * @param sourceDto the source dto
	 * @param targetEntity the target entity
	 */
	private ItemEntity convert(@NotNull ItemDto sourceDto, ItemEntity targetEntity) {
		if(null != targetEntity) {
			targetEntity.setId(sourceDto.getId());
			targetEntity.setName(sourceDto.getName());
			targetEntity.setDescription(sourceDto.getDescription());
		}
		return targetEntity;
	}
}

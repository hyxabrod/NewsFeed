package com.mako.newsfeed.core

interface DataEntity {
    fun toDomain(): DomainEntity
}

interface DomainEntity {
    fun toPresentation(): PresentationEntity
}

interface PresentationEntity
